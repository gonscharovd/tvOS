# TOS-013 — аудит безопасности CleanYourTV

**Дата:** 2026-11-10  
**Ветка:** `feat/package-safety-policy-2026-11`  
**PR:** [#19 — Universal CleanYourTV Package Safety Policy](https://github.com/gonscharovd/tvOS/pull/19)  
**База:** `feat/dynamic-home-detection-2026-11` (TOS-012 / PR #18).

## 1. Итог аудита

CleanYourTV теперь использует общую fail-closed политику для отключения пакетов и удаления пользовательских приложений. Решение отделено от UI и shell transport. Ошибка или неполнота Android evidence не трактуется как разрешение.

Состояния политики: `SAFE_TO_DISABLE`, `SAFE_TO_DELETE`, `PROTECTED`, `UNKNOWN`. У `Decision` есть причина и уровень достоверности. `UNKNOWN` блокирует опасное действие.

Ключевые изменения TOS-013:
- единый `PackageSafetyPolicy`;
- Android evidence adapter `AndroidPackageSafetyInspector`;
- финальный shell guard `PackageMutationCommandGuard`;
- проверка HOME-кандидатов, включая отключённые компоненты;
- защита системных boot receivers, системных ролей, критических permissions, Device Owner / Profile Owner, persistent apps и system UID;
- запрет shell uninstall и прямого удаления OEM/system apps;
- валидация package names при восстановлении;
- fail-closed при неизвестном или конфликтующем OEM evidence.

## 2. Карта операций и зависимостей

| Операция | Проверка | Исполнитель / последний барьер |
|---|---|---|
| CleanYourTV checklist / batch disable | `CleanYourTv.isSafeToDisable()` → `PackageSafetyPolicy` | `executeDebloatBatch()` → `LocalAdb.runShellCommandsOnHost()` → `PackageMutationCommandGuard` → повторная Android safety check |
| Manual Debloat / single disable | `toggleSinglePackage()` → `PackageSafetyPolicy` | `LocalAdb.execShellSequential()` → транспортный guard |
| Список системных HOME | `AndroidHomeAppDetector` + общая политика | `getInstalledSystemLaunchers()`; HOME-кандидаты не допускаются к отключению |
| Restore previously disabled apps | строгая валидация имени пакета | только точный `pm enable --user 0 <package>`; compound/invalid команды блокируются |
| LocalAdb shell | `PackageMutationCommandGuard.inspect()` | `runShellCommandsOnHost()` — единая граница для shell-команд |
| Удаление пользовательского приложения | `CleanYourTv.isSafeToDelete()` → `PackageSafetyPolicy.evaluateDelete()` | `SceneHome` проверяет при построении меню и повторно перед `Intent.ACTION_DELETE` |
| PowerService / MainActivity / SetupActivity | прямых disable/delete путей не найдено | не являются исполнителями CleanYourTV mutation |

Дополнительно проверено отсутствие прямых `deletePackage()`, `setApplicationEnabledSetting()`, `setPackagesSuspended()`, `setApplicationHidden()` и `ACTION_UNINSTALL_PACKAGE` в Java source. `Perms` больше не показывает ручную команду `pm disable-user`, которая обходила бы общую политику.

## 3. HOME protection

- HOME candidates обнаруживаются через `ACTION_MAIN + CATEGORY_HOME + CATEGORY_DEFAULT`.
- Android adapter запрашивает `MATCH_DEFAULT_ONLY | MATCH_DISABLED_COMPONENTS`, чтобы не потерять отключённые recovery-кандидаты.
- Выбранный HOME определяется по явным preferred-activity данным; `resolveActivity()` и порядок кандидатов не используются как доказательство выбора.
- API 29+ `RoleManager` используется только для подтверждения роли самого testOS; публичный API не раскрывает произвольного владельца HOME.
- Выбранный HOME и все альтернативные HOME-кандидаты защищены. `UNKNOWN` блокирует отключение.
- testOS не классифицируется как OEM HOME; собственный пакет защищён отдельным `ownApplication` правилом.
- Firmware, которая перехватывает физическую кнопку HOME вне стандартных RoleManager / PackageManager путей, остаётся runtime-зависимостью конкретного OEM.

## 4. Android / Android TV critical evidence

`AndroidPackageSafetyInspector` собирает read-only evidence:
- `ApplicationInfo.FLAG_SYSTEM` и `FLAG_UPDATED_SYSTEM_APP`;
- `FLAG_PERSISTENT` — защищается независимо от system flag;
- system UID (`uid < Process.FIRST_APPLICATION_UID`);
- critical permissions: System UI/status bar, permission management, reboot/power, recovery/update/install/delete, network stack/settings, Bluetooth privileged/stack, TV input, input method, accessibility, VPN, notification listener, quick settings, wallpaper/dream/print services;
- `queryIntentServices()` для TV Input Framework, Input Method, Accessibility, VPN, Notification Listener и других системных сервисных ролей;
- `queryIntentActivities()` для Settings, Package Installer и Permission Controller handlers;
- `queryBroadcastReceivers()` для `BOOT_COMPLETED`, `LOCKED_BOOT_COMPLETED` и Device Admin; boot receiver защищает system app, а не автоматически любой user app;
- `DevicePolicyManager.getActiveAdmins()`, `isDeviceOwnerApp()` и `isProfileOwnerApp()`.

Если любой обязательный запрос PackageManager / DevicePolicyManager не выполнен, evidence становится неизвестным и решение не разрешает отключение. На Android 29+ отсутствие обнаруживаемого Permission Controller handler также приводит к fail-closed состоянию.

## 5. Device Profile и OEM profiles

- `DeviceProfile` остаётся классификатором устройства, а не разрешением на удаление.
- Для разрешения system-package candidate необходимы известная Build identity, непустой полный package inventory, отсутствие конфликта Build/package OEM evidence и совпадение OEM family выбранного model profile.
- OEM family, выведенная только из имён установленных пакетов, не авторизует system debloat.
- Package должен отдельно присутствовать в списке выбранного model profile; одного производителя или совпадения модели недостаточно.
- Если inventory пуст, недоступен или противоречив, system-package решение не может стать `SAFE_TO_DISABLE`.
- База `assets/cleanyourtv/index.txt` содержит 341 профиль и не переписывалась массово.

### Sber-specific profile review

В базе найдены два Sber профиля: `sber-salute-tv-at30a6` и `sber-salute-tv-pb766`. В первом списке кандидатов присутствуют `ru.sberdevices.updater`, `ru.sberdevices.rcfupdater` и `ru.sberdevices.services.core`; StarLauncher защищён отдельно. Во втором профиле StarLauncher не включён в список кандидатов, а заметка профиля прямо требует оставить штатный launcher включённым.

Функции этих updater/service пакетов на реальном устройстве в рамках этого аудита не подтверждались. Их защита зависит от Android evidence (permissions, system UID, persistent flag, boot receiver, DPM/role). Не делать вывод об их безопасности только по профилю. Для Sber SDX-50U4123B точное совпадение с этими двумя профилями не подтверждено; похожий профиль не следует считать точным.

## 6. Аудит `NEVER_DISABLE_EXACT`

В текущем коде 24 точных deny-правила. Они только запрещают действие и никогда не разрешают другие пакеты.

| Пакет | Причина сохранения | Универсальность / замена API |
|---|---|---|
| `com.techgenius.tvoslauncher` | Защита самого testOS | Дублируется `ownApplication`; оставить как defense-in-depth |
| `com.cltv.mal` | Назначение по исходникам достоверно не установлено | Нет подтверждённой стандартной замены; оставить |
| `com.mediatek.tis` | Legacy MediaTek TV input/service | Часть риска покрывается TV Input Framework и permissions; OEM fallback оставить |
| `com.mediatek.tv.service` | MediaTek TV service stack | Стандартная роль не гарантирована на всех прошивках; оставить |
| `com.mediatek.tv.agent` | MediaTek TV agent | Стандартная роль не гарантирована; оставить |
| `com.android.tv.settings` | TV Settings | `CORE_RE` и Settings intent resolution; exact оставить |
| `com.android.settings` | Android Settings | `CORE_RE` и Settings intent resolution; exact оставить |
| `com.android.systemui` | System UI | `CORE_RE`, status-bar permissions, system UID; exact оставить |
| `com.android.vending` | Play Store / package distribution | `CORE_RE`; exact redundant but harmless |
| `com.google.android.gms` | Google Play services | `CORE_RE` и critical permissions; exact оставить |
| `com.google.android.gsf` | Google Services Framework | `CORE_RE`; exact оставить |
| `com.google.android.webview` | WebView runtime | `CORE_RE`; exact оставить |
| `com.google.android.tungsten.setupwraith` | Google TV setup component может владеть сохранённой Wi-Fi конфигурацией; защита должна работать и в Manual Debloat | Нет универсальной публичной роли Wi-Fi owner; exact protective fallback добавлен |
| `org.droidtv.playtv` | Philips TV playback/input | Часть функций может определяться TV Input API; firmware fallback оставить |
| `org.droidtv.tunerservice` | Philips tuner service | TV Input / hardware permission могут покрыть, но не гарантировано; оставить |
| `org.droidtv.channels` | Philips channels provider | TV Input / provider API могут покрыть, но не гарантировано; оставить |
| `com.android.tv` | Android TV live TV component | Не всегда HOME candidate; exact fallback оставить |
| `com.google.android.tv` | Google TV / TV component | Не всегда HOME candidate; exact fallback оставить |
| `com.xiaomi.bluetooth` | Xiaomi Bluetooth / remote connectivity | Часть защиты есть по Bluetooth privileged/stack permissions; exact fallback оставить |
| `com.mi.bluetooth` | Xiaomi Bluetooth / remote connectivity | Часть защиты есть по permissions; exact fallback оставить |
| `com.xiaomi.mitv.bluetooth` | Xiaomi/Mi TV Bluetooth | Часть защиты есть по permissions; exact fallback оставить |
| `com.mitv.bluetooth` | Xiaomi/Mi TV Bluetooth | Часть защиты есть по permissions; exact fallback оставить |
| `ru.sberdevices.SberDevicesServiceSystem` | Sber Device Owner / system service | DPM active-admin / owner detection должна защищать при доступности; exact fallback оставить |
| `ru.sberdevices.starlauncher` | Sber stock launcher; firmware может перехватывать HOME вне стандартных API | Dynamic HOME защищает стандартный candidate; exact OEM fallback оставить |

Xiaomi Bluetooth package names также дублируются в `isCriticalProtectedService()`. Это избыточная защитная проверка, а не allowlist. Не удалять её до runtime проверки на Xiaomi.

## 7. CORE_RE / CORE_OK

`CORE_RE` защищает Android/Google namespaces и системные сервисы: System UI, Settings, providers, input methods, server, permission/package installer, vending, Bluetooth/NFC, network stack, Wi-Fi, WebView, framework stubs и другие platform components.

`CORE_OK` исключает из regex только отдельные необязательные компоненты: Calendar/Contacts/UserDictionary providers, Dreams, Print Spooler/BIPS, STK, Cell Broadcast Receiver, Call Log Backup и Managed Provisioning. Это не автоматическое разрешение: system app всё равно должен пройти OEM identity, selected model package membership, HOME, DPM, critical role, permission, UID и persistent checks. `com.android.providers.tv` и `com.android.statementservice` не являются CORE_OK исключениями.

## 8. Shell guard и restore

`PackageMutationCommandGuard` классифицирует disable, enable, delete и unsupported mutation:
- disable допускается только для точной одиночной команды user 0 с валидным package name и после общей safety policy;
- enable допускается только для точной одиночной recovery-команды user 0 с валидным package name;
- compound, malformed и unsupported package mutation commands блокируются;
- shell uninstall/delete всегда блокируется;
- restore list не считается доверенным shell input; каждый package name проверяется до построения команды.

Это закрывает ранее обнаруженный риск, при котором некорректное сохранённое имя могло добавить shell chaining к `pm enable`.

## 9. Тестирование и ограничения

Автоматические тесты:
- `HomeAppDetectorTest.java` — выбранный/альтернативный/user/testOS HOME, disabled alternate HOME, ambiguous/unknown state;
- `DeviceProfileTest.java` — известные OEM, package-only evidence, Build/package conflict, unknown inventory;
- `PackageSafetyPolicyTest.java` — unknown/critical/system/persistent/DPM/permission/HOME/profile/delete states; policy parity для API 24, 28, 34, 35;
- `PackageMutationCommandGuardTest.java` — exact disable/enable, invalid package, compound commands, delete blocking;
- Python contract tests — Android wiring, UI/batch/LocalAdb/restore/uninstall paths, отсутствие альтернативных mutation API и first-run/background debloat.

Pure-Java policy tests запускаются на Linux и Windows. Android adapter компилируется в APK build, но настоящий PackageManager/RoleManager/DevicePolicyManager runtime на Android 24, 28 и современных версиях не эмулируется этими JVM тестами.

Не проверено на физическом телевизоре: реальные разрешения и flags Sber/Xiaomi/TCL/Philips/Sony/Hisense, OEM boot receivers, доступность Permission Controller intent, перехват HOME firmware, реальные Sber updater package metadata. Не устанавливался APK, не выполнялись shell-команды на ТВ, не менялись HOME, настройки и package state.

## 10. Следующие действия

1. Дождаться завершения Linux и Windows CI на последнем commit PR #19 и проверить именно последний head.
2. Провести code review Draft PR #19; не менять его base с PR #18 на `main` до слияния dependency chain.
3. Не сливать PR без явного разрешения.
4. Отдельным согласованным этапом выполнить только read-only runtime inventory на реальных Android TV; не отключать пакеты и не менять HOME.
5. Для любого OEM, где критические роли или Permission Controller не обнаруживаются, сохранить `UNKNOWN` и не ослаблять policy без подтверждённых данных.