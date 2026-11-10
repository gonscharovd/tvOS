## 2026-11-10 — TOS-009: сохранение legacy data и profile rollback

- Удалены first-launch вызов и реализация destructive cleanup из MainActivity; legacy preference files, shelf keys, cache/tmdb и files/continue.json больше не удаляются приложением.
- SingleUserMigration helper не изменён. SingleUserPrefs сохраняет исходные profiles.list/current и ask_on_wake, добавляя только migration metadata; это сохраняет чтение выбранного профиля предыдущей версией.
- Settings backup включает profiles и проецирует сохранённые legacy list/current в экспортируемый registry для уже мигрированных установок; импорт merge-based.
- Обновлены backup safety и migration regression contracts; backup safety test подключён к Windows job (уже был в Linux job).
- Полный CI #330 для конечного TOS-009 HEAD прошёл success на Linux и Windows. На ТВ не запускались ADB, backup/restore, APK install или OTA; main не менялся, PR не сливались.

## 2026-11-09 — TOS-006 runtime regression preflight

- Проверены текущие GitHub branches, PR #14/#15, main, release metadata и Actions. PR #14 head `d1523ade55d66f32139cfa6fc31bc60dac7f50b2`; CI #322 success; main unchanged.
- Последний stable release подтверждён как `v2.1.73` / versionCode 88; PR #14 development target — 2.1.74 / code 89.
- Release workflow проверяет SHA-256 certificate fingerprint `5dbf8846d9bb76a764928da8c8a833a837b44ba00f4a7c0b60dc77490319b10a`. Installed TV APK fingerprint пока неизвестен; debug CI artifacts не подходят для in-place update.
- Изучены `AppUpdater`, `SingleUserPrefs`, `SingleUserMigration`, `MainActivity`, `SettingsActivity`, `SceneHome`, `CleanYourTv`, `CustomMedia` и workflow/tests.
- Обнаружен P1 data-retention blocker: first-launch `cleanupRemovedFeatureData()` очищает retired-feature preferences/cache отдельно от migration. Добавлен TOS-009 / DEBT-009; TOS-006 runtime update заблокирован до решения.
- Подготовлен runtime test plan: read-only inventory, signature gate, settings backup limits, migration validation, HOME/D-pad/Back/Night Scenes/Settings/CleanYourTV, lifecycle, rollback. Ни одной команды на ТВ не выполняли; APK не устанавливался; OTA не запускалась; main/PR merge не затрагивались.

## 2026-11-09 — Single-user migration test в CI (TOS-008)

