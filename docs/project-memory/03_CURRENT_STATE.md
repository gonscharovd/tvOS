# Текущее состояние проекта

**Дата среза:** 2026-11-09  
**Репозиторий:** `gonscharovd/tvOS`  
**Default branch:** `main`

## Git и релиз

| Объект | Подтверждённое состояние |
|---|---|
| main | `a765318b577e66fae66a8ffe28439dcd6233f38f` |
| Активная рабочая ветка | `refactor/testos-core-cleanup-2026-11` |
| Последний commit рабочей ветки | `c08bc91e06b7fa66f6bd185e3ab5633033884c33` |
| Pull Request | [#14](https://github.com/gonscharovd/tvOS/pull/14), Open, Draft |
| PR base | `main` at `a765318b577e66fae66a8ffe28439dcd6233f38f` |
| PR head | `c08bc91e06b7fa66f6bd185e3ab5633033884c33` |
| PR diff | 20 файлов, 775 additions, 4604 deletions |
| Последний опубликованный стабильный релиз | [v2.1.73 / testOS 2.1.73](https://github.com/gonscharovd/tvOS/releases/tag/v2.1.73) |
| Release APK | `tvos-home.apk`, 6,192,577 bytes |
| Package ID релиза | `com.gonscharovd.tvoslauncher` |
| versionCode релиза | 88 |

## Состояние PR #14

Цель PR — упростить core testOS и удалить устаревшие функции.

В текущем head реализовано:
- Night Scenes — единственный активный home root.
- Удалены старые Classic grid, Classic wallpaper UI, Hero/Continue Watching UI и связанные dead code paths из MainActivity.
- Удалена устаревшая multi-profile модель; введены SingleUserPrefs/SingleUserMigration для безопасного перехода к одному пользователю.
- Legacy пользовательские wallpapers/folder metadata сохраняются и мигрируют в Night Scenes без перезаписи уже существующих данных.
- Удалены старые feed/cloud integration paths и ненужные UI/settings entries, ранее перечисленные в dependency map.
- Сохранены app launch, dock, More Apps, Search, Weather, Control Panel, Recent Apps, Button Mapper, CleanYourTV, OTA, shared artwork/backdrop и другие активные функции.
- Добавлены/обновлены регрессионные проверки, в том числе SingleUserMigrationTest и проверки отсутствия Classic/Continue Watching/profile references.

PR остаётся Draft и не merged. Код PR не считается частью main и не опубликован как OTA.

## CI и сборка

- Последняя проверка PR head: [run #293](https://github.com/gonscharovd/tvOS/actions/runs/42517077387), run ID `42517077387`, commit `c08bc91...`, conclusion `success`.
- Ранее run #284 завершился failure на более раннем промежуточном commit; ошибка была исправлена последующими коммитами. Последующая проверка #293 успешна.
- CI выполняет Python regression tests, Java tests, сборку APK, apksigner verification и artifact upload.
- Последний CI preview использует debug signing key runner. Это не подтверждение совпадения сертификата с установленным на ТВ release APK.
- Сборка на пользовательском ПК и runtime-проверка последнего PR APK на телевизоре в этой сессии не выполнялись.

## Реализовано в стабильном main / последнем релизе

- Русскоязычная однопользовательская конфигурация testOS.
- Night Scenes и основные возможности launcher.
- CleanYourTV с защитой системных пакетов и проверкой состояния после disable/restore.
- OTA updater, VersionComparator, settings backup/restore.
- Control Panel, Recent Apps, Button Mapper, Weather, Search и другие описанные в [README](../../README.md) функции.
- Последний релиз — v2.1.73, опубликован 2026-11-08.

## Неподтверждённое / неизвестное

- Точная версия и сертификат APK, установленного на телевизоре после последнего OTA-обновления, не инспектировались в этой сессии. Пользователь ранее подтвердил работу OTA и установленный testOS, но нельзя автоматически приравнивать установленную версию к latest release.
- Runtime поведение PR #14 на Sber SDX-50U4123B после удаления Classic UI не проверено.
- Реальный результат полного тест-плана на ТВ неизвестен.
- Текущий доступ ADB к ТВ не проверялся.
- Точное состояние пользовательских разрешений/пакетов на ТВ после последнего OTA не проверялось.
- Совместимость Presentation-модуля с конкретной прошивкой не проверялась в этой сессии.

## Известные ограничения

- Для in-place APK update нужен тот же сертификат подписи.
- CI preview artifact с временным debug key нельзя считать безопасной заменой release APK.
- Сборочные defaults build.sh и build.ps1 расходятся; README заявляет единые defaults, что не соответствует фактическим скриптам.
- README/features/architecture содержат устаревшие упоминания Continue Watching/старых UI.
- PR #14 ещё не влит; main пока не содержит все его изменения.
