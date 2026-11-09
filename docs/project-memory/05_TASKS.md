# Список задач testOS

**Срез:** 2026-11-09. Задачи относятся к текущему состоянию репозитория и PR #14. Не создавай дубли — обновляй существующий ID.

Статусы: BACKLOG, READY, IN PROGRESS, BLOCKED, REVIEW, DONE.

| ID | Задача | Приоритет | Статус | Зависимости |
|---|---|---:|---|---|
| TOS-001 | Review core cleanup в PR #14: Night Scenes only, удаление Classic UI/старых профилей, миграция настроек | P1 | REVIEW | CI #317 success; требуется review и runtime validation |
| TOS-002 | Унифицировать build version defaults для build.sh/build.ps1/CI через единый источник | P1 | REVIEW | Реализовано в PR #14; CI #313 success, ожидает review вместе с PR |
| TOS-003 | Синхронизировать README/features/architecture/install с поведением PR #14 | P1 | REVIEW | TOS-001; изменения внесены в PR #15, ожидают review |
| TOS-004 | Удалить hard-coded путь JDK пользователя из build.ps1 и проверить Windows build | P2 | REVIEW | Реализовано; CI #317 Linux/Windows success; ожидает review PR #14 |
| TOS-005 | Проверить HOME Activity component command по итоговому APK manifest и обновить install docs | P2 | BLOCKED | APK manifest подтверждён и docs обновлены; остаётся проверка PackageManager на ТВ |
| TOS-006 | Выполнить runtime regression на Sber SDX-50U4123B для PR #14 | P1 | BLOCKED | Нужен тест на телевизоре и release-compatible APK |
| TOS-007 | Подтвердить сертификат установленного APK и OTA in-place update после PR #14 | P1 | BLOCKED | TOS-006, release signing |
| TOS-008 | Подключить SingleUserMigrationTest.java к CI workflow | P2 | READY | TOS-001 |

## Детализация

### TOS-001 — Core cleanup
- **Описание:** завершить review 40-коммитного PR #14, который удаляет Classic home UI и multi-profile remnants, оставляя Night Scenes единственным home root.
- **Критерии:** CI latest head зелёный; migration tests проходят; code review не выявил потери пользовательских данных; runtime на Sber TV пройден; Product Owner одобрил merge.
- **Связанные файлы:** `MainActivity.java`, `SceneHome.java`, `Scenes.java`, `SettingsActivity.java`, `SingleUserPrefs.java`, `SingleUserMigration.java`, `tests/`.

### TOS-002 — Version source
- **Результат:** добавлен `version.properties` с development defaults `2.1.74` / `89`, release base `15` и prefix `2.1.`. `build.sh`, `build.ps1` (через `tools/version.ps1`), build CI и release CI используют единый источник; environment overrides сохранены.
- **Regression:** `tests/test_build_contract.py` проверяет Bash defaults/overrides, release formula и workflow wiring; `tests/test_build_contract.ps1` проверяет PowerShell parser/defaults/overrides. CI #313 прошёл на Ubuntu и Windows, обе APK собраны и проверены.
- **Критерии:** выполнены; PR #14 остаётся Draft и ожидает review.
- **Статус:** REVIEW — реализация и CI validation завершены, merge не выполнялся.
- **Связанные файлы:** `version.properties`, `build.sh`, `build.ps1`, `tools/version.ps1`, `.github/workflows/build-apk.yml`, `.github/workflows/release.yml`, `tests/test_build_contract.py`, `tests/test_build_contract.ps1`.

### TOS-003 — Documentation sync
- **Описание:** убрать устаревшие описания Continue Watching/Classic/profile UI и привести release/build/signing claims к коду.
- **Результат:** обновлены README, features, architecture, install, getting-started, docs index и dependency map в PR #15. Исправлены component names после инспекции compiled APK manifest; preview signing описан как runner debug key.
- **Статус:** REVIEW — изменения готовы в PR #15, ожидают review/merge.
- **Связанные файлы:** `README.md`, `docs/features.md`, `docs/architecture.md`, `docs/install.md`, `docs/getting-started.md`, `docs/README.md`, `docs/refactor-cleanup-dependency-map.md`.

### TOS-004 — Windows build
- **Результат:** machine-specific JDK fallback удалён. Добавлен `tools/java.ps1`: валидный `JAVA_HOME` имеет приоритет; при пустом `JAVA_HOME` инструменты `javac`, `java`, `keytool` разрешаются из `PATH`; некорректный `JAVA_HOME` вызывает понятную ошибку.
- **Regression:** `tests/test_build_contract.ps1` проверяет JAVA_HOME, PATH fallback и invalid JAVA_HOME; Python test запрещает старый абсолютный путь и проверяет wiring helper.
- **CI:** run #317 прошёл на Ubuntu/Linux и Windows; PowerShell tests прошли на обоих runners; Windows `build.ps1` собрал APK, package/version/signature checks и artifact upload успешны.
- **Статус:** REVIEW — реализация и CI validation завершены, PR #14 не merged.
- **Связанные файлы:** `build.ps1`, `tools/java.ps1`, `tests/test_build_contract.ps1`, `tests/test_build_contract.py`, `docs/getting-started.md`.

### TOS-005 — Install component
- **Описание:** проверить расхождение source namespace `com.techgenius.tvoslauncher` и final application ID `com.gonscharovd.tvoslauncher` в install commands.
- **Результат:** compiled CI APK manifest подтверждает package `com.gonscharovd.tvoslauncher` и Activity class `com.techgenius.tvoslauncher.MainActivity`; docs commands исправлены на полное имя класса.
- **Осталось:** проверить flattened component через PackageManager на целевом ТВ.
- **Связанные файлы:** `AndroidManifest.xml`, `build.sh`, `build.ps1`, `docs/install.md`, `docs/getting-started.md`.

### TOS-006 / TOS-007 — Runtime and OTA
- **Описание:** проверить реальные remote navigation, lifecycle, permissions, migration, CleanYourTV и OTA.
- **Критерии:** чек-лист из [04_ROADMAP.md](04_ROADMAP.md) пройден; APK имеет ожидаемые package/version/certificate; in-place update не требует удаления приложения.
- **Ограничение:** CI build не подтверждает runtime на телевизоре.
