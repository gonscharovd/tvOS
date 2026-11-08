# Правила работы ИИ над testOS

Эти правила универсальны для ChatGPT, Claude, других ИИ и разработчиков.

## 1. Перед началом задачи

1. Прочитать [00_START_HERE.md](00_START_HERE.md), [03_CURRENT_STATE.md](03_CURRENT_STATE.md), [10_SESSION_HANDOFF.md] и относящиеся к задаче документы.
2. Проверить актуальную ветку, commit, PR, CI и release в GitHub.
3. Открыть реальные исходники/конфигурацию, прежде чем предлагать изменения.
4. Сверить документацию с кодом. Не повторять завершённые проверки.
5. Определить область задачи и риски. Кратко сообщить план и продолжать безопасные шаги автономно.

## 2. Архитектурные ограничения

- Android application / HOME launcher, не firmware.
- Java; Java 11 language level; min API 24, target API 34.
- JDK 17+, Android SDK platform 34, build-tools 34.0.0.
- Сборка без Gradle: build.sh и build.ps1.
- Не добавлять runtime libraries без доказанной необходимости и решения PO.
- Views создаются программно; не вводить XML layouts без архитектурного обоснования.
- UI Russian-only; один пользователь. Английская раскладка поиска остаётся.
- Night Scenes — единственный активный Home root в PR #14.
- Не возвращать Classic UI, multi-user profiles, news/recommendation feeds, Nuvio/Stremio cloud catalogue или notification-listener media service без отдельного решения PO.
- Сохранять package ID `com.gonscharovd.tvoslauncher`, Java namespace `com.techgenius.tvoslauncher`, release signing compatibility и OTA contract.

## 3. Изменение кода

- Сначала изучи все callers, lifecycle, persistence и manifest references затрагиваемого компонента.
- Предпочитай локальные изменения, а не переписывание модулей.
- Не удаляй ресурсы/assets/preferences, пока не доказано отсутствие активных и legacy references.
- При удалении функции проверь manifest permissions, intent filters, receivers, services, strings, settings routes, caches, assets и tests.
- Миграции должны быть идемпотентными и сохранять legacy данные, если нет явного разрешения на destructive cleanup.
- Не меняй package ID, signing key, release workflow или OTA архитектуру без отдельного согласования.
- Не коммить debug/release keystore, пароли, tokens, signing materials, device identifiers или личные данные.

## 4. Стиль и именование

- Следуй существующему стилю файла.
- Java source — ASCII-only; русские строки кодируй через Unicode escapes там, где это правило действует.
- Размеры UI задавай через dp/Ui.px, не raw pixels.
- Новые строки интерфейса добавляй в I18n.java и проверяй localization tests.
- Сохраняй короткие комментарии о причинах/ограничениях; не оставляй закомментированный код.

## 5. Тестирование

- Запускай соответствующие Python/Java regression tests и полный CI workflow для значимых изменений.
- Проверяй build, APK package/version/manifest и signature.
- Для UI: D-pad up/down/left/right, OK, Back, Home, focus, clipping и overlay dismissal.
- Для lifecycle/permissions/network/storage: проверяй на Android 9 / API 28, если возможно.
- Для performance: измеряй gfxinfo/meminfo на целевом ТВ.
- CI success не заменяет runtime test.
- Указывай, какие проверки реально запускались; не приписывай себе локальную сборку, если её не было.

## 6. Git и PR

- Работай в отдельной feature/fix/docs ветке.
- Перед созданием ветки проверь наличие имени и текущее состояние репозитория.
- Не перезаписывай чужие изменения и не force-push без согласования.
- Делай узкие коммиты с понятными сообщениями.
- Открывай PR в нужную base branch; указывай scope, проверки, ограничения и runtime status.
- Не сливай PR без явного разрешения Product Owner.
- Не публикуй OTA/release без явного разрешения Product Owner.

## 7. Безопасность устройства и CleanYourTV

- Root не является предпосылкой; не предлагай обход ограничений.
- Не отключай Device Owner, штатный Sber launcher, SystemUI, TV Settings, TvBoot, HDMI/input, package installer и критические системные пакеты.
- Не запускай массовые disable/uninstall команды без точного перечня, обоснования, backup и согласования.
- Не изменяй /system, /vendor, eMMC или bootloader.
- Проверяй PackageManager state после каждой операции; успешная ADB-сессия не означает успешную команду.
- Общий settings backup не должен включать ADB/security state, disabled-package backup или credentials.

## 8. Конфигурация и секреты

- Читай только имена переменных/точки использования. Не выводи значения секретов.
- GitHub Secrets для release signing используются в workflow; не копируй и не запрашивай их в чате.
- Не помещай секреты, пароли, ключи и private signing files в docs/project-memory.
- Не логируй полный environment dump.

## 9. Документация и передача

После логической задачи обнови только релевантные файлы:
- состояние — CURRENT_STATE;
- задачи — TASKS;
- архитектура/решение — ARCHITECTURE и DECISIONS;
- значимое изменение — CHANGELOG;
- проблемы — TECHNICAL_DEBT;
- планы — ROADMAP;
- завершение сессии — SESSION_HANDOFF.

Перед завершением сессии проверь ссылки, согласованность статусов и один NEXT ACTION. Документация должна отражать уже выполненное, а не желаемое.
