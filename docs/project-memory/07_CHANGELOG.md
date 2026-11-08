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
