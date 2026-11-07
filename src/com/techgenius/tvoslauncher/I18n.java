package com.techgenius.tvoslauncher;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Russian-only localization for this device-specific testOS build. */
public final class I18n {
    public static final String LANG_EN = "en", LANG_TR = "tr", LANG_SV = "sv", LANG_HI = "hi",
            LANG_ES = "es", LANG_FR = "fr", LANG_DE = "de", LANG_IT = "it", LANG_RU = "ru",
            LANG_PT = "pt", LANG_AR = "ar", LANG_JA = "ja", LANG_ZH = "zh", LANG_KO = "ko";
    public static final String[][] LANGUAGES = {{LANG_RU, "Русский", "Использовать testOS на русском"}};
    private static String cachedLang;
    private static final Map<String, Map<String, String>> LANG_MAPS = new HashMap<>();

    static {
        LANG_MAPS.put(LANG_RU, new HashMap<String, String>());
        add("Exact model match", "Точное совпадение модели");
        add("Exact model code match", "Точное совпадение кода модели");
        add("Similar model name", "Похожее название модели");
        add("Generic Android TV / Google TV profile", "Общий профиль Android TV / Google TV");
        add("TV Model Matched", "Модель телевизора определена");
        add("Change Model…", "Изменить модель…");
        add("Pull full app list from ADB and match models by system/OEM package names", "Получить полный список приложений через ADB и сопоставить модели по системным пакетам и пакетам производителя");
        add("📋 Browse All 341 TV Models", "📋 Просмотреть все 341 модели телевизоров");
        add("Scanning TV System Apps…", "Сканирование системных приложений телевизора…");
        add("Pulling full package list from ADB and comparing system/OEM package names across 341 TV models…", "Получаем полный список пакетов через ADB и сравниваем системные пакеты и пакеты производителей с 341 моделью телевизоров…");
        add("e.g. tcl, sony, hisense, xiaomi, philips, skyworth…", "например: TCL, Sony, Hisense, Xiaomi, Philips, Skyworth…");
        add("Search TV Model or Brand", "Поиск модели или бренда телевизора");
        add("Google Assistant / Voice Search (optional)", "Google Assistant / голосовой поиск (необязательно)");
        add("YouTube Music (optional)", "YouTube Music (необязательно)");
        add("Google Play Games (optional)", "Google Play Games (необязательно)");
        add("Android Accessibility Suite / TalkBack (optional)", "Android Accessibility Suite / TalkBack (необязательно)");
        add("Chromecast built-in / Google Cast receiver (optional — keep if you Cast from phone)", "Встроенный Chromecast / приёмник Google Cast (необязательно — оставьте, если транслируете с телефона)");
        add("Android TV Remote Service (optional — keep if you use Google TV phone remote)", "Android TV Remote Service (необязательно — оставьте, если используете пульт Google TV на телефоне)");
        add("System home launcher detected on this TV", "На этом телевизоре обнаружен системный главный экран (лаунчер)");
        add("None found", "Ничего не найдено");
        add("🛑 Critical system service — protected (cannot be disabled)", "🛑 Критическая системная служба — защищена (отключение запрещено)");
        add("⚠️ Core system package — disabling may affect system stability", "⚠️ Основной системный пакет — отключение может нарушить стабильность системы");
        add("Protected Core Package", "Защищённый системный пакет");
        add("Disabling Package…", "Отключение пакета…");
        add("Enabling Package…", "Включение пакета…");
        add("ADB Connection Required", "Требуется подключение ADB");
        add("Could not connect to local ADB on port 5555.\nPlease enable USB / Network Debugging in Developer Options and tap Pair / Repair ADB first.", "Не удалось подключиться к локальному ADB на порту 5555.\nВключите отладку по USB / сети в параметрах разработчика и сначала нажмите «Подключить / восстановить ADB».");
        add("Package State Not Verified", "Состояние пакета не подтверждено");
        add("No stock Android TV / Google TV launcher packages were detected on this TV.", "На этом телевизоре не обнаружены пакеты штатных лаунчеров Android TV / Google TV.");
        add("Disable System Launchers", "Отключить системные лаунчеры");
        add("Disable All Stock Launchers", "Отключить все штатные лаунчеры");
        add("Stock Google TV Home & banner ads", "Штатный главный экран Google TV и рекламные баннеры");
        add("Google TV setup wizard & launcher watchdog", "Мастер первоначальной настройки Google TV и служба контроля лаунчера");
        add("Stock Android TV Home & sponsored rows", "Штатный главный экран Android TV и рекламные блоки");
        add("Google telemetry & diagnostics", "Телеметрия и диагностика Google");
        add("Google TV store / Movies & TV", "Магазин Google TV / Фильмы и ТВ");
        add("Bloatware / background service", "Ненужное ПО / фоновая служба");
        add("Preparing CleanYourTV…", "Подготовка CleanYourTV…");
        add("⚠️ Detected: Your current Wi-Fi network IS owned by setupwraith (cname=com.google.android.tungsten.setupwraith).\n\n", "⚠️ Обнаружено: текущая сеть Wi-Fi принадлежит setupwraith (cname=com.google.android.tungsten.setupwraith).\n\n");
        add("ℹ️ Google TV Notice (setupwraith Wi-Fi check):\n\n", "ℹ️ Предупреждение Google TV (проверка Wi-Fi setupwraith):\n\n");
        add("No Matching Apps Found", "Подходящие приложения не найдены");
        add("Scan Installed Apps", "Сканировать установленные приложения");
        add("Debloating TV…", "Очистка телевизора…");
        add("Could not connect to local ADB on port 5555.\nPlease make sure USB / Network Debugging is enabled in Developer Options and tap Pair / Repair ADB first.", "Не удалось подключиться к локальному ADB на порту 5555.\nУбедитесь, что отладка по USB / сети включена в параметрах разработчика, и сначала нажмите «Подключить / восстановить ADB».");
        add("Debloat Failed", "Не удалось выполнить очистку");
        add("Debloat Partially Complete", "Очистка выполнена частично");
        add("Disabled Apps Record", "Список отключённых приложений");
        add("Restore Debloated Apps", "Восстановить отключённые приложения");
        add("Select TV Model First", "Сначала выберите модель телевизора");
        add("Choose TV Model…", "Выбрать модель телевизора…");
        add("Restore by Selected TV Model", "Восстановить по выбранной модели телевизора");
        add("Restoring Apps…", "Восстановление приложений…");
        add("Could not connect to local ADB on port 5555.", "Не удалось подключиться к локальному ADB на порту 5555.");
        add("Restore Partially Complete", "Восстановление выполнено частично");
        add("Custom 2", "Свой вариант 2");
        add("Custom 3", "Свой вариант 3");
        add("Custom 4", "Свой вариант 4");
        add("Custom 5", "Свой вариант 5");
        add("Folder Shuffle", "Смена изображений из папки");
        add("Custom Lock Image", "Своё изображение экрана блокировки");
        add("Tessellated Pavement", "Мозаичный берег");
        add("Grose Valley", "Долина Гроуз");
        add("Albury Storm", "Гроза в Олбери");
        add("Aresquiers Beach", "Пляж Арескьер");
        add("Vang Vieng Sunset", "Закат в Ванг-Вьенге");
        add("Aurora", "Северное сияние");
        add("Midnight", "Полночь");
        add("Ocean", "Океан");
        add("Sunset", "Закат");
        add("Graphite", "Графит");

        add("m", "мин");
        add("Continue watching", "Продолжить просмотр");
        add("Resume", "Продолжить");
        add("Remove from Keep Watching", "Убрать из «Продолжить просмотр»");
        add(" min", " мин");
        add("Control Center", "Панель управления");
        add("Weather Forecast", "Прогноз погоды");
        add("space", "пробел");
        add("clear", "Очистить");
        add("Say a movie, show or app", "Назовите фильм, сериал или приложение");
        add("Voice input isn't available on this TV", "Голосовой ввод недоступен на этом телевизоре");
        add("Voice needs the internet", "Для голосового поиска нужен интернет");
        add("Didn’t catch that — press the mic to try again", "Не удалось распознать — нажмите на микрофон, чтобы повторить");
        add("Listening…", "Слушаем…");
        add("Search apps, settings and inputs", "Поиск приложений, настроек и ТВ-входов");
        add("Apps & Settings", "Приложения и настройки");
        add("No results", "Ничего не найдено");
        add("Recent Searches", "Недавние запросы");
        add("Type to search apps, settings and inputs", "Введите название приложения, настройки или ТВ-входа");
        add("Display", "Экран");
        add("Screen Saver", "Заставка");
        add("Date & Time", "Дата и время");
        add("Accessibility", "Специальные возможности");
        add("Storage", "Хранилище");
        add("No movies or shows found", "Фильмы или сериалы не найдены");
        add("On ", "В ");
        add("", "");
        add("Series", "Сериал");
        add("Movie", "Фильм");
        add("HOW TO WATCH", "КАК СМОТРЕТЬ");
        add("Not streaming right now.", "Сейчас недоступно для просмотра.");
        add("Not installed", "Не установлено");
        add("Update download failed.", "Не удалось загрузить обновление.");
        add("Send Key Code…", "Отправить код клавиши…");
        add("Custom Key Code (Enter Number…)", "Свой код клавиши (введите число…)");
        add("Send Key Code", "Отправить код клавиши");
        add("e.g. 85 (Play/Pause), 164 (Mute), 178 (TV Input)", "например, 85 (воспроизведение/пауза), 164 (звук), 178 (вход ТВ)");
        add("Enter Android Key Code", "Введите код клавиши Android");
        add("min", "мин");
        add("Now", "Сейчас");
        add("Today", "Сегодня");
        add("Could not check for updates.", "Не удалось проверить наличие обновлений.");
        add("ADB is active — selecting Install will download and install the update automatically.", "ADB включён — при выборе «Установить» обновление будет загружено и установлено автоматически.");
        add("ADB is turned off (Secure Mode) — selecting Install will download the update and open the system package installer.", "ADB выключен (безопасный режим) — при выборе «Установить» обновление будет загружено, после чего откроется системный установщик пакетов.");
        add("Show Changelog", "Показать список изменений");
        add("Install", "Установить");
        add("No changelog text provided for this release.", "Для этого выпуска список изменений не предоставлен.");
        add("Could not start installer.", "Не удалось запустить установщик.");
        add("Update installed!", "Обновление установлено!");
        add("Mute / Unmute (164)", "Отключить / включить звук (164)");
        add("Volume Up (24)", "Увеличить громкость (24)");
        add("Volume Down (25)", "Уменьшить громкость (25)");
        add("Audio Track (222)", "Аудиодорожка (222)");
        add("Back (4)", "Назад (4)");
        add("Home (3)", "Домой (3)");
        add("App Switch / Recents (187)", "Переключение приложений / Недавние (187)");
        add("Notifications (83)", "Уведомления (83)");
        add("Sleep / Standby (223)", "Сон / ожидание (223)");
        add("Mode 2 (HY320 / Projector)", "Режим 2 (HY320 / проектор)");
        add("Mode 1 (Standard · Default)", "Режим 1 (стандартный · по умолчанию)");
        add("Red Button", "Красная кнопка");
        add("Green Button", "Зелёная кнопка");
        add("Yellow Button", "Жёлтая кнопка");
        add("Blue Button", "Синяя кнопка");
        add("Menu Button", "Кнопка меню");
        add("Guide Button", "Кнопка телепрограммы");
        add("Info Button", "Кнопка информации");
        add("Subtitles / CC", "Субтитры / скрытые субтитры");
        add("TV Input / Source", "Вход / источник сигнала ТВ");
        add("Channel Up", "Следующий канал");
        add("Channel Down", "Предыдущий канал");
        add("Bookmark / Favorite", "Закладка / избранное");
        add("Last Channel", "Предыдущий канал");
        add("Settings Button", "Кнопка настроек");
        add("Search Button", "Кнопка поиска");
        add("Play / Pause", "Воспроизведение / пауза");
        add("Play", "Воспроизвести");
        add("Pause", "Пауза");
        add("Stop", "Остановить");
        add("Rewind", "Перемотка назад");
        add("Fast Forward", "Перемотка вперёд");
        add("Next Track", "Следующая дорожка");
        add("Previous Track", "Предыдущая дорожка");
        add("Record Button", "Кнопка записи");
        add("Key: ", "Клавиша: ");
        add("Not Connected", "Не подключено");
        add(" paired", " сопряжено");
        add("No apps to close", "Нет приложений для закрытия");
        add("TV Speakers", "Динамики телевизора");
        add(" min left", " мин осталось");
        add("Auto", "Авто");
        add("Dark", "Тёмный");
        add("Black", "Чёрный");
        add("White", "Белый");
        add("Gray", "Серый");
        add("Red", "Красный");
        add("Orange", "Оранжевый");
        add("Yellow", "Жёлтый");
        add("Green", "Зелёный");
        add("Teal", "Бирюзовый");
        add("Blue", "Синий");
        add("Indigo", "Индиго");
        add("Purple", "Фиолетовый");
        add("Pink", "Розовый");
        add("App Icon Editor", "Редактор значков приложений");
        add("Preview", "Предпросмотр");
        add("Background color", "Цвет фона");
        add("Zoom", "Масштаб");
        add("Zoom out", "Уменьшить");
        add("Zoom in", "Увеличить");
        add("Move icon", "Переместить значок");
        add("Up", "Вверх");
        add("Down", "Вниз");
        add("Apply", "Применить");
        add("Not selected", "Не выбрано");
        add("images", "изображений");
        add("Open System File Picker…", "Открыть системный выбор файлов…");
        add("No system file picker available on this TV.", "На этом телевизоре нет системного выбора файлов.");
        add("Grant All Files Access…", "Предоставить доступ ко всем файлам…");
        add("Back to folders", "Назад к папкам");
        add("Could not decode image file.", "Не удалось прочитать файл изображения.");
        add("No images in this folder (open a subfolder below)", "В этой папке нет изображений (откройте вложенную папку ниже)");
        add("Back to parent folder", "Назад к родительской папке");
        add("Settings Backed Up", "Резервная копия настроек создана");
        add("Backup Failed", "Не удалось создать резервную копию");
        add("Grant Storage Access…", "Предоставить доступ к хранилищу…");
        add("No settings backup (.json) found in this folder", "В этой папке не найдена резервная копия настроек (.json)");
        add("Choose Folder to Restore Settings", "Выберите папку для восстановления настроек");
        add("Settings Restored", "Настройки восстановлены");
        add("Restore Failed", "Не удалось восстановить настройки");
        add("Persistent ADB Mode (Keep port 5555 open)", "Постоянный режим ADB (оставить порт 5555 открытым)");
        add("Secure Mode — WRITE_SECURE_SETTINGS & Close ADB (Recommended)", "Безопасный режим — WRITE_SECURE_SETTINGS и закрытие ADB (рекомендуется)");
        add("Secure Mode Active (ADB Not Used · Self-Repair Ready)", "Безопасный режим активен (ADB не используется · самовосстановление готово)");
        add("Secure Mode (Needs One-Time Pair to Grant Permissions)", "Безопасный режим (требуется однократное подключение для выдачи разрешений)");
        add("Connected & Paired (Port 5555 Open)", "Подключено и сопряжено (порт 5555 открыт)");
        add("Port 5555 Open · Ready to Pair", "Порт 5555 открыт · готово к сопряжению");
        add("Paired · ADB Enabled", "Сопряжено · ADB включён");
        add("ADB Enabled · Not Paired", "ADB включён · сопряжение не выполнено");
        add("Disconnected (ADB Off)", "Отключено (ADB выключен)");
        add("Make testOS your home screen?", "Сделать testOS главным экраном?");
        add("The Home button on the remote will open testOS.", "Кнопка «Домой» на пульте будет открывать testOS.");
        add("Make Home Screen", "Назначить главным экраном");
        add("Not Now", "Не сейчас");
        add("App icon reset to default", "Значок приложения сброшен.");
        add("App icon updated", "Значок приложения обновлён.");
        add("Show on Home Screen", "Показать на главном экране");
        add("Hide", "Скрыть");
        add("App Info", "О приложении");
        add("Reset Layout", "Сбросить расположение");
        add("testOS Settings", "Настройки testOS");
        add("% used  ·  Clean", "% использовано  ·  Очистить");
        add("This TV has no inputs", "У этого телевизора нет входов");
        add("Not connected", "Не подключено");
        add("Switch Profile", "Сменить профиль");
        add("Add User", "Добавить пользователя");
        add("New Profile", "Новый профиль");
        add("Change Emoji", "Изменить эмодзи");
        add("Change Colour", "Изменить цвет");
        add("Colour", "Цвет");
        add("Delete", "Удалить");
        add("Its layout and Continue Watching list will be removed.", "Его расположение приложений и список «Продолжить просмотр» будут удалены.");
        add("Weather from", "Погода от");
        add("Free forecast service, no account needed", "Бесплатный прогноз погоды, регистрация не требуется");
        add("LANGUAGE", "ЯЗЫК");
        add("DOCK", "ПАНЕЛЬ ПРИЛОЖЕНИЙ");
        add("Dock size", "Размер панели приложений");
        add("Show app names", "Показывать названия приложений");
        add(" seconds", " сек.");
        add("Reset app order & unhide all", "Сбросить порядок приложений и показать все");
        add("App layout reset to default.", "Расположение приложений сброшено.");
        add("Manage system apps in TV settings…", "Управление системными приложениями в настройках ТВ…");
        add("Tip: Hold OK on any app on the home screen dock to move or hide it.", "Совет: удерживайте OK на приложении в панели главного экрана, чтобы переместить или скрыть его.");
        add("Continue Watching on dock", "Раздел «Продолжить просмотр» на панели");
        add("Account", "Аккаунт");
        add("Signed in · Sign out", "Вход выполнен · Выйти");
        add("Not signed in", "Вход не выполнен");
        add("Watching now · OK to switch or rename", "Сейчас смотрит · OK — сменить или переименовать");
        add("OK to switch or edit", "Нажмите OK, чтобы переключить или изменить");
        add("Add a person…", "Добавить пользователя…");
        add("OPTIONS", "ПАРАМЕТРЫ");
        add("Ask who is watching on wake", "Спрашивать, кто смотрит, при включении ТВ");
        add("Each person keeps their own Keep Watching row and app order.", "У каждого пользователя свои раздел «Продолжить просмотр» и порядок приложений.");
        add("Rename person", "Переименовать пользователя");
        add("Button Mapper accessibility service", "Служба специальных возможностей Button Mapper");
        add("Active (works in all apps)", "Активна (работает во всех приложениях)");
        add("Accessibility Service", "Служба специальных возможностей");
        add("This TV does not have a built-in Accessibility Settings screen. You can enable the Button Mapper & Control Panel service automatically via Local ADB.", "На этом телевизоре нет встроенного экрана специальных возможностей. Службу Button Mapper и панели управления можно включить автоматически через локальный ADB.");
        add("Enable via ADB", "Включить через ADB");
        add("Fix Home button (Launch testOS)", "Исправление кнопки «Домой» (запуск testOS)");
        add("Short press Home opens testOS; long press opens TV Settings.", "Короткое нажатие «Домой» открывает testOS, долгое — настройки телевизора.");
        add("Home button is handled by the TV system.", "Кнопка «Домой» обрабатывается системой телевизора.");
        add("When the Home fix is enabled, a short press opens testOS and a long press opens the TV's own Settings. With the fix disabled, Home is handled by the TV system.", "Если исправление включено, короткое нажатие «Домой» открывает testOS, а долгое — настройки телевизора. Если исправление выключено, кнопкой управляет система ТВ.");
        add("Long-press volume fix enabled (for TVs where holding volume stops repeating).", "Исправление долгого нажатия громкости включено (для телевизоров, где удержание кнопки не повторяет нажатия).");
        add("Long-press volume fix disabled (using native TV volume).", "Исправление долгого нажатия громкости выключено (используется штатная регулировка ТВ).");
        add("Volume button fix mode", "Режим исправления кнопок громкости");
        add("BUTTON REMAPPING", "ПЕРЕНАЗНАЧЕНИЕ КНОПОК");
        add("REMAPPED BUTTONS (1X / 2X / 3X TAP)", "ПЕРЕНАЗНАЧЕННЫЕ КНОПКИ (1 / 2 / 3 НАЖАТИЯ)");
        add("Clear all remapped buttons", "Очистить все переназначения кнопок");
        add("All button mappings cleared.", "Все переназначения кнопок очищены.");
        add("Home screen weather shortcut", "Ярлык погоды на главном экране");
        add("Classic Top Shelf suggestions", "Рекомендации классической верхней полки");
        add("ADB & SECURITY MODE", "ADB И РЕЖИМ БЕЗОПАСНОСТИ");
        add("ADB operating mode", "Режим работы ADB");
        add("Turn On / Off ADB", "Включить / выключить ADB");
        add("Remove / revoke all permissions", "Удалить / отозвать все разрешения");
        add("Revoke ›", "Отозвать ›");
        add("STARTUP & KEEP ALIVE", "ЗАПУСК И KEEP ALIVE");
        add("Keep Alive (Auto-repair Accessibility)", "Keep Alive (автовосстановление специальных возможностей)");
        add("On (Default)", "Включено (по умолчанию)");
        add("Security Note: Keep Alive is enabled by default. In Secure Mode, testOS uses WRITE_SECURE_SETTINGS to restore Accessibility directly in-process without touching ADB (even if ADB is enabled on the TV). In Persistent ADB Mode, Keep Alive can also use Local ADB on port 5555.", "Безопасность: Keep Alive включён по умолчанию. В безопасном режиме testOS восстанавливает специальные возможности напрямую через WRITE_SECURE_SETTINGS, не используя ADB, даже если он включён на ТВ. В режиме постоянного ADB Keep Alive также может использовать локальный ADB на порту 5555.");
        add("Disable system launchers…", "Отключить системные лаунчеры…");
        add("Manual debloat (All system apps)…", "Ручное отключение (все системные приложения)…");
        add("Advanced ›", "Дополнительно ›");
        add("CleanYourTV (Beta) runs the CleanYourTV debloat profiles directly on your TV over Local ADB without needing a PC. Choose your TV model (or scan by installed system app packages), disable stock launchers or bloatware, and restore them anytime with one tap.", "CleanYourTV (бета) применяет профили очистки непосредственно на телевизоре через локальный ADB — компьютер не нужен. Выберите модель ТВ (или выполните поиск по установленным системным пакетам), отключайте штатные лаунчеры и ненужные приложения, а при необходимости восстанавливайте их одним нажатием.");
        add("BACKUP & RESTORE", "РЕЗЕРВНОЕ КОПИРОВАНИЕ И ВОССТАНОВЛЕНИЕ");
        add("Backup settings to Download", "Сохранить настройки в папку «Download»");
        add("Restore settings from folder…", "Восстановить настройки из папки…");
        add("Choose folder ›", "Выбрать папку ›");
        add("Automatic updates enabled.", "Автоматическое обновление включено.");
        add("Automatic updates disabled.", "Автоматическое обновление выключено.");
        add("Tap Check for updates", "Нажмите «Проверить обновления»");
        add("Checks GitHub Releases (techgenius1/tvOS-Launcher) for the latest APK and installs updates directly on your TV.", "Проверяет GitHub Releases (techgenius1/tvOS-Launcher), загружает последнюю версию APK и устанавливает обновление прямо на телевизор.");
        add("Downloading update", "Загрузка обновления");
        add("Update download failed", "Не удалось загрузить обновление");
        add("Download complete — opening installer", "Загрузка завершена — открываем установщик");
        add("Mode 1 — Standard (Default, for most Android TVs & IR remotes)", "Режим 1 — стандартный (по умолчанию для большинства Android TV и ИК-пультов)");
        add("Mode 2 — HY320 / Projector (AudioService & IR pulse-burst mode)", "Режим 2 — HY320 / проектор (AudioService и импульсный режим ИК)");
        add("Volume Button Fix Mode", "Режим исправления кнопок громкости");
        add("Volume Fix Mode 1 (Standard) selected.", "Выбран режим исправления громкости 1 (стандартный).");
        add("Volume Fix Mode 2 (HY320 / Projector) selected.", "Выбран режим исправления громкости 2 (HY320 / проектор).");
        add("Sign out", "Выйти");
        add("Secure Mode — Grant WRITE_SECURE_SETTINGS & Close ADB (Recommended)", "Безопасный режим — выдать WRITE_SECURE_SETTINGS и закрыть ADB (рекомендуется)");
        add("Persistent ADB Mode — Keep Network ADB Open (Optional)", "Режим постоянного ADB — оставить сетевой ADB включённым (необязательно)");
        add("ADB Operating Mode", "РЕЖИМ РАБОТЫ ADB");
        add("Enable ADB for Persistent Mode?", "Включить ADB для постоянного режима?");
        add("ADB debugging is currently turned off. Persistent ADB Mode requires ADB to be enabled.\n\nWould you like to enable ADB and switch to Persistent ADB Mode?", "Отладка ADB сейчас выключена. Для постоянного режима необходимо включить ADB.\n\nВключить ADB и перейти в постоянный режим?");
        add("Enable ADB & Switch", "Включить ADB и переключить режим");
        add("Turning On ADB…", "Включение ADB…");
        add("Enabling ADB debugging and switching to Persistent ADB Mode…", "Включаем отладку ADB и переходим в постоянный режим…");
        add("ADB enabled and switched to Persistent ADB Mode.", "ADB включён, выбран постоянный режим.");
        add("Turn On ADB Debugging", "Включить отладку ADB");
        add("Open Developer Options and turn on USB Debugging / Network Debugging to complete switching to Persistent ADB Mode.", "Откройте параметры разработчика и включите отладку по USB / сети, чтобы завершить переход в постоянный режим ADB.");
        add("Open Developer Options…", "Открыть параметры разработчика…");
        add("Secure Mode selected — ADB will grant WRITE_SECURE_SETTINGS and close port 5555 when paired.", "Выбран безопасный режим — после сопряжения ADB выдаст WRITE_SECURE_SETTINGS и закроет порт 5555.");
        add("Persistent ADB Mode selected — Port 5555 stays open for CleanYourTV & ADB tools.", "Выбран постоянный режим ADB — порт 5555 останется открытым для CleanYourTV и инструментов ADB.");
        add("Enabling ADB debugging…", "Включение отладки ADB…");
        add("ADB debugging enabled.", "Отладка ADB включена.");
        add("Open Developer Options and turn on USB Debugging / Network Debugging.", "Откройте параметры разработчика и включите отладку по USB / сети.");
        add("Close ADB (Disable Port 5555)", "Закрыть ADB (отключить порт 5555)");
        add("Close ADB Now", "Закрыть ADB сейчас");
        add("Closing ADB…", "Закрытие ADB…");
        add("Running settings put global adb_enabled 0 and verifying port 5555…", "Выполняется settings put global adb_enabled 0 и проверяется порт 5555…");
        add("Developer Options…", "Параметры разработчика…");
        add("Keep Alive disabled.", "Keep Alive выключен.");
        add("Enable Keep Alive?", "Включить Keep Alive?");
        add("Enable Keep Alive", "Включить Keep Alive");
        add("Keep Alive enabled.", "Keep Alive включён.");
        add("Pair / Repair ADB", "Подключить / восстановить ADB");
        add("Connecting to local ADB on port 5555 and granting permissions…\n\nIf an “Allow USB debugging?” prompt appears, check “Always allow” and press Allow.", "Подключаемся к локальному ADB на порту 5555 и выдаём разрешения…\n\nЕсли появится запрос «Разрешить отладку по USB?», отметьте «Всегда разрешать» и нажмите «Разрешить».");
        add("ADB paired and all permissions granted!", "ADB подключён, все разрешения выданы!");
        add("ADB Debugging Needed", "Требуется отладка ADB");
        add("Could not connect to local ADB on port 5555.\n\n1. Open Settings → System → About and click Build 7 times.\n2. Open Developer Options and turn on USB Debugging / Network Debugging.\n3. Return here and tap Pair / Repair again.", "Не удалось подключиться к локальному ADB на порту 5555.\n\n1. Откройте «Настройки → Система → Об устройстве» и нажмите на номер сборки 7 раз.\n2. Откройте параметры разработчика и включите отладку по USB / сети.\n3. Вернитесь сюда и снова нажмите «Подключить / восстановить ADB».");
        add("Remove / Revoke All Permissions", "УДАЛИТЬ / ОТОЗВАТЬ ВСЕ РАЗРЕШЕНИЯ");
        add("This will disable Keep Alive and remove Accessibility Service, Notification Access, Default Home Role, and launcher permissions (both locally and via ADB).", "Это выключит Keep Alive и отзовёт службу специальных возможностей, доступ к уведомлениям, роль приложения «Домой» и разрешения лаунчера — локально и через ADB.");
        add("Revoke All Permissions", "Отозвать все разрешения");
        add("Revoking Permissions…", "Отзыв разрешений…");
        add("Revoking all permissions…", "Отзываем все разрешения…");
        add("⚠️ Manual Debloat Warning", "⚠️ Предупреждение о ручном отключении");
        add("I Understand — Open Manual Debloat", "Понятно — открыть ручное отключение");
        add("Back", "Назад");
        add("DOCK SCALE (-50% TO +50%)", "МАСШТАБ ПАНЕЛИ (-50% ДО +50%)");
        add("Default", "По умолчанию");
        add("Larger dock — extra apps automatically move to Other apps if needed", "Увеличенная панель — лишние приложения при необходимости перемещаются в «Другие приложения»");
        add("Smaller, more compact dock", "Уменьшенная, компактная панель");
        add("Standard Apple tvOS dock size", "Стандартный размер панели Apple tvOS");
        add("Ambient colour themes", "Цветовые темы фонового режима");
        add("AMBIENT THEMES", "ТЕМЫ ФОНОВОГО РЕЖИМА");
        add("FORECAST PROVIDER", "ИСТОЧНИК ПРОГНОЗА");
        add("Classic Top Shelf", "Классическая верхняя полка");
        add("TOP SHELF CONTENT", "СОДЕРЖИМОЕ ВЕРХНЕЙ ПОЛКИ");
        add("Remove person", "Удалить пользователя");
        add("Wi-Fi", "Wi-Fi");
        add("Bluetooth", "Bluetooth");
        add("Choose Action", "Выбрать действие");
        add("TARGET ACTION / APPLICATION", "ЦЕЛЕВОЕ ДЕЙСТВИЕ / ПРИЛОЖЕНИЕ");
        add("Default (Ambient Mode)", "По умолчанию (фоновый режим)");
        add("Default (Automatic)", "По умолчанию (автоматически)");
        add("Open TV Ambient Mode / Screensaver (or built-in fallback)", "Открыть фоновый режим / заставку телевизора (или встроенный резервный вариант)");
        add("Use built-in Android TV shortcut", "Использовать встроенную команду Android TV");
        add("Lock Screen", "Экран блокировки");
        add("Show Clock, Date & Greeting over your chosen wallpaper (press any key to return)", "Показывать часы, дату и приветствие поверх выбранных обоев (нажмите любую кнопку для возврата)");
        add("Choose Activity", "Выбрать действие");
        add("ACTIVITIES IN ", "ДЕЙСТВИЯ В ");
        add("Default App Launch (Main Activity)", "Запуск приложения по умолчанию (главная активность)");
        add("TAP ACTIONS", "ДЕЙСТВИЯ ПО НАЖАТИЮ");
        add("REMOVE", "УДАЛИТЬ");
        add("BUILT-IN ACTIONS", "ВСТРОЕННЫЕ ДЕЙСТВИЯ");
        add("None (No action)", "Нет (без действия)");
        add("Ambient Mode / StandBy", "Фоновый режим / ожидание");
        add("Power Off (Screen Off)", "Выключить экран");
        add("Send Key Code (Remote / Media / System)…", "Отправить код клавиши (пульт / мультимедиа / система)…");
        add("Send any Android KeyEvent keycode without 3rd-party apps", "Отправить любой код клавиши Android KeyEvent без сторонних приложений");
        add("INSTALLED APPS", "УСТАНОВЛЕННЫЕ ПРИЛОЖЕНИЯ");
        add("DEFAULT LAUNCHER SETUP (TCL / PHILIPS / GOOGLE TV)", "НАСТРОЙКА ЛАУНЧЕРА ПО УМОЛЧАНИЮ (TCL / PHILIPS / GOOGLE TV)");
        add("1. Set testOS as Default via Local ADB", "1. Назначить testOS лаунчером по умолчанию через локальный ADB");
        add("Run ROLE_HOME & set-home-activity ›", "Выполнить ROLE_HOME и set-home-activity ›");
        add("Setting Default Launcher…", "Назначение лаунчера по умолчанию…");
        add("Setting ROLE_HOME and default home activity over Local ADB…", "Назначение ROLE_HOME и главной активности через локальный ADB…");
        add("2. Disable System Launchers (Google TV / Android TV / OEM)", "2. Отключить системные лаунчеры (Google TV / Android TV / OEM)");
        add("3. Fix Home Button via Accessibility (Button Mapper)", "3. Исправить кнопку «Домой» через специальные возможности (Button Mapper)");
        add("On — Pressing Home launches testOS", "Включено — нажатие «Домой» запускает testOS");
        add("Off — Tap to enable Home button remap", "Выключено — нажмите, чтобы переназначить кнопку «Домой»");
        add("Home button fix enabled.", "Исправление кнопки «Домой» включено.");
        add("Home button fix disabled.", "Исправление кнопки «Домой» выключено.");
        add("4. Open Android System Home Settings", "4. Открыть системные настройки главного экрана Android");
        add("Open system selector (if supported by TV) ›", "Открыть системный выбор лаунчера (если поддерживается ТВ) ›");
        add("On TCL and Philips Android 11/12 TVs, the OEM firmware often hides the Android Default Home UI and forces the stock launcher when pressing Home. Disabling the stock launcher (Option 2) or enabling the Home Button Fix (Option 3) guarantees pressing Home always opens testOS.", "На телевизорах TCL и Philips с Android 11/12 прошивка производителя часто скрывает системный выбор главного экрана и запускает штатный лаунчер при нажатии «Домой». Отключение штатного лаунчера (вариант 2) или включение исправления кнопки (вариант 3) гарантирует запуск testOS.");
        add("PERMISSION STATUS", "СОСТОЯНИЕ РАЗРЕШЕНИЙ");
        add("Grant All Permissions via Local ADB", "Выдать все разрешения через локальный ADB");
        add("Grant All ›", "Выдать все ›");
        add("Default Home Screen", "Главный экран по умолчанию");
        add("Not set", "Не задано");
        add("Button Mapper & Control Panel (Accessibility)", "Button Mapper и панель управления (специальные возможности)");
        add("Disabled", "Отключено");
        add("Wi-Fi Name (Location)", "Имя сети Wi-Fi (местоположение)");
        add("Not allowed", "Не разрешено");
        add("ACTIONS", "ДЕЙСТВИЯ");
        add("🔍 Scan Installed System Apps via ADB", "🔍 Сканировать установленные системные приложения через ADB");
        add("Pull full app list from ADB and match models by OEM/system packages", "Получить полный список приложений через ADB и определить модель по пакетам производителя и системы");
        add("📋 Browse / Search All 341 TV Models", "📋 Просмотреть / найти среди 341 модели телевизоров");
        add("View or filter the complete list of supported TV models", "Просмотреть или отфильтровать полный список поддерживаемых моделей телевизоров");
        add("MATCHING MODELS FOR ", "ПОДХОДЯЩИЕ МОДЕЛИ ДЛЯ ");
        add("Models by System App Similarity", "Модели по совпадению системных приложений");
        add("Scanning installed system packages over ADB and comparing across 341 TV models…", "Сканируем установленные системные пакеты через ADB и сравниваем с 341 моделью телевизоров…");
        add("All 341 TV Models", "Все 341 модели телевизоров");
        add("🔍 Search / Filter by brand or model…", "🔍 Поиск / фильтр по бренду или модели…");
        add("All (341)", "Все (341)");
        add("Clear filter (Show all)", "Сбросить фильтр (показать все)");
        add("QUICK BRAND FILTERS", "БЫСТРЫЕ ФИЛЬТРЫ ПО БРЕНДУ");
        add("Filter ›", "Фильтр ›");
        add("Previous Page", "Предыдущая страница");
        add("Next Page", "Следующая страница");
        add("PAGE NAVIGATION", "НАВИГАЦИЯ ПО СТРАНИЦАМ");
        add("e.g. tcl, philips, sony, hisense, xiaomi…", "например: TCL, Philips, Sony, Hisense, Xiaomi…");
        add("Filter TV Models", "Фильтр моделей телевизоров");
        add("Filter", "Фильтр");
        add("Debloat TV — ", "Очистка ТВ — ");
        add("🔍 Scan Installed System Apps to Find Matching Model", "🔍 Сканировать системные приложения для определения модели");
        add("GOOGLE TV WI-FI NOTICE", "ПРЕДУПРЕЖДЕНИЕ О WI-FI В GOOGLE TV");
        add("⚠️ Your Wi-Fi network is currently owned by setupwraith. Before disabling setupwraith, open Wi-Fi Settings, Forget your Wi-Fi network, and re-connect so Wi-Fi stays saved.", "⚠️ Сейчас сетью Wi-Fi управляет setupwraith. Перед отключением setupwraith откройте настройки Wi-Fi, забудьте сеть и подключитесь к ней заново, чтобы сохранить подключение.");
        add("On Google TV, if you disable setupwraith, make sure your Wi-Fi network was re-added in Wi-Fi Settings.", "На Google TV перед отключением setupwraith убедитесь, что вы заново добавили свою сеть в настройках Wi-Fi.");
        add("📶 Open Wi-Fi Settings…", "📶 Открыть настройки Wi-Fi…");
        add("Run via ADB ›", "Запустить через ADB ›");
        add("Select at least one package first.", "Сначала выберите хотя бы один пакет.");
        add("Select All / Select None", "Выбрать все / снять выделение");
        add("Disable system launchers", "Отключить системные лаунчеры");
        add("No stock Android TV / Google TV / OEM launcher packages were detected on this TV.", "На этом телевизоре не обнаружены пакеты штатных лаунчеров Android TV / Google TV / производителя.");
        add("ONE-TAP ACTIONS", "ДЕЙСТВИЯ В ОДНО КАСАНИЕ");
        add("Disable All Stock Launchers & Set testOS as Home", "Отключить все штатные лаунчеры и назначить testOS главным");
        add("Disable all ›", "Отключить все ›");
        add("System Launchers", "Системные лаунчеры");
        add("Re-enable All Stock Launchers", "Повторно включить все штатные лаунчеры");
        add("Enable all ›", "Включить все ›");
        add(" [Enabled — Tap to Disable]", " [Включено — нажмите, чтобы отключить]");
        add(" [Disabled — Tap to Enable]", " [Отключено — нажмите, чтобы включить]");
        add("Checked items are currently DISABLED. Disabling the stock system launcher (e.g. Google TV Home / LauncherX + setupwraith, Android TV Home, or TCL/Philips OEM Home) forces Android to use testOS when pressing the Home key.", "Отмеченные пункты сейчас ОТКЛЮЧЕНЫ. Отключение штатного системного лаунчера (например, Google TV Home / LauncherX + setupwraith, Android TV Home или лаунчера TCL/Philips) заставляет Android запускать testOS при нажатии кнопки «Домой».");
        add("Manual debloat (All system apps)", "Ручное отключение (все системные приложения)");
        add("🔍 Filter system apps…", "🔍 Фильтр системных приложений…");
        add("Clear filter", "Сбросить фильтр");
        add("↻ Refresh system app list", "↻ Обновить список системных приложений");
        add(" [Critical System Service — Protected]", " [Критическая системная служба — защищена]");
        add("🛑 Critical System Service", "🛑 Критическая системная служба");
        add("OK", "ОК");
        add("🛑 Protected System Package", "🛑 Защищённый системный пакет");
        add("Disable System App?", "Отключить системное приложение?");
        add("Disable", "Отключить");
        add("App name or package name…", "Название приложения или имя пакета…");
        add("Filter System Apps", "Фильтр системных приложений");
        add("No disabled packages have been recorded by CleanYourTV yet.", "CleanYourTV пока не сохранил список отключённых пакетов.");
        add("Restore all ›", "Восстановить все ›");
        add("SAVED DISABLED PACKAGES (TAP TO RE-ENABLE)", "СОХРАНЁННЫЕ ОТКЛЮЧЁННЫЕ ПАКЕТЫ (НАЖМИТЕ ДЛЯ ВКЛЮЧЕНИЯ)");
        add("Restore debloated apps", "Восстановить отключённые приложения");
        add("RESTORE OPTIONS", "ПАРАМЕТРЫ ВОССТАНОВЛЕНИЯ");
        add("Re-enable all packages disabled by CleanYourTV over ADB", "Повторно включить через ADB все пакеты, отключённые CleanYourTV");
        add("No saved disabled-app backup was found, and no TV model is selected yet. Choose a TV model first if you want to restore by model profile.", "Резервная копия отключённых приложений не найдена, модель телевизора ещё не выбрана. Сначала выберите модель, если хотите восстановить приложения по профилю.");
        add("Choose TV model…", "Выбрать модель телевизора…");
        add("Choose custom scene image", "Выбрать собственное изображение сцены");
        add("Custom scene wallpaper updated.", "Обои сцены обновлены.");
        add("Could not load selected image.", "Не удалось загрузить выбранное изображение.");
        add("Choose custom background image", "Выбрать собственное фоновое изображение");
        add("Custom background updated.", "Фон обновлён.");
        add("Choose wallpaper folder to shuffle", "Выбрать папку обоев для смены");
        add("Folder shuffle", "Смена изображений из папки");
        add("Turn on Folder shuffle", "Включить смену изображений из папки");
        add("Choose folder…", "Выбрать папку…");
        add("Turn off Folder shuffle", "Выключить смену изображений из папки");
        add("Choose background folder to shuffle", "Выбрать папку фоновых изображений для смены");
        add("Use folder shuffle background", "Использовать фон со сменой изображений из папки");
        add("Lock screen settings", "Настройки экрана блокировки");
        add("LOCK SCREEN OPTIONS", "ПАРАМЕТРЫ ЭКРАНА БЛОКИРОВКИ");
        add("Auto-launch Lock Screen when idle", "Автоматически открывать экран блокировки при бездействии");
        add("Each wallpaper stays", "Длительность показа одних обоев");
        add("Preview Lock Screen now", "Предпросмотр экрана блокировки");
        add("NIGHT SCENES", "НОЧНЫЕ СЦЕНЫ");
        add("CLASSIC WALLPAPERS", "КЛАССИЧЕСКИЕ ОБОИ");
        add("CUSTOM & FOLDER SHUFFLE", "СВОИ ИЗОБРАЖЕНИЯ И СМЕНА ИЗ ПАПКИ");
        add("On the Lock Screen, only Clock, Date, and Greeting are shown above your selected backgrounds. Pressing any remote button returns to where you were before.", "На экране блокировки поверх выбранного фона отображаются только часы, дата и приветствие. Нажатие любой кнопки пульта возвращает на предыдущий экран.");
        add("One wallpaper has to stay on.", "Необходимо оставить хотя бы одни обои.");
        add("Choose Lock Screen image", "Выбрать изображение экрана блокировки");
        add("Custom Lock Screen image updated.", "Изображение экрана блокировки обновлено.");
        add("Custom image", "Своё изображение");
        add("Turn off Custom image", "Выключить своё изображение");
        add("Turn on Custom image", "Включить своё изображение");
        add("Choose Lock Screen folder to shuffle", "Выбрать папку изображений для экрана блокировки");
        add("City name, e.g. Istanbul", "Название города, например Челябинск");
        add("Choose City", "Выбрать город");
        add("Searching…", "Поиск…");
        add("Couldn't search. Check the internet connection.", "Не удалось выполнить поиск. Проверьте подключение к интернету.");
        add("Matching places", "Подходящие места");
        add("Changes the language used on the home screen, date, weather and settings.", "Изменяет язык главного экрана, даты, погоды и настроек.");
        add("CONTROL PANEL ACTIONS", "ДЕЙСТВИЯ ПАНЕЛИ УПРАВЛЕНИЯ");
        add("Reset control panel actions", "Сбросить действия панели управления");
        add("Reset control panel?", "Сбросить настройки панели управления?");
        add("All custom app and activity assignments will be cleared. The built-in actions will be restored.", "Все пользовательские назначения приложений и действий будут удалены. Будут восстановлены стандартные действия.");
        add("Control panel actions restored to defaults.", "Стандартные действия панели управления восстановлены.");
        add("Choose which app or activity opens from each Control Panel tile. Leave an action on Default to use the TV's built-in settings.", "Выберите приложение или действие, которое будет запускаться из каждой плитки панели управления. Оставьте значение «По умолчанию», чтобы использовать встроенные настройки телевизора.");
        add("HOME SCREEN", "ГЛАВНЫЙ ЭКРАН");
        add("PEOPLE & PROFILES", "ПОЛЬЗОВАТЕЛИ И ПРОФИЛИ");
        add("DIAGNOSTICS & RECOVERY", "ДИАГНОСТИКА И ВОССТАНОВЛЕНИЕ");
        add("Remote control", "Пульт управления");
        add("TMDB connected", "TMDB подключён");
        add("Information", "Информация");
        add("Main screen", "Главный экран");
        add("Appearance and clock", "Внешний вид и часы");
        add("Control panel", "Панель управления");
        add("Advanced settings", "Расширенные настройки");
        add("Information", "Информация");
        initTranslations();
        initRussianExtras();
    }

