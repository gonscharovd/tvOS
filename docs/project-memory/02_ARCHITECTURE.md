# Архитектура testOS

**Срез кода:** ветка `refactor/testos-core-cleanup-2026-11`, commit `fa91b18305e57bae536d6664af88cb6230a4ec7e`  
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
Настройки хранятся локально в SharedPreferences. `SingleUserPrefs` выполняет одноразовую миграцию старых profile-scoped preferences в canonical `home`; `SingleUserMigration` содержит чистую Java-логику миграции; standalone `SingleUserMigrationTest.java` запускается в Linux и Windows CI через JDK 11 language target. Regression contract проверяет CI wiring и наличие тестов сохранения legacy/source data. Legacy значения/файлы сохраняются для rollback. Общий settings backup экспортирует только allowlist в `Download/tvos-launcher-settings.json`; запись использует AtomicFile. Backup CleanYourTV — отдельный файл `cleanyourtv_disabled_backup.txt`, он не входит в общий backup.

### CleanYourTV
UI в `CleanYourTv.java` определяет модель/профиль из `assets/cleanyourtv/`, проверяет hard-protected package policy, выполняет disable/restore через Local ADB и подтверждает результат чтением состояния PackageManager. Нельзя считать установление ADB-сессии подтверждением успешного disable.

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
