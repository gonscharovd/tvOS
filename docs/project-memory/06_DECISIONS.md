# Журнал решений testOS

Записи ниже восстановлены по текущему коду, PR и документации репозитория. Даты отражают подтверждённый период принятия/реализации, а не обязательно точное время отдельного обсуждения.

## DEC-001 — Android TV launcher, не прошивка
- **Дата:** 2026-11-04 — 2026-11-09
- **Решение:** проект развивается как Android application / HOME launcher; не как замена firmware/system image.
- **Причина:** фактическая реализация — APK с manifest, Android SDK build и PackageInstaller OTA.
- **Альтернативы:** модификация firmware/system partitions — вне текущего согласованного объёма.
- **Последствия:** не менять /system, /vendor, bootloader или eMMC без отдельного продуктового решения.
- **Статус:** принято.

## DEC-002 — Sber SDX-50U4123B как основной целевой девайс
- **Дата:** 2026-11-04
- **Решение:** приоритетная проверка на Sber SDX-50U4123B / Android 9 API 28 / ARMv7.
- **Причина:** это фактическое устройство пользователя.
- **Альтернативы:** только универсальная проверка Android TV emulator/других TV.
- **Последствия:** API 24+ compatibility не заменяет тестирование на Android 9 и реальном пульте.
- **Статус:** принято.

## DEC-003 — Русскоязычный интерфейс и один пользователь
- **Дата:** до 2026-11-08; подтверждено текущим README/кодом/PR #14
- **Решение:** UI приложения — Russian-only; активная модель — single-user. Английская раскладка клавиатуры поиска остаётся для ввода англоязычных запросов.
- **Причина:** упрощение персональной сборки и удаление неиспользуемых языковых/profile flows.
- **Альтернативы:** мультиязычный UI и multi-profile support.
- **Последствия:** не возвращать удалённые UI packs/profile flows без явного решения.
- **Статус:** принято.

## DEC-004 — Night Scenes как единственный Home root
- **Дата:** 2026-11-08, PR #14
- **Решение:** удалить Classic home UI и оставить Night Scenes единственным активным домашним интерфейсом.
- **Причина:** сокращение dead code и единый UX.
- **Альтернативы:** сохранять переключатель Classic/Night Scenes.
- **Последствия:** старые Classic wallpapers и пользовательские данные не удаляются; предусмотрена миграция в Night Scenes. Shared backdrop/artwork helpers сохраняются.
- **Статус:** принято в PR #14; ещё не влито в main на дату среза.

## DEC-005 — Удаление старого Continue Watching / Hero shelf
- **Дата:** 2026-11-08, PR #14
- **Решение:** удалить shelf, мёртвые hero/recommendation paths и связанные TV Provider permission requests.
- **Причина:** shelf не является активной частью выбранного продукта; старые feed/cloud integrations также удалены.
- **Альтернативы:** сохранять app-published TV Provider row.
- **Последствия:** не возвращать TV listings permission и старый media-session tracking без решения PO; shared app artwork/search helpers оставить.
- **Статус:** принято в PR #14; ещё не влито в main на дату среза.

## DEC-006 — Безопасная миграция preferences вместо удаления legacy данных
- **Дата:** 2026-11-08, PR #14
- **Решение:** SingleUserPrefs/SingleUserMigration копируют выбранный профиль в canonical home prefs и сохраняют legacy p1/preferences/metadata.
- **Причина:** переход к одному пользователю не должен уничтожать настройки.
- **Альтернативы:** удалять старые профили/файлы и переписывать все данные.
- **Последствия:** migration idempotent; legacy данные остаются для rollback. Уже удалённые старым кодом файлы восстановить нельзя.
- **Статус:** принято в PR #14; runtime validation требуется.

## DEC-007 — Сборка без Gradle
- **Дата:** историческое решение репозитория; подтверждено CONTRIBUTING и build scripts
- **Решение:** использовать Android SDK command-line tools и plain Java build scripts.
- **Причина:** компактная кодовая база и минимальные build dependencies.
- **Альтернативы:** Gradle/Android Studio build.
- **Последствия:** поддерживать оба скрипта build.sh/build.ps1 и их общий контракт.
- **Статус:** принято.

## DEC-008 — Release OTA через GitHub Releases и стабильную подпись
- **Дата:** до 2026-11-09
- **Решение:** updater получает APK из GitHub Releases; release workflow подписывает APK persistent key через GitHub Secrets.
- **Причина:** обновление установленного launcher без ручной переустановки.
- **Альтернативы:** сторонний OTA backend или случайные CI artifacts.
- **Последствия:** нельзя менять release key/package ID без миграционного плана; CI debug artifact не считать release-compatible.
- **Статус:** принято.

## DEC-009 — Единый источник build version metadata
- **Дата:** 2026-11-09, TOS-002
- **Решение:** хранить defaults development version и release numbering parameters в простом `version.properties`, который читают Bash, PowerShell и GitHub Actions.
- **Значения:** development default `2.1.74` / versionCode `89`; release code base `15`; prefix `2.1.`.
- **Приоритет:** переменные окружения `VERSION_CODE` и `VERSION_NAME` имеют приоритет над defaults для локальных/CI сборок; release workflow вычисляет versionCode как `15 + PATCH`.
- **Причина:** исключить drift между Linux/Windows scripts и CI, сохранив исторический OTA numbering contract.
- **Последствия:** при смене development defaults или release series изменять общий файл и соответствующие regression tests; не дублировать constants в build scripts/workflows.
- **Статус:** реализовано в PR #14; ожидается итоговая проверка Linux/Windows CI.
