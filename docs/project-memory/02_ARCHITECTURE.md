# Архитектура testOS

**Срез кода:** ветка `refactor/testos-core-cleanup-2026-11`, commit `6d3fc12a271ad8d5cb3681cc614e8182041fddcf`  
**Основная ветка:** `main`, commit `a765318b577e66fae66a8ffe28439dcd6233f38f`  
**Важно:** описание отражает код рабочей ветки PR #14; изменения PR ещё не влиты в main.

## 1. Технологии и сборка

- Язык приложения: Java.
- Android API: minSdk 24, targetSdk 34; целевой телевизор Android 9 / API 28 / ARMv7.
- Языковой уровень компиляции: Java 11 (`javac --release 11`).
- JDK для сборки: 17+.
- Android SDK platform: android-34; build-tools: 34.0.0.
- Сборка без Gradle и без Android Studio: `aapt2` → `javac` → `d8` → упаковка DEX/assets → `zipalign` → `apksigner`.
- Скрипты: [build.sh](../../build.sh), [build.ps1](../../build.ps1).
- Windows JDK discovery: `build.ps1` вызывает [tools/java.ps1](../../tools/java.ps1), который предпочитает валидный `JAVA_HOME` и при его отсутствии разрешает `javac`, `java`, `keytool` через `PATH`; machine-specific JDK paths запрещены.
- Единый источник версии: [version.properties](../../version.properties); development defaults — `2.1.74` / versionCode `89`, release base — `15`, release prefix — `2.1.`.
- Оба build script читают общий файл; `VERSION_CODE` и `VERSION_NAME` из environment имеют приоритет над defaults. PowerShell использует общий parser `tools/version.ps1`.
- Общий выходной файл: `build/tvos-home.apk`.
- Встроенные ресурсы компилируются из [res/](../../res/), исходники — из [src/](../../src/).

В репозитории нет Gradle build files и нет отдельного backend-сервиса. Runtime сторонние библиотеки по правилам CONTRIBUTING не добавляются. Вендорный QR encoder находится в `src/io/nayuki/qrcodegen/` (MIT); шрифт Inter — в `assets/fonts/` (OFL). Pillow/fonttools используются в CI для подготовки assets, не как runtime-зависимости приложения.

## 2. Идентификаторы приложения

- Source manifest package: `com.techgenius.tvoslauncher`.
- Build command использует `aapt2 link --rename-manifest-package com.gonscharovd.tvoslauncher`.
- Итоговый package/application ID, указанный в README и CI: `com.gonscharovd.tvoslauncher`.
- Основные Java-классы и manifest component names остаются в namespace `com.techgenius.tvoslauncher`.
- Label приложения: `testOS`.

При изменении package ID или manifest components обязательно проверить итоговый APK и точные component names. Команды из install docs с коротким именем Activity требуют проверки против итогового manifest; не предполагать, что source namespace и application ID совпадают.

## 3. Основные модули

