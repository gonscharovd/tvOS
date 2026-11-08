# Session handoff — testOS

**Последнее обновление:** 2026-11-09 02:10 +05:00  
**Проект:** `gonscharovd/tvOS`  
**Кодовая точка:** PR #14, `refactor/testos-core-cleanup-2026-11`, commit `c08bc91e06b7fa66f6bd185e3ab5633033884c33`  
**Документационная точка:** PR #15, `docs/project-memory-2026-11` (проверять актуальный branch HEAD в GitHub; этот handoff входит в тот же PR)

## Контекст

Восстановлена последняя подтверждённая точка из предыдущего handoff. Classic UI cleanup в PR #14 завершён и CI прошёл. Текущий handoff был синхронизацией пользовательской документации с кодом PR #14 и устранением противоречий в PR descriptions.

## Что проверено

- Репозиторий metadata, default branch, ветки, последние commits, PR #14/#15, PR diff и GitHub Actions.
- Все 11 документов `docs/project-memory/`.
- README, docs index, features, architecture, install, getting-started и dependency map.
- `MainActivity.java`, `SceneHome.java`, `SettingsActivity.java`, `SetupActivity.java`, `Suggestions.java`, `Scenes.java`.
- `AndroidManifest.xml`, `build.sh`, `build.ps1`, `build-apk.yml`, `release.yml`.
- Скачан CI artifact PR #14 и разобран binary AndroidManifest.xml: package `com.gonscharovd.tvoslauncher`, HOME class `com.techgenius.tvoslauncher.MainActivity`.
- Проверена последняя успешная CI run #293 на PR #14 head c08bc91.

## Что изменено

В документационной ветке PR #15 обновлены:
- `README.md`
- `docs/README.md`
- `docs/features.md`
- `docs/architecture.md`
- `docs/install.md`
- `docs/getting-started.md`
- `docs/refactor-cleanup-dependency-map.md`
- `docs/project-memory/03_CURRENT_STATE.md`
- `docs/project-memory/05_TASKS.md`
- `docs/project-memory/10_SESSION_HANDOFF.md`

Также обновлены PR #14 и PR #15 descriptions, чтобы они отражали завершённый core cleanup, успешный CI и фактический объём документационного PR.

## Подтверждённое состояние GitHub

- `main`: `a765318b577e66fae66a8ffe28439dcd6233f38f`.
- PR #14: Open / Draft, base main, head `c08bc91e06b7fa66f6bd185e3ab5633033884c33`.
- PR #14 latest CI: run #293 / ID `42517077387`, success.
- PR #15: Open / Draft, base `refactor/testos-core-cleanup-2026-11`; актуальный head проверяется в GitHub перед продолжением.
- PR #15 содержит 18 Markdown-файлов (11 новых project-memory + 7 обновлённых docs).
- PR #15 не имеет отдельного CI run, так как workflow PR trigger настроен на base `main`; PR #15 направлен в feature branch.
- Stable release: v2.1.73 / versionCode 88.
- PR preview artifact — runner debug-signed; не устанавливать его поверх release APK без сравнения сертификата.
- Никаких изменений в main, OTA release или установок APK на телевизор не выполнялось.

## Осталось

- PR #15 документационные изменения ожидают review.
- build.sh и build.ps1 имеют разные version defaults.
- build.ps1 содержит machine-specific JDK fallback.
- SingleUserMigrationTest.java не включён в CI.
- Runtime regression PR #14 на Sber TV не выполнена.
- HOME component подтверждён по compiled APK manifest, но не проверен PackageManager на телевизоре.

## NEXT ACTION

**Выполнить TOS-002: спроектировать и внедрить единый источник VERSION_CODE / VERSION_NAME для build.sh, build.ps1 и CI, сохранив environment overrides и текущую release formula; затем добавить/обновить build-contract tests.**