    private static void add(String en, String ru) { LANG_MAPS.get(LANG_RU).put(en, ru); }

    private static void initTranslations() {
        // Greetings
        add("Good morning", "Доброе утро");
        add("Good afternoon", "Добрый день");
        add("Good evening", "Добрый вечер");
        add("Good night", "Спокойной ночи");

        // Weather conditions
        add("Clear", "Ясно");
        add("Sunny", "Солнечно");
        add("Mostly Sunny", "Малооблачно");
        add("Mostly Clear", "Почти ясно");
        add("Partly Cloudy", "Переменная облачность");
        add("Cloudy", "Облачно");
        add("Foggy", "Туман");
        add("Drizzle", "Морось");
        add("Rain", "Дождь");
        add("Heavy Rain", "Сильный дождь");
        add("Sleet", "Мокрый снег");
        add("Snow", "Снег");
        add("Heavy Snow", "Сильный снег");
        add("Showers", "Ливень");
        add("Thunderstorms", "Гроза");

        // Sidebar & Main Sections
        add("Settings", "Настройки");
        add("Language", "Язык");
        add("Location", "Местоположение");
        add("Weather", "Погода");
        add("Weather and location", "Погода и местоположение");
        add("Look and scenes", "Вид и сцены");
        add("Appearance and scenes", "Вид и сцены");
        add("Apps", "Приложения");
        add("People", "Профили");
        add("Button Mapper", "Кнопки пульта");
        add("Button mapper", "Кнопки пульта");
        add("Quick settings", "Быстрые настройки");
        add("Quick buttons", "Быстрые настройки");
        add("Home & System", "Главный экран и система");
        add("Advanced", "Дополнительно");
        add("About", "О приложении");

        // Home screen & Scenes
        add("LOOK", "ВИД");
        add("Night scenes", "Ночные сцены");
        add("A big glass clock, scenes taking turns", "Большие стеклянные часы и сменяющиеся сцены");
        add("Classic", "Классический");
        add("One photo and a grid of app banners", "Одно фото и сетка баннеров приложений");
        add("SCENES", "СЦЕНЫ");
        add("Night", "Ночь");
        add("Golden hour", "Золотой час");
        add("Waterline", "Водная гладь");
        add("Fog", "Туман");
        add("Rainforest dawn", "Рассвет в тропиках");
        add("Each scene stays", "Длительность сцены");
        add("Clock", "Часы");
        add("Clock style", "Стиль часов");
        add("RAM", "ОЗУ");
        add("Launcher", "Лаунчер");
        add("TV", "ТВ");
        add("Disconnected", "Отключён");
        add("Back to return   ·   Hold OK for options", "Нажмите «Назад» для возврата · удерживайте OK для действий");
        add("Who’s Watching?", "Кто смотрит?");
        add("Delete profile", "Удалить профиль");
        add("Getting the weather…", "Загружаем погоду…");
        add("Continue Watching & Do Not Disturb", "Продолжить просмотр и режим «Не беспокоить»");
        add("Set Default via Local ADB (Recommended)", "Назначить по умолчанию через локальный ADB (рекомендуется)");
        add("Disable Stock TV Launchers (TCL / Philips / Google TV)", "Отключить штатные лаунчеры ТВ (TCL / Philips / Google TV)");
        add("Enable Home Button Fix (Accessibility Remap)", "Включить перехват кнопки «Домой» (спец. возможности)");
        add("Open Android Default Home Settings", "Открыть настройки главного экрана Android");
        add("Set Default Home Launcher", "Назначить лаунчер главным экраном");
        add("Default home launcher set via ADB!", "Лаунчер назначен главным экраном через ADB!");
        add("Home Button Fix & Accessibility enabled!", "Перехват кнопки «Домой» и специальные возможности включены!");
        add("Home Button Fix enabled!", "Перехват кнопки «Домой» включён!");
        add("Select Home App", "Выберите приложение главного экрана");
        add("This TV's settings have no switch for this permission. Whoever set up the TV can turn it on once from a computer on the same network:\n\n", "В настройках этого телевизора нет переключателя для данного разрешения. Его можно один раз включить с компьютера в той же сети:\n\n");
        add("Allow testOS to install unknown apps; installation will resume when you return.", "Разрешите testOS устанавливать неизвестные приложения — после возврата установка продолжится.");
        add("Could not open install permission settings.", "Не удалось открыть настройки разрешения на установку.");
        add("The downloaded update is no longer available. Please download it again.", "Скачанный файл обновления больше недоступен. Скачайте его повторно.");
        add("Could not install update.", "Не удалось установить обновление.");
        add("A new version of testOS is available on GitHub.", "На GitHub доступна новая версия testOS.");
        add("Current version:", "Текущая версия:");
        add("New version:", "Новая версия:");
        add("✕  Remove", "✕  Удалить");
        add("▶  Resume", "▶  Продолжить");
        add("Emoji", "Эмодзи");
        add("iPhone Duo", "iPhone Duo");
        add("iPhone Rounded", "iPhone Rounded");
        add("Tall Glass", "Высокий");
        add("Classic", "Классический");
        add("Serif", "С засечками");
        add("Digital", "Цифровой");
        add("Condensed", "Узкий");
        add("Thin", "Тонкий");
        add("Bold", "Жирный");
        add("iPhone Duo", "iPhone Duo");
        add("Glass", "Стекло");
        add("12 hour", "12 часов");
        add("24 hour", "24 часа");
        add("One scene has to stay on.", "Хотя бы одна сцена должна оставаться включенной.");
        add("Keep watching", "Продолжить просмотр");
        add("Continue Watching", "Продолжить просмотр");
        add("System playback history is not available in this build.", "История воспроизведения Android недоступна в этой сборке.");
        add("More apps", "Все приложения");
        add("More Apps", "Все приложения");
        add("Inputs", "Входы");
        add("Presentation", "Презентация");
        add("Choose how you want your Home Screen to look.", "Выберите внешний вид главного экрана.");
        add("Choose Your Scenes", "Выберите сцены");
        add("Choose which scenes take turns behind the glass clock.", "Выберите сцены, которые будут сменять друг друга за часами.");
        add("Choose a Background", "Выберите фон");
        add("Choose a photo or ambient colour theme behind your apps.", "Выберите фото или цветовую тему позади приложений.");
        add("Fewer apps", "Свернуть");
        add("Home settings", "Настройки главного экрана");
        add("Home Settings", "Настройки главного экрана");
        add("Language, weather, scenes, apps and people", "Язык, погода, сцены, приложения и профили");
        add("TV settings", "Настройки ТВ");
        add("TV Settings", "Настройки ТВ");
        add("Wi-Fi, picture, sound and the rest of the TV", "Wi-Fi, изображение, звук и другие настройки ТВ");
        add("Sleep timer", "Таймер сна");
        add("Sleep Timer", "Таймер сна");
        add("Turn the TV off after", "Выключить ТВ через");
        add("Off", "Выкл.");
        add("On", "Вкл.");
        add("Search", "Поиск");
        add("Refresh", "Обновить");
        add("Cancel", "Отмена");
        add("Next", "Далее");
        add("Continue", "Продолжить");
        add("Get Started", "Начать");
        add("Let’s set up testOS.", "Давайте настроим testOS.");
        add("Choose a look", "Выберите вид");
        add("Use My Location", "Использовать моё местоположение");
        add("Choose a City…", "Выбрать город…");
        add("Search by name", "Поиск по названию");
        add("Don't Show Weather", "Не показывать погоду");
        add("Finish Setting Up", "Завершение настройки");
        add("Easy Setup (Automatic via ADB)", "Простая настройка (автоматически через ADB)");
        add("Grants all required permissions automatically at once", "Автоматически предоставляет все необходимые разрешения сразу");
        add("Manual Setup", "Ручная настройка");
        add("Grant permissions one by one manually", "Предоставить разрешения вручную по одному");
        add("Enable ADB Debugging", "Включите отладку ADB");
        add("Grant Permissions Now", "Предоставить разрешения сейчас");
        add("Switch to Manual Setup", "Перейти к ручной настройке");
        add("Make testOS the Home Screen", "Сделать testOS главным экраном");
        add("Button Mapper & Control Panel", "Кнопки пульта и Панель управления");
        add("Active in Accessibility", "Включено в Спец. возможностях");
        add("Enable in Accessibility so remapped buttons work in all apps", "Включить в Спец. возможностях для работы кнопок во всех приложениях");
        add("You're All Set", "Всё готово");
        add("Go to Home Screen", "Перейти на главный экран");
        add("Save", "Сохранить");
        add("Reset", "Сбросить");

        // Control Panel Overlay
        add("Control Panel", "Панель управления");
        add("Control Panel (Overlay)", "Панель управления (поверх окон)");
        add("Connected", "Подключено");
        add("Settings ›", "Настройки ›");
        add("Accessories", "Аксессуары");
        add("Memory", "Память");
        add("VPN (Happ)", "VPN (Happ)");
        add("VPN toggle sent to Happ", "Команда переключения отправлена в Happ");
        add("Happ VPN toggle is unavailable", "Управление VPN через Happ недоступно");
        add("VPN connected", "VPN подключён");
        add("Cleanup started", "Очистка запущена");
        add("No recent apps to clean", "Нет приложений для очистки");
        add("Clean", "Очистить");
        add("Cleaned · ", "Очищено · ");
        add(" free", " свободно");
        add("Do Not Disturb", "Не беспокоить");
        add("Audio", "Звук");
        add("Sound", "Звук");
        add("Output", "Вывод");
        add("Ambient Mode", "Спящий режим");
        add("Screensaver", "Заставка");
        add("Power Off", "Выключить");
        add("Sleep", "Сон");
        add("Grant ›", "Разрешить ›");
        add("No recent apps", "Нет недавних приложений");

        // Settings hints & notes
        add("Up and down to move, OK when done", "Вверх и вниз для перемещения, OK для завершения");
        add("Up and down to browse, OK to change, Back to close", "Вверх/вниз для обзора, OK для изменения, Назад для закрытия");
        add("OK to change, hold OK to move, Back to go back", "OK — изменить, удерживайте OK — переместить, Назад — вернуться");
        add("OK to change, Back to go back", "OK — изменить, Назад — вернуться");
        add("Changes the words on the home screen, date, weather and settings.", "Изменяет язык главного экрана, даты, погоды и настроек.");

        // Location & Weather settings
        add("Current place", "Текущее место");
        add("Automatic (from internet)", "Автоматически (из интернета)");
        add("Set manually", "Задано вручную");
        add("Detected automatically", "Определено автоматически");
        add("Search for a city or town…", "Найти город или населенный пункт…");
        add("Use automatic location", "Использовать автоопределение");
        add("Used for the weather on the home screen and local headlines.", "Используется для прогноза погоды на главном экране и местных новостей.");
        add("Weather on the home screen", "Погода на главном экране");
        add("Temperature", "Температура");
        add("Update every", "Обновлять каждые");
        add(" minutes", " мин.");
        add("Weather source", "Источник погоды");
        add("Refresh weather now", "Обновить погоду сейчас");
        add("Updating…", "Обновление…");
        add("Updated", "Обновлено");

        // Classic wallpaper & Apps & Profiles
        add("WALLPAPER", "ОБОИ");
        add("Show in dock · press OK to hide", "В доке · нажмите OK, чтобы скрыть");
        add("Hidden from dock · press OK to show", "Скрыто из дока · нажмите OK, чтобы показать");
        add("Press OK on an app to show or hide it from the home screen dock.", "Нажмите OK на приложении, чтобы показать или скрыть его в доке главного экрана.");
        add("Active", "Активен");
        add("Switch", "Выбрать");
        add("+ Add a person", "+ Добавить профиль");
        add("Each person gets their own pinned apps and Continue Watching list.", "У каждого профиля свои закрепленные приложения и список «Продолжить просмотр».");
        add("Rename…", "Переименовать…");
        add("Avatar color…", "Цвет аватара…");
        add("Delete profile", "Удалить профиль");
        add("Rename Profile", "Переименовать профиль");
        add("Avatar Color", "Цвет аватара");

        // Button Mapper & Recent Apps & Skip Setup
        add("Skip Setup", "Пропустить настройку");
        add("Recent Apps", "Недавние приложения");
        add("Recent Apps Switcher", "Переключатель недавних приложений");
        add("Recent Apps Switcher (Overlay)", "Недавние приложения (поверх окон)");
        add("No recent apps yet", "Пока нет недавних приложений");
        add("Press ▲ on an app to close it", "Нажмите ▲ на приложении, чтобы закрыть его");
        add("Close All", "Закрыть все");
        add("Close", "Закрыть");
        add("Done", "Готово");
        add("None", "Нет");
        add("None (Clear action)", "Нет (очистить действие)");
        add("Single Tap (1x)", "Одиночное нажатие (1x)");
        add("Double Tap (2x)", "Двойное нажатие (2x)");
        add("Triple Tap (3x)", "Тройное нажатие (3x)");
        add("+ Add New Button to Remap…", "+ Добавить кнопку для переназначения…");
        add("Assign Single Tap (1x), Double Tap (2x) & Triple Tap (3x) actions", "Назначить действия для одиночного (1x), двойного (2x) и тройного (3x) нажатия");
        add("Map any button on your remote with Single Tap (1x), Double Tap (2x) and Triple Tap (3x) actions — open the Control Panel, Recent Apps Switcher, or any app.", "Назначьте на любую кнопку пульта одиночное (1x), двойное (2x) и тройное (3x) нажатие — открывайте Панель управления, Недавние приложения или любое приложение.");
        add("ACCESSIBILITY SERVICE", "СЛУЖБА СПЕЦИАЛЬНЫХ ВОЗМОЖНОСТЕЙ");
        add("Button Mapper service", "Служба переназначения кнопок");
        add("Enable in Accessibility…", "Включить в Спец. возможностях…");
        add("REMAPPED BUTTONS", "ПЕРЕНАЗНАЧЕННЫЕ КНОПКИ");
        add("+ Add new button to remap", "+ Добавить кнопку для переназначения");
        add("Listen for a remote key press", "Нажмите кнопку на пульте");
        add("Add new button to remap", "Добавить кнопку");
        add("Press the button on your remote that you want to remap…\n\n(D-pad navigation, OK and Back are kept for normal navigation.)", "Нажмите кнопку на пульте, которую хотите переназначить…\n\n(Стрелки, OK и Назад сохраняются для обычной навигации.)");
        add("Change what this button opens…", "Изменить действие кнопки…");
        add("Remove button remap", "Удалить переназначение");
        add("Enable testOS in Android TV Accessibility Settings so remapped remote buttons and the Control Panel overlay work over any app.", "Включите testOS в специальных возможностях Android TV, чтобы переназначенные кнопки и Панель управления работали поверх любых приложений.");

        // Home & System & About
        add("HOME & PERMISSIONS", "ГЛАВНЫЙ ЭКРАН И РАЗРЕШЕНИЯ");
        add("Default home screen", "Главный экран по умолчанию");
        add("Set as default…", "Сделать по умолчанию…");
        add("Power Off & Sleep Timer permission", "Разрешение на выключение и таймер сна");
        add("Continue Watching & DND permission", "Разрешение для «Продолжить просмотр» и «Не беспокоить»");
        add("Allowed", "Разрешено");
        add("Allow…", "Разрешить…");
        add("Not on this TV", "Недоступно на этом ТВ");
        add("Navigation click sounds", "Звуки щелчков навигации");
        add("PRESENTATION &CLASSIC SHELF", "ПРЕЗЕНТАЦИЯ И КЛАССИЧЕСКАЯ ПОЛКА");
        add("Presentation mode", "Режим презентации");
        add("Presentation address & QR code", "Адрес презентации и QR-код");
        add("Turn on first", "Сначала включите");
        add("Classic Top Shelf content", "Содержимое классической верхней полки");
        add("Recommendations", "Рекомендации");
        add("News", "Новости");
        add("Recommendations & News", "Рекомендации и новости");
        add("Run first-time setup again…", "Запустить первоначальную настройку заново…");
        add("Open Android TV Settings…", "Открыть настройки Android TV…");
        add("Home screen", "Главный экран");
        add("Developer", "Разработчик");
        add("Version", "Версия");
        add("Settings kept", "Хранение настроек");
        add("ADB & PERMISSIONS", "ADB И РАЗРЕШЕНИЯ");
        add("ADB connection status", "Статус подключения ADB");
        add("Pair / Repair ADB connection", "Подключить / Исправить ADB");
        add("Check permissions", "Проверить разрешения");
        add("Check Permissions", "Проверить разрешения");
        add("Grant All via ADB", "Разрешить всё через ADB");
        add("Repair", "Исправить");
        add("Pair now", "Подключить");
        add("On this TV", "На этом ТВ");
        add("Reset settings to defaults", "Сбросить настройки по умолчанию");
        add("Reset settings?", "Сбросить настройки?");
        add("This resets look, scenes, clock and language settings back to defaults.", "Вид, сцены, часы и язык будут сброшены к значениям по умолчанию.");
        add("Settings reset to defaults.", "Настройки сброшены по умолчанию.");

        // Context Menu & Dock
        add("Open", "Открыть");
        add("Rename", "Переименовать");
        add("Rename App", "Переименовать приложение");
        add("Move left in dock", "Переместить влево в доке");
        add("Move right in dock", "Переместить вправо в доке");
        add("Remove from dock", "Убрать из дока");
        add("Pin to Home", "Закрепить на главном");
        add("Unpin from Home", "Открепить с главного");
        add("Move", "Переместить");
        add("Hide from All Apps", "Скрыть из всех приложений");
        add("App info", "О приложении");
        add("Uninstall", "Удалить");

        // Additional Settings Sections, Control Panel, Button Mapper, Advanced, Setup & CleanYourTV (Beta)
        add("LOCATION", "МЕСТОПОЛОЖЕНИЕ");
        add("WEATHER", "ПОГОДА");
        add("APPLICATIONS", "ПРИЛОЖЕНИЯ");
        add("PROFILES", "ПРОФИЛИ");
        add("APPLICATION", "ПРИЛОЖЕНИЕ");
        add("RESET", "СБРОС");
        add("Add", "Добавить");
        add("Name", "Имя");
        add("Add a person", "Добавить профиль");
        add("UNIVERSAL SHORTCUT ACTIONS", "УНИВЕРСАЛЬНЫЕ ЯРЛЫКИ");
        add("Inputs (Control Panel & Dock)", "Входы (Панель управления и док)");
        add("INPUTS APP ON DOCK", "ПРИЛОЖЕНИЕ ВХОДОВ В ДОКЕ");
        add("Show Inputs app on Dock", "Показывать приложение «Входы» в доке");
        add("Reset Control Panel actions to default", "Сбросить действия Панели управления");
        add("Automatic", "Автоматически");
        add("Each TV brand uses different package and activity names for Wi-Fi, Bluetooth, Sound, and Inputs. Choose an app and then pick the exact activity to launch from the Control Panel (and from the Inputs app on the dock).", "Разные марки ТВ используют разные пакеты для Wi-Fi, Bluetooth, звука и входов. Выберите приложение и экран для запуска из Панели управления.");

        // Button Mapper extra options
        add("REMOTE FIXES", "ИСПРАВЛЕНИЯ ПУЛЬТА");
        add("Long-press volume button fix", "Исправление долгого нажатия громкости");
        add("Clear all button mappings", "Очистить все назначения кнопок");
        add("Assign Single Tap (1x), Double Tap (2x) and Triple Tap (3x) actions for each remote button — such as opening the Control Panel overlay, the Recent Apps Switcher overlay, or any installed app.", "Назначайте действия на одиночное (1x), двойное (2x) и тройное (3x) нажатие кнопок пульта — например, открытие Панели управления, Недавних приложений или любой программы.");

        // Advanced & Keep Alive
        add("KEEP ALIVE", "АВТОВОССТАНОВЛЕНИЕ (KEEP ALIVE)");
        add("Keep Alive (Auto-repair permissions)", "Keep Alive (автовосстановление разрешений)");
        add("Paired & Ready", "Подключено и готово");
        add("Port 5555 open (Tap to pair)", "Порт 5555 открыт (нажмите для привязки)");
        add("Not paired (ADB off)", "Не подключено (ADB выкл.)");
        add("active", "активно");
        add("Some TVs disable Accessibility or other permissions automatically in the background. When Keep Alive is on, testOS lightly checks permission status in the background without extra RAM or CPU load and automatically restores any disabled permissions via ADB.", "Некоторые ТВ сами отключают Спец. возможности в фоне. Когда функция Keep Alive включена, testOS без нагрузки на память и процессор проверяет разрешения и автоматически восстанавливает их через ADB.");

        // CleanYourTV (Beta)
        add("CLEANYOURTV (BETA)", "CLEANYOURTV (БЕТА)");
        add("Choose TV model", "Выбрать модель ТВ");
        add("Tap to detect / select…", "Нажмите для определения / выбора…");
        add("Debloat status", "Статус очистки (Debloat)");
        add("Not done", "Не выполнено");
        add("Not done (Select TV model first)", "Не выполнено (сначала выберите модель ТВ)");
        add("Debloat TV…", "Очистить ТВ от лишнего ПО…");
        add("Start ›", "Начать ›");
        add("Disabled apps backup", "Резервная копия отключенных приложений");
        add("None saved", "Нет записей");
        add("apps", "приложений");
        add("packages", "пакетов");
        add("Restore debloated apps…", "Восстановить отключенные приложения…");
        add("Restore ›", "Восстановить ›");
        add("Ad-blocking Private DNS (AdGuard)", "Блокировка рекламы Private DNS (AdGuard)");
        add("CleanYourTV (Beta) runs the CleanYourTV debloat profiles directly on your TV over Local ADB without needing a PC. Choose your TV model (or scan by installed system app packages), pick which bloatware and telemetry apps to disable, and restore them anytime with one tap.", "CleanYourTV (Бета) запускает профили очистки CleanYourTV прямо на вашем ТВ через локальный ADB без ПК. Выберите модель ТВ (или просканируйте системные пакеты), отметьте ненужные приложения и телеметрию для отключения и восстанавливайте их в одно касание.");
        add("🔍 Search…", "🔍 Поиск…");
        add("Scan Apps (ADB)", "Сканировать приложения (ADB)");
        add("All 341 Models", "Все 341 модели");
        add("Disable Selected", "Отключить выбранные");
        add("Select All / None", "Выбрать все / снять");
        add("Restore All", "Восстановить все");
        add(" [Optional]", " [Необязательно]");
        add(" [Already disabled]", " [Уже отключено]");
        add(" [Enabled]", " [Включено]");
        add(" [Disabled]", " [Отключено]");
        add("✅ Re-added Wi-Fi (Continue)", "✅ Wi-Fi переподключен (Далее)");
        add("📶 Wi-Fi Settings…", "📶 Настройки Wi-Fi…");
        add("🛡️ Skip setupwraith", "🛡️ Пропустить setupwraith");
        add("Google TV Wi-Fi Warning", "Предупреждение Wi-Fi для Google TV");
        add("Debloat Complete", "Очистка завершена");
        add("Restore Complete", "Восстановление завершено");
        add("Welcome", "Добро пожаловать");
        add("Select your language to begin", "Выберите язык для начала работы");
        add("Automatic Setup", "Автоматическая настройка");
        add("Choose how to grant permissions", "Выберите способ предоставления разрешений");
        add("Manual Permissions", "Ручные разрешения");
        add("Setup Complete", "Настройка завершена");
        add("Other apps", "Другие приложения");
        add("Other Apps", "Другие приложения");
        add("Move to Other Apps", "Переместить в Другие приложения");
        add("Hidden apps", "Скрытые приложения");
        add("Back to Apps", "Назад к Приложениям");
        add("OK to open  ·  Hold OK to restore  ·  Back to go back", "OK — открыть  ·  Удерживайте OK — восстановить  ·  Назад — вернуться");
        add("OK to open  ·  Hold OK to restore", "OK — открыть  ·  Удерживайте OK — восстановить");
        add("No hidden apps. Hold OK on any app on the home screen dock and choose Hide.", "Нет скрытых приложений. Удерживайте OK на любом приложении в доке и выберите «Скрыть».");

        // Custom Wallpaper, Clock Position, App Icon Changer & Updates
        add("Custom", "Свой");
        add("Clock position", "Позиция часов");
        add("Left", "Слеवा");
        add("Center", "По центру");
        add("Right", "Справа");
        add("Clock font", "Шрифт часов");
        add("CLASSIC BACKGROUND", "КЛАССИЧЕСКИЙ ФОН");
        add("Ambient colour themes…", "Цветовые темы…");
        add("Classic background", "Классический фон");
        add("Change app icon", "Изменить значок приложения");
        add("Reset to default icon", "Сбросить значок по умолчанию");
        add("Choose image…", "Выбрать изображение…");
        add("Choose new image…", "Выбрать новое изображение…");
        add("Turn on Custom scene", "Включить свою сцену");
        add("Turn off Custom scene", "Выключить свою сцену");
        add("Use custom background", "Использовать свой фон");
        add("Updates", "Обновления");
        add("UPDATES", "ОБНОВЛЕНИЯ");
        add("Automatic updates", "Автоматические обновления");
        add("Check for updates", "Проверить обновления");
        add("Check now ›", "Проверить сейчас ›");
        add("VERSION INFO", "ИНФОРМАЦИЯ О ВЕРСИИ");
        add("Current version", "Текущая версия");
        add("GitHub release latest version", "Последняя версия на GitHub");
        add("Checking…", "Проверка…");
        add("Update Available", "Доступно обновление");
        add("Download & Install", "Скачать и установить");
        add("Up to Date", "Установлена последняя версия");
    }