| Файл / каталог | Ответственность |
|---|---|
| [MainActivity.java](../../src/com/techgenius/tvoslauncher/MainActivity.java) | HOME Activity, root view, lifecycle, app loading/launch, overlay dispatch, общие bitmap/backdrop helpers |
| [SceneHome.java](../../src/com/techgenius/tvoslauncher/SceneHome.java) | Единственный активный home layout в PR #14: Night Scenes, dock, More Apps, D-pad focus и app actions |
| [Scenes.java](../../src/com/techgenius/tvoslauncher/Scenes.java) | Сцены, пользовательские обои/галерея, сохранение предпочтений и миграция старых Classic assets |
| [SettingsActivity.java](../../src/com/techgenius/tvoslauncher/SettingsActivity.java) | Настройки, четыре группы, D-pad navigation, OTA, CleanYourTV и backup/restore entry points |
| [SetupActivity.java](../../src/com/techgenius/tvoslauncher/SetupActivity.java) | Первоначальная настройка однопользовательского интерфейса |
| [I18n.java](../../src/com/techgenius/tvoslauncher/I18n.java) | Русские UI-строки и динамические подписи |
| [Search.java](../../src/com/techgenius/tvoslauncher/Search.java), [SearchPanel.java](../../src/com/techgenius/tvoslauncher/SearchPanel.java) | Поиск приложений/настроек, JustWatch title lookup, голосовой ввод, RU/EN клавиатура |
| [ControlPanelOverlay.java](../../src/com/techgenius/tvoslauncher/ControlPanelOverlay.java) | Универсальная панель быстрых настроек поверх приложений или HOME |
| [RecentAppsOverlay.java](../../src/com/techgenius/tvoslauncher/RecentAppsOverlay.java) | Переключатель недавних приложений |
| [ButtonMapper.java](../../src/com/techgenius/tvoslauncher/ButtonMapper.java), [PowerService.java](../../src/com/techgenius/tvoslauncher/PowerService.java) | Переназначение кнопок пульта и AccessibilityService |
| [CleanYourTv.java](../../src/com/techgenius/tvoslauncher/CleanYourTv.java) | Определение модели ТВ, защита пакетов, disable/restore и backup состояния |
| [PackageSafetyPolicy.java](../../src/com/techgenius/tvoslauncher/PackageSafetyPolicy.java) | Чистая Java policy: SAFE_TO_DISABLE / SAFE_TO_DELETE / PROTECTED / UNKNOWN, причины и confidence |
| [AndroidPackageSafetyInspector.java](../../src/com/techgenius/tvoslauncher/AndroidPackageSafetyInspector.java) | Read-only Android evidence: HOME, PackageManager, DevicePolicyManager, critical permissions/roles, system UID, persistent apps and boot receivers |
| [PackageMutationCommandGuard.java](../../src/com/techgenius/tvoslauncher/PackageMutationCommandGuard.java) | Классификация package disable/enable/delete команд; fail-closed блокировка compound/unsupported shell mutations |
| [LocalAdb.java](../../src/com/techgenius/tvoslauncher/LocalAdb.java) | Локальная ADB-связь и выдача/ремонт нужных разрешений |
| [Perms.java](../../src/com/techgenius/tvoslauncher/Perms.java) | Android permissions, DND, выбор HOME и системные fallback-переходы |
| [AppUpdater.java](../../src/com/techgenius/tvoslauncher/AppUpdater.java), [VersionComparator.java](../../src/com/techgenius/tvoslauncher/VersionComparator.java) | GitHub Releases OTA, сравнение версий, загрузка и PackageInstaller |
| [CustomMedia.java](../../src/com/techgenius/tvoslauncher/CustomMedia.java) | Пользовательские media/assets, settings backup/restore |
| [Weather*.java](../../src/com/techgenius/tvoslauncher/) | Источники погоды, кэш и UI |
| [VpnController.java](../../src/com/techgenius/tvoslauncher/VpnController.java), [AirPlayGuard.java](../../src/com/techgenius/tvoslauncher/AirPlayGuard.java) | VPN detection/launch и AirPlay visibility/recovery |
| [Inputs.java](../../src/com/techgenius/tvoslauncher/Inputs.java), [SleepReceiver.java](../../src/com/techgenius/tvoslauncher/SleepReceiver.java), [Clicks.java](../../src/com/techgenius/tvoslauncher/Clicks.java) | TV inputs, sleep timer, navigation sounds |
| [present/](../../src/com/techgenius/tvoslauncher/present/) | Дополнительный Presentation-модуль: локальный сервер/плеер и связанные компоненты |
| [assets/](../../assets/), [res/](../../res/) | Изображения, шрифты, CleanYourTV profiles, drawable и XML resources |

В PR #14 удалены старые Classic home grid/wallpaper/hero paths и модель Profiles. Night Scenes остаётся единственным активным домашним интерфейсом. Старые пользовательские Classic wallpapers и folder metadata не удаляются: предусмотрена миграция в Night Scenes.

## 4. Потоки данных

### Home / app launch
Android запускает `MainActivity`; она создаёт `SceneHome`, загружает доступные TV-приложения через PackageManager и передаёт навигацию в SceneHome. Search, Weather, Control Panel и Recent Apps открываются через отдельные классы/оверлеи.

### Remote controls
`PowerService` — AccessibilityService. Он получает разрешённые события пульта, выполняет ButtonMapper actions, поддерживает Keep Alive/repair и открывает системные overlays. Control Panel и Recent Apps могут отображаться через Accessibility overlay поверх других приложений; на HOME используется соответствующий Activity UI.

