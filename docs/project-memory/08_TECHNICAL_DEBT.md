# Технический долг и известные проблемы

**Срез:** 2026-11-09. Статусы подтверждены чтением PR branch, README/docs, build scripts и CI.

| ID | Проблема | Влияние / серьёзность | Подтверждённая причина | Приоритет | Статус |
|---|---|---|---|---:|---|
| DEBT-001 | build.sh/build.ps1 имеют разные version defaults | Риск разных APK для одного исходного кода; высокий | build.sh: 15 / 2.1.2-testOS1; build.ps1: 13 / 2.1.2-custom1 | P1 | OPEN |
| DEBT-002 | Документация описывала удалённые функции | Риск считать Continue Watching/Classic UI доступными | README.md, features, architecture, install, getting-started и docs index синхронизированы с PR #14 в Draft PR #15 | P1 | IN REVIEW |
| DEBT-003 | build.ps1 содержит путь JDK из профиля другого разработчика | Сборка на другой Windows-машине может не найти JDK автоматически | hard-coded `C:\Users\asen4\AppData\Local\Programs\Microsoft\jdk-17.0.10.7-hotspot` | P2 | OPEN |
| DEBT-004 | Install docs используют короткое имя Activity при различии app ID и Java namespace | Возможна неверная команда выбора HOME | manifest components используют `com.techgenius.tvoslauncher.*`, build renames manifest package to `com.gonscharovd.tvoslauncher`; точную flattened component string нужно проверить на APK | P2 | NEEDS VALIDATION |
| DEBT-005 | PR description/dependency-map header отставали от фактического head | Вводили в заблуждение о выполненном Classic cleanup и CI status | PR #14 body и dependency map обновлены: Classic cleanup завершён, CI #293 success | P2 | RESOLVED IN PR |
| DEBT-006 | Runtime validation PR #14 на целевом ТВ отсутствует | CI не покрывает D-pad/lifecycle/permissions/OTA на реальном Android 9 | Последняя проверка #293 — CI build/tests, не device run | P1 | OPEN |
| DEBT-007 | CI preview APK подписан debug key runner | Нельзя считать preview artifact заменой установленного release APK | build-apk.yml вызывает build.sh без release keystore; runner создаёт свой debug keystore | P2 | EXPECTED LIMITATION |
| DEBT-008 | SingleUserMigrationTest не подключён к CI workflow | Поведение migration helper не покрывается автоматическим запуском этого Java unit test | Файл `tests/SingleUserMigrationTest.java` есть, но в build-apk.yml нет javac/java шага для него | P2 | OPEN |

## Подробности

### DEBT-001 — Version source
Устранено в PR #14: общий `version.properties` задаёт development defaults `2.1.74` / `89`, release code base `15` и prefix `2.1.`. `build.sh`, `build.ps1` (через `tools/version.ps1`), build CI и release workflow используют общий контракт. `VERSION_CODE` / `VERSION_NAME` environment overrides сохраняют приоритет. Release formula остаётся `15 + PATCH`; v2.1.73 → 88, v2.1.74 → 89. Linux CI прошёл на промежуточном commit; полная Windows build validation ожидает последнего CI run.

### DEBT-002 — Stale docs
В текущем PR branch тесты и source удаляют Classic home UI, old Hero/Continue Watching shelf и связанные permission paths. При этом README всё ещё утверждает, что native Continue Watching row может отображаться; docs/features описывает shelf; docs/architecture упоминает shelf. README также содержит устаревшее preview version 2.1.72 и некорректное утверждение, что PR preview использует persistent release signing key.

### DEBT-003 — Windows JDK path
В build.ps1 есть fallback на путь `C:\Users\asen4\...`, не относящийся к владельцу репозитория. Нужно предпочитать JAVA_HOME и PATH, не вводя machine-specific defaults.

### DEBT-004 — HOME component
В AndroidManifest.xml объявлена Activity с полным Java именем `com.techgenius.tvoslauncher.MainActivity`, а build renames manifest package до `com.gonscharovd.tvoslauncher`. Документация использует `com.gonscharovd.tvoslauncher/.MainActivity`. Это потенциальное несоответствие, но runtime failure не подтверждён; проверить итоговый APK manifest и PackageManager на устройстве.

### DEBT-005 — stale PR/dependency docs
PR #14 body и dependency map актуализированы в GitHub: описывают завершённый Classic cleanup и успешный CI #293. Старые промежуточные ошибки остаются только в истории CI/коммитов, а не как текущий статус.

### DEBT-006 — Runtime
Нужна проверка реального телевизора после изменения MainActivity, SceneHome, SettingsActivity и preference migration. Не ставить временно подписанный CI artifact поверх release install без проверки сертификата.

### DEBT-007 — Preview signing
Debug artifact годится для CI проверки/скачивания, но не гарантирует in-place update поверх release-signed app. Release workflow использует отдельные GitHub Secrets; значения секретов не документировать.

### DEBT-008 — Migration test coverage
`tests/SingleUserMigrationTest.java` содержит pure-Java regression cases для выбора профиля, сохранения legacy настроек и повторного запуска миграции. Но текущий `.github/workflows/build-apk.yml` запускает VersionComparatorTest и I18nDynamicTranslationTest, а отдельного шага для SingleUserMigrationTest нет. Добавить его в CI и проверить результат до merge PR #14.
