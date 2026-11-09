## 2026-11-09 — единый build version contract (TOS-002)

- Добавлен общий `version.properties`: development defaults `2.1.74` / versionCode `89`, release base `15`, prefix `2.1.`.
- `build.sh` и `build.ps1` читают один источник; environment overrides `VERSION_CODE` / `VERSION_NAME` сохранены.
- Добавлен PowerShell parser `tools/version.ps1` и regression test `tests/test_build_contract.ps1`; Python build-contract test проверяет Bash overrides, defaults, release formula и workflow wiring.
- `build-apk.yml` запускает Linux и Windows build jobs, проверяет APK package/version и загружает отдельные debug artifacts.
- `release.yml` читает release base/prefix из version.properties; формула остаётся `15 + PATCH`.
- Linux build, signature/package/version checks и PowerShell contract tests прошли в CI run #309; полный Windows build в run #313 ожидает завершения на момент записи.
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
- Последний commit на дату среза: `c08bc91e06b7fa66f6bd185e3ab5633033884c33`.
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
- [Последний CI run #293](https://github.com/gonscharovd/tvOS/actions/runs/42517077387)
