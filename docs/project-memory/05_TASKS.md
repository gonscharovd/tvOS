# Список задач testOS

**Срез:** 2026-11-09. Задачи относятся к текущему состоянию репозитория и PR #14. Не создавай дубли — обновляй существующий ID.

Статусы: BACKLOG, READY, IN PROGRESS, BLOCKED, REVIEW, DONE.

| ID | Задача | Приоритет | Статус | Зависимости |
|---|---|---:|---|---|
| TOS-001 | Review core cleanup в PR #14: Night Scenes only, удаление Classic UI/старых профилей, миграция настроек | P1 | REVIEW | CI #293 success; требуется review и runtime validation |
| TOS-002 | Унифицировать build version defaults для build.sh/build.ps1/CI через единый источник | P1 | READY | TOS-001 |
| TOS-003 | Синхронизировать README/features/architecture/install с поведением PR #14 | P1 | READY | TOS-001 |
| TOS-004 | Удалить hard-coded путь JDK пользователя из build.ps1 и проверить Windows build | P2 | READY | TOS-002 |
| TOS-005 | Проверить HOME Activity component command по итоговому APK manifest и обновить install docs | P2 | READY | TOS-003 |
| TOS-006 | Выполнить runtime regression на Sber SDX-50U4123B для PR #14 | P1 | BLOCKED | Нужен тест на телевизоре и release-compatible APK |
| TOS-007 | Подтвердить сертификат установленного APK и OTA in-place update после PR #14 | P1 | BLOCKED | TOS-006, release signing |

## Детализация

### TOS-001 — Core cleanup
- **Описание:** завершить review 40-коммитного PR #14, который удаляет Classic home UI и multi-profile remnants, оставляя Night Scenes единственным home root.
- **Критерии:** CI latest head зелёный; migration tests проходят; code review не выявил потери пользовательских данных; runtime на Sber TV пройден; Product Owner одобрил merge.
- **Связанные файлы:** `MainActivity.java`, `SceneHome.java`, `Scenes.java`, `SettingsActivity.java`, `SingleUserPrefs.java`, `SingleUserMigration.java`, `tests/`.

### TOS-002 — Version source
- **Описание:** build.sh сейчас имеет defaults 15 / 2.1.2-testOS1; build.ps1 — 13 / 2.1.2-custom1; release workflow вычисляет code как 15 + PATCH.
- **Критерии:** один canonical source; оба скрипта и CI читают одинаковые значения; environment overrides сохраняются; tests/test_build_contract.py проверяет контракт.
- **Связанные файлы:** `build.sh`, `build.ps1`, `.github/workflows/build-apk.yml`, `.github/workflows/release.yml`, `tests/test_build_contract.py`.

### TOS-003 — Documentation sync
- **Описание:** убрать устаревшие описания Continue Watching/Classic/profile UI и привести release/build/signing claims к коду.
- **Критерии:** README, features, architecture и install guide соответствуют текущему PR; нет заявлений о непроверенных runtime функциях.
- **Связанные файлы:** `README.md`, `docs/features.md`, `docs/architecture.md`, `docs/install.md`, `docs/refactor-cleanup-dependency-map.md`.

### TOS-004 — Windows build
- **Описание:** заменить путь JDK, привязанный к профилю другого разработчика, на JAVA_HOME/PATH и проверить сборку.
- **Критерии:** build.ps1 работает на чистой Windows-среде с документированными требованиями.
- **Связанные файлы:** `build.ps1`, `docs/getting-started.md`.

### TOS-005 — Install component
- **Описание:** проверить расхождение source namespace `com.techgenius.tvoslauncher` и final application ID `com.gonscharovd.tvoslauncher` в install commands.
- **Критерии:** component string подтверждён итоговым APK manifest/PackageManager и проверен на устройстве.
- **Связанные файлы:** `AndroidManifest.xml`, `build.sh`, `build.ps1`, `docs/install.md`, `docs/getting-started.md`.

### TOS-006 / TOS-007 — Runtime and OTA
- **Описание:** проверить реальные remote navigation, lifecycle, permissions, migration, CleanYourTV и OTA.
- **Критерии:** чек-лист из [04_ROADMAP.md](04_ROADMAP.md) пройден; APK имеет ожидаемые package/version/certificate; in-place update не требует удаления приложения.
- **Ограничение:** CI build не подтверждает runtime на телевизоре.
