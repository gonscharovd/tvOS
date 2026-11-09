# Текущее состояние проекта

**Дата среза:** 2026-11-09  
**Репозиторий:** `gonscharovd/tvOS`  
**Default branch:** `main`

## Git и PR

| Объект | Подтверждённое состояние |
|---|---|
| main | `a765318b577e66fae66a8ffe28439dcd6233f38f` |
| Кодовая рабочая ветка | `refactor/testos-core-cleanup-2026-11` |
| Последний commit кода | `d1523ade55d66f32139cfa6fc31bc60dac7f50b2` |
| PR #14 | Open / Draft, base `main`, head `d1523ad...` |
| PR #14 diff | актуальный diff проверять в GitHub; TOS-008 добавила workflow/test files |
| Документационная ветка | `docs/project-memory-2026-11` |
| PR #15 | Open / Draft, base — ветка PR #14; проверяй актуальный head непосредственно в GitHub |
| PR #15 diff | 18 Markdown-файлов: 11 новых project-memory документов и 7 обновлённых docs-файлов |
| Последний стабильный релиз | [v2.1.73](https://github.com/gonscharovd/tvOS/releases/tag/v2.1.73) |
| Package ID релиза | `com.gonscharovd.tvoslauncher` |
| versionCode релиза | 88 |
| Release APK | `tvos-home.apk`, 6,192,577 bytes |

PR #15 намеренно основан на ветке PR #14, чтобы память и синхронизированная документация описывали именно проверяемый core-cleanup код. API GitHub по-прежнему возвращает для base PR #15 старый SHA `fa91b183...`, хотя base ref указывает на текущую ветку PR #14; перед merge нужно проверить/обновить base synchronization. Ни один из PR не влит в main.

## Состояние PR #14

В текущем head `d1523ade55d66f32139cfa6fc31bc60dac7f50b2` подтверждено:
- Night Scenes — единственный активный home root.
- Удалены Classic grid/wallpaper UI, Hero/Continue Watching shelf и связанный dead code.
- Удалена старая multi-profile модель; используются `SingleUserPrefs` и `SingleUserMigration`.
- `SingleUserPrefs` сохраняет migration-specific legacy profile data: исходные profile files остаются на месте; прежние `profiles.list/current` копируются в `legacy_list_before_single_user` / `legacy_current_before_single_user`; при переносе non-p1 создаётся `home_legacy_p1`, если старый `home` непустой. Wallpapers и wallpaper-folder metadata не удаляются migration helper.
- **Отдельный риск:** `MainActivity.cleanupRemovedFeatureData()` выполняется до основного `homePrefs()` при первом старте и очищает SharedPreferences `suggestions`, `nuvio_cloud`, `stremio_cloud`, `watch_cloud`; удаляет из canonical `home` ключи `shelf_mode`, `cw_enabled`, `shelf`, `top_shelf_tab`; удаляет `cache/tmdb` и `files/continue.json`. Это не часть `SingleUserMigration`, не покрыто migration JVM test и необратимо для этих данных. До runtime test требуется отдельное решение/изменение TOS-009.
- Сохранены app launch, dock, More Apps, Search, Weather, Control Panel, Recent Apps, Button Mapper, CleanYourTV, OTA и shared artwork/backdrop helpers.
- Добавлен единый version contract: `version.properties` задаёт development defaults `2.1.74` / versionCode `89`, release base `15` и prefix `2.1.`.
- `build.sh` и `build.ps1` читают общий источник; environment overrides `VERSION_CODE` / `VERSION_NAME` сохраняют приоритет.
- Build и release workflows читают version.properties; release formula сохранена как `15 + PATCH`.
- `build.ps1` больше не содержит machine-specific JDK path. Новый `tools/java.ps1` предпочитает валидный `JAVA_HOME`, иначе ищет `javac`, `java` и `keytool` в `PATH`; invalid `JAVA_HOME` завершается понятной ошибкой.
- PR description и dependency map обновлены и больше не описывают удаление Classic UI или version drift как незавершённую работу.

PR #14 остаётся Draft и не merged. Код не опубликован как OTA.

## CI и сборка

- Последняя проверка PR #14 head: [run #322](https://github.com/gonscharovd/tvOS/actions/runs/42607667504), run ID `42607667504`, commit `d1523ade55d66f32139cfa6fc31bc60dac7f50b2`, conclusion `success`.
- Run #322 завершился успешно в обоих jobs: Ubuntu/Linux и Windows. `SingleUserMigration.java` с `SingleUserMigrationTest.java` скомпилированы `javac --release 11` и выполнены на обоих runners; regression contract проверил wiring обоих jobs и защитные assertions legacy preservation. Все предыдущие Python/PowerShell contracts, Linux/Windows APK builds, signature и package/version assertions, artifacts — success.
- Artifacts run #317: `testOS-ci-debug` (ID `13666833681`) и `testOS-ci-debug-windows` (ID `13667867069`), runner-generated debug-signed previews.
- В run #293 прошли настроенные Python regression tests и Java comparator/I18n tests; `build.sh` собрал APK; apksigner проверил подпись; artifact upload прошёл.
- CI artifact: `testOS-ci-debug`, artifact ID `13631916994`. Это runner-generated debug-signed preview, не release OTA.
- Artifact был скачан и распакован для инспекции бинарного AndroidManifest.xml. Подтверждено: APK package `com.gonscharovd.tvoslauncher`; HOME activity class `com.techgenius.tvoslauncher.MainActivity`. Корректный flattened component: `com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity`.
- `tests/SingleUserMigrationTest.java` подключён к обоим CI jobs; `tests/test_single_user_migration_contract.py` защищает wiring и ключевые assertions от случайного удаления.
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
- Machine-specific JDK fallback удалён из `build.ps1`; `JAVA_HOME` и `PATH` resolution подтверждены run #317.
- Runtime regression PR #14 на Sber SDX-50U4123B не выполнена. Подготовлен безопасный план TOS-006; исполнение заблокировано TOS-009 (first-launch destructive cleanup audit) и проверкой signing compatibility.
- Сертификат установленного на ТВ APK не сверялся с release certificate в этой сессии. Release workflow проверяет ожидаемый SHA-256 certificate fingerprint `5dbf8846d9bb76a764928da8c8a833a837b44ba00f4a7c0b60dc77490319b10a`; совпадение с устройством не доказано.
- Последний GitHub Release — `v2.1.73`, versionCode 88; PR #14 development target — `2.1.74`, versionCode 89. Официальный release asset не является PR preview.
- На старте PR #14 runtime test latest installed TV version/package fingerprint не были получены заново. Ранее в истории упоминалась установка 2.1.61, но это не считать текущим фактом без read-only проверки.
- Точное HOME component подтверждено по compiled APK, но фактическая команда PackageManager на телевизоре не тестировалась.
- Runtime-поведение миграции на физическом ТВ по-прежнему не проверено; pure-Java migration test теперь запускается в CI.
- Стабильная `main` не изменена; OTA не выпускалась; APK на ТВ не устанавливался.

## Следующие приоритеты

1. Review PR #15 и документационных изменений.
2. TOS-008: подключена к CI и прошла Linux/Windows в run #322; ожидает review.
3. TOS-009: устранить/обезопасить first-launch legacy cleanup до runtime migration test.
4. TOS-006: выполнить read-only preflight, подтвердить installed APK fingerprint и только затем согласовать release-signed in-place candidate.
5. TOS-007: отдельная OTA validation после TOS-006; не запускать GitHub latest release updater во время TOS-006.
