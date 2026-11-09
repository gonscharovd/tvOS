# Текущее состояние проекта

**Дата среза:** 2026-11-09  
**Репозиторий:** `gonscharovd/tvOS`  
**Default branch:** `main`

## Git и PR

| Объект | Подтверждённое состояние |
|---|---|
| main | `a765318b577e66fae66a8ffe28439dcd6233f38f` |
| Кодовая рабочая ветка | `refactor/testos-core-cleanup-2026-11` |
| Последний commit кода | `fa91b18305e57bae536d6664af88cb6230a4ec7e` |
| PR #14 | Open / Draft, base `main`, head `fa91b18...` |
| PR #14 diff | 28 файлов, 1,037 additions, 4,628 deletions |
| Документационная ветка | `docs/project-memory-2026-11` |
| PR #15 | Open / Draft, base — ветка PR #14; проверяй актуальный head непосредственно в GitHub |
| PR #15 diff | 18 Markdown-файлов: 11 новых project-memory документов и 7 обновлённых docs-файлов |
| Последний стабильный релиз | [v2.1.73](https://github.com/gonscharovd/tvOS/releases/tag/v2.1.73) |
| Package ID релиза | `com.gonscharovd.tvoslauncher` |
| versionCode релиза | 88 |
| Release APK | `tvos-home.apk`, 6,192,577 bytes |

PR #15 намеренно основан на ветке PR #14, чтобы память и синхронизированная документация описывали именно проверяемый core-cleanup код. Ни один из PR не влит в main.

## Состояние PR #14

В текущем head `fa91b18305e57bae536d6664af88cb6230a4ec7e` подтверждено:
- Night Scenes — единственный активный home root.
- Удалены Classic grid/wallpaper UI, Hero/Continue Watching shelf и связанный dead code.
- Удалена старая multi-profile модель; используются `SingleUserPrefs` и `SingleUserMigration`.
- Legacy preferences, wallpapers и wallpaper-folder metadata сохраняются для миграции/rollback.
- Сохранены app launch, dock, More Apps, Search, Weather, Control Panel, Recent Apps, Button Mapper, CleanYourTV, OTA и shared artwork/backdrop helpers.
- Добавлен единый version contract: `version.properties` задаёт development defaults `2.1.74` / versionCode `89`, release base `15` и prefix `2.1.`.
- `build.sh` и `build.ps1` читают общий источник; environment overrides `VERSION_CODE` / `VERSION_NAME` сохраняют приоритет.
- Build и release workflows читают version.properties; release formula сохранена как `15 + PATCH`.
- PR description и dependency map обновлены и больше не описывают удаление Classic UI или version drift как незавершённую работу.

PR #14 остаётся Draft и не merged. Код не опубликован как OTA.

## CI и сборка

- Последняя проверка PR #14 head: [run #313](https://github.com/gonscharovd/tvOS/actions/runs/42561116519), run ID `42561116519`, commit `fa91b18...`, conclusion `success`.
- Run #313 завершился успешно в обоих jobs: Ubuntu/Linux и Windows. Python build-contract regression и PowerShell version contract прошли; `build.sh` и `build.ps1` собрали APK; обе APK прошли signature verification и package/version assertions.
- Artifacts run #313: `testOS-ci-debug` (ID `13648674193`) и `testOS-ci-debug-windows` (ID `13649636643`), runner-generated debug-signed previews.
- В run #293 прошли настроенные Python regression tests и Java comparator/I18n tests; `build.sh` собрал APK; apksigner проверил подпись; artifact upload прошёл.
- CI artifact: `testOS-ci-debug`, artifact ID `13631916994`. Это runner-generated debug-signed preview, не release OTA.
- Artifact был скачан и распакован для инспекции бинарного AndroidManifest.xml. Подтверждено: APK package `com.gonscharovd.tvoslauncher`; HOME activity class `com.techgenius.tvoslauncher.MainActivity`. Корректный flattened component: `com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity`.
- `tests/SingleUserMigrationTest.java` существует, но workflow пока не запускает этот test (DEBT-008).
- PR #15 не имеет отдельного CI run: текущий `build-apk.yml` слушает PR, направленные в `main`, а PR #15 направлен в ветку PR #14. В PR #15 меняется только Markdown.
- Локальная сборка и runtime-проверка на телевизоре не выполнялись.

## Состояние документации

В PR #15 синхронизированы:
- `README.md`
- `docs/features.md`
- `docs/architecture.md`
- `docs/install.md`
- `docs/getting-started.md`
- `docs/README.md`
- `docs/refactor-cleanup-dependency-map.md`

Убраны устаревшие заявления о доступном Continue Watching/Hero UI, исправлено описание preview signing, обновлены stable release/version сведения и ADB component commands. TOS-003 выполнена в документационной ветке и ожидает review вместе с PR #15.

## Открытые ограничения

- `build.sh` / `build.ps1` version defaults унифицированы в `version.properties`; DEBT-001 устранён в PR #14.
- `build.ps1` содержит machine-specific JDK fallback path.
- Runtime regression PR #14 на Sber SDX-50U4123B не выполнена.
- Сертификат установленного на ТВ APK не сверялся с release certificate в этой сессии.
- Точное HOME component подтверждено по compiled APK, но фактическая команда PackageManager на телевизоре не тестировалась.
- `SingleUserMigrationTest.java` не включён в CI workflow.
- Стабильная `main` не изменена; OTA не выпускалась; APK на ТВ не устанавливался.

## Следующие приоритеты

1. Review PR #15 и документационных изменений.
2. TOS-004: убрать hard-coded JDK path из build.ps1.
3. TOS-008: добавить SingleUserMigrationTest в CI.
4. Runtime regression PR #14 и затем OTA/signing validation на целевом ТВ.
