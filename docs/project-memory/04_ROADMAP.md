# Roadmap testOS

Дорожная карта описывает последовательность работ, а не обещание выпуска конкретной версии. Приоритеты продукта подтверждает Product Owner.

## Долгосрочное направление

Поддерживать компактный, стабильный и удобный русскоязычный Android TV launcher для Sber SDX-50U4123B, сохраняя общую совместимость Android TV API 24+. Развивать только нужные функции, не возвращать удалённые облачные/мультиязычные/многопользовательские подсистемы без отдельного решения.

## Принятый ближайший этап — завершение PR #14

1. Провести review изменений core cleanup и документационного PR.
2. Синхронизировать пользовательскую документацию с фактическим кодом PR — **выполнено в Draft PR #15, ожидает review**.
3. Устранить расхождения build version defaults и тестов контракта сборки (TOS-002) — реализация и Linux/Windows CI validation завершены в PR #14; ожидается review.
4. Выполнить доступные регрессионные тесты на Sber TV (TOS-006).
5. После review и одобрения Product Owner решить вопрос о merge. Не выполнять merge автоматически.

## Следующий технический этап

- Удаление hard-coded локального пути JDK из build.ps1 (TOS-004) — реализовано и проверено Linux/Windows CI #317; ожидает review PR #14.
- Подключение pure-Java SingleUserMigrationTest к Linux/Windows CI (TOS-008) — реализовано, полный CI #321 success; ожидает review PR #14.
- Проверка install/launcher component command по итоговому APK manifest.
- Проверка package ID, version metadata и release certificate перед OTA.
- Обновление README, docs/features, docs/architecture, docs/install и dependency map.

В dependency map указан target следующего development build: testOS 2.1.74 / versionCode 89. Это плановая цель ветки, не опубликованный релиз и не подтверждённая текущая версия.

## Проверка на целевом устройстве

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
