# Список задач testOS

**Срез:** 2026-11-10. Задачи отражают PR #14–#17 и новую цель универсального Android TV. Не создавай дубли — обновляй существующий ID.

Статусы: BACKLOG, READY, IN PROGRESS, BLOCKED, REVIEW, DONE.

| ID | Задача | Приоритет | Статус | Зависимости |
|---|---|---:|---|---|
| TOS-001 | Review core cleanup в PR #14: Night Scenes only, удаление Classic UI/старых профилей, миграция настроек | P1 | REVIEW | CI #322 success; migration JVM test included; требуется review и runtime validation |
| TOS-002 | Унифицировать build version defaults для build.sh/build.ps1/CI через единый источник | P1 | REVIEW | Реализовано в PR #14; CI #313 success, ожидает review вместе с PR |
| TOS-003 | Синхронизировать README/features/architecture/install с поведением PR #14 | P1 | REVIEW | TOS-001; изменения внесены в PR #15, ожидают review |
| TOS-004 | Удалить hard-coded путь JDK пользователя из build.ps1 и проверить Windows build | P2 | REVIEW | Реализовано; CI #317 Linux/Windows success; ожидает review PR #14 |
| TOS-005 | Проверить HOME Activity component command по итоговому APK manifest и обновить install docs | P2 | BLOCKED | APK manifest подтверждён и docs обновлены; остаётся проверка PackageManager на ТВ |
| TOS-006 | Выполнить runtime regression на Sber SDX-50U4123B для PR #14 | P1 | BLOCKED | TOS-009 CI/review; read-only device inventory; verified release signing compatibility |
| TOS-007 | Подтвердить сертификат установленного APK и OTA in-place update после PR #14 | P1 | BLOCKED | TOS-006, release signing |
| TOS-008 | Подключить SingleUserMigrationTest.java к CI workflow | P2 | REVIEW | TOS-001; Linux/Windows CI #322 success |
| TOS-009 | Сделать first-launch cleanup безопасным для legacy data до runtime migration | P1 | REVIEW | Реализация в PR #14; полный CI #330 success; ожидает code review |

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

### TOS-006 — Runtime regression на Sber SDX-50U4123B

- **Подготовлено:** план верификации на Android 9 / API 28 / ARMv7: read-only preflight, certificate/package/version gate, non-destructive migration verification, HOME/D-pad/Back/Night Scenes/Settings/CleanYourTV, lifecycle и rollback.
- **Текущее устройство:** package/version, HOME resolver, disabled packages и installed APK certificate не считаны заново в этой сессии. Исторический ADB endpoint `192.168.1.154:5555` не считать активным.
- **Подпись:** release workflow проверяет SHA-256 fingerprint `5dbf8846d9bb76a764928da8c8a833a837b44ba00f4a7c0b60dc77490319b10a`. CI preview APK использует runner debug key и запрещён для in-place install. Fingerprint installed APK пока неизвестен.
- **Release:** latest stable GitHub Release `v2.1.73` / code 88; PR development target `2.1.74` / code 89. Не запускать latest OTA во время runtime test; PR #14 не опубликован.
- **Migration:** `SingleUserPrefs.migrateIfNeeded()` выбирает текущий профиль, переносит его в `home`, сохраняет прежние registry values в legacy keys и при переносе non-p1 сохраняет непустой старый `home` в `home_legacy_p1`. Старые `home_<profile>` файлы не удаляются.
- **TOS-009 implementation:** вызов и методы `cleanupRemovedFeatureData()` / `deleteLegacyFeatureFiles()` удалены из `MainActivity`. Первый запуск больше не очищает prefs `suggestions`, `nuvio_cloud`, `stremio_cloud`, `watch_cloud`; shelf keys `shelf_mode`, `cw_enabled`, `shelf`, `top_shelf_tab`; каталог `cache/tmdb` и файл `files/continue.json`.
- **Profile rollback:** `SingleUserMigration.java` не менялся. `SingleUserPrefs` сохраняет исходные `profiles.list/current` и `ask_on_wake`, добавляя только новые migration metadata keys; старый APK сможет читать прежний выбор. Settings JSON backup включает `profiles` и для уже мигрированных установок экспортирует legacy values как `list/current`; импорт выполняется в новой версии до downgrade.
- **Rollback gate:** не удалять приложение; сохранить stock `ru.sberdevices.starlauncher` включённым; не менять Device Owner; до установки подтвердить package/version/cert и наличие release-signed rollback APK. Возврат HOME на stock должен опираться на фактически разрешённый системный HOME component, не на догадку.
- **Статус:** BLOCKED — план готов, device/runtime действий не выполнялось.
- **Связанные файлы:** `MainActivity.java`, `SingleUserPrefs.java`, `SingleUserMigration.java`, `AppUpdater.java`, `CustomMedia.java`, `CleanYourTv.java`, `AndroidManifest.xml`, `release.yml`.

### TOS-009 — First-launch legacy cleanup safety

