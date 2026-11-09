# Технический долг и известные проблемы

**Срез:** 2026-11-09. Статусы подтверждены чтением PR branch, README/docs, build scripts и CI.

| ID | Проблема | Влияние / серьёзность | Подтверждённая причина | Приоритет | Статус |
|---|---|---|---|---:|---|
| DEBT-001 | build.sh/build.ps1 имели разные version defaults | Риск разных APK для одного исходного кода; высокий | Общий `version.properties` подключён в PR #14; оба build scripts и workflows читают его | P1 | RESOLVED IN PR |
| DEBT-002 | Документация описывала удалённые функции | Риск считать Continue Watching/Classic UI доступными | Пользовательские docs синхронизированы с PR #14 в PR #15; ожидают review | P1 | IN REVIEW |
| DEBT-003 | build.ps1 содержал путь JDK из профиля другого разработчика | Сборка на другой Windows-машине могла не найти JDK автоматически | Machine-specific path удалён; `tools/java.ps1` реализует JAVA_HOME/PATH discovery, CI #317 прошёл | P2 | RESOLVED IN PR |
| DEBT-004 | Install docs использовали короткое имя Activity при различии app ID и Java namespace | Возможна неверная команда выбора HOME | Compiled APK manifest подтверждён; install docs исправлены на полное имя Activity; PackageManager на ТВ ещё не проверен | P2 | NEEDS VALIDATION |
| DEBT-005 | PR description/dependency-map header отставали от фактического head | Вводили в заблуждение о выполненном Classic cleanup и CI status | PR #14 body и dependency map обновлены: Classic cleanup и TOS-002 завершены, CI #313 success | P2 | RESOLVED IN PR |
| DEBT-006 | Runtime validation PR #14 на целевом ТВ отсутствует | CI не покрывает D-pad/lifecycle/permissions/OTA на реальном Android 9 | Последняя проверка #317 — CI build/tests на Linux и Windows, не device run | P1 | OPEN |
| DEBT-007 | CI preview APK подписан debug key runner | Нельзя считать preview artifact заменой установленного release APK | build-apk.yml вызывает build.sh без release keystore; runner создаёт свой debug keystore | P2 | EXPECTED LIMITATION |
| DEBT-008 | SingleUserMigrationTest не был подключён к CI workflow | Migration helper не проверялся автоматическим запуском unit test | TOS-008 добавила javac/java compile/run и regression contract в Linux и Windows jobs; CI #322 success | P2 | RESOLVED IN PR |
| DEBT-009 | First-launch cleanup необратимо удаляет часть retired-feature prefs/cache | Потеря legacy data до runtime проверки single-user migration | `MainActivity.cleanupRemovedFeatureData()` очищает suggestions/nuvio_cloud/stremio_cloud/watch_cloud prefs, shelf keys, cache/tmdb и files/continue.json до основной инициализации HOME; Settings backup не включает profiles/CleanYourTV state | P1 | OPEN |

## Подробности

### DEBT-001 — Version source
Устранено в PR #14: общий `version.properties` задаёт development defaults `2.1.74` / `89`, release code base `15` и prefix `2.1.`. `build.sh`, `build.ps1` (через `tools/version.ps1`), build CI и release workflow используют общий контракт. `VERSION_CODE` / `VERSION_NAME` environment overrides сохраняют приоритет. Release formula остаётся `15 + PATCH`; v2.1.73 → 88, v2.1.74 → 89. Linux и Windows builds, signature/package/version checks и contract tests прошли в CI run #313.

### DEBT-002 — Stale docs
В PR #15 README, docs/features, docs/architecture и связанные страницы синхронизированы с фактическим PR #14: удалены устаревшие заявления о Classic/Hero/Continue Watching, обновлены release/version сведения и исправлено описание preview signing. Изменения ожидают review.

### DEBT-003 — Windows JDK path
Устранено в PR #14: из `build.ps1` удалён machine-specific fallback. `tools/java.ps1` сначала проверяет `JAVA_HOME` и наличие `javac`, `java`, `keytool` в его `bin`; если `JAVA_HOME` пуст, разрешает все три команды через `PATH`. Некорректный непустой `JAVA_HOME` не маскируется fallback-ом, а выдаёт понятную ошибку. PowerShell regression прошёл на Linux и Windows, Windows build прошёл в CI #317.

### DEBT-004 — HOME component
В AndroidManifest.xml объявлена Activity с полным Java именем `com.techgenius.tvoslauncher.MainActivity`, а build renames manifest package до `com.gonscharovd.tvoslauncher`. Документация исправлена на `com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity` после проверки compiled APK manifest. Осталось проверить команду PackageManager на физическом ТВ; runtime failure не подтверждён.

### DEBT-005 — stale PR/dependency docs
PR #14 body и dependency map актуализированы в GitHub: описывают завершённый Classic cleanup, единый version contract и успешный CI #313. Старые промежуточные ошибки остаются только в истории CI/коммитов, а не как текущий статус.

### DEBT-006 — Runtime
Нужна проверка реального телевизора после изменения MainActivity, SceneHome, SettingsActivity и preference migration. Не ставить временно подписанный CI artifact поверх release install без проверки сертификата.

### DEBT-007 — Preview signing
Debug artifact годится для CI проверки/скачивания, но не гарантирует in-place update поверх release-signed app. Release workflow использует отдельные GitHub Secrets; значения секретов не документировать.

### DEBT-008 — Migration test coverage
Устранено в PR #14 (TOS-008). `SingleUserMigration.java` — pure-Java helper без Android API; workflow компилирует helper и `tests/SingleUserMigrationTest.java` с `javac --release 11`, затем запускает test main в Linux и Windows jobs. `tests/test_single_user_migration_contract.py` проверяет наличие обоих шагов в обоих jobs и сохраняет guardrails для legacy data/source immutability. CI run #322 прошёл. Runtime migration на пользовательском устройстве не выполнялась.


### DEBT-009 — First-launch legacy cleanup

Подтверждено статическим чтением PR #14. В `MainActivity.onCreate()` вызывается `cleanupRemovedFeatureData()` до присвоения `prefs = SingleUserPrefs.homePrefs(this)`. Одноразовая очистка удаляет prefs-файлы `suggestions`, `nuvio_cloud`, `stremio_cloud`, `watch_cloud`; удаляет из canonical `home` ключи `shelf_mode`, `cw_enabled`, `shelf`, `top_shelf_tab`; рекурсивно удаляет `cache/tmdb` и `files/continue.json`. Marker `recommendations_news_cloud_removed_v1` предотвращает повторный запуск, но не предоставляет rollback. Эти операции не являются частью `SingleUserMigration` и не покрываются TOS-008 JVM test. `CustomMedia.buildSettingsBackupJson()` исключает `profiles` и CleanYourTV prefs; manifest `allowBackup=false`. Дополнительный риск rollback: migration переписывает `profiles.list/current` в single-user `p1`, а старые значения сохраняет в новых `legacy_*` keys, которые предыдущая версия может не читать. До runtime update требуется TOS-009: определить non-destructive handling и проверяемый backup/rollback для feature data и profile registry.
