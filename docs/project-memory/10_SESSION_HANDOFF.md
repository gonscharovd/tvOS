# Session handoff — testOS

**Последнее обновление:** 2026-11-10
**Проект:** gonscharovd/tvOS
**Кодовая точка:** PR #14, refactor/testos-core-cleanup-2026-11, commit 6d3fc12a271ad8d5cb3681cc614e8182041fddcf
**Документационная точка:** PR #15, docs/project-memory-2026-11 (проверять актуальный branch HEAD в GitHub)

## Контекст

TOS-008 завершена: migration JVM test подключён к Linux/Windows CI (#322 success). TOS-009 implementation внесена в PR #14: destructive first-launch cleanup удалён, profile registry сохранён, backup rollback projection добавлена. Финальный CI run #330 для текущего head выполняется. TOS-006 остаётся BLOCKED до зелёного CI/review, read-only device inventory и signing compatibility. PR #14 и #15 остаются Open / Draft; main не менялся.

## Проверенное состояние GitHub

- main = a765318b577e66fae66a8ffe28439dcd6233f38f.
- PR #14 Open / Draft, base main, head 6d3fc12a271ad8d5cb3681cc614e8182041fddcf.
- PR #15 Open / Draft, base ref — PR #14 branch; GitHub API по-прежнему возвращает base SHA fa91b183..., устаревший относительно текущего code head. Перед merge проверить синхронизацию.
- Последний подтверждённый CI до TOS-009 — run #322 success на предыдущем head d1523ade... Новые прогоны #328/#329 завершились failure из-за временно удалённых JSON imports в SingleUserPrefs; imports восстановлены. Текущий полный CI run #330 (https://github.com/gonscharovd/tvOS/actions/runs/42791143018) выполняется на финальном head 6d3fc12a271ad8d5cb3681cc614e8182041fddcf.
- Последний GitHub Release — v2.1.73, versionCode 88, asset tvos-home.apk. PR #14 development target — 2.1.74 / code 89; release не опубликован.

## TOS-006 — аудит runtime regression

### Подтверждённые технические сведения

- Target: Sber SDX-50U4123B, Android 9 / API 28, ARMv7, firmware 1.103.91-b32273.
- App package: com.gonscharovd.tvoslauncher; Java Activity: com.techgenius.tvoslauncher.MainActivity; compiled HOME component ранее подтверждён как com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity.
- Manifest: minSdk 24, targetSdk 34, allowBackup=false.
- PR #14 build defaults: versionName 2.1.74, versionCode 89.
- Release workflow проверяет SHA-256 certificate fingerprint 5dbf8846d9bb76a764928da8c8a833a837b44ba00f4a7c0b60dc77490319b10a. Это expected release cert; fingerprint установленного TV APK в текущей сессии не считывался.
- CI preview APK подписан runner-generated debug key; запрещено устанавливать его поверх текущего приложения.
- AppUpdater смотрит GitHub latest release. Latest сейчас v2.1.73; не использовать latest OTA как способ установки PR #14. На первом запуске MainActivity автоматически проверяет updates, auto-update default true; при появлении prompt не выбирать Install.
- AppUpdater использует Android PackageInstaller, требует разрешение install unknown apps на API 26+, и при возврате из настроек может возобновить pending install. Не оставлять pending update во время теста.

### Migration / legacy data

- SingleUserPrefs.migrateIfNeeded() выполняется при первом вызове homePrefs() / userName(). Выбранный non-p1 profile копируется в canonical home; непустой старый home копируется в home_legacy_p1, если backup file пустой. Старые home_<profile> files остаются. Старые profiles.list/current сохраняются в legacy_list_before_single_user / legacy_current_before_single_user.
- TOS-009 удалил вызов и методы cleanupRemovedFeatureData() / deleteLegacyFeatureFiles() из MainActivity. Первый запуск больше не очищает prefs suggestions, nuvio_cloud, stremio_cloud, watch_cloud; shelf keys shelf_mode, cw_enabled, shelf, top_shelf_tab; cache/tmdb и files/continue.json. Код app startup не удаляет эти legacy data.
- SingleUserMigration.java не менялся. SingleUserPrefs сохраняет исходные profiles.list/current и ask_on_wake, добавляя migration metadata; предыдущий APK сможет прочитать прежний selected profile.
- Settings JSON backup включает profiles; для уже мигрированных установок export использует legacy_list_before_single_user / legacy_current_before_single_user как list/current. Restore merge-based и не очищает prefs. CleanYourTV state, ADB/security state и custom media files остаются вне JSON backup, но TOS-009 их не удаляет. Android OS может независимо очищать cache.
- Regression contracts обновлены; test_settings_backup_safety.py подключён к Linux и Windows jobs. На run #329 все Python/Java regression steps прошли, но APK compilation failed из-за временно удалённых JSON imports; imports restored in final head. Run #330 is pending.
- Не запускать runtime update до зелёного CI/review, свежего read-only device inventory и certificate gate. Не удалять приложение и не обходить signing mismatch.

### План после снятия блокеров

1. **Read-only device inventory:** проверить adb devices -l, модель/build/API/ABI, package/versionCode/versionName, текущий HOME resolver, наличие и enabled state штатного ru.sberdevices.starlauncher и Device Owner ru.sberdevices.SberDevicesServiceSystem, список disabled packages и свободное место.
2. **Certificate gate:** pm path com.gonscharovd.tvoslauncher → pull installed base.apk → apksigner verify --print-certs; compare SHA-256 with release fingerprint above. Candidate must have exact same cert, package ID, minSdk/API compatibility and higher versionCode. Stop on mismatch; no uninstall.
3. **Backup gate:** export Settings JSON before update and copy it off TV; separately record CleanYourTV disabled package list/count and copy custom user assets. Explicitly note backup omissions. Preserve stock launcher enabled.
4. **Install gate:** only after PO approval, use a release-signed PR #14 candidate with code 89; never use CI debug artifact. Prefer in-place adb install -r; do not uninstall. Do not use AppUpdater/latest release for PR branch.
5. **Migration verification:** first launch; confirm Night Scenes and user-visible preferences (dock order, hidden apps, weather, wallpapers/scenes). Confirm migration does not repeat after force-stop/relaunch/reboot. Verify saved JSON after migration. Private SharedPreferences cannot be read by normal ADB shell without root; do not claim internal files verified unless a supported export/diagnostic exists.
6. **HOME and remote:** confirm HOME opens testOS; verify stock Sber launcher remains enabled; test D-pad all directions on Night Scenes, dock, More Apps, top buttons; OK launch; Back closes sheets/menus and returns focus correctly; no accidental exit/crash.
7. **Settings:** enter each of four groups; test Up/Down, Right into content, Left/Back to sidebar, submenu Back and scrolling/focus retention.
8. **CleanYourTV:** open status/model/restore-list views read-only; do not invoke disable, enable, batch debloat or restore during TOS-006. Compare disabled package state before/after; protect stock launcher, Device Owner, SystemUI, Settings and TV input services.
9. **Lifecycle/performance:** force-stop/relaunch, sleep/wake and reboot; check logcat, meminfo, gfxinfo. Record any crash, ANR, focus loss or package-state delta.
10. **Rollback:** keep stock launcher enabled and verify a way to select it through system Home settings before test. Keep exact prior release APK and data backup. If PR build fails, return HOME to stock via verified system selector; do not uninstall testOS. Downgrade only after verifying same certificate and PackageManager acceptance; otherwise stop and reassess.

### Не выполнялось

- Нет ADB команд в этой сессии, нет свежего device inventory.
- Не извлекался installed APK, не проверялся installed certificate.
- Не собирался release-signed candidate; CI debug artifacts не скачивались/не устанавливались.
- Не делались backup/restore, миграция на пользовательских данных, disable/enable пакетов или HOME changes.
- APK не устанавливался; OTA не запускалась; main не менялся; PR не сливались.

## NEXT ACTION

**TOS-009 — дождаться результата полного CI run #330 на PR #14 head 6d3fc12a271ad8d5cb3681cc614e8182041fddcf; при success перепроверить regression contracts, обновить статусы TOS-009/DEBT-009 и синхронизировать Library. Не выполнять ADB, install, OTA, merge или изменения main.**
