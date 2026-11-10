# Текущее состояние проекта

**Дата среза:** 2026-11-10 (обновлено после PR #16/#17)  
**Репозиторий:** `gonscharovd/tvOS`  
**Default branch:** `main`

## Git и PR

| Объект | Подтверждённое состояние |
|---|---|
| main | `a765318b577e66fae66a8ffe28439dcd6233f38f` |
| Кодовая рабочая ветка | `refactor/testos-core-cleanup-2026-11` |
| Последний commit кода | `6d3fc12a271ad8d5cb3681cc614e8182041fddcf` |
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
- **TOS-009 implementation in PR #14:** удалены вызов и реализация `MainActivity.cleanupRemovedFeatureData()` / `deleteLegacyFeatureFiles()`. Первый запуск больше не очищает `suggestions`, `nuvio_cloud`, `stremio_cloud`, `watch_cloud`, shelf keys в `home`, `cache/tmdb` или `files/continue.json`. Это не меняет `SingleUserMigration` helper.
- Сохранены app launch, dock, More Apps, Search, Weather, Control Panel, Recent Apps, Button Mapper, CleanYourTV, OTA и shared artwork/backdrop helpers.
- Добавлен единый version contract: `version.properties` задаёт development defaults `2.1.74` / versionCode `89`, release base `15` и prefix `2.1.`.
- `build.sh` и `build.ps1` читают общий источник; environment overrides `VERSION_CODE` / `VERSION_NAME` сохраняют приоритет.
- Build и release workflows читают version.properties; release formula сохранена как `15 + PATCH`.
- `build.ps1` больше не содержит machine-specific JDK path. Новый `tools/java.ps1` предпочитает валидный `JAVA_HOME`, иначе ищет `javac`, `java` и `keytool` в `PATH`; invalid `JAVA_HOME` завершается понятной ошибкой.
- PR description и dependency map обновлены и больше не описывают удаление Classic UI или version drift как незавершённую работу.

PR #14 остаётся Draft и не merged. Код не опубликован как OTA.

## CI и сборка

- Полный CI run #330 на финальном head `6d3fc12a271ad8d5cb3681cc614e8182041fddcf` завершился `success` на Linux и Windows: https://github.com/gonscharovd/tvOS/actions/runs/42791143018. Промежуточные #328/#329 падали из-за временно удалённых JSON imports; imports восстановлены до #330.
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
- **Profile rollback:** `SingleUserPrefs` больше не переписывает `profiles.list/current` и `ask_on_wake`; старый APK сможет прочитать прежний выбранный профиль. Значения также сохраняются в `legacy_list_before_single_user` / `legacy_current_before_single_user`. Settings JSON backup теперь включает `profiles` и проецирует эти legacy values обратно в экспортируемые `list/current`, чтобы поддержать ручное восстановление для уже мигрированных установок. Import/rollback на физическом ТВ ещё не проверен.
- Runtime regression PR #14 на Sber SDX-50U4123B не выполнена. TOS-009 implementation и полный CI #330 завершены, ожидают review; TOS-006 остаётся заблокирована read-only device inventory и проверкой signing compatibility.
- Сертификат установленного на ТВ APK не сверялся с release certificate в этой сессии. Release workflow проверяет ожидаемый SHA-256 certificate fingerprint `5dbf8846d9bb76a764928da8c8a833a837b44ba00f4a7c0b60dc77490319b10a`; совпадение с устройством не доказано.
- Последний GitHub Release — `v2.1.73`, versionCode 88; PR #14 development target — `2.1.74`, versionCode 89. Официальный release asset не является PR preview.
- На старте PR #14 runtime test latest installed TV version/package fingerprint не были получены заново. Ранее в истории упоминалась установка 2.1.61, но это не считать текущим фактом без read-only проверки.
- Точное HOME component подтверждено по compiled APK, но фактическая команда PackageManager на телевизоре не тестировалась.
- Runtime-поведение миграции на физическом ТВ по-прежнему не проверено; pure-Java migration test теперь запускается в CI.
- Стабильная `main` не изменена; OTA не выпускалась; APK на ТВ не устанавливался.

## Следующие приоритеты

1. Review PR #15 и документационных изменений.
2. TOS-008: подключена к CI и прошла Linux/Windows в run #322; ожидает review.
3. TOS-009: implementation и полный CI #330 успешны; ожидает review PR #14.
4. TOS-006: выполнить read-only preflight, подтвердить installed APK fingerprint и только затем согласовать release-signed in-place candidate.
5. TOS-007: отдельная OTA validation после TOS-006; не запускать GitHub latest release updater во время TOS-006.



## Universal portability audit — 2026-11-10

- PR #16 Open / Draft, base PR #14, head `85513e87f4b6abfe49352e67e8844d5fdbceb56a`; TOS-010 changed build asset profile from OEM-specific `sber` to language-based `ru`. GitHub Actions run #338 passed Linux and Windows.
- PR #17 Open / Draft, base PR #16, head `a7fa0057b22c3125c087f9a78d60f7d7837def1c`; TOS-011 adds `DeviceProfile.java`, `DeviceProfileResolver.java`, `DeviceProfileTest.java`; runs #339/#340 were still in progress at last check.
- PR #14 Open / Draft, head `6d3fc12a271ad8d5cb3681cc614e8182041fddcf`, CI #330 success. PR #15 Open / Draft; GitHub reports base SHA `fa91b18305e57bae536d6664af88cb6230a4ec7e`, older than current PR #14 head; sync required before merge.
- Confirmed code issues: `CleanYourTv.java` retains Sber Device Owner/StarLauncher hard-protection entries; `Perms.java` has Philips/TCL-specific HOME handling and a static stock HOME package list; `VpnController.java` toggles Happ only. 341 CleanYourTV profiles have not been rewritten.
- PR #16 only removes OEM coupling from asset optimization. PR #17 adds isolated device identity metadata; neither proves universal runtime behavior.
- `main` unchanged; no PR merged; no APK installed on TV; no OTA published; no runtime device validation.


## TOS-012 — Dynamic HOME Detection (2026-11-10, CI pending)

- Work branch: `feat/dynamic-home-detection-2026-11`, based on PR #17 head `a7fa0057b22c3125c087f9a78d60f7d7837def1c`. Current code head at documentation update: `877b74597e46a7b355485458274ac93492902e8e`.
- Added pure-Java `HomeAppDetector` policy model, Android adapter `AndroidHomeAppDetector`, and `HomeAppDetectorTest`.
- Current HOME is identified from explicit `PackageManager.getPreferredActivities` evidence; API 29+ also checks public `RoleManager.isRoleHeld(ROLE_HOME)` for testOS. Candidate discovery uses `queryIntentActivities(ACTION_MAIN + CATEGORY_HOME)`. No candidate order/first-match heuristic is used; `resolveActivity` is intentionally not used for HOME selection.
- `Perms.isDefaultHome` and `hasEnabledStockLauncher` use dynamic detection. Static OEM package list and Philips/TCL lock heuristic were removed from `Perms.java`.
- CleanYourTV protects the selected HOME, and candidate/unknown states fail closed. `getInstalledSystemLaunchers` does not offer the current or ambiguous HOME for disabling. Existing 341 TV profiles were not rewritten.
- `LocalAdb` role-holder removal and `PowerService` boot guard now use dynamically detected system HOME candidates instead of static stock launcher names.
- Android manifest declares HOME package visibility. Setup copy no longer hard-codes Philips/TCL/Google TV claims.
- Added policy tests for single/multiple/system/user/testOS/unknown/empty/ambiguous HOME, PackageManager failure, API 24/28, RoleManager API 29+, and limited visibility; added a static wiring contract for Linux/Windows CI.
- CI runs #341–#356 include intermediate failures while the adapter and wiring were being corrected. Latest run #357 for head `877b74597e46a7b355485458274ac93492902e8e` was still running at this documentation snapshot. Do not mark TOS-012 complete until the latest Linux and Windows runs pass.
- No PR #18 created yet at this snapshot; no merge, APK install, OTA or TV state change. `main` remains `a765318b577e66fae66a8ffe28439dcd6233f38f`.


## TOS-012 — финальный статус после CI и PR (2026-11-10)

- Draft PR #18 создан: https://github.com/gonscharovd/tvOS/pull/18.
- Base: `feat/device-profile-foundation-2026-11` / PR #17 head `a7fa0057b22c3125c087f9a78d60f7d7837def1c`. Head PR #18: `fc6d18cf6eb18805d2b42c6e4f88f8f678305324`.
- GitHub Actions run #363 (`42838478394`) на этом head завершился `success` для Linux и Windows. Включая HOME policy tests, Android wiring contract, localization tests, APK builds, package/version verification и artifact uploads.
- TOS-012 implementation и CI завершены; PR #18 ожидает review. Это не подтверждает runtime поведение на реальных Android TV устройствах.
- PR #14–#18 не слиты; `main` остаётся `a765318b577e66fae66a8ffe28439dcd6233f38f`; OTA не публиковалась; APK не устанавливался; телевизор и пользовательские данные не менялись.
