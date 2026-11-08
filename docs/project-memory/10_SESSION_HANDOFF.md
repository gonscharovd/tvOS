# Session handoff — testOS

**Последнее обновление:** 2026-11-09 01:32 +05:00  
**Проект:** `gonscharovd/tvOS`  
**Срез кода:** PR #14, ветка `refactor/testos-core-cleanup-2026-11`  
**Последний commit кода:** `c08bc91e06b7fa66f6bd185e3ab5633033884c33`

## Контекст

Проведён аудит доступной части репозитория и создана первая версия переносимой внешней памяти в `docs/project-memory/`. Эта документация фиксирует состояние рабочего PR #14, а не только default branch.

## Что изучено

- GitHub repository metadata, default branch, main, рабочая ветка, commit history и PR #14.
- Diff PR #14: 20 файлов, 775 additions, 4604 deletions.
- README, CONTRIBUTING, manifest, build scripts, architecture/features/install/getting-started docs.
- MainActivity, SceneHome, SettingsActivity, Scenes, SingleUserPrefs/SingleUserMigration, CleanYourTv, LocalAdb, PowerService, overlays, tests и workflows.
- Последняя опубликованная release v2.1.73 / versionCode 88.
- Последний CI run #293 на PR head c08bc91: success.
- Предыдущая документация Library по Sber TV/testOS и аудит от 07.11 использованы как исторический контекст, но их статусы сверены с текущим GitHub.

## Что установлено

- PR #14 остаётся Open / Draft, не merged.
- Classic UI и старый Continue Watching/Hero paths удалены в рабочей ветке; Night Scenes — единственный Home root.
- Добавлена single-user preference migration с сохранением legacy данных.
- CI latest head прошёл; runtime на телевизоре после изменений PR не подтверждён.
- Build version defaults расходятся между build.sh и build.ps1.
- README/features/architecture содержат устаревшие описания удалённой полки/старого UI.
- PR preview workflow использует debug key runner; это не release-signed OTA artifact.
- Точная версия/подпись APK на ТВ после последнего OTA не проверялась в этой сессии.

## Изменения текущей сессии

- Создано 11 Markdown документов в `docs/project-memory/`.
- Документация находится в ветке `docs/project-memory-2026-11`; открыт Draft PR [#15](https://github.com/gonscharovd/tvOS/pull/15) в ветку PR #14.
- Сверка diff подтвердила: изменены только 11 файлов `docs/project-memory/*`; исходники приложения, manifest, build scripts, workflows и пользовательские данные не менялись.
- PR #15 не объединён; не выполнять merge автоматически.

## Проверки

- GitHub branch head и PR metadata сверены.
- Latest commit check run #293: success.
- Проверены список файлов PR и актуальная структура исходников.
- Дополнительно обнаружено: `tests/SingleUserMigrationTest.java` существует, но не включён в текущий build-apk.yml; зафиксировано в DEBT-008.
- Не запускались локальная сборка, локальные тесты и runtime tests на ТВ: в этой сессии использовались GitHub repository/CI данные.

## Текущая ветка и коммиты

- Кодовая рабочая ветка: `refactor/testos-core-cleanup-2026-11`.
- Последний commit кода: `c08bc91e06b7fa66f6bd185e3ab5633033884c33`.
- main: `a765318b577e66fae66a8ffe28439dcd6233f38f`.
- Система памяти: ветка `docs/project-memory-2026-11`, последний commit на момент handoff обновления — `036d45c88e192c518d93aa3f6540086e2b5a3d7a` (после правок handoff SHA изменится); Draft PR #15.

## Оставшиеся вопросы

- Подтвердить на телевизоре установленную версию и release certificate после последнего OTA.
- Проверить точную HOME Activity component string из итогового APK.
- Выполнить runtime regression PR #14.
- Сверить PR description/dependency map с последним CI и текущим diff.

## NEXT ACTION

**Синхронизировать README.md, docs/features.md, docs/architecture.md и docs/install.md с фактическим поведением PR #14: удалить устаревшие упоминания Continue Watching/Classic UI, обновить release/version сведения и исправить описание preview signing после проверки точных команд установки.**