### Settings / local data
Настройки хранятся локально в SharedPreferences. `SingleUserPrefs` выполняет одноразовую миграцию выбранного profile-scoped home в canonical `home`; чистый Java helper `SingleUserMigration` не менялся в TOS-009. `SingleUserPrefs` сохраняет исходные `profiles.list/current` и `ask_on_wake`, а также legacy snapshot keys, чтобы предыдущий APK мог прочитать прежний выбор. Standalone `SingleUserMigrationTest.java` запускается в Linux и Windows CI; regression contract защищает также registry retention. Общий settings backup экспортирует allowlist, включая `profiles`, в `Download/tvos-launcher-settings.json`; для уже мигрированных установок экспортируемые `list/current` проецируются из legacy snapshot. Restore merge-based, без очистки файла; запись backup использует AtomicFile. Backup CleanYourTV — отдельный файл `cleanyourtv_disabled_backup.txt`, он не входит в общий backup.

### CleanYourTV
UI в `CleanYourTv.java` использует единую `PackageSafetyPolicy`. `AndroidPackageSafetyInspector` собирает read-only evidence через PackageManager/DevicePolicyManager; `UNKNOWN` всегда блокирует disable/delete. System app может стать `SAFE_TO_DISABLE` только при подтверждённой Build OEM identity, полном package inventory, совпадении OEM family и наличии package в выбранном model profile, плюс при успешных независимых HOME/DPM/role/permission/UID/persistent checks. Profile membership само по себе не разрешает действие. Все shell mutation проходят через `LocalAdb.runShellCommandsOnHost()` и `PackageMutationCommandGuard`; shell uninstall запрещён. Restore принимает только валидное имя пакета и точную `pm enable` команду. `SceneHome` проверяет пользовательский uninstall до показа действия и повторно перед `Intent.ACTION_DELETE`. Нельзя считать установление ADB-сессии подтверждением успешного disable.

### OTA
`AppUpdater` получает информацию о последнем GitHub Release, сравнивает версии через `VersionComparator`, загружает APK и передаёт установку Android PackageInstaller либо настроенному local ADB пути. Для обновления установленного приложения сертификат APK должен совпадать с установленным.

## 5. Внешние API и интеграции

- GitHub Releases API — проверка обновлений и загрузка APK.
- Open-Meteo, MET Norway, wttr.in — погодные данные.
- JustWatch — поиск контента/названий.
- Android PackageManager, PackageInstaller, Accessibility, UsageStats, DND policy, Location, Storage — системные API.
- Локальная ADB-связь — настройка разрешений и CleanYourTV.
- AirPlayGuard и VpnController работают с возможностями/приложениями устройства.
- Presentation-модуль содержит локальный HTTP/admin server; его актуальное использование на целевом устройстве нужно проверить отдельно.

Не обнаружено базы данных или ORM. Схемы SQL и серверные миграции в текущем проекте отсутствуют. Долгоживущие пользовательские данные — SharedPreferences и файлы в локальном storage.

## 6. CI/CD

- [build-apk.yml](../../.github/workflows/build-apk.yml): Linux и Windows build jobs, Python/PowerShell build-contract tests, Java tests, APK build, package/version/signature verification и artifact upload.
- [release.yml](../../.github/workflows/release.yml): release branch workflow, release signing через GitHub Secrets, версия из `release/2.1.PATCH`, GitHub Release/OTA asset.
- Release version code по текущему правилу: `RELEASE_VERSION_CODE_BASE + PATCH`; база `15` берётся из `version.properties`, поэтому v2.1.73 → 88 и v2.1.74 → 89.
- Build workflow не использует persistent release keystore; он собирает preview APK с debug key runner. Такой artifact не следует устанавливать поверх release-signed приложения без проверки сертификата.
- Секреты release signing существуют как имена переменных/Secrets в workflow; их значения не читаются и не должны попадать в документацию.

## 7. Тесты

Python regression tests находятся в [tests/](../../tests/). В workflow включены проверки CleanYourTV, settings cleanup, backup safety, bitmap policy, asset optimizer, font subset, Settings navigation, OTA flow, Russian localization, Control Panel, build contract и Java comparator/I18n tests. `tests/SingleUserMigrationTest.java` компилируется и запускается в обоих jobs `.github/workflows/build-apk.yml`; CI run #322 прошёл на Linux и Windows. Тест проверяет pure-Java migration helper, а не Android SharedPreferences adapter/runtime на телевизоре.

CI подтверждает только реально запущенные шаги и не заменяет тестирование реального D-pad, lifecycle, разрешений, OTA install и производительности на Sber TV.

### First-launch legacy data retention