    private static void initRussianExtras() {
        Map<String, String> ru = LANG_MAPS.get(LANG_RU);
        ru.put("Home settings", "Настройки главного экрана");
        ru.put("Control Panel", "Панель управления");
        ru.put("Control Center", "Центр управления");
        ru.put("Appearance", "Внешний вид");
        ru.put("Profiles", "Профили");
        ru.put("Quick buttons", "Быстрые кнопки");
        ru.put("Advanced", "Дополнительно");
        ru.put("About", "О приложении");
        ru.put("Accessibility Service", "Служба специальных возможностей");
        ru.put("Accessibility service", "Служба специальных возможностей");
        ru.put("Active (works in all apps)", "Активно (работает во всех приложениях)");
        ru.put("Active — Set as default home launcher", "Активно — установлен как главный экран");
        ru.put("Not Connected", "Не подключено");
        ru.put("Not connected", "Не подключено");
        ru.put("Not signed in", "Не выполнен вход");
        ru.put("Not set", "Не задано");
        ru.put("Not Now", "Не сейчас");
        ru.put("On (Default)", "Включено (по умолчанию)");
        ru.put("Off", "Выключено");
        ru.put("On", "Включено");
        ru.put("OK", "ОК");
        ru.put("Back", "Назад");
        ru.put("Close", "Закрыть");
        ru.put("Close All", "Закрыть всё");
        ru.put("Resume", "Продолжить");
        ru.put("Continue", "Продолжить");
        ru.put("Next", "Далее");
        ru.put("Previous Page", "Предыдущая страница");
        ru.put("Next Page", "Следующая страница");
        ru.put("Page ", "Страница ");
        ru.put("PAGE ", "СТРАНИЦА ");
        ru.put("Search", "Поиск");
        ru.put("Searching…", "Поиск…");
        ru.put("Filter", "Фильтр");
        ru.put("Clear filter", "Сбросить фильтр");
        ru.put("Clear filter (Show all)", "Сбросить фильтр (показать всё)");
        ru.put("Show app names", "Показывать названия приложений");
        ru.put("Show on Home Screen", "Показывать на главном экране");
        ru.put("Hide", "Скрыть");
        ru.put("Delete", "Удалить");
        ru.put("Rename person", "Переименовать профиль");
        ru.put("Remove person", "Удалить профиль");
        ru.put("Switch Profile", "Сменить профиль");
        ru.put("Add User", "Добавить пользователя");
        ru.put("Create Your Profile", "Создайте свой профиль");
        ru.put("Your name", "Ваше имя");
        ru.put("Your Home Screen", "Ваш главный экран");
        ru.put("Weather Forecast", "Прогноз погоды");
        ru.put("Weather from", "Источник погоды");
        ru.put("Wi-Fi", "Wi-Fi");
        ru.put("Wi-Fi Name (Location)", "Название Wi-Fi (местоположение)");
        ru.put("TV Speakers", "Динамики телевизора");
        ru.put("Bluetooth", "Bluetooth");
        ru.put("Memory", "Оперативная память");
        ru.put("RAM ", "ОЗУ ");
        ru.put("Clean", "Очистить");
        ru.put("No apps to close", "Нет приложений для закрытия");
        ru.put("Recent Apps", "Недавние приложения");
        ru.put("Recent Apps Switcher", "Переключатель приложений");
        ru.put("VPN (Happ)", "VPN (Happ)");
        ru.put("VPN connected", "VPN подключён");
        ru.put("Cleanup started", "Очистка запущена");
        ru.put("ADB & SECURITY MODE", "ADB И РЕЖИМ БЕЗОПАСНОСТИ");
        ru.put("ADB Operating Mode", "Режим работы ADB");
        ru.put("ADB operating mode", "Режим работы ADB");
        ru.put("ADB Mode Required", "Требуется режим ADB");
        ru.put("ADB Debugging Needed", "Требуется отладка ADB");
        ru.put("ADB connection status", "Состояние подключения ADB");
        ru.put("Pair / Repair ADB", "Подключить / восстановить ADB");
        ru.put("Pair / Repair ADB connection", "Подключить / восстановить ADB");
        ru.put("Pair now", "Подключить");
        ru.put("Paired & Ready", "Подключено и готово");
        ru.put("Not paired (ADB off)", "Не подключено (ADB выключен)");
        ru.put("Persistent ADB Mode", "Постоянный режим ADB");
        ru.put("Persistent ADB Setup", "Настройка постоянного режима ADB");
        ru.put("Secure Setup (ADB)", "Безопасная настройка (ADB)");
        ru.put("Secure Mode — Grant WRITE_SECURE_SETTINGS & Close ADB (Recommended)", "Безопасный режим — выдать WRITE_SECURE_SETTINGS и выключить ADB (рекомендуется)");
        ru.put("Persistent ADB Mode — Keep Network ADB Open (Optional)", "Постоянный режим — оставить сетевой ADB включённым (необязательно)");
        ru.put("Enable ADB Now", "Включить ADB");
        ru.put("Close ADB Now", "Выключить ADB");
        ru.put("Turn On / Off ADB", "Включить / выключить ADB");
        ru.put("Turn On ADB Debugging", "Включить отладку ADB");
        ru.put("Enable ADB & Switch", "Включить ADB и переключить режим");
        ru.put("Enable USB / Network Debugging in Developer Options", "Включите отладку по USB / сети в параметрах разработчика");
        ru.put("Open Developer Options…", "Открыть параметры разработчика…");
        ru.put("Developer Options…", "Параметры разработчика…");
        ru.put("Grant All Permissions via Local ADB", "Выдать все разрешения через локальный ADB");
        ru.put("Grant Permissions Now", "Выдать разрешения");
        ru.put("Grant All ›", "Выдать все ›");
        ru.put("Check permissions", "Проверить разрешения");
        ru.put("Permissions Granted & ADB Closed", "Разрешения выданы, ADB выключен");
        ru.put("Remove / revoke all permissions", "Отозвать все разрешения");
        ru.put("Revoke All Permissions", "Отозвать все разрешения");
        ru.put("Keep Alive enabled.", "Keep Alive включён.");
        ru.put("Keep Alive disabled.", "Keep Alive выключен.");
        ru.put("STARTUP & KEEP ALIVE", "АВТОЗАПУСК И KEEP ALIVE");
        ru.put("Launch automatically on system boot", "Запускать автоматически при включении телевизора");
        ru.put("testOS will launch immediately on system boot.", "testOS будет запускаться сразу после включения телевизора.");
        ru.put("CLEANYOURTV (BETA)", "CLEANYOURTV (БЕТА)");
        ru.put("Choose TV model", "Выбрать модель телевизора");
        ru.put("Debloat status", "Состояние очистки системы");
        ru.put("Debloat TV…", "Очистить телевизор…");
        ru.put("Disabled apps backup", "Резервная копия отключённых приложений");
        ru.put("Restore debloated apps…", "Восстановить отключённые приложения…");
        ru.put("Restore all ›", "Восстановить всё ›");
        ru.put("Manual debloat (All system apps)", "Ручная очистка (все системные приложения)");
        ru.put("WARNING: Manual Debloat lists ALL system apps installed on your TV.\n\n", "ВНИМАНИЕ: ручная очистка показывает ВСЕ системные приложения телевизора.\n\n");
        ru.put("⚠️ Manual Debloat Warning", "⚠️ Предупреждение о ручной очистке");
        ru.put("⚠️ Core System Package", "⚠️ Критически важный системный пакет");
        ru.put("INSTALLED APPS", "УСТАНОВЛЕННЫЕ ПРИЛОЖЕНИЯ");
        ru.put("PACKAGES ON THIS TV (", "ПАКЕТЫ НА ЭТОМ ТВ (");
        ru.put("SAVED DISABLED PACKAGES (TAP TO RE-ENABLE)", "СОХРАНЁННЫЕ ОТКЛЮЧЁННЫЕ ПАКЕТЫ (НАЖМИТЕ ДЛЯ ВКЛЮЧЕНИЯ)");
        ru.put("Re-enable All Stock Launchers", "Включить все штатные лаунчеры");
        ru.put("Disable system launchers", "Отключить системные лаунчеры");
        ru.put("Manage system apps in TV settings…", "Управление системными приложениями в настройках ТВ…");
        ru.put("App Info", "Сведения о приложении");
        ru.put("App info", "Сведения о приложении");
        ru.put("Version", "Версия");
        ru.put("Developer", "Разработчик");
        ru.put("Current version", "Текущая версия");
        ru.put("Check for updates", "Проверить обновления");
        ru.put("Tap Check for updates", "Нажмите «Проверить обновления»");
        ru.put("Checking…", "Проверка…");
        ru.put("Update Available", "Доступно обновление");
        ru.put("Download & Install", "Скачать и установить");
        ru.put("Up to Date", "Установлена последняя версия");
        ru.put("BACKUP & RESTORE", "РЕЗЕРВНОЕ КОПИРОВАНИЕ И ВОССТАНОВЛЕНИЕ");
        ru.put("Backup settings to Download", "Сохранить настройки в папку Download");
        ru.put("Restore settings from folder…", "Восстановить настройки из папки…");
        ru.put("RESET", "СБРОС");
        ru.put("Reset settings to defaults", "Сбросить настройки");
        ru.put("Reset settings?", "Сбросить настройки?");
        ru.put("Done", "Готово");
        ru.put("Not done", "Не завершено");
        ru.put("testOS Settings", "Настройки testOS");
        ru.put("TV Settings", "Настройки телевизора");
        ru.put("Cancel", "Отмена");
        ru.put("Apply", "Применить");
        ru.put("Save", "Сохранить");
        ru.put("Preview", "Предпросмотр");
        ru.put("Screen Off", "Выключить экран");
        ru.put("Power Off (Screen Off)", "Выключить экран");
        ru.put("Lock Now", "Заблокировать сейчас");
        ru.put("Lock Screen", "Экран блокировки");
        ru.put("Preview Lock Screen now", "Посмотреть экран блокировки");
        ru.put("Use Presentation", "Режим презентации");
        ru.put("Presentation", "Презентация");
        ru.put("Inputs", "Входы");
        ru.put("Ambient Mode", "Режим заставки");
        ru.put("Screensaver", "Заставка");
        ru.put("Sleep timer", "Таймер сна");
        ru.put("Turn the TV off after", "Выключить телевизор через");
        ru.put("seconds", "секунд");
        ru.put(" seconds", " секунд");
        ru.put(" min", " мин");
        ru.put(" min left", " мин осталось");
        ru.put("Off in ", "Выключение через ");
        ru.put(" paired", " подключено");
        ru.put("e.g. tcl, philips, sony, hisense, xiaomi…", "например: TCL, Philips, Sony, Hisense, Xiaomi…");
        ru.put("Matching places", "Совпадения");
        ru.put("Popular movies and series on your streaming services", "Популярные фильмы и сериалы в ваших онлайн-кинотеатрах");
        ru.put("Headlines from your city and the world", "Новости вашего города и мира");
        ru.put("No recent apps yet", "Пока нет недавних приложений");
        ru.put("No place found for ", "Не найдено место: ");
        ru.put("Couldn't search. Check the internet connection.", "Не удалось выполнить поиск. Проверьте подключение к интернету.");
        ru.put("TMDB connected", "TMDB подключён");

        // Weather, profiles and streaming sign-in labels.
        ru.put(" and enter:", " и введите:");
        ru.put("5-DAY FORECAST", "ПРОГНОЗ НА 5 ДНЕЙ");
        ru.put("Couldn't reach ", "Не удалось связаться с ");
        ru.put("Getting the weather…", "Загрузка погоды…");
        ru.put("H:", "В:");
        ru.put("HOURLY FORECAST", "ПОЧАСОВОЙ ПРОГНОЗ");
        ru.put("Humidity ", "Влажность ");
        ru.put("L:", "Н:");
        ru.put("My Location", "Моё местоположение");
        ru.put("Nothing to continue yet", "Пока нечего продолжать");
        ru.put("Now", "Сейчас");
        ru.put("Or open ", "Или откройте ");
        ru.put("Preparing QR code…", "Подготовка QR-кода…");
        ru.put("Profile ", "Профиль ");
        ru.put("Scan with your phone and approve.", "Отсканируйте телефоном и подтвердите вход.");
        ru.put("Sign in to ", "Войти в ");
        ru.put("Tall Glass", "Высокие стеклянные часы");
        ru.put("Today", "Сегодня");
        ru.put("Up next", "Далее");
        ru.put("Updated ", "Обновлено ");
        ru.put("Waiting for approval…", "Ожидание подтверждения…");
        ru.put("Wind ", "Ветер ");
        ru.put("iPhone Rounded", "Округлый iPhone");
        ru.put("min", "мин");


        // Search, voice input, ADB status and media-provider labels.
        ru.put("ADB Enabled · Not Paired", "ADB включён · не сопряжено");
        ru.put("Accessibility", "Специальные возможности");
        ru.put("Apps & Settings", "Приложения и настройки");
        ru.put("Can’t reach the internet", "Нет подключения к интернету");
        ru.put("Connected & Paired (Port 5555 Open)", "Подключено и сопряжено (порт 5555 открыт)");
        ru.put("Date & Time", "Дата и время");
        ru.put("Didn’t catch that — press the mic to try again", "Не удалось распознать — нажмите на микрофон и попробуйте ещё раз");
        ru.put("Disconnected (ADB Off)", "Отключено (ADB выключен)");
        ru.put("Display", "Экран");
        ru.put("Get ", "Получить ");
        ru.put("HOW TO WATCH", "ГДЕ СМОТРЕТЬ");
        ru.put("Keep typing to search movies and shows", "Продолжайте вводить запрос для поиска фильмов и сериалов");
        ru.put("Listening…", "Слушаю…");
        ru.put("Movie", "Фильм");
        ru.put("Movies & TV", "Фильмы и ТВ");
        ru.put("No movies or shows found", "Фильмы и сериалы не найдены");
        ru.put("No results", "Нет результатов");
        ru.put("Not installed", "Не установлено");
        ru.put("Not streaming right now.", "Сейчас недоступно для просмотра.");
        ru.put("On ", "Включено ");
        ru.put("Paired · ADB Enabled", "Сопряжено · ADB включён");
        ru.put("Persistent ADB Mode (Keep port 5555 open)", "Постоянный режим ADB (оставить порт 5555 открытым)");
        ru.put("Port 5555 Open · Ready to Pair", "Порт 5555 открыт · готово к сопряжению");
        ru.put("Recent Searches", "Недавние запросы");
        ru.put("Refresh Carousel", "Обновить карусель");
        ru.put("Say a movie, show or app", "Назовите фильм, сериал или приложение");
        ru.put("Screen Saver", "Заставка");
        ru.put("Search apps, movies and shows", "Поиск приложений, фильмов и сериалов");
        ru.put("Secure Mode (Needs One-Time Pair to Grant Permissions)", "Безопасный режим (однократное сопряжение для выдачи разрешений)");
        ru.put("Secure Mode Active (ADB Not Used · Self-Repair Ready)", "Безопасный режим активен (ADB не используется · самовосстановление готово)");
        ru.put("Secure Mode — WRITE_SECURE_SETTINGS & Close ADB (Recommended)", "Безопасный режим — WRITE_SECURE_SETTINGS и выключение ADB (рекомендуется)");
        ru.put("Series", "Сериал");
        ru.put("Storage", "Хранилище");
        ru.put("Trending", "В тренде");
        ru.put("Type to search your apps, settings, movies and shows", "Введите запрос для поиска приложений, настроек, фильмов и сериалов");
        ru.put("Voice input isn't available on this TV", "Голосовой ввод недоступен на этом телевизоре");
        ru.put("Voice needs the internet", "Для голосового ввода нужен интернет");
        ru.put("clear", "очистить");
        ru.put("space", "пробел");
        ru.put("▶  Open in ", "▶  Открыть в ");


        // Remaining dialogs, remote-key names, icon editor and update flow.
        ru.put("A new version of testOS is available on GitHub.\\n", "На GitHub доступна новая версия testOS.\\n");
        ru.put("ADB install failed, opening package installer…", "Не удалось установить через ADB, открываем системный установщик…");
        ru.put("ADB is active — selecting Install will download and install the update automatically.", "ADB включён — при выборе «Установить» обновление будет скачано и установлено автоматически.");
        ru.put("ADB is turned off (Secure Mode) — selecting Install will download the update and open the system package installer.", "ADB выключен (безопасный режим) — при выборе «Установить» обновление будет скачано и откроется системный установщик.");
        ru.put("App Icon Editor", "Редактор значков приложений");
        ru.put("App Switch / Recents (187)", "Переключение приложений / Недавние (187)");
        ru.put("Audio Track (222)", "Аудиодорожка (222)");
        ru.put("Auto", "Авто");
        ru.put("Back (4)", "Назад (4)");
        ru.put("Back to folders", "Назад к папкам");
        ru.put("Back to parent folder", "К родительской папке");
        ru.put("Background color", "Цвет фона");
        ru.put("Backup Failed", "Не удалось создать резервную копию");
        ru.put("Black", "Чёрный");
        ru.put("Blue", "Синий");
        ru.put("Blue Button", "Синяя кнопка");
        ru.put("Bookmark / Favorite", "Закладка / Избранное");
        ru.put("Changelog — v", "Список изменений — v");
        ru.put("Channel Down", "Следующий канал вниз");
        ru.put("Channel Up", "Следующий канал вверх");
        ru.put("Choose Folder to Restore Settings", "Выберите папку для восстановления настроек");
        ru.put("Could not check for updates.", "Не удалось проверить наличие обновлений.");
        ru.put("Could not decode image file.", "Не удалось прочитать файл изображения.");
        ru.put("Could not restore settings from:\n", "Не удалось восстановить настройки из:\n");
        ru.put("Could not start installer.", "Не удалось запустить установщик.");
        ru.put("Could not write to Download/", "Не удалось записать файл в Download/");
        ru.put("Custom Key Code (Enter Number…)", "Свой код кнопки (введите число…)");
        ru.put("Dark", "Тёмный");
        ru.put("Down", "Вниз");
        ru.put("Downloading update v", "Скачивание обновления v");
        ru.put("Enter Android Key Code", "Введите код кнопки Android");
        ru.put("Fast Forward", "Перемотка вперёд");
        ru.put("Grant All Files Access…", "Разрешить доступ ко всем файлам…");
        ru.put("Grant Storage Access…", "Разрешить доступ к хранилищу…");
        ru.put("Gray", "Серый");
        ru.put("Green", "Зелёный");
        ru.put("Green Button", "Зелёная кнопка");
        ru.put("Guide Button", "Кнопка телегида");
        ru.put("Home (3)", "Домой (3)");
        ru.put("Indigo", "Индиго");
        ru.put("Info Button", "Кнопка информации");
        ru.put("Install", "Установить");
        ru.put("Installing update v", "Установка обновления v");
        ru.put("Key: ", "Кнопка: ");
        ru.put("Last Channel", "Предыдущий канал");
        ru.put("Menu Button", "Кнопка меню");
        ru.put("Mode 1 (Standard · Default)", "Режим 1 (стандартный · по умолчанию)");
        ru.put("Mode 2 (HY320 / Projector)", "Режим 2 (HY320 / проектор)");
        ru.put("Move icon", "Переместить значок");
        ru.put("Mute / Unmute (164)", "Выключить / включить звук (164)");
        ru.put("Next Track", "Следующая дорожка");
        ru.put("No changelog text provided for this release.", "Для этого выпуска список изменений не предоставлен.");
        ru.put("No image files (", "Нет файлов изображений (");
        ru.put("No images in this folder (open a subfolder below)", "В этой папке нет изображений (откройте вложенную папку ниже)");
        ru.put("No settings backup (.json) found in this folder", "В этой папке не найдена резервная копия настроек (.json)");
        ru.put("No system file picker available on this TV.", "На этом телевизоре нет системного выбора файлов.");
        ru.put("Not selected", "Не выбрано");
        ru.put("Notifications (83)", "Уведомления (83)");
        ru.put("Open System File Picker…", "Открыть системный выбор файлов…");
        ru.put("Orange", "Оранжевый");
        ru.put("Pause", "Пауза");
        ru.put("Pink", "Розовый");
        ru.put("Play", "Воспроизвести");
        ru.put("Play / Pause", "Воспроизведение / пауза");
        ru.put("Please allow 'Install unknown apps' for testOS.", "Разрешите testOS устанавливать неизвестные приложения.");
        ru.put("Previous Track", "Предыдущая дорожка");
        ru.put("Purple", "Фиолетовый");
        ru.put("Record Button", "Кнопка записи");
        ru.put("Red", "Красный");
        ru.put("Red Button", "Красная кнопка");
        ru.put("Restore Failed", "Не удалось восстановить настройки");
        ru.put("Restore from Download/", "Восстановить из Download/");
        ru.put("Restore from this folder (", "Восстановить из этой папки (");
        ru.put("Rewind", "Перемотка назад");
        ru.put("Search Button", "Кнопка поиска");
        ru.put("Select this folder & shuffle (", "Выбрать эту папку и менять изображения случайно (");
        ru.put("Send Key Code", "Отправить код кнопки");
        ru.put("Send Key Code…", "Отправить код кнопки…");
        ru.put("Settings Backed Up", "Резервная копия настроек создана");
        ru.put("Settings Button", "Кнопка настроек");
        ru.put("Settings Restored", "Настройки восстановлены");
        ru.put("Settings were successfully restored from:\\n", "Настройки успешно восстановлены из:\\n");
        ru.put("Show Changelog", "Показать список изменений");
        ru.put("Sleep / Standby (223)", "Сон / режим ожидания (223)");
        ru.put("Stop", "Стоп");
        ru.put("Subtitles / CC", "Субтитры / скрытые субтитры");
        ru.put("TV Input / Source", "Вход / источник сигнала ТВ");
        ru.put("Teal", "Бирюзовый");
        ru.put("Up", "Вверх");
        ru.put("Update download failed.", "Не удалось скачать обновление.");
        ru.put("Update installed!", "Обновление установлено!");
        ru.put("Volume Down (25)", "Уменьшить громкость (25)");
        ru.put("Volume Up (24)", "Увеличить громкость (24)");
        ru.put("White", "Белый");
        ru.put("Yellow", "Жёлтый");
        ru.put("Yellow Button", "Жёлтая кнопка");
        ru.put("You are up to date (v", "У вас установлена последняя версия (v");
        ru.put("Your current testOS settings have been saved to:\\n\\n", "Текущие настройки testOS сохранены в:\\n\\n");
        ru.put("Zoom", "Масштаб");
        ru.put("Zoom in", "Увеличить");
        ru.put("Zoom out", "Уменьшить");
        ru.put("e.g. 85 (Play/Pause), 164 (Mute), 178 (TV Input)", "например, 85 (воспроизведение/пауза), 164 (звук), 178 (вход ТВ)");
        ru.put("images", "изображений");


        // Exact Unicode / newline variants used by the source UI strings.
        ru.put("% used  ·  Clean", "% использовано  ·  Очистить");
        ru.put("1. Open TV Settings → System → About and click Build 7 times to unlock Developer Options.\n", "1. Откройте настройки ТВ → Система → О телевизоре и нажмите на номер сборки 7 раз, чтобы открыть параметры разработчика.\n");
        ru.put("ADB debugging is currently turned off. Persistent ADB Mode requires ADB to be enabled.\n\nWould you like to enable ADB and switch to Persistent ADB Mode?", "Сейчас отладка ADB выключена. Для постоянного режима ADB её необходимо включить.\n\nВключить ADB и перейти в постоянный режим?");
        ru.put("Back to return   ·   Hold OK for options", "Назад — вернуться   ·   Удерживайте OK для параметров");
        ru.put("Choose a City…", "Выберите город…");
        ru.put("Connecting to local ADB on port 5555 and granting permissions…\n\nIf an “Allow USB debugging?” prompt appears, check “Always allow” and press Allow.", "Подключение к локальному ADB на порту 5555 и выдача разрешений…\n\nЕсли появится запрос «Разрешить отладку по USB?», отметьте «Всегда разрешать» и нажмите «Разрешить».");
        ru.put("Could not connect to local ADB on port 5555.\n\n1. Open Settings → System → About and click Build 7 times.\n2. Open Developer Options and turn on USB Debugging / Network Debugging.\n3. Return here and tap Pair / Repair again.", "Не удалось подключиться к локальному ADB на порту 5555.\n\n1. Откройте Настройки → Система → О телевизоре и нажмите на номер сборки 7 раз.\n2. В параметрах разработчика включите отладку по USB / сети.\n3. Вернитесь сюда и нажмите «Подключить / восстановить».");
        ru.put("Could not connect to local ADB on port 5555.\n\nPlease make sure USB Debugging / Network Debugging is turned ON in Developer Options, then try again — or choose Manual Setup.", "Не удалось подключиться к локальному ADB на порту 5555.\n\nУбедитесь, что в параметрах разработчика включена отладка по USB / сети, и повторите попытку или выберите ручную настройку.");
        ru.put("Delete “", "Удалить «");
        ru.put("Granting permissions via local ADB…\n\nIf an “Allow USB debugging?” prompt appears on screen, check “Always allow” and press Allow.", "Выдача разрешений через локальный ADB…\n\nЕсли появится запрос «Разрешить отладку по USB?», отметьте «Всегда разрешать» и нажмите «Разрешить».");
        ru.put("Keep Alive monitors the Accessibility Service and automatically restores it if your TV turns it off.\n\n", "Keep Alive следит за службой специальных возможностей и автоматически восстанавливает её, если телевизор отключит её.\n\n");
        ru.put("Let’s set up testOS.", "Настроим testOS.");
        ru.put("No place found for “", "Место не найдено: «");
        ru.put("Open Developer Options / TV Settings…", "Открыть параметры разработчика / настройки ТВ…");
        ru.put("Open on a phone or computer on the same Wi-Fi:\n\n", "Откройте на телефоне или компьютере в той же сети Wi-Fi:\n\n");
        ru.put("Pairing code\n", "Код сопряжения\n");
        ru.put("This runs 'settings put global adb_enabled 0' over Local ADB and verifies that port 5555 is closed on your home network.\n\n", "Выполняет команду 'settings put global adb_enabled 0' через локальный ADB и проверяет, что порт 5555 закрыт в домашней сети.\n\n");
        ru.put("WRITE_SECURE_SETTINGS and all launcher permissions are now active, and ADB (port 5555) has been closed for your security.\n\n", "WRITE_SECURE_SETTINGS и все разрешения лаунчера активированы, а ADB (порт 5555) выключен для вашей безопасности.\n\n");
        ru.put("Who’s Watching?", "Кто смотрит?");
        ru.put("You need the ADB mode enabled for this troubleshooting step. You can always change the ADB mode from Settings later.\n\nWould you like to enable ADB now or skip this step?", "Для этого шага диагностики необходимо включить режим ADB. Позже его можно изменить в настройках.\n\nВключить ADB сейчас или пропустить этот шаг?");
        ru.put("⚠️ Security Warning:\n\n", "⚠️ Предупреждение безопасности:\n\n");


        ru.put("% used  \\u00b7  Clean", "% использовано  ·  Очистить");
        ru.put("1. Open TV Settings \\u2192 System \\u2192 About and click Build 7 times to unlock Developer Options.\\n", "1. Откройте настройки ТВ → Система → О телевизоре и нажмите на номер сборки 7 раз, чтобы открыть параметры разработчика.\\n");
        ru.put("1. Set testOS as Default via Local ADB", "1. Назначить testOS главным экраном через локальный ADB");
        ru.put("2. Disable System Launchers (Google TV / Android TV / OEM)", "2. Отключить системные лаунчеры (Google TV / Android TV / производителя)");
        ru.put("3. Fix Home Button via Accessibility (Button Mapper)", "3. Настроить кнопку Home через специальные возможности (Button Mapper)");
        ru.put("4. Open Android System Home Settings", "4. Открыть системные настройки главного экрана Android");
        ru.put("A small weather display on Home, with a 5-day forecast.", "Компактный прогноз погоды на главном экране на 5 дней.");
        ru.put("ADB debugging enabled.", "Отладка ADB включена.");
        ru.put("ADB debugging is currently turned off. Persistent ADB Mode requires ADB to be enabled.\\n\\nWould you like to enable ADB and switch to Persistent ADB Mode?", "Сейчас отладка ADB выключена. Для постоянного режима ADB её необходимо включить.\\n\\nВключить ADB и перейти в постоянный режим?");
        ru.put("ADB enabled and switched to Persistent ADB Mode.", "ADB включён, выбран постоянный режим ADB.");
        ru.put("ADB paired and all permissions granted!", "ADB подключён, все разрешения выданы!");
        ru.put("Back to return   \\u00b7   Hold OK for options", "Назад — вернуться   ·   Удерживайте OK для параметров");
        ru.put("Change any of this later in Settings, from the gear in Control Center.", "Все эти параметры можно изменить позже в настройках, открыв шестерёнку в центре управления.");
        ru.put("Checked items are currently DISABLED. Disabling the stock system launcher (e.g. Google TV Home / LauncherX + setupwraith, Android TV Home, or TCL/Philips OEM Home) forces Android to use testOS when pressing the Home key.", "Отмеченные элементы сейчас ОТКЛЮЧЕНЫ. Отключение штатного системного лаунчера (например, Google TV Home / LauncherX + setupwraith, Android TV Home или лаунчера TCL/Philips) заставляет Android открывать testOS при нажатии Home.");
        ru.put("Checking installed packages over ADB for ", "Проверка установленных пакетов через ADB для ");
        ru.put("Checks GitHub Releases (techgenius1/tvOS-Launcher) for the latest APK and installs updates directly on your TV.", "Проверяет GitHub Releases проекта testOS на наличие новой APK и устанавливает обновления прямо на телевизор.");
        ru.put("Choose a City\\u2026", "Выберите город…");
        ru.put("Choose custom background image", "Выбрать изображение для своего фона");
        ru.put("Choose custom scene image", "Выбрать изображение для своей сцены");
        ru.put("Choose folder ›", "Выбрать папку ›");
        ru.put("CleanYourTV (Beta) runs the CleanYourTV debloat profiles directly on your TV over Local ADB without needing a PC. Choose your TV model (or scan by installed system app packages), disable stock launchers or bloatware, and restore them anytime with one tap.", "CleanYourTV (бета) запускает профили очистки прямо на телевизоре через локальный ADB — компьютер не нужен. Выберите модель ТВ (или выполните поиск по системным пакетам), отключите штатные лаунчеры и ненужные приложения. Восстановить их можно в любой момент одним нажатием.");
        ru.put("Connecting to local ADB on port 5555 and granting permissions…\\n\\nIf an \\u201cAllow USB debugging?\\u201d prompt appears, check \\u201cAlways allow\\u201d and press Allow.", "Подключение к локальному ADB на порту 5555 и выдача разрешений…\\n\\nЕсли появится запрос «Разрешить отладку по USB?», отметьте «Всегда разрешать» и нажмите «Разрешить».");
        ru.put("Continue watching on the dock will stop showing your ", "В панели приложений больше не будет отображаться раздел «Продолжить просмотр» для ");
        ru.put("Could not connect to ADB — enable USB/Network Debugging or use Step 3 / Step 4.", "Не удалось подключиться к ADB — включите отладку по USB / сети или выполните шаг 3 / шаг 4.");
        ru.put("Could not connect to local ADB on port 5555.\\n\\n1. Open Settings \\u2192 System \\u2192 About and click Build 7 times.\\n2. Open Developer Options and turn on USB Debugging / Network Debugging.\\n3. Return here and tap Pair / Repair again.", "Не удалось подключиться к локальному ADB на порту 5555.\\n\\n1. Откройте Настройки → Система → О телевизоре и нажмите на номер сборки 7 раз.\\n2. В параметрах разработчика включите отладку по USB / сети.\\n3. Вернитесь сюда и нажмите «Подключить / восстановить».");
        ru.put("Could not connect to local ADB on port 5555.\\n\\nPlease make sure USB Debugging / Network Debugging is turned ON in Developer Options, then try again — or choose Manual Setup.", "Не удалось подключиться к локальному ADB на порту 5555.\\n\\nУбедитесь, что в параметрах разработчика включена отладка по USB / сети, и повторите попытку или выберите ручную настройку.");
        ru.put("Custom ", "Свой вариант ");
        ru.put("Delete \\u201C", "Удалить «");
        ru.put("Each TV brand uses different package and activity names for Wi-Fi, Bluetooth, Sound, Inputs, and Ambient Mode. You can also choose Lock Screen for Ambient Mode (for TVs without built-in Ambient Mode) and toggle individual Control Panel items on or off.", "У разных производителей ТВ отличаются названия пакетов и экранов Wi-Fi, Bluetooth, звука, входов и режима заставки. Для телевизоров без встроенной заставки можно выбрать экран блокировки. Здесь также можно включать и отключать отдельные элементы панели управления.");
        ru.put("Each person keeps their own Keep Watching row and app order.", "У каждого профиля свой список «Продолжить просмотр» и собственный порядок приложений.");
        ru.put("Enable ADB for Persistent Mode?", "Включить ADB для постоянного режима?");
        ru.put("Enabling ADB debugging and switching to Persistent ADB Mode…", "Включение отладки ADB и переход в постоянный режим…");
        ru.put("Folder shuffle enabled: ", "Случайная смена изображений из папки включена: ");
        ru.put("Grant permissions one by one manually through Android TV Settings", "Выдавать разрешения вручную по одному через настройки Android TV");
        ru.put("Granting permissions via local ADB…\\n\\nIf an \\u201cAllow USB debugging?\\u201d prompt appears on screen, check \\u201cAlways allow\\u201d and press Allow.", "Выдача разрешений через локальный ADB…\\n\\nЕсли появится запрос «Разрешить отладку по USB?», отметьте «Всегда разрешать» и нажмите «Разрешить».");
        ru.put("Grants WRITE_SECURE_SETTINGS & closes port 5555 when done", "Выдаёт WRITE_SECURE_SETTINGS и закрывает порт 5555 после завершения");
        ru.put("Grants WRITE_SECURE_SETTINGS once to self-repair Accessibility, then closes port 5555", "Один раз выдаёт WRITE_SECURE_SETTINGS для восстановления специальных возможностей, затем закрывает порт 5555");
        ru.put("Home button fix enabled — please also enable Accessibility service above so Home key can be intercepted.", "Исправление кнопки Home включено — также включите службу специальных возможностей выше, чтобы перехватывать кнопку Home.");
        ru.put("Home button fix enabled — pressing Home will launch testOS.", "Исправление кнопки Home включено — нажатие Home будет запускать testOS.");
        ru.put("Images, videos and webpages sent from any phone or computer on the same Wi-Fi. For everyone on this TV.", "Фотографии, видео и веб-страницы, отправленные с телефона или компьютера в той же сети Wi-Fi. Доступно всем пользователям этого ТВ.");
        ru.put("In Control Center (needs location access)", "В центре управления (требуется доступ к местоположению)");
        ru.put("Its layout and Continue Watching list will be removed.", "Расположение приложений и список «Продолжить просмотр» будут удалены.");
        ru.put("Keep Alive (Auto-repair Accessibility)", "Keep Alive (автовосстановление специальных возможностей)");
        ru.put("Keep Alive monitors the Accessibility Service and automatically restores it if your TV turns it off.\\n\\n", "Keep Alive следит за службой специальных возможностей и автоматически восстанавливает её, если телевизор отключит её.\\n\\n");
        ru.put("Keep Screen Awake", "Не выключать экран");
        ru.put("Keeps network ADB enabled for CleanYourTV debloating (Note: leaves port 5555 open on LAN)", "Оставляет сетевой ADB включённым для очистки ТВ через CleanYourTV (порт 5555 остаётся открытым в локальной сети)");
        ru.put("Let\\u2019s set up testOS.", "Настроим testOS.");
        ru.put("Location set to ", "Местоположение задано: ");
        ru.put("Lock Screen folder shuffle enabled: ", "Случайная смена обоев экрана блокировки включена: ");
        ru.put("Long-press volume fix disabled (using native TV volume).", "Исправление долгого нажатия громкости отключено (используется штатное управление ТВ).");
        ru.put("Long-press volume fix enabled (for TVs where holding volume stops repeating).", "Исправление долгого нажатия громкости включено (для ТВ, где при удержании прекращается изменение громкости).");
        ru.put("Looks like ", "Похоже, ");
        ru.put("Mode 1 — Standard (Default, for most Android TVs & IR remotes)", "Режим 1 — стандартный (по умолчанию для большинства Android TV и ИК-пультов)");
        ru.put("Mode 2 — HY320 / Projector (AudioService & IR pulse-burst mode)", "Режим 2 — HY320 / проектор (AudioService и импульсный режим ИК)");
        ru.put("Move ", "Переместить ");
        ru.put("No direct model name match for ", "Нет точного совпадения по названию модели: ");
        ru.put("No place found for \\u201c", "Место не найдено: «");
        ru.put("No saved disabled-app backup was found, and no TV model is selected yet. Choose a TV model first if you want to restore by model profile.", "Резервная копия отключённых приложений не найдена, модель ТВ не выбрана. Сначала выберите модель, если хотите восстановить приложения по профилю.");
        ru.put("No screensaver or sleep while presenting", "Не включать заставку и спящий режим во время презентации");
        ru.put("None of the packages listed in ", "Ни один из пакетов в списке ");
        ru.put("On Google TV, if you disable setupwraith, make sure your Wi-Fi network was re-added in Wi-Fi Settings.", "На Google TV перед отключением setupwraith убедитесь, что ваша сеть Wi-Fi заново добавлена в настройках Wi-Fi.");
        ru.put("On TCL and Philips Android 11/12 TVs, the OEM firmware often hides the Android Default Home UI and forces the stock launcher when pressing Home. Disabling the stock launcher (Option 2) or enabling the Home Button Fix (Option 3) guarantees pressing Home always opens testOS.", "На телевизорах TCL и Philips с Android 11/12 прошивка производителя часто скрывает системный выбор главного экрана и принудительно запускает штатный лаунчер по кнопке Home. Отключение штатного лаунчера (вариант 2) или включение исправления кнопки Home (вариант 3) гарантирует запуск testOS.");
        ru.put("On TCL, Philips, and Google TVs, OEM firmware often forces the stock launcher when pressing Home. Follow the steps below so pressing Home always opens testOS, then click Continue.", "На телевизорах TCL, Philips и Google прошивка производителя может принудительно запускать штатный лаунчер по кнопке Home. Выполните шаги ниже, чтобы кнопка Home всегда открывала testOS, затем нажмите «Продолжить».");
        ru.put("On the Lock Screen, only Clock, Date, and Greeting are shown above your selected backgrounds. Pressing any remote button returns to where you were before.", "На экране блокировки поверх выбранных обоев отображаются только часы, дата и приветствие. Нажатие любой кнопки пульта возвращает к предыдущему экрану.");
        ru.put("On — Pressing Home launches testOS", "Включено — нажатие Home запускает testOS");
        ru.put("Open Developer Options / TV Settings…", "Открыть параметры разработчика / настройки ТВ…");
        ru.put("Open Developer Options and turn on USB Debugging / Network Debugging to complete switching to Persistent ADB Mode.", "Откройте параметры разработчика и включите отладку по USB / сети, чтобы завершить переход в постоянный режим ADB.");
        ru.put("Open on a phone or computer on the same Wi-Fi:\\n\\n", "Откройте на телефоне или компьютере в той же сети Wi-Fi:\\n\\n");
        ru.put("Pairing code\\n", "Код сопряжения\\n");
        ru.put("Persistent ADB Mode selected — Port 5555 stays open for CleanYourTV & ADB tools.", "Выбран постоянный режим ADB — порт 5555 останется открытым для CleanYourTV и инструментов ADB.");
        ru.put("Please enable USB Debugging / Network Debugging in Developer Options, then return here to run this step.", "Включите отладку по USB / сети в параметрах разработчика, затем вернитесь сюда и повторите этот шаг.");
        ru.put("Proceed to the next setup step when you are done here", "Когда закончите, перейдите к следующему шагу настройки");
        ru.put("Pull full app list from ADB and match models by OEM/system packages", "Получить полный список приложений через ADB и определить модель по пакетам производителя / системы");
        ru.put("Re-enable all packages disabled by CleanYourTV over ADB", "Включить все пакеты, отключённые CleanYourTV через ADB");
        ru.put("Re-enable all packages in ", "Включить все пакеты в ");
        ru.put("Removed remap for ", "Удалено переназначение для ");
        ru.put("Required so mapped buttons work over every app", "Необходимо, чтобы переназначенные кнопки работали во всех приложениях");
        ru.put("Restore All Saved Packages (", "ВОССТАНОВИТЬ ВСЕ СОХРАНЁННЫЕ ПАКЕТЫ (");
        ru.put("Restore Saved Disabled Apps (", "ВОССТАНОВИТЬ СОХРАНЁННЫЕ ОТКЛЮЧЁННЫЕ ПРИЛОЖЕНИЯ (");
        ru.put("Restore Selected Model Profile (", "ВОССТАНОВИТЬ ПРОФИЛЬ ВЫБРАННОЙ МОДЕЛИ (");
        ru.put("Run ROLE_HOME & set-home-activity over Local ADB ›", "Выполнить ROLE_HOME и set-home-activity через локальный ADB ›");
        ru.put("Running settings put global adb_enabled 0 and verifying port 5555…", "Выполняется settings put global adb_enabled 0 и проверяется закрытие порта 5555…");
        ru.put("Secure Mode selected — ADB will grant WRITE_SECURE_SETTINGS and close port 5555 when paired.", "Выбран безопасный режим — ADB выдаст WRITE_SECURE_SETTINGS и закроется после подключения.");
        ru.put("Security Note: Keep Alive is enabled by default. In Secure Mode, testOS uses WRITE_SECURE_SETTINGS to restore Accessibility directly in-process without touching ADB (even if ADB is enabled on the TV). In Persistent ADB Mode, Keep Alive can also use Local ADB on port 5555.", "Безопасность: Keep Alive включён по умолчанию. В безопасном режиме testOS использует WRITE_SECURE_SETTINGS для восстановления специальных возможностей без обращения к ADB. В постоянном режиме Keep Alive также может использовать локальный ADB на порту 5555.");
        ru.put("Select a usage mode for permissions (Button Mapper Accessibility, Continue Watching, and Wi-Fi). Pick ONE usage mode below:", "Выберите режим выдачи разрешений (специальные возможности Button Mapper, продолжение просмотра и Wi-Fi). Ниже выберите ОДИН режим:");
        ru.put("Send Key Code (Remote / Media / System)…", "Отправить код кнопки (пульт / мультимедиа / система)…");
        ru.put("Send any Android KeyEvent keycode without 3rd-party apps", "Отправлять любой код Android KeyEvent без сторонних приложений");
        ru.put("Setting ROLE_HOME and default home activity over Local ADB…", "Настройка ROLE_HOME и главного экрана через локальный ADB…");
        ru.put("Show Clock, Date & Greeting over your chosen wallpaper (press any key to return)", "Показывать часы, дату и приветствие поверх выбранных обоев (нажмите любую кнопку для возврата)");
        ru.put("Sign out of ", "Выйти из ");
        ru.put("Signed out of ", "Выполнен выход из ");
        ru.put("Step 1 complete: Default home launcher set via ADB!", "Шаг 1 завершён: главный экран назначен через ADB!");
        ru.put("Step 1: Set testOS as Default via Local ADB", "Шаг 1: назначить testOS главным экраном через локальный ADB");
        ru.put("Step 2: Disable System Launchers (Google TV / Android TV / OEM)", "Шаг 2: отключить системные лаунчеры (Google TV / Android TV / производителя)");
        ru.put("Step 3: Fix Home Button via Accessibility (Button Mapper)", "Шаг 3: настроить кнопку Home через специальные возможности (Button Mapper)");
        ru.put("Step 4: Open Android System Home Settings", "Шаг 4: открыть системные настройки главного экрана Android");
        ru.put("Switch between Secure Auto Setup, Persistent ADB, or Manual Setup", "Переключаться между безопасной автоматической настройкой, постоянным ADB и ручной настройкой");
        ru.put("Switch to ", "Переключиться на ");
        ru.put("This TV does not have a built-in Accessibility Settings screen. You can enable the Button Mapper & Control Panel service automatically via Local ADB.", "На этом телевизоре нет встроенного экрана специальных возможностей. Включить службу Button Mapper и панели управления можно автоматически через локальный ADB.");
        ru.put("This runs 'settings put global adb_enabled 0' over Local ADB and verifies that port 5555 is closed on your home network.\\n\\n", "Выполняет команду 'settings put global adb_enabled 0' через локальный ADB и проверяет, что порт 5555 закрыт в домашней сети.\\n\\n");
        ru.put("This will disable Keep Alive and remove Accessibility Service, Notification Access, Default Home Role, and launcher permissions (both locally and via ADB).", "Это отключит Keep Alive и отзовёт специальные возможности, доступ к уведомлениям, роль главного экрана и разрешения лаунчера (локально и через ADB).");
        ru.put("Tip: Hold OK on any app on the home screen dock to move or hide it.", "Совет: удерживайте OK на приложении в панели главного экрана, чтобы переместить или скрыть его.");
        ru.put("To sign in, focus the ", "Чтобы войти, выберите ");
        ru.put("Top shelf, 6-column app grid and custom wallpapers", "Верхняя карусель, сетка приложений на 6 столбцов и свои обои");
        ru.put("Turning on ADB debugging for this troubleshooting step…", "Включение отладки ADB для этого шага диагностики…");
        ru.put("Uses local ADB to grant permissions and keeps ADB open", "Выдаёт разрешения через локальный ADB и оставляет его включённым");
        ru.put("View or filter the complete list of supported TV models", "Просмотр и фильтрация полного списка поддерживаемых моделей ТВ");
        ru.put("Volume Fix Mode 1 (Standard) selected.", "Выбран режим исправления громкости 1 (стандартный).");
        ru.put("Volume Fix Mode 2 (HY320 / Projector) selected.", "Выбран режим исправления громкости 2 (HY320 / проектор).");
        ru.put("WRITE_SECURE_SETTINGS and all launcher permissions are now active, and ADB (port 5555) has been closed for your security.\\n\\n", "WRITE_SECURE_SETTINGS и все разрешения лаунчера активированы, а ADB (порт 5555) выключен для вашей безопасности.\\n\\n");
        ru.put("Who\\u2019s Watching?", "Кто смотрит?");
        ru.put("You need the ADB mode enabled for this troubleshooting step. You can always change the ADB mode from Settings later.\\n\\nWould you like to enable ADB now or skip this step?", "Для этого шага диагностики необходимо включить режим ADB. Позже его можно изменить в настройках.\\n\\nВключить ADB сейчас или пропустить этот шаг?");
        ru.put("Your name and an avatar. Each profile keeps its own apps, background and Home Screen. Up to three people can have one.", "Ваше имя и аватар. У каждого профиля свои приложения, фон и главный экран. Можно создать до трёх профилей.");
        ru.put("testOS works best as the home screen. Allow these once; anything this TV doesn't support is skipped.", "testOS лучше всего работает в качестве главного экрана. Разрешите доступ один раз — неподдерживаемые этим ТВ возможности будут пропущены.");
        ru.put("⚠️ Security Warning:\\n\\n", "⚠️ Предупреждение безопасности:\\n\\n");
        ru.put("⚠️ Your Wi-Fi network is currently owned by setupwraith. Before disabling setupwraith, open Wi-Fi Settings, Forget your Wi-Fi network, and re-connect so Wi-Fi stays saved.", "⚠️ Сейчас управление сетью Wi-Fi связано с setupwraith. Перед отключением setupwraith откройте настройки Wi-Fi, забудьте свою сеть и подключитесь к ней заново, чтобы она сохранилась.");
        ru.put("✅ Disable Selected Packages (", "✅ Отключить выбранные пакеты (");
        ru.put("📋 Browse / Search All 341 TV Models", "📋 Просмотреть / найти среди 341 модели ТВ");
        ru.put("📶 Open Wi-Fi Settings…", "📶 Открыть настройки Wi-Fi…");
        ru.put("🔍 Filter system apps…", "🔍 Фильтр системных приложений…");
        ru.put("🔍 Scan Installed System Apps to Find Matching Model", "🔍 Просканировать системные приложения и определить модель");
        ru.put("🔍 Scan Installed System Apps via ADB", "🔍 Просканировать системные приложения через ADB");
        ru.put("🔍 Search / Filter by brand or model…", "🔍 Поиск / фильтр по бренду или модели…");


        ru.put(" [Critical System Service — Protected]", " [Критически важная системная служба — защищено]");
        ru.put(" [Disabled — Tap to Enable]", " [Отключено — нажмите, чтобы включить]");
        ru.put(" [Enabled — Tap to Disable]", " [Включено — нажмите, чтобы отключить]");
        ru.put("% used  \\u00b7  Clean", "% использовано  ·  Очистить");
        ru.put("% used · Recent Apps", "% использовано · Недавние приложения");
        ru.put("ACTIONS", "ДЕЙСТВИЯ");
        ru.put("ACTIVITIES IN ", "ДЕЙСТВИЯ В ");
        ru.put("ALL SYSTEM APPS (", "ВСЕ СИСТЕМНЫЕ ПРИЛОЖЕНИЯ (");
        ru.put("AMBIENT THEMES", "ЦВЕТОВЫЕ ТЕМЫ");
        ru.put("Account", "Учётная запись");
        ru.put("Add a person…", "Добавить профиль…");
        ru.put("Adds a Presentation card to Home", "Добавляет карточку презентации на главный экран");
        ru.put("Advanced ›", "Дополнительно ›");
        ru.put("All (", "Все (");
        ru.put("All (341)", "Все (341)");
        ru.put("All 341 TV Models", "Все 341 модель телевизора");
        ru.put("All button mappings cleared.", "Все назначения кнопок сброшены.");
        ru.put("All permissions granted automatically!", "Все разрешения выданы автоматически!");
        ru.put("Ambient Mode / StandBy", "Режим заставки / ожидания");
        ru.put("Ambient colour themes", "Цветовые темы");
        ru.put("App icon reset to default", "Значок приложения восстановлен");
        ru.put("App icon updated", "Значок приложения обновлён");
        ru.put("App layout reset to default.", "Расположение приложений сброшено.");
        ru.put("App name or package name…", "Название приложения или имя пакета…");
        ru.put("Ask who is watching on wake", "Спрашивать, кто смотрит, при включении");
        ru.put("Auto-launch Lock Screen when idle", "Автоматически включать экран блокировки при простое");
        ru.put("Automatic launch on boot disabled.", "Автозапуск при включении отключён.");
        ru.put("Automatic updates disabled.", "Автоматические обновления отключены.");
        ru.put("Automatic updates enabled.", "Автоматические обновления включены.");
        ru.put("BUILT-IN ACTIONS", "ВСТРОЕННЫЕ ДЕЙСТВИЯ");
        ru.put("BUTTON REMAPPING", "ПЕРЕНАЗНАЧЕНИЕ КНОПОК");
        ru.put("Back to Usage Mode Selection", "Назад к выбору режима работы");
        ru.put("Back to return   ·   Hold OK for options", "Назад — вернуться · Удерживайте OK для настроек");
        ru.put("Button Mapper & Control Panel (Accessibility)", "Button Mapper и панель управления (спец. возможности)");
        ru.put("Button Mapper accessibility service", "Служба специальных возможностей Button Mapper");
        ru.put("CLASSIC WALLPAPERS", "КЛАССИЧЕСКИЕ ОБОИ");
        ru.put("CLOSEST MATCHES (", "НАИБОЛЕЕ ПОДХОДЯЩИЕ МОДЕЛИ (");
        ru.put("CONTROL PANEL ITEMS (SHOW / HIDE)", "ЭЛЕМЕНТЫ ПАНЕЛИ УПРАВЛЕНИЯ (ПОКАЗАТЬ / СКРЫТЬ)");
        ru.put("CUSTOM & FOLDER SHUFFLE", "СВОИ ОБОИ И СЛУЧАЙНАЯ СМЕНА");
        ru.put("Can't open ", "Не удалось открыть ");
        ru.put("Can't switch to ", "Не удалось переключиться на ");
        ru.put("Change Colour", "Изменить цвет");
        ru.put("Change Emoji", "Изменить эмодзи");
        ru.put("Choose Action", "Выберите действие");
        ru.put("Choose Activity", "Выберите экран приложения");
        ru.put("Choose City", "Выберите город");
        ru.put("Choose Lock Screen folder to shuffle", "Выберите папку для смены обоев экрана блокировки");
        ru.put("Choose Lock Screen image", "Выберите изображение экрана блокировки");
        ru.put("Choose TV model…", "Выберите модель телевизора…");
        ru.put("Choose a City…", "Выберите город…");
        ru.put("Choose background folder to shuffle", "Выберите папку для смены фона");
        ru.put("Choose folder…", "Выберите папку…");
        ru.put("Choose testOS when asked", "При запросе выберите testOS");
        ru.put("Choose wallpaper folder to shuffle", "Выберите папку для случайной смены обоев");
        ru.put("City name, e.g. Istanbul", "Название города, например Челябинск");
        ru.put("Classic Top Shelf", "Карусель рекомендаций");
        ru.put("Classic Top Shelf suggestions", "Рекомендации в верхней карусели");
        ru.put("Classic look", "Классический вид");
        ru.put("Classic photo", "Обычная фотография");
        ru.put("Clear Continue Watching history", "Очистить историю просмотра");
        ru.put("Clear all remapped buttons", "Сбросить все переназначения кнопок");
        ru.put("Close ADB (Disable Port 5555)", "Выключить ADB (закрыть порт 5555)");
        ru.put("Closed ", "Закрыто: ");
        ru.put("Closing ADB…", "Выключение ADB…");
        ru.put("Colour", "Цвет");
        ru.put("Continue Watching & DND (Notification Access)", "Продолжение просмотра и «Не беспокоить» (доступ к уведомлениям)");
        ru.put("Continue Watching cleared.", "История просмотра очищена.");
        ru.put("Continue Watching on dock", "Продолжение просмотра в панели приложений");
        ru.put("Continue watching", "Продолжить просмотр");
        ru.put("Control Panel actions and items reset to default.", "Действия и элементы панели управления сброшены.");
        ru.put("Could not load selected image.", "Не удалось загрузить выбранное изображение.");
        ru.put("Couldn't search. Check the internet connection.", "Не удалось выполнить поиск. Проверьте подключение к интернету.");
        ru.put("Custom Lock Screen image updated.", "Изображение экрана блокировки обновлено.");
        ru.put("Custom background updated.", "Свой фон обновлён.");
        ru.put("Custom image", "Своё изображение");
        ru.put("Custom scene wallpaper updated.", "Обои пользовательской сцены обновлены.");
        ru.put("DEFAULT LAUNCHER SETUP (TCL / PHILIPS / GOOGLE TV)", "НАСТРОЙКА ГЛАВНОГО ЭКРАНА (TCL / PHILIPS / GOOGLE TV)");
        ru.put("DOCK", "ПАНЕЛЬ ПРИЛОЖЕНИЙ");
        ru.put("DOCK SCALE (-50% TO +50%)", "РАЗМЕР ПАНЕЛИ (-50% ДО +50%)");
        ru.put("Debloat TV — ", "Очистка ТВ — ");
        ru.put("Default", "По умолчанию");
        ru.put("Default (Ambient Mode)", "По умолчанию (режим заставки)");
        ru.put("Default (Automatic)", "По умолчанию (автоматически)");
        ru.put("Default App Launch (Main Activity)", "Запуск приложения по умолчанию (главный экран)");
        ru.put("Default Home Screen", "Главный экран по умолчанию");
        ru.put("Delete “", "Удалить «");
        ru.put("Disable All Stock Launchers & Set testOS as Home", "Отключить штатные лаунчеры и назначить testOS главным экраном");
        ru.put("Disable Anyway", "Всё равно отключить");
        ru.put("Disable all ›", "Отключить все ›");
        ru.put("Disable system launchers…", "Отключить системные лаунчеры…");
        ru.put("Disabled", "Отключено");
        ru.put("Dock size", "Размер панели приложений");
        ru.put("Dock size set to ", "Размер панели приложений: ");
        ru.put("Each wallpaper stays", "Длительность показа обоев");
        ru.put("Enable Button Mapper in Accessibility…", "Включить Button Mapper в специальных возможностях…");
        ru.put("Enable Keep Alive", "Включить Keep Alive");
        ru.put("Enable Keep Alive?", "Включить Keep Alive?");
        ru.put("Enable all ›", "Включить все ›");
        ru.put("Enable via ADB", "Включить через ADB");
        ru.put("Enabling ADB debugging…", "Включение отладки ADB…");
        ru.put("Enabling ADB…", "Включение ADB…");
        ru.put("FORECAST PROVIDER", "ИСТОЧНИК ПРОГНОЗА");
        ru.put("Filter System Apps", "Фильтр системных приложений");
        ru.put("Filter TV Models", "Фильтр моделей телевизоров");
        ru.put("Filter ›", "Фильтр ›");
        ru.put("Fix Home button (Launch testOS)", "Исправить кнопку Home (запуск testOS)");
        ru.put("Folder shuffle", "Случайная смена изображений из папки");
        ru.put("Found from your internet connection", "Определено по интернет-подключению");
        ru.put("Free forecast service, no account needed", "Бесплатный прогноз погоды, регистрация не нужна");
        ru.put("GOOGLE TV WI-FI NOTICE", "ВАЖНО: WI-FI НА GOOGLE TV");
        ru.put("Grant Permissions & Close ADB Now", "Выдать разрешения и выключить ADB");
        ru.put("Grant Permissions Now (Keep ADB Open)", "Выдать разрешения (оставить ADB включённым)");
        ru.put("Home button fix disabled.", "Исправление кнопки Home отключено.");
        ru.put("Home button fix enabled.", "Исправление кнопки Home включено.");
        ru.put("Home screen weather shortcut", "Погода на главном экране");
        ru.put("I Understand — Open Manual Debloat", "Понятно — открыть ручную очистку");
        ru.put("INSTALLED SYSTEM LAUNCHERS ON THIS TV (", "УСТАНОВЛЕННЫЕ СИСТЕМНЫЕ ЛАУНЧЕРЫ НА ЭТОМ ТВ (");
        ru.put("Included in rotation", "Участвует в смене");
        ru.put("Just your background for now", "Пока только ваш фон");
        ru.put("LOCK SCREEN OPTIONS", "НАСТРОЙКИ ЭКРАНА БЛОКИРОВКИ");
        ru.put("Larger dock — extra apps automatically move to Other apps if needed", "Увеличенная панель — лишние приложения автоматически перемещаются в «Другие приложения»");
        ru.put("Let’s set up testOS.", "Давайте настроим testOS.");
        ru.put("Lock screen settings", "Настройки экрана блокировки");
        ru.put("Lock screen settings…", "Настройки экрана блокировки…");
        ru.put("MATCHING MODELS FOR ", "ПОДХОДЯЩИЕ МОДЕЛИ ДЛЯ ");
        ru.put("MODELS (", "МОДЕЛИ (");
        ru.put("Make Home Screen", "Назначить главным экраном");
        ru.put("Make testOS Default Home", "Назначить testOS главным экраном");
        ru.put("Make testOS your home screen?", "Сделать testOS главным экраном?");
        ru.put("Manual debloat (All system apps)…", "Ручная очистка (все системные приложения)…");
        ru.put("Mode: Manual Setup (Without ADB)", "Режим: ручная настройка (без ADB)");
        ru.put("Mode: Persistent ADB Setup (Keep ADB Open)", "Режим: постоянный ADB (оставить включённым)");
        ru.put("Mode: Secure Auto Setup & Close ADB (Recommended)", "Режим: безопасная настройка и выключение ADB (рекомендуется)");
        ru.put("Models by System App Similarity", "Модели по сходству системных приложений");
        ru.put("NIGHT SCENES", "НОЧНЫЕ СЦЕНЫ");
        ru.put("New Profile", "Новый профиль");
        ru.put("No disabled packages have been recorded by CleanYourTV yet.", "CleanYourTV пока не сохранил список отключённых пакетов.");
        ru.put("No stock Android TV / Google TV / OEM launcher packages were detected on this TV.", "Системные лаунчеры Android TV / Google TV / производителя не обнаружены.");
        ru.put("None (No action)", "Нет (без действия)");
        ru.put("Not allowed", "Не разрешено");
        ru.put("OK to switch or edit", "Нажмите OK для переключения или изменения");
        ru.put("ONE-TAP ACTIONS", "ДЕЙСТВИЯ В ОДНО КАСАНИЕ");
        ru.put("OPTIONS", "ПАРАМЕТРЫ");
        ru.put("Off — Tap to enable Home button remap", "Выключено — нажмите, чтобы переназначить кнопку Home");
        ru.put("One wallpaper has to stay on.", "Должны быть включены хотя бы одни обои.");
        ru.put("Open Developer Options / TV Settings…", "Открыть параметры разработчика / настройки ТВ…");
        ru.put("Open Developer Options and turn on USB Debugging / Network Debugging.", "Откройте параметры разработчика и включите отладку по USB / сети.");
        ru.put("Open TV Ambient Mode / Screensaver (or built-in fallback)", "Открыть режим заставки / экранную заставку ТВ");
        ru.put("Open system selector (if supported by TV) ›", "Открыть системный выбор (если поддерживается ТВ) ›");
        ru.put("PAGE NAVIGATION", "НАВИГАЦИЯ ПО СТРАНИЦАМ");
        ru.put("PEOPLE", "ПРОФИЛИ");
        ru.put("PERMISSION STATUS", "СОСТОЯНИЕ РАЗРЕШЕНИЙ");
        ru.put("Pairing code\\n", "Код сопряжения\\n");
        ru.put("Play as soon as the TV turns on (signage)", "Запускать воспроизведение сразу после включения ТВ (информационный экран)");
        ru.put("QUICK BRAND FILTERS", "БЫСТРЫЕ ФИЛЬТРЫ ПО БРЕНДУ");
        ru.put("REMAPPED BUTTONS (1X / 2X / 3X TAP)", "ПЕРЕНАЗНАЧЕННЫЕ КНОПКИ (1 / 2 / 3 НАЖАТИЯ)");
        ru.put("REMOVE", "УДАЛИТЬ");
        ru.put("RESTORE OPTIONS", "ПАРАМЕТРЫ ВОССТАНОВЛЕНИЯ");
        ru.put("Remember what you play in each app", "Запоминать, что вы смотрите в каждом приложении");
        ru.put("Remove / Revoke All Permissions", "Удалить / отозвать все разрешения");
        ru.put("Remove from Keep Watching", "Убрать из «Продолжить просмотр»");
        ru.put("Reset Layout", "Сбросить расположение");
        ru.put("Reset app order & unhide all", "Сбросить порядок приложений и показать все");
        ru.put("Restore debloated apps", "Восстановить отключённые приложения");
        ru.put("Resume playback", "Продолжить воспроизведение");
        ru.put("Revoke ›", "Отозвать ›");
        ru.put("Revoking Permissions…", "Отзыв разрешений…");
        ru.put("Revoking all permissions…", "Отзыв всех разрешений…");
        ru.put("Rotating nature scenes with a glass clock and bottom dock", "Сменяющиеся природные сцены, стеклянные часы и нижняя панель приложений");
        ru.put("Run ROLE_HOME & set-home-activity ›", "Выполнить ROLE_HOME и set-home-activity ›");
        ru.put("Run via ADB ›", "Выполнить через ADB ›");
        ru.put("Scanning installed system packages over ADB and comparing across 341 TV models…", "Сканирование системных пакетов через ADB и сравнение с 341 моделью ТВ…");
        ru.put("Select All / Select None", "Выбрать всё / снять выбор");
        ru.put("Select at least one package first.", "Сначала выберите хотя бы один пакет.");
        ru.put("Selected model: ", "Выбранная модель: ");
        ru.put("Send to this TV", "Отправить на этот телевизор");
        ru.put("Setting Default Launcher…", "Назначение главного экрана…");
        ru.put("Show Continue Watching on Home", "Показывать «Продолжить просмотр» на главном экране");
        ru.put("Show the Wi-Fi Name", "Показывать название Wi-Fi");
        ru.put("Sign out", "Выйти");
        ru.put("Signed in · Sign out", "Вход выполнен · Выйти");
        ru.put("Skip This Step", "Пропустить этот шаг");
        ru.put("Smaller, more compact dock", "Меньшая и компактная панель приложений");
        ru.put("Standard Apple tvOS dock size", "Стандартный размер панели Apple tvOS");
        ru.put("Start with the TV", "Запускать вместе с телевизором");
        ru.put("System Launchers", "Системные лаунчеры");
        ru.put("TAP ACTIONS", "ДЕЙСТВИЯ ПО НАЖАТИЮ");
        ru.put("TARGET ACTION / APPLICATION", "ДЕЙСТВИЕ / ПРИЛОЖЕНИЕ");
        ru.put("TOP SHELF CONTENT", "СОДЕРЖИМОЕ ВЕРХНЕЙ КАРУСЕЛИ");
        ru.put("The Home button on the remote will open testOS.", "Кнопка Home на пульте будет открывать testOS.");
        ru.put("The Home button opens testOS", "Кнопка Home открывает testOS");
        ru.put("This TV has no inputs", "На этом телевизоре нет доступных входов");
        ru.put("Turn off Custom image", "Выключить своё изображение");
        ru.put("Turn off Folder shuffle", "Выключить случайную смену изображений из папки");
        ru.put("Turn on Custom image", "Включить своё изображение");
        ru.put("Turn on Folder shuffle", "Включить случайную смену изображений из папки");
        ru.put("Turn on Presentation mode first", "Сначала включите режим презентации");
        ru.put("Turning On ADB…", "Включение ADB…");
        ru.put("Use built-in Android TV shortcut", "Использовать встроенную команду Android TV");
        ru.put("Use folder shuffle background", "Использовать случайную смену фона из папки");
        ru.put("Volume Button Fix Mode", "Режим исправления кнопок громкости");
        ru.put("Volume button fix mode", "Режим исправления кнопок громкости");
        ru.put("Watching now · OK to switch or rename", "Сейчас смотрит · OK для переключения или переименования");
        ru.put("What appears at the top of Home, behind your apps. Choose any, all or none.", "Что показывать в верхней части главного экрана за приложениями. Выберите нужные элементы.");
        ru.put("What you were watching, mixed into the carousel", "Недавно просмотренное в общей карусели");
        ru.put("Who’s Watching?", "Кто смотрит?");
        ru.put("You can also configure buttons later in Settings", "Позже кнопки можно настроить в разделе «Настройки»");
        ru.put("m", "м");
        ru.put("↻ Refresh system app list", "↻ Обновить список системных приложений");
        ru.put("🛑 Critical System Service", "🛑 Критически важная системная служба");

    }