- **Описание:** не допускать destructive cleanup retired-feature данных при первом запуске PR #14 и обеспечить обратимость single-user profile registry.
- **Изменения в PR #14:** удалены вызов и методы cleanup; legacy prefs, shelf keys и files/cache больше не затрагиваются кодом первого запуска. `SingleUserMigration.java` оставлен без изменений. `SingleUserPrefs` сохраняет исходные `profiles.list/current` и `ask_on_wake`; добавляет только legacy snapshot и single-user metadata.
- **Rollback export:** `CustomMedia` включает `profiles` в settings backup; если legacy snapshot существует, экспортируемые `list/current` берутся из него. Импорт выполняется merge-based, без очистки prefs. Для rollback: сохранить backup после обновления; при необходимости открыть Settings → Restore и импортировать его в PR #14 до downgrade; затем устанавливать старый APK только при совпадающем сертификате и допустимом downgrade. Кодовый путь ещё не проверен физически.
- **Ограничения backup:** CleanYourTV state, ADB/security state и custom media files остаются вне общего JSON backup, но TOS-009 их не удаляет. Android OS может очищать cache независимо от приложения.
- **Regression:** обновлены `tests/test_settings_backup_safety.py` и `tests/test_single_user_migration_contract.py`; backup safety test подключён к Windows job, Linux уже запускал его.
- **Критерии:** полный CI для текущего head зелёный; выполнено. Осталось code review; runtime/rollback на ТВ отдельно не проверялись. Никакого удаления legacy data; helper `SingleUserMigration` без изменений.
- **Статус:** REVIEW — изменения закоммичены в PR #14, полный CI #330 success; ожидает review.

### TOS-008 — Single-user migration CI coverage
- **Аудит:** `SingleUserMigration.migrate` — чистая Java-функция без Android-зависимостей; `SingleUserMigrationTest.java` — standalone `main` test, поэтому не требуются Android SDK stubs, Robolectric или instrumentation.
- **Изменения:** `.github/workflows/build-apk.yml` запускает compile/run `SingleUserMigration.java` + `SingleUserMigrationTest.java` через `javac --release 11` и `java` отдельно в Ubuntu и Windows jobs. Добавлен `tests/test_single_user_migration_contract.py`, проверяющий оба CI wiring и сохранность проверок legacy prefs/source immutability.
- **Границы:** production `SingleUserMigration.java` и `SingleUserPrefs.java` не менялись; миграция не запускалась против пользовательского хранилища, legacy данные не удалялись.
- **CI:** [run #322](https://github.com/gonscharovd/tvOS/actions/runs/42607667504) — success на обоих runners, включая тест и сборку APK.
- **Статус:** REVIEW — implementation и CI validation завершены, ожидает review PR #14.
- **Связанные файлы:** `.github/workflows/build-apk.yml`, `tests/SingleUserMigrationTest.java`, `tests/test_single_user_migration_contract.py`, `src/com/techgenius/tvoslauncher/SingleUserMigration.java`.



| TOS-010 | Remove OEM/Sber coupling from build-time asset optimization; profile describes language, not TV brand | P1 | REVIEW | PR #16; run #338 Linux/Windows success; asset-build scope only |
| TOS-011 | Add Device Profile Architecture: immutable device identity, Android Build/PackageManager resolver, fail-closed unknown/conflicting evidence | P1 | IN PROGRESS | PR #17; runs #339/#340 pending at last check; no runtime integrations yet |
| TOS-012 | Dynamic HOME detection via explicit PackageManager preferred activity + RoleManager self-role check; fail-safe protection of selected/alternate HOME candidates | P1 | REVIEW | Draft PR #18 based on PR #17; CI run #363 Linux/Windows success; no runtime validation |
| TOS-013 | Universal CleanYourTV safety: shared fail-closed policy, HOME/system/boot protection, guarded shell/restore/uninstall | P1 | REVIEW | Draft PR #19 based on PR #18; CI #456 Linux/Windows success; runtime validation pending |
| TOS-014 | Separate VPN provider abstraction from Happ; make Happ optional adapter | P2 | READY | Common provider contract; test missing apps and VPN API availability |
| TOS-015 | Build compatibility matrix API 24+ × OEM × features with evidence confidence | P2 | READY | TOS-011–014 |
| TOS-016 | Add regression scenarios for unknown OEM, stock/OEM HOME, missing integrations and restricted APIs | P1 | READY | TOS-011–015; Linux/Windows CI; runtime proof tracked separately |


### TOS-013 — Universal CleanYourTV Safety Audit

- **Реализация:** `PackageSafetyPolicy.java`, `AndroidPackageSafetyInspector.java`, `PackageMutationCommandGuard.java`; CleanYourTV single/batch disable and restore, LocalAdb transport guard, SceneHome user-app uninstall gate.
- **Safety:** `UNKNOWN` blocks action. Selected/alternate HOME (including disabled candidates), critical Android/TV roles and permissions, system UID, persistent apps, active admins/Device Owner/Profile Owner and system boot receivers are protected. System package candidate requires Build identity + complete inventory + matching OEM family + selected model package membership + independent Android checks.
- **Audit:** 341 profiles retained. `NEVER_DISABLE_EXACT` audited rule-by-rule; no existing rule removed. Added `com.google.android.tungsten.setupwraith` protection because existing code identifies its Wi-Fi ownership risk. Sber updater/service package runtime metadata remains unknown.
- **Restore fix:** validate saved package names; command guard accepts only exact single-package user-0 enable/disable; compound and shell uninstall commands are rejected.
- **Tests:** pure-Java policy, HOME, DeviceProfile and command-guard tests; Python contracts for Android wiring and all mutation paths. API 24/28/34/35 policy parity is JVM-level only; no real Android runtime simulation.
- **CI:** run #456 passed Linux and Windows on code commit `22d46a96a5dba243ae95c1a516b6890c6e72015d`; PR #19 latest head has a later docs-only audit report commit.
- **Статус:** REVIEW — Draft PR #19, not merged. Physical OEM runtime validation remains separate.
- **Связанные файлы:** `CleanYourTv.java`, `AndroidPackageSafetyInspector.java`, `PackageSafetyPolicy.java`, `PackageMutationCommandGuard.java`, `LocalAdb.java`, `AndroidHomeAppDetector.java`, `DeviceProfile.java`, `DeviceProfileResolver.java`, `SceneHome.java`, tests, `docs/cleanyourtv-safety-audit.md`.