TOS-009 removes `cleanupRemovedFeatureData()` and `deleteLegacyFeatureFiles()` from `MainActivity`; no first-launch code clears retired-feature preferences, shelf keys, `cache/tmdb` or `files/continue.json`. This is intentionally non-destructive even though the corresponding UI/features were removed. `tests/test_settings_backup_safety.py` protects this contract and is wired into Linux and Windows jobs. Android may still evict cache files under storage pressure; the app no longer explicitly deletes them. Runtime behavior on Sber TV remains unverified.



## Universal Android TV target architecture (approved 2026-11-10)

- **testOS Core:** OEM-neutral home UI, apps, dock, search, settings, DPAD/focus, OTA and themes.
- **Android TV Compatibility Layer:** standard Android APIs for PackageManager, ActivityManager, TV Input Framework, HOME intents/roles, permissions and system settings. Feature availability must be probed, not assumed.
- **OEM Device Profiles:** advisory manufacturer/family/device metadata and capability adapters. Unknown OEM is a supported mode; identity must never authorize package removal.
- **Optional Integrations:** CleanYourTV, VPN providers (Happ optional), weather and system tools. Missing integrations must not impair core launcher startup.

TOS-011 adds the `DeviceProfile` model/resolver. TOS-012 wires dynamic HOME detection; TOS-013 uses corroborated OEM identity only as one evidence gate for system-package candidates. Package-name-only OEM inference and incomplete/conflicting package inventory never authorize system debloat. Existing 341-entry CleanYourTV model database remains intact.


## Dynamic HOME detection (TOS-012, implementation under CI review — 2026-11-10)

- `HomeAppDetector` is a pure-Java policy model. It separates the explicitly selected HOME from the list of apps that can handle `ACTION_MAIN + CATEGORY_HOME`.
- `AndroidHomeAppDetector` uses `PackageManager.queryIntentActivities` to enumerate HOME candidates and `PackageManager.getPreferredActivities` to identify an explicit preferred HOME. It does **not** use `resolveActivity` for selection because priority-based best-match resolution can select a high-priority candidate without proving that the user selected it.
- On API 29+, public `RoleManager.isRoleHeld(ROLE_HOME)` confirms when testOS itself holds the HOME role. The public API does not expose other role holders; otherwise the implementation relies on explicit PackageManager preference evidence.
- Candidate visibility is declared through the manifest HOME `<queries>` intent and existing `QUERY_ALL_PACKAGES` permission. PackageManager failures, missing preferences and ambiguous selections produce `UNKNOWN`.
- `CleanYourTv.isSafeToDisable` protects the selected HOME and fails closed when HOME/candidate roles cannot be established. Launcher selection lists omit the currently selected HOME and ambiguous candidates.
- `LocalAdb` removes role holders only for dynamically detected system HOME candidates; `PowerService` boot interception uses the dynamically discovered system HOME candidate set rather than a static vendor list.
- OEM package names remain in CleanYourTV's separate profile/catalog and friendly-label tables; they are not used to identify the selected HOME.

Runtime behavior of OEM firmware that intercepts the physical HOME key outside Android's standard HOME preference/role APIs remains device-specific and unverified. Unknown states are reported conservatively rather than guessed.


## CleanYourTV safety architecture (TOS-013, PR #19)

- `PackageSafetyPolicy` is pure Java and returns explicit state/reason/confidence. `UNKNOWN` is not equivalent to safe.
- `AndroidPackageSafetyInspector` checks selected and alternate HOME (including disabled candidates), active admins / Device Owner / Profile Owner, critical permissions, system service roles, Settings/Package Installer/Permission Controller handlers, system UID, persistent flag and system boot receivers.
- A complete package inventory and Build-property OEM identity are required before a model profile can be considered; conflicting Build/package OEM signals produce `UNKNOWN`. A package-only OEM inference is advisory, not authorization.
- `PackageMutationCommandGuard` is the last ADB boundary. Only exact single-package user-0 disable and recovery-enable commands are accepted; delete and compound/unsupported mutations are rejected.
- `SceneHome` user-app uninstall is gated by `evaluateDelete`; OEM/system and persistent packages cannot be deleted.
- Full audit and exact deny-rule review: [cleanyourtv-safety-audit.md](https://github.com/gonscharovd/tvOS/blob/feat/package-safety-policy-2026-11/docs/cleanyourtv-safety-audit.md). CI #456 passed Linux/Windows on code commit `22d46a96a5dba243ae95c1a516b6890c6e72015d`; subsequent PR #19 head change is documentation-only.
- No physical Android runtime validation yet; OEM firmware HOME interception and real device PackageManager/permission metadata remain unknown.
