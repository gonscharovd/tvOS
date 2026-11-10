# Память проекта testOS — начни отсюда

**Репозиторий:** [gonscharovd/tvOS](https://github.com/gonscharovd/tvOS)  
**Дата первичной фиксации:** 2026-11-09  
**Язык документации:** русский  
**Источник истины:** Git-дерево, код, CI и история этого репозитория.

## Назначение

Этот каталог — переносимая внешняя память проекта. Он предназначен для любого ИИ и разработчика, который впервые подключается к репозиторию. История чатов не является обязательным источником контекста.

## Что это за проект

testOS — универсальный русскоязычный Android TV launcher (API 24+) в форке `gonscharovd/tvOS`. Sber SDX-50U4123B — первая тестовая платформа, не основа архитектуры. Целевые OEM: Sber, Xiaomi, TCL, Philips, Sony, Hisense и другие Android TV / Google TV устройства.

## Порядок изучения

Прочитай документы в следующем порядке:

1. [01_PROJECT.md](01_PROJECT.md) — назначение, границы и критерии успеха.
2. [03_CURRENT_STATE.md](03_CURRENT_STATE.md) — фактическое состояние веток, PR, релиза и CI.
3. [10_SESSION_HANDOFF.md](10_SESSION_HANDOFF.md) — точка продолжения и единственный следующий шаг.
4. [05_TASKS.md](05_TASKS.md) и [04_ROADMAP.md](04_ROADMAP.md) — задачи и согласованное направление.
5. [02_ARCHITECTURE.md](02_ARCHITECTURE.md) — архитектура и карта кода.
6. [06_DECISIONS.md](06_DECISIONS.md), [07_CHANGELOG.md](07_CHANGELOG.md), [08_TECHNICAL_DEBT.md](08_TECHNICAL_DEBT.md) — причины решений, история и проблемы.
7. [09_AI_WORKING_RULES.md](09_AI_WORKING_RULES.md) — обязательные правила работы.

## Состав каталога

| Файл | Назначение |
|---|---|
| 00_START_HERE.md | Входная инструкция |
| 01_PROJECT.md | Паспорт проекта |
| 02_ARCHITECTURE.md | Архитектура и зависимости |
| 03_CURRENT_STATE.md | Актуальное состояние |
| 04_ROADMAP.md | Этапы развития |
| 05_TASKS.md | Единый список задач |
| 06_DECISIONS.md | Журнал решений |
| 07_CHANGELOG.md | Значимые изменения |
| 08_TECHNICAL_DEBT.md | Проблемы и риски |
| 09_AI_WORKING_RULES.md | Правила работы ИИ |
| 10_SESSION_HANDOFF.md | Передача контекста |

## Инструкция новому ИИ

Ты подключаешься к существующему проекту, а не начинаешь его заново.

1. Открой эту страницу и прочитай документы по порядку выше.
2. Проверь GitHub: default branch, нужную рабочую ветку, открытые PR, последние коммиты, проверки CI и актуальные релизы. Сведения в документах могли устареть после последнего обновления.
3. Сверь ключевые утверждения с исходниками и конфигурацией. При конфликте приоритет у актуального Git/code/CI, затем у более свежего подтверждённого решения.
4. Разделяй подтверждённые факты, пользовательские сообщения, предположения и неизвестное. Не превращай отсутствие данных в отрицательный факт.
5. Не повторяй уже завершённые проверки без причины. Не изменяй исходный код, если задача — только аудит или документация.
6. Выполняй безопасные изменения самостоятельно в отдельной ветке. Не сливай PR, не публикуй OTA и не меняй production/устройство без соответствующего разрешения.
7. После логического этапа обновляй только затронутые документы. Перед передачей работы обнови CURRENT_STATE, TASKS и SESSION_HANDOFF.
8. В SESSION_HANDOFF всегда оставляй один конкретный NEXT ACTION.

## Правила достоверности

- Факт — подтверждён исходником, GitHub API/CI, тестом или прямым сообщением пользователя; указывай источник и дату, когда это важно.
- Предположение — помечай явно и не используй как основу необратимых действий.
- Неизвестно — так и пиши; не заполняй пробелы догадками.
- Успешная сборка не доказывает работу на телевизоре. Успешный CI не доказывает OTA-установку.
- Успешная публикация релиза не доказывает, что конкретный телевизор уже обновился.

## Быстрые ссылки

- [Репозиторий](https://github.com/gonscharovd/tvOS)
- [Основная ветка](https://github.com/gonscharovd/tvOS/tree/main)
- [Рабочая ветка PR #14](https://github.com/gonscharovd/tvOS/tree/refactor/testos-core-cleanup-2026-11)
- [PR #14 — core cleanup](https://github.com/gonscharovd/tvOS/pull/14)
- [PR #15 — project memory and documentation sync](https://github.com/gonscharovd/tvOS/pull/15)
- [PR #19 — TOS-013 CleanYourTV safety policy](https://github.com/gonscharovd/tvOS/pull/19)
- [TOS-013 safety audit report](https://github.com/gonscharovd/tvOS/blob/feat/package-safety-policy-2026-11/docs/cleanyourtv-safety-audit.md)
- [Последний известный релиз v2.1.73](https://github.com/gonscharovd/tvOS/releases/tag/v2.1.73)
- [Последняя успешная проверка ветки на момент фиксации: run #322](https://github.com/gonscharovd/tvOS/actions/runs/42607667504)



## Универсальная цель (2026-11-10)

testOS — универсальный Android TV launcher для API 24+. Sber SDX-50U4123B — первая тестовая платформа, не основа архитектуры. Целевые OEM: Sber, Xiaomi/Mi TV, TCL, Philips, Sony, Hisense и другие Android TV / Google TV. Архитектурные уровни: testOS Core, Android TV Compatibility Layer, OEM Device Profiles, Optional Integrations.