    private I18n() { }

    public static String detectSystemLang(Context c) { return LANG_RU; }
    public static void resetCache() { cachedLang = null; }

    public static String getLang(Context c) {
        if (cachedLang != null) return cachedLang;
        if (c != null) {
            android.content.SharedPreferences p = c.getSharedPreferences("settings", Context.MODE_PRIVATE);
            if (!LANG_RU.equals(p.getString("lang", null)) || !p.getBoolean("lang_user_selected", false)) {
                p.edit().putString("lang", LANG_RU).putBoolean("lang_user_selected", true).apply();
            }
        }
        cachedLang = LANG_RU;
        return cachedLang;
    }

    public static boolean isDefault24hForLang(String lang) { return true; }

    public static void setLang(Context c, String lang) {
        cachedLang = LANG_RU;
        if (c != null) c.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit().putString("lang", LANG_RU).putBoolean("lang_user_selected", true).apply();
        if (c != null) Scenes.setClock24h(c, true);
    }

    public static boolean isTr(Context c) { return false; }
    public static Locale locale(Context c) { return Locale.forLanguageTag("ru-RU"); }
    public static String s(Context c, String en, String ignored) { return translateOther(LANG_RU, en); }
    public static String t(Context c, String enKey) { return translateOther(LANG_RU, enKey); }

