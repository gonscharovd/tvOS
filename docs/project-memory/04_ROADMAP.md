# Roadmap testOS

Дорожная карта описывает последовательность работ, а не обещание выпуска конкретной версии. Приоритеты продукта подтверждает Product Owner.

## Долгосрочное направление

Развивать универсальный Android TV launcher API 24+; Sber SDX-50U4123B — первая тестовая платформа. Ядро отделено от Android compatibility, OEM profiles и optional integrations.

## Принятый ближайший этап — завершение PR #14

1. Провести review изменений core cleanup и документационного PR.
2. Синхронизировать пользовательскую документацию с фактическим кодом PR — **выполнено в Draft PR #15, ожидает review**.
3. Устранить расхождения build version defaults и тестов контракта сборки (TOS-002) — реализация и Linux/Windows CI validation завершены в PR #14; ожидается review.
4. TOS-009: удалён destructive first-launch cleanup; профильный registry сохраняется, backup rollback projection добавлена; полный CI #330 success, ожидает review.
5. Выполнить runtime regression на Sber TV (TOS-006) только после read-only inventory и certificate gate.
5. После review и одобрения Product Owner решить вопрос о merge. Не выполнять merge автоматически.

## Следующий технический этап

- Удаление hard-coded локального пути JDK из build.ps1 (TOS-004) — реализовано и проверено Linux/Windows CI #317; ожидает review PR #14.
- Подключение pure-Java SingleUserMigrationTest к Linux/Windows CI (TOS-008) — реализовано, полный CI #322 success; ожидает review PR #14.
- TOS-009 — implementation внесена в PR #14; полный CI #330 success, ожидает review. TOS-006 остаётся заблокированной до зелёного CI, read-only inventory и certificate gate.
- Проверка install/launcher component command по итоговому APK manifest.
- Проверка package ID, version metadata и release certificate перед OTA.
- Обновление README, docs/features, docs/architecture, docs/install и dependency map.

В dependency map указан target следующего development build: testOS 2.1.74 / versionCode 89. Это плановая цель ветки, не опубликованный релиз и не подтверждённая текущая версия.

## Проверка на целевом устройстве

**Gate:** не устанавливать PR #14 до зелёного CI/review TOS-009, read-only inventory и доказанного совпадения release certificate. CI preview debug APK запрещён для in-place update.

После успешного CI и до релиза выполнить тест-план на Sber SDX-50U4123B:
- HOME, D-pad, Back, More Apps, reorder/hide/launch.
- Search и голосовой ввод.
- Control Panel и Recent Apps поверх HOME и стороннего приложения.
- Weather, VPN shortcut, DND, Button Mapper, Keep Alive.
- Night Scenes gallery, custom wallpapers, folder shuffle, lock-screen gallery.
- Settings backup/restore и single-user migration.
- CleanYourTV safety/restore.
- OTA update с release-signed APK и сохранение пользовательских настроек.

Снимать logcat, dumpsys gfxinfo и meminfo для ошибок/производительности; не считать субъективную плавность заменой измерения.

## После проверки

- Merge PR — только после review/разрешения владельца продукта.
- Release — отдельная ветка `release/2.1.PATCH`, CI release workflow и подтверждение корректной подписи.
- Не публиковать OTA автоматически после изменения кода или успешного PR CI.

## Предложения, не являющиеся утверждёнными функциями

Новые UI/системные функции, смена дизайна, изменение поведения CleanYourTV, firmware-level tweaks и новые внешние сервисы требуют отдельного решения Product Owner. Не добавлять их в roadmap как обязательства без согласования.



## Universal Android TV portability roadmap (approved 2026-11-10)

1. **TOS-010 — Universal Platform Foundation:** remove OEM dependence from build asset optimization. PR #16, run #338 Linux/Windows success; REVIEW.
2. **TOS-011 — Device Profile Architecture:** immutable device identity + Build/PackageManager resolver + fail-closed unknown/conflicting signals. PR #17; CI #339/#340 pending at last check.
3. **TOS-012 — Dynamic HOME Detection:** use PackageManager HOME resolution and actual candidates; remove OEM guesses from generic behavior.
4. **TOS-013 — CleanYourTV Safety:** protect current/system HOME and critical Android/TV services dynamically; unknown OEM safe behavior; audit but do not rewrite 341 profiles without evidence.
5. **TOS-014 — VPN Provider Architecture:** common provider contract, Happ optional adapter; no VPN client must not break launcher.
6. **TOS-015 — Compatibility Matrix:** API 24+ × OEM × feature availability, with verified/code-reviewed/unknown confidence.
7. **TOS-016 — Regression Testing:** unknown OEM, stock Android TV, OEM shell, missing Happ/VPN/services, nonstandard HOME and restricted APIs. Build success alone is not proof.


## TOS-012 execution status — 2026-11-10

- Implementation is in `feat/dynamic-home-detection-2026-11`, based on PR #17. The approach uses explicit PackageManager preferred-activity evidence and public RoleManager self-role check, not `resolveActivity` best-match priority.
- CleanYourTV current-HOME protection and fail-safe handling of ambiguous/unknown candidate roles are wired. LocalAdb and PowerService no longer rely on the static stock HOME package list.
- Pure-Java policy tests and static integration contract are connected to Linux/Windows CI. Latest run #357 was pending when this note was written; TOS-012 remains IN PROGRESS until final CI result and Draft PR review.
- Next after TOS-012 review: TOS-013 full CleanYourTV safety audit; do not rewrite the 341-profile database without evidence.


## TOS-012 — результат (2026-11-10)

- Реализация внесена в Draft PR #18, основанный на PR #17. Head `fc6d18cf6eb18805d2b42c6e4f88f8f678305324`.
- GitHub Actions run #363: Linux и Windows jobs завершились success, включая policy tests, Android wiring, локализацию, APK build и package/version verification.
- Статус: REVIEW — реализация и CI готовы; PR не вливался. Runtime validation на телевизорах не выполнена.
- Следующий этап после review/merge в dependency order — TOS-013 CleanYourTV Safety; аудит 341 профиля, не переписывать их без подтверждённых причин.