- Изучены `SingleUserMigration.java`, `SingleUserPrefs.java` и `tests/SingleUserMigrationTest.java`. Helper — pure Java без Android API; test — standalone main.
- Добавлены compile/run steps через `javac --release 11` и `java` в Linux и Windows jobs workflow.
- Добавлен `tests/test_single_user_migration_contract.py`: проверяет wiring обоих jobs, запуск test main, наличие legacy preservation/source immutability assertions.
- Production migration code не менялся; миграция не запускалась на пользовательских данных; legacy files/preferences не удалялись.
- CI [run #322](https://github.com/gonscharovd/tvOS/actions/runs/42607667504) прошёл полностью на Ubuntu/Linux и Windows, включая APK build, package/version/signature checks и artifact upload.
- Промежуточные CI #319/#320 выявили неверное расположение шагов в workflow; размещение исправлено, финальный run #322 зелёный. PR #14 обновлён; main, OTA и устройство не затрагивались.

## 2026-11-09 — portable JDK discovery (TOS-004)

- Удалён machine-specific путь `C:\Users\asen4\...` из `build.ps1`.
- Добавлен `tools/java.ps1`: приоритет у валидного `JAVA_HOME`; при пустом `JAVA_HOME` `javac`, `java`, `keytool` ищутся через `PATH`.
- Некорректный непустой `JAVA_HOME` вызывает понятную ошибку вместо тихого fallback на случайный JDK.
- `tests/test_build_contract.ps1` покрывает JAVA_HOME, PATH fallback и invalid JAVA_HOME; Python contract test проверяет отсутствие старого пути и подключение helper.
- Обновлён `docs/getting-started.md`: требования JDK и единые version defaults приведены к актуальному состоянию.
- CI run #317 прошёл на Ubuntu/Linux и Windows; PowerShell tests прошли на обоих runners; `build.sh` и `build.ps1` собрали APK, проверки подписи/package/version и artifact upload прошли.
- PR #14 обновлён; merge, OTA release и установка APK на ТВ не выполнялись.

## 2026-11-09 — единый build version contract (TOS-002)

- Добавлен общий `version.properties`: development defaults `2.1.74` / versionCode `89`, release base `15`, prefix `2.1.`.
- `build.sh` и `build.ps1` читают один источник; environment overrides `VERSION_CODE` / `VERSION_NAME` сохранены.
- Добавлен PowerShell parser `tools/version.ps1` и regression test `tests/test_build_contract.ps1`; Python build-contract test проверяет Bash overrides, defaults, release formula и workflow wiring.
- `build-apk.yml` запускает Linux и Windows build jobs, проверяет APK package/version и загружает отдельные debug artifacts.
- `release.yml` читает release base/prefix из version.properties; формула остаётся `15 + PATCH`.
- CI run #313 прошёл на Ubuntu/Linux и Windows: оба build scripts собрали APK, подписи и package/version metadata проверены, оба debug artifacts загружены.
- Изменения внесены в рабочую ветку PR #14; merge, OTA release и установка APK на ТВ не выполнялись.

## 2026-11-09 — синхронизация пользовательской документации с PR #14

- README, docs index, features, architecture, install, getting-started и dependency map обновлены по фактическому Night Scenes-only коду PR #14.
- Удалены устаревшие описания доступного Continue Watching/Hero UI; уточнено отсутствие user-created app folders.
- Исправлено описание preview signing: PR CI использует runner-generated debug key, release workflow — persistent release key.
- Stable release обновлён до v2.1.73 / versionCode 88.
- По compiled CI APK manifest подтверждены package `com.gonscharovd.tvoslauncher` и HOME activity `com.techgenius.tvoslauncher.MainActivity`; исправлены ADB component commands.
- PR #14 description актуализирован; PR #15 расширен до 18 Markdown-файлов (11 project-memory документов + 7 docs).
- Изменения находятся в Draft PR #15, не влиты в PR #14 или main.

# Значимые изменения testOS

Этот журнал фиксирует только подтверждённые значимые изменения. Полный commit history остаётся в GitHub.

## 2026-11-09 — создана внешняя память проекта
- Добавлен каталог `docs/project-memory/` с 11 файлами, описывающими проект, архитектуру, состояние, roadmap, задачи, решения, историю, технический долг и handoff.
- Документация создаётся в отдельной ветке; исходники приложения не меняются.
- Открыт Draft PR [#15](https://github.com/gonscharovd/tvOS/pull/15) в рабочую ветку PR #14.
- При дополнительной сверке workflow обнаружено, что `SingleUserMigrationTest.java` существует, но не запускается текущим build-apk.yml; это записано как DEBT-008.

## 2026-11-08 — PR #14: core cleanup (ещё не merged)
Рабочая ветка: `refactor/testos-core-cleanup-2026-11`.
- Удалён старый Classic home UI и устаревшие Hero/Continue Watching paths.
- Night Scenes оставлен единственным активным Home root.
- Удалена multi-profile модель; добавлена безопасная single-user preference migration.
- Сохранены legacy user assets/preferences для rollback.
- Добавлены/обновлены regression guards и Java migration test.
- На момент завершения core cleanup head был `c08bc91e06b7fa66f6bd185e3ab5633033884c33`; позднее PR #14 расширен TOS-002 до `fa91b18305e57bae536d6664af88cb6230a4ec7e`.
- PR #14 Open / Draft; merge и OTA не выполнялись.

## 2026-11-08 — testOS 2.1.73
- Опубликован стабильный GitHub Release `v2.1.73`.
- APK asset: `tvos-home.apk`, package `com.gonscharovd.tvoslauncher`, versionCode 88.
- Release содержит предшествующие PR #12 и #13; он не включает незавершённый PR #14.

## 2026-11-08 — упрощённая русская сборка
- В main влит PR #13 (merge commit `a765318b577e66fae66a8ffe28439dcd6233f38f`).
- Основная линия — упрощённый Russian-only testOS на базе 2.1.72.
- Стабильная ветка и PR #14 должны рассматриваться отдельно.

## 2026-11-07 — CleanYourTV safety и build regression work
- Предыдущий аудит выявил проблемы проверки фактического package state, защиты системных пакетов и версионного сравнения.
- Исправления и regression tests существовали в отдельной рабочей ветке; не переносить старые статусы без проверки актуальной ветки.
- Исторический аудит: `LAUNCHER_AUDIT_2026-11-07.md` в Library пользователя; актуальные подтверждения — GitHub branch/CI.

## Ссылки
- [PR #14](https://github.com/gonscharovd/tvOS/pull/14)
- [Release v2.1.73](https://github.com/gonscharovd/tvOS/releases/tag/v2.1.73)
- [Последний CI run #313](https://github.com/gonscharovd/tvOS/actions/runs/42561116519)


## 2026-11-10 — TOS-012 Dynamic HOME Detection (CI review pending)

- Added pure-Java `HomeAppDetector` model and Android `AndroidHomeAppDetector` adapter.
- HOME candidates are enumerated with PackageManager; current HOME requires explicit preferred-activity evidence. `resolveActivity` is deliberately not used for HOME selection because best-match/priority is not proof of user selection.
- API 29+ checks public `RoleManager.isRoleHeld` for testOS; it does not call hidden `getRoleHolders`.
- Removed fixed 23-package stock HOME list and Philips/TCL HOME lock heuristic from `Perms.java`. Updated LocalAdb role-holder removal and PowerService boot guard to use discovered system HOME candidates.
- CleanYourTV protects the selected HOME and fails closed when selection/candidate role is unknown; no changes to the 341 model profiles.
- Added Android HOME manifest package visibility, pure-Java policy tests and Linux/Windows wiring contract.
- Current branch `feat/dynamic-home-detection-2026-11`; run #357 on head `877b74597e46a7b355485458274ac93492902e8e` pending at time of entry. No PR merged, no OTA, no APK install, no TV changes.