    private static String translateOther(String lang, String en) {
        if (en == null) return "";
        Map<String, String> map = LANG_MAPS.get(LANG_RU);
        String direct = map != null ? map.get(en) : null;
        if (direct != null) return direct;
        // Dynamic labels shared by launcher screens and dialogs.
        if (en.startsWith("You are up to date (v") && en.endsWith(")")) {
            return "У вас установлена последняя версия (v"
                    + en.substring("You are up to date (v".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("Changelog — v")) return "Список изменений — v" + en.substring("Changelog — v".length());
        if (en.startsWith("Downloading update v") && en.endsWith("…")) {
            return "Загружается обновление v" + en.substring("Downloading update v".length());
        }
        if (en.startsWith("RAM ") && en.contains(" used of ")) {
            int split = en.indexOf(" used of ");
            return "ОЗУ: " + en.substring("RAM ".length(), split) + " из "
                    + en.substring(split + " used of ".length());
        }
        if (en.startsWith("Closed ") && en.contains(" app")) {
            int appEnd = en.indexOf(" app");
            String count = en.substring("Closed ".length(), appEnd);
            String result = "Закрыто приложений: " + count;
            int freedAt = en.indexOf(" · freed ", appEnd);
            if (freedAt >= 0 && en.endsWith(" MB")) {
                result += " · освобождено " + en.substring(freedAt + " · freed ".length(),
                        en.length() - " MB".length()) + " МБ";
            }
            return result;
        }
        if (en.startsWith("Move ") && en.endsWith("  ·  Left/Right, OK when done")) {
            return "Переместить " + en.substring("Move ".length(),
                    en.length() - "  ·  Left/Right, OK when done".length())
                    + " · влево/вправо, OK — готово";
        }
        if (en.startsWith("Off in ") && en.endsWith(" min")) {
            return "Выключение через " + en.substring("Off in ".length(), en.length() - " min".length()) + " мин";
        }
        if (en.endsWith(" is not installed on this TV.")) {
            return "Приложение " + en.substring(0, en.length() - " is not installed on this TV.".length())
                    + " не установлено на этом телевизоре";
        }
        if (en.endsWith(" is not installed on this TV")) {
            return en.substring(0, en.length() - " is not installed on this TV".length())
                    + " не установлено на этом телевизоре";
        }
        if (en.startsWith("Open in ")) return "Открыть в " + en.substring("Open in ".length());
        if (en.startsWith("▶  Open in ")) return "▶  Открыть в " + en.substring("▶  Open in ".length());
        if (en.startsWith("Get ")) return "Скачать " + en.substring("Get ".length());
        if (en.startsWith("Sign in to ")) return "Войти в " + en.substring("Sign in to ".length());
        if (en.startsWith("Couldn't reach ") && en.endsWith(". Focus the tile again to retry.")) {
            return "Не удалось связаться с " + en.substring("Couldn't reach ".length(),
                    en.length() - ". Focus the tile again to retry.".length())
                    + ". Чтобы повторить попытку, снова выделите плитку.";
        }
        if (en.startsWith("Couldn't reach ")) return "Не удалось связаться с " + en.substring("Couldn't reach ".length());
        if (en.startsWith("Can't open ")) return "Не удалось открыть " + en.substring("Can't open ".length());
        if (en.endsWith(" moved to Other Apps")) {
            return "Перемещено в «Другие приложения»: "
                    + en.substring(0, en.length() - " moved to Other Apps".length());
        }
        if (en.endsWith(" is back on Home")) {
            return "Возвращено на главный экран: " + en.substring(0, en.length() - " is back on Home".length());
        }
        if (en.endsWith(" hidden (Settings > Apps > Hidden apps)")) {
            return "Скрыто: " + en.substring(0, en.length() - " hidden (Settings > Apps > Hidden apps)".length())
                    + " (Настройки > Приложения > Скрытые приложения)";
        }
        if (en.startsWith("Can't switch to ")) {
            return "Не удалось переключиться на " + en.substring("Can't switch to ".length());
        }
        if (en.startsWith("No image files (") && en.endsWith(") in this folder")) {
            return "В этой папке нет файлов изображений ("
                    + en.substring("No image files (".length(), en.length() - ") in this folder".length()) + ")";
        }
        if (en.startsWith("Select this folder & shuffle (") && en.endsWith(" images)")) {
            return "Выбрать эту папку и менять изображения (" + en.substring("Select this folder & shuffle (".length(),
                    en.length() - " images)".length()) + " изображений)";
        }
        if (en.startsWith("Your current tvOS Launcher settings have been saved to:\n\n")) {
            return "Текущие настройки tvOS Launcher сохранены по адресу:\n\n"
                    + en.substring("Your current tvOS Launcher settings have been saved to:\n\n".length());
        }
        if (en.startsWith("Could not write to Download/") && en.endsWith("Please allow storage access and try again.")) {
            String detail = en.substring("Could not write to ".length(),
                    en.length() - "\n\nPlease allow storage access and try again.".length());
            return "Не удалось записать файл:\n" + detail + "\n\nРазрешите доступ к хранилищу и повторите попытку.";
        }
        if (en.startsWith("Restore from Download/")) return "Восстановить из Download/" + en.substring("Restore from Download/".length());
        if (en.startsWith("Restore from this folder (") && en.endsWith(")")) {
            return "Восстановить из этой папки (" + en.substring("Restore from this folder (".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("Settings were successfully restored from:\n")) {
            return "Настройки успешно восстановлены из файла:\n"
                    + en.substring("Settings were successfully restored from:\n".length());
        }
        if (en.startsWith("Could not restore settings from:\n")) {
            return "Не удалось восстановить настройки из файла:\n"
                    + en.substring("Could not restore settings from:\n".length());
        }
        // Dynamic CleanYourTV model-matching, debloat and restore messages.
        if (en.startsWith("Model: ")) return "Модель: " + en.substring("Model: ".length());
        if (en.startsWith("Product: ")) return "Продукт: " + en.substring("Product: ".length());
        if (en.startsWith("Board/Device: ")) return "Плата / устройство: " + en.substring("Board/Device: ".length());
        if (en.startsWith("Brand/OEM: ")) return "Бренд / производитель: " + en.substring("Brand/OEM: ".length());
        if (en.contains(" OEM pkgs: ")) {
            int split = en.indexOf(" OEM pkgs: ");
            return en.substring(0, split) + " пакетов производителя: " + en.substring(split + " OEM pkgs: ".length());
        }
        if (en.startsWith("OEM family: ")) return "Семейство производителя: " + en.substring("OEM family: ".length());
        if (en.contains(" apps (") && en.endsWith(")")) {
            int split = en.indexOf(" apps (");
            return en.substring(0, split) + " приложений (" + en.substring(split + " apps (".length());
        }
        if (en.startsWith("Exact TV model found and selected automatically:\n\n• ")) {
            return "Точная модель телевизора найдена и выбрана автоматически:\n\n• "
                    + en.substring("Exact TV model found and selected automatically:\n\n• ".length());
        }
        if (en.startsWith("Choose Closest TV Model — ")) {
            return "Выберите наиболее подходящую модель ТВ — " + en.substring("Choose Closest TV Model — ".length());
        }
        if (en.startsWith("Models with Similar System Apps (") && en.endsWith(" apps scanned)")) {
            return "Модели с похожими системными приложениями ("
                    + en.substring("Models with Similar System Apps (".length(), en.length() - " apps scanned)".length())
                    + " приложений просканировано)";
        }
        if (en.startsWith("All TV Models (") && en.endsWith(")")) {
            return "Все модели телевизоров (" + en.substring("All TV Models (".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("Search: \"") && en.endsWith(")")) {
            int split = en.lastIndexOf("\" (");
            if (split > 7) return "Поиск: " + en.substring("Search: ".length(), split + 1)
                    + " (" + en.substring(split + 3);
        }
        if (en.startsWith("All disabled (") && en.endsWith(")")) {
            return "Все отключены (" + en.substring("All disabled (".length(), en.length() - 1) + ")";
        }
        if (en.contains(" active / ") && en.endsWith(" found ›")) {
            int split = en.indexOf(" active / ");
            return en.substring(0, split) + " активно / "
                    + en.substring(split + " active / ".length(), en.length() - " found ›".length()) + " найдено ›";
        }
        if (en.startsWith(" is a protected core system package and cannot be disabled automatically.")) {
            return en.substring(0, en.length() - " is a protected core system package and cannot be disabled automatically.".length())
                    + " — защищённый системный пакет, его нельзя отключить автоматически.";
        }
        String protectedCoreSuffix = " is a protected core system package and cannot be disabled automatically.";
        if (en.endsWith(protectedCoreSuffix)) {
            return en.substring(0, en.length() - protectedCoreSuffix.length())
                    + " — защищённый системный пакет, его нельзя отключить автоматически.";
        }
        if (en.startsWith("The command was sent, but Android did not confirm that ") && en.endsWith("\nThe restore list was not changed.")) {
            String body = en.substring("The command was sent, but Android did not confirm that ".length(),
                    en.length() - "\nThe restore list was not changed.".length());
            body = body.replace(" is disabled.", " отключён.")
                    .replace(" is enabled.", " включён.");
            return "Команда отправлена, но Android не подтвердил, что пакет " + body
                    + "\nСписок восстановления не изменён.";
        }
        if (en.startsWith("Checking installed packages and Wi-Fi status over ADB for ") && en.endsWith("…")) {
            return "Проверяем установленные пакеты и состояние Wi-Fi через ADB для "
                    + en.substring("Checking installed packages and Wi-Fi status over ADB for ".length());
        }
        if (en.startsWith("On Google TV sets, Wi-Fi networks saved during initial setup belong to com.google.android.tungsten.setupwraith.")) {
            return "На телевизорах Google TV сети Wi-Fi, сохранённые при первоначальной настройке, принадлежат пакету com.google.android.tungsten.setupwraith. "
                    + "Отключение setupwraith, необходимое для блокировки штатного лаунчера Google TV, удалит сохранённую этим пакетом сеть Wi-Fi.\n\n"
                    + "Перед отключением setupwraith:\n"
                    + "1. Откройте настройки Wi-Fi, выберите свою сеть и нажмите «Забыть сеть».\n"
                    + "2. Подключитесь к сети заново (теперь она будет сохранена пакетом com.android.tv.settings).\n"
                    + "3. Вернитесь сюда и нажмите «Я забыл сеть и подключился снова — продолжить».";
        }
        if (en.startsWith("⚠️ Detected: Your current Wi-Fi network IS owned by setupwraith")
                || en.startsWith("ℹ️ Google TV Notice (setupwraith Wi-Fi check):")) {
            String status = en.startsWith("⚠️")
                    ? "⚠️ Обнаружено: текущая сеть Wi-Fi принадлежит setupwraith (cname=com.google.android.tungsten.setupwraith).\n\n"
                    : "ℹ️ Предупреждение Google TV (проверка Wi-Fi setupwraith):\n\n";
            return status
                    + "На телевизорах Google TV сети Wi-Fi, сохранённые при первоначальной настройке, принадлежат пакету com.google.android.tungsten.setupwraith. "
                    + "Отключение setupwraith, необходимое для блокировки штатного лаунчера Google TV, удалит сохранённую этим пакетом сеть Wi-Fi.\n\n"
                    + "Перед отключением setupwraith:\n"
                    + "1. Откройте настройки Wi-Fi, выберите свою сеть и нажмите «Забыть сеть».\n"
                    + "2. Подключитесь к сети заново (теперь она будет сохранена пакетом com.android.tv.settings).\n"
                    + "3. Вернитесь сюда и нажмите «Я забыл сеть и подключился снова — продолжить».";
        }
        if (en.startsWith("None of the packages listed in ") && en.contains(" are installed on this TV.\n\n")
                && en.endsWith("closest matching TV model?")) {
            int split = en.indexOf(" are installed on this TV.\n\n");
            String model = en.substring("None of the packages listed in ".length(), split);
            return "На этом телевизоре не установлено ни одного пакета из списка " + model + ".\n\n"
                    + "Просканировать установленные приложения через ADB, чтобы найти наиболее подходящую модель ТВ?";
        }
        if (en.startsWith("Select Apps to Disable — ") && en.endsWith(" found)")) {
            return "Выберите приложения для отключения — "
                    + en.substring("Select Apps to Disable — ".length(), en.length() - " found)".length()) + " найдено)";
        }
        if (en.startsWith("Starting ADB debloat for ") && en.endsWith(" packages…")) {
            return "Запускаем отключение через ADB для "
                    + en.substring("Starting ADB debloat for ".length(), en.length() - " packages…".length())
                    + " пакетов…";
        }
        if (en.startsWith("Disabling (") && en.contains("):\n")) {
            return "Отключение (" + en.substring("Disabling (".length());
        }
        if (en.startsWith("Verified disabled: ")) {
            String[] parts = en.split("\n");
            if (parts.length >= 3) {
                String first = parts[0].substring("Verified disabled: ".length()).replace(" of ", " из ")
                        .replace(" packages for ", " пакетов для ");
                String second = parts[1].replace("Failed or unverified: ", "Не удалось подтвердить: ");
                String third = parts[2].replace("Saved restore list: ", "Сохранено в списке восстановления: ")
                        .replace(" packages.", " пакетов.");
                return "Отключение подтверждено: " + first + ".\n" + second + "\n" + third;
            }
        }
        if (en.startsWith("No disabled app list has been saved by CleanYourTV yet.")) {
            return "CleanYourTV пока не сохранил список отключённых приложений.\n\n"
                    + "После запуска очистки каждый отключённый пакет будет записан здесь. Вы сможете проверить список, включить отдельные приложения или восстановить их все.";
        }
        if (en.startsWith("Disabled Apps (") && en.endsWith(" — Tap an app to re-enable)")) {
            return "Отключённые приложения (" + en.substring("Disabled Apps (".length(),
                    en.length() - " — Tap an app to re-enable)".length()) + " — нажмите на приложение, чтобы включить его)";
        }
        if (en.startsWith("Found saved disabled-app data (") && en.endsWith(" saved packages over ADB?")) {
            String count = en.substring("Found saved disabled-app data (".length(),
                    en.indexOf(" packages)."));
            return "Найдены сохранённые данные об отключённых приложениях (" + count + " пакетов).\n\n"
                    + "Повторно включить все " + count + " сохранённых пакетов через ADB?";
        }
        if (en.startsWith("Restore Saved (") && en.endsWith(")")) {
            return "Восстановить сохранённые (" + en.substring("Restore Saved (".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("Restore Model List (") && en.endsWith(")")) {
            return "Восстановить список модели (" + en.substring("Restore Model List (".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("No saved disabled-app backup was found, and no TV model is selected.")) {
            return "Резервная копия отключённых приложений не найдена, модель телевизора не выбрана.\n\n"
                    + "Сначала выберите модель телевизора, чтобы определить, какие пакеты нужно включить.";
        }
        if (en.startsWith("No saved disabled-app list was found.\n\n")) {
            String prefix = "Restore will use your selected TV model (";
            int start = en.indexOf(prefix);
            int end = en.indexOf(") and re-enable its ", start + prefix.length());
            int countEnd = en.indexOf(" packages over ADB.", end);
            if (start >= 0 && end > start && countEnd > end) {
                return "Сохранённый список отключённых приложений не найден.\n\n"
                        + "Для восстановления будет использована выбранная модель телевизора ("
                        + en.substring(start + prefix.length(), end) + "). Будут повторно включены "
                        + en.substring(end + ") and re-enable its ".length(), countEnd) + " пакетов через ADB.";
            }
        }
        if (en.startsWith("Restore (") && en.endsWith(" pkgs)")) {
            return "Восстановить (" + en.substring("Restore (".length(), en.length() - " pkgs)".length()) + " пакетов)";
        }
        if (en.startsWith("Re-enabling ") && en.endsWith(" packages over ADB…")) {
            return "Повторно включаем через ADB "
                    + en.substring("Re-enabling ".length(), en.length() - " packages over ADB…".length()) + " пакетов…";
        }
        if (en.startsWith("Enabling (") && en.contains("):\n")) {
            return "Включение (" + en.substring("Enabling (".length());
        }
        if (en.startsWith("Verified enabled: ")) {
            String result = en.replace("Verified enabled: ", "Включение подтверждено: ")
                    .replace(". Failed or unverified: ", ". Не удалось подтвердить: ")
                    .replace(". Remaining in restore list: ", ". Осталось в списке восстановления: ");
            return result;
        }
        if (en.startsWith("1. Open TV Settings → System → About and click Build 7 times to unlock Developer Options.")) {
            String ending = en.contains("(ADB port 5555 will be closed automatically right after setup)")
                    ? "(порт ADB 5555 будет автоматически закрыт сразу после настройки)."
                    : "(ADB останется включённым для CleanYourTV).";
            return "1. Откройте «Настройки ТВ → Система → Об устройстве» и нажмите на номер сборки 7 раз, чтобы открыть параметры разработчика.\n"
                    + "2. Откройте «Настройки → Система → Параметры разработчика» и включите отладку по USB / сети.\n"
                    + "3. Вернитесь сюда и выберите «Выдать разрешения сейчас» " + ending;
        }
        if (en.startsWith("WRITE_SECURE_SETTINGS and all launcher permissions are now active, and ADB (port 5555) has been closed for your security.")) {
            return "WRITE_SECURE_SETTINGS и все разрешения лаунчера активны. Для вашей безопасности ADB (порт 5555) закрыт.\n\n"
                    + "Совет: для дополнительной защиты можно также выбрать «Отозвать разрешения на отладку по USB» в параметрах разработчика.";
        }
        // Dynamic SettingsActivity messages. The legacy second argument to tr()
        // is Turkish, so dynamic English expressions must be translated here.
        if (en.endsWith(" restored to Home Screen.")) {
            return en.substring(0, en.length() - " restored to Home Screen.".length())
                    + " восстановлено на главном экране.";
        }
        if (en.startsWith("To sign in, focus the ") && en.endsWith(" tile on the Home screen and scan the QR code.")) {
            String app = en.substring("To sign in, focus the ".length(),
                    en.length() - " tile on the Home screen and scan the QR code.".length());
            return "Чтобы войти, выделите плитку " + app + " на главном экране и отсканируйте QR-код.";
        }
        if (en.startsWith("Sign out of ") && en.endsWith("?")) {
            String app = en.substring("Sign out of ".length(), en.length() - 1);
            return "Выйти из аккаунта " + app + "?";
        }
        if (en.startsWith("Continue watching on the dock will stop showing your ")) {
            String prefix = "Continue watching on the dock will stop showing your ";
            String middle = " titles. The ";
            String suffix = " app itself stays signed in.";
            int split = en.indexOf(middle, prefix.length());
            if (split > prefix.length() && en.endsWith(suffix)) {
                String app = en.substring(prefix.length(), split);
                String appAgain = en.substring(split + middle.length(), en.length() - suffix.length());
                if (app.equals(appAgain)) {
                    return "Раздел «Продолжить просмотр» на панели больше не будет показывать материалы " + app
                            + ". В самом приложении " + app + " вход останется выполнен.";
                }
            }
        }
        if (en.startsWith("Signed out of ") && en.endsWith(".")) {
            return "Выполнен выход из аккаунта " + en.substring("Signed out of ".length(), en.length() - 1) + ".";
        }
        String criticalSuffix = " is a critical system service required for your TV remote and Bluetooth connectivity.\n\nIt cannot be disabled under any circumstances.";
        if (en.endsWith(criticalSuffix)) {
            return en.substring(0, en.length() - criticalSuffix.length())
                    + " — критическая системная служба, необходимая для работы пульта и Bluetooth.\n\nОтключать её нельзя ни при каких обстоятельствах.";
        }
        String protectedSuffix = " is protected by testOS because it may be required for Android, TV input, Device Owner, or launcher recovery. It cannot be disabled from Manual Debloat.";
        if (en.endsWith(protectedSuffix)) {
            return en.substring(0, en.length() - protectedSuffix.length())
                    + " защищён testOS, поскольку может быть необходим для Android, ТВ-входов, Device Owner или восстановления лаунчера. Отключение в ручном режиме запрещено.";
        }
        String manualDisablePrefix = "You are about to disable ";
        String manualDisableSuffix = ".\n\nThis is a manual operation. It may affect TV features. Continue only if you know what this package does.";
        if (en.startsWith(manualDisablePrefix) && en.endsWith(manualDisableSuffix)) {
            return "Вы собираетесь отключить " + en.substring(manualDisablePrefix.length(),
                    en.length() - manualDisableSuffix.length())
                    + ".\n\nЭто действие выполняется вручную и может повлиять на функции телевизора. Продолжайте, только если знаете назначение этого пакета.";
        }
        if (en.startsWith("This runs 'settings put global adb_enabled 0'")) {
            return "Команда 'settings put global adb_enabled 0' будет выполнена через локальный ADB, после чего проверим, закрыт ли порт 5555.\n\n"
                    + "Если WRITE_SECURE_SETTINGS уже выдано, testOS продолжит восстанавливать специальные возможности непосредственно в приложении — ADB для этого не потребуется.\n\n"
                    + "Для дополнительной безопасности можно также открыть параметры разработчика и выбрать «Отозвать разрешения на отладку по USB».";
        }
        if (en.startsWith("Keep Alive monitors the Accessibility Service and automatically restores it")) {
            return "Keep Alive следит за службой специальных возможностей и автоматически восстанавливает её, если телевизор отключает её.\n\n"
                    + "✓ Поскольку этому телевизору выдано WRITE_SECURE_SETTINGS, Keep Alive восстанавливает службу непосредственно в приложении. Сетевой ADB (порт 5555) оставлять включённым не нужно.";
        }
        if (en.startsWith("⚠️ Security Warning:")) {
            return "⚠️ Предупреждение безопасности:\n\n"
                    + "Keep Alive отслеживает разрешения и восстанавливает их, если телевизор отключает их в фоновом режиме.\n\n"
                    + "Без WRITE_SECURE_SETTINGS для работы Keep Alive должна оставаться включённой сетевая отладка (ADB на порту 5555) в домашней сети. Для большей безопасности сначала выполните «Подключить / восстановить ADB» в безопасном режиме, чтобы выдать WRITE_SECURE_SETTINGS и закрыть порт 5555.\n\n";
        }
        if (en.startsWith("WARNING: Manual Debloat lists ALL system apps installed on your TV.")) {
            return "ПРЕДУПРЕЖДЕНИЕ: в ручном режиме отображаются ВСЕ системные приложения телевизора.\n\n"
                    + "• Отключайте пакеты, только если точно знаете их назначение.\n"
                    + "• Отключение основных служб Android, служб ТВ-входов / тюнера или настроек может нарушить работу HDMI, Wi-Fi, пульта или привести к циклической перезагрузке.\n\n"
                    + "Продолжить на свой риск?";
        }
        if (en.startsWith("Dock size set to ") && en.endsWith("%.")) {
            return "Размер панели установлен: " + en.substring("Dock size set to ".length());
        }
        if (en.startsWith("Removed remap for ") && en.endsWith(".")) {
            return "Удалено переназначение кнопки " + en.substring("Removed remap for ".length(), en.length() - 1) + ".";
        }
        if (en.startsWith("No direct model name match for ") && en.endsWith(" Use Scan Installed System Apps via ADB above to find the closest TV profile.")) {
            String prefix = "No direct model name match for ";
            String suffix = " Use Scan Installed System Apps via ADB above to find the closest TV profile.";
            return "Точное совпадение по названию модели не найдено: "
                    + en.substring(prefix.length(), en.length() - suffix.length())
                    + ". Используйте расположенную выше команду «Сканировать системные приложения через ADB», чтобы подобрать ближайший профиль телевизора.";
        }
        if (en.startsWith("Selected model: ")) return "Выбрана модель: " + en.substring("Selected model: ".length());
        if (en.startsWith("CLOSEST MATCHES (") && en.endsWith(" APPS SCANNED)")) {
            return "БЛИЖАЙШИЕ СОВПАДЕНИЯ (" + en.substring("CLOSEST MATCHES (".length(), en.length() - " APPS SCANNED)".length()) + " ПРИЛОЖЕНИЙ ПРОВЕРЕНО)";
        }
        if (en.startsWith("PAGE ") && en.contains(" OF ")) {
            return en.replace("PAGE ", "СТРАНИЦА ").replace(" OF ", " ИЗ ");
        }
        if (en.startsWith("MODELS (") && en.endsWith(")")) {
            return "МОДЕЛИ (" + en.substring("MODELS (".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("Page ") && en.contains(" of ")) {
            return en.replace("Page ", "Страница ").replace(" of ", " из ");
        }
        if (en.startsWith("Checking installed packages over ADB for ") && en.endsWith("…")) {
            return "Проверяем установленные пакеты через ADB для "
                    + en.substring("Checking installed packages over ADB for ".length());
        }
        if (en.startsWith("None of the packages listed in ") && en.endsWith(" are installed on this TV.")) {
            return "На этом телевизоре не установлено ни одного пакета из списка модели "
                    + en.substring("None of the packages listed in ".length(),
                    en.length() - " are installed on this TV.".length()) + ".";
        }
        if (en.startsWith("Disable Selected Packages (") && en.endsWith(")")) {
            return "Отключить выбранные пакеты (" + en.substring("Disable Selected Packages (".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("PACKAGES ON THIS TV (") && en.endsWith(")")) {
            return "ПАКЕТЫ НА ЭТОМ ТЕЛЕВИЗОРЕ (" + en.substring("PACKAGES ON THIS TV (".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("INSTALLED SYSTEM LAUNCHERS ON THIS TV (") && en.endsWith(")")) {
            return "УСТАНОВЛЕННЫЕ СИСТЕМНЫЕ ЛАУНЧЕРЫ (" + en.substring("INSTALLED SYSTEM LAUNCHERS ON THIS TV (".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("All (") && en.endsWith(")")) {
            return "Все (" + en.substring("All (".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("ALL SYSTEM APPS (") && en.endsWith(" — TAP TO DISABLE / ENABLE)")) {
            return "ВСЕ СИСТЕМНЫЕ ПРИЛОЖЕНИЯ (" + en.substring("ALL SYSTEM APPS (".length(),
                    en.length() - " — TAP TO DISABLE / ENABLE)".length()) + " — НАЖМИТЕ, ЧТОБЫ ОТКЛЮЧИТЬ / ВКЛЮЧИТЬ)";
        }
        if (en.startsWith("Restore All Saved Packages (") && en.endsWith(")")) {
            return "Восстановить все сохранённые пакеты (" + en.substring("Restore All Saved Packages (".length(), en.length() - 1) + ")";
        }
        if (en.startsWith("Restore Saved Disabled Apps (") && en.endsWith(" packages)")) {
            return "Восстановить сохранённые отключённые приложения ("
                    + en.substring("Restore Saved Disabled Apps (".length(), en.length() - " packages)".length()) + " пакетов)";
        }
        if (en.startsWith("Restore Selected Model Profile (") && en.endsWith(" packages)")) {
            String value = en.substring("Restore Selected Model Profile (".length(), en.length() - " packages)".length());
            return "Восстановить профиль выбранной модели (" + value + " пакетов)";
        }
        if (en.startsWith("Re-enable all packages in ")) {
            return "Повторно включить все пакеты профиля " + en.substring("Re-enable all packages in ".length());
        }
        if (en.startsWith("Custom ")) return "Своё значение: " + en.substring("Custom ".length());
        if (en.startsWith("Folder shuffle enabled: ")) {
            return "Смена изображений из папки включена: " + en.substring("Folder shuffle enabled: ".length());
        }
        if (en.startsWith("Lock Screen folder shuffle enabled: ")) {
            return "Смена изображений из папки для экрана блокировки включена: "
                    + en.substring("Lock Screen folder shuffle enabled: ".length());
        }
        if (en.startsWith("No place found for “") && en.endsWith("”.")) {
            return "Место «" + en.substring("No place found for “".length(), en.length() - "”.".length()) + "» не найдено.";
        }
        if (en.startsWith("Location set to ") && en.endsWith(".")) {
            return "Местоположение задано: " + en.substring("Location set to ".length(), en.length() - 1) + ".";
        }
        if (en.startsWith("Switch to ")) return "Переключиться на " + en.substring("Switch to ".length());
        if (en.endsWith("% used · Recent Apps")) {
            String num = en.substring(0, en.length() - "% used · Recent Apps".length());
            return num + "% · " + map.getOrDefault("Recent Apps", "Недавние приложения");
        }
        if (en.endsWith("% used · Clean")) {
            String num = en.substring(0, en.length() - "% used · Clean".length());
            return num + "% · " + map.getOrDefault("Clean", "Очистить");
        }
        if (en.endsWith("% used  ·  Clean")) {
            String num = en.substring(0, en.length() - "% used  ·  Clean".length());
            return num + "% использовано · " + map.getOrDefault("Clean", "Очистить");
        }
        if (en.endsWith(" min left")) return en.substring(0, en.length() - " min left".length()) + " мин";
        if (en.startsWith("Move ")) {
            String rest = en.substring("Move ".length());
            if (!rest.equals("left in dock") && !rest.equals("right in dock")) return map.getOrDefault("Move", "Переместить") + " " + rest;
        }
        if (en.startsWith("Done (") && en.endsWith(" apps disabled)")) {
            String num = en.substring("Done (".length(), en.length() - " apps disabled)".length());
            return map.getOrDefault("Done", "Готово") + " (" + num + " " + map.getOrDefault("apps", "приложений") + ")";
        }
        if (en.startsWith("Not done (") && en.endsWith(" disabled)")) {
            String ratio = en.substring("Not done (".length(), en.length() - " disabled)".length());
            return map.getOrDefault("Not done", "Не выполнено") + " (" + ratio + ")";
        }
        return en;
    }

    public static String formatSceneDate(Context c, Date date) {
        return new SimpleDateFormat("EEEE d MMMM", Locale.forLanguageTag("ru-RU")).format(date);
    }

    public static String profileName(Context c, String name) {
        if (name == null) return "";
        if (name.equals("Default")) return "По умолчанию";
        if (name.startsWith("Person ")) return "Профиль " + name.substring("Person ".length());
        if (name.startsWith("Profile ")) return "Профиль " + name.substring("Profile ".length());
        if (name.equals("Kids")) return "Детский профиль";
        return name;
    }
}
