package com.techgenius.tvoslauncher;

/** JVM-level regression tests for dynamic Russian UI translations. */
public final class I18nDynamicTranslationTest {
    private static int checks;

    private static void expect(String english, String russian) {
        String actual = I18n.translateOther(I18n.LANG_RU, english);
        checks++;
        if (!russian.equals(actual)) {
            throw new AssertionError(
                    "Translation mismatch for: " + english
                            + "\nExpected: " + russian
                            + "\nActual:   " + actual);
        }
    }

    public static void main(String[] args) {
        expect("You are up to date (v2.1.67)", "У вас установлена последняя версия (v2.1.67)");
        expect("Changelog — v2.1.68", "Список изменений — v2.1.68");
        expect("Downloading update v2.1.68…", "Загружается обновление v2.1.68…");
        expect("RAM 0.8 GB used of 1.3 GB", "ОЗУ: 0.8 GB из 1.3 GB");
        expect("Closed 3 apps · freed 42 MB", "Закрыто приложений: 3 · освобождено 42 МБ");
        expect("Move Zona  ·  Left/Right, OK when done", "Переместить Zona · влево/вправо, OK — готово");
        expect("Off in 10 min", "Выключение через 10 мин");
        expect("Zona is not installed on this TV", "Приложение Zona не установлено на этом телевизоре");
        expect("▶  Open in Zona", "▶  Открыть в Zona");
        expect("Get Zona", "Скачать Zona");
        expect("Sign in to Stremio", "Войти в Stremio");
        expect("Couldn't reach Stremio. Focus the tile again to retry.",
                "Не удалось связаться с Stremio. Чтобы повторить попытку, снова выделите плитку.");
        expect("Sign out of Stremio?", "Выйти из аккаунта Stremio?");
        expect("No place found for “Chelyabinsk”.", "Место «Chelyabinsk» не найдено.");
        expect("Location set to Chelyabinsk.", "Местоположение задано: Chelyabinsk.");
        expect("Restore from Download/tvos-settings-backup.json",
                "Восстановить из Download/tvos-settings-backup.json");
        expect("Settings were successfully restored from:\n/storage/emulated/0/Download/settings.json",
                "Настройки успешно восстановлены из файла:\n/storage/emulated/0/Download/settings.json");
        expect("Model: MT9632", "Модель: MT9632");
        expect("OEM family: Sber", "Семейство производителя: Sber");
        expect("PAGE 2 OF 10 (11–20 OF 100)", "СТРАНИЦА 2 ИЗ 10 (11–20 ИЗ 100)");
        expect("Search: \"Sber\" (12)", "Поиск: \"Sber\" (12)");
        expect("Verified disabled: 3 of 5 packages for Sber TV.\n"
                        + "Failed or unverified: 2.\n"
                        + "Saved restore list: 3 packages.",
                "Отключение подтверждено: 3 из 5 пакетов для Sber TV.\n"
                        + "Не удалось подтвердить: 2.\n"
                        + "Пакетов в списке восстановления: 3.");
        expect("Verified enabled: 4. Failed or unverified: 1. Remaining in restore list: 1.",
                "Включение подтверждено: 4. Не удалось подтвердить: 1. Осталось в списке восстановления: 1.");
        expect("1. Open TV Settings → System → About and click Build 7 times to unlock Developer Options.\n"
                        + "2. Open Settings → System → Developer Options and enable USB Debugging / Network Debugging.\n"
                        + "3. Return here and select Grant Permissions Now (ADB port 5555 will be closed automatically right after setup).",
                "1. Откройте «Настройки ТВ → Система → Об устройстве» и нажмите на номер сборки 7 раз, чтобы открыть параметры разработчика.\n"
                        + "2. Откройте «Настройки → Система → Параметры разработчика» и включите отладку по USB / сети.\n"
                        + "3. Вернитесь сюда и выберите «Выдать разрешения сейчас» (порт ADB 5555 будет автоматически закрыт сразу после настройки).");
        expect("WRITE_SECURE_SETTINGS and all launcher permissions are now active, and ADB (port 5555) has been closed for your security.\n\n"
                        + "Tip: For maximum security, you can also select “Revoke USB debugging authorizations” in Developer Options.",
                "WRITE_SECURE_SETTINGS и все разрешения лаунчера активны. Для вашей безопасности ADB (порт 5555) закрыт.\n\n"
                        + "Совет: для дополнительной защиты можно также выбрать «Отозвать разрешения на отладку по USB» в параметрах разработчика.");
        expect("No image files (.jpg, .png) in this folder",
                "В этой папке нет файлов изображений (.jpg, .png)");
        expect("Select this folder & shuffle (12 images)",
                "Выбрать эту папку и менять изображения (12 изображений)");
        expect("Could not write to Download/tvos-settings-backup.json\n\nPlease allow storage access and try again.",
                "Не удалось записать файл:\nDownload/tvos-settings-backup.json\n\nРазрешите доступ к хранилищу и повторите попытку.");

        expect("All TV Models (341)", "Все модели телевизоров (341)");
        expect("MODELS (341)", "МОДЕЛИ (341)");
        expect("CLOSEST MATCHES (4 APPS SCANNED)", "БЛИЖАЙШИЕ СОВПАДЕНИЯ (проверено приложений: 4)");
        expect("3 active / 5 found ›", "3 активно / 5 найдено ›");
        expect("Select Apps to Disable — Sber 50U4123B (4 found)",
                "Выберите приложения для отключения — Sber 50U4123B (4 найдено)");
        expect("Starting ADB debloat for 12 packages…",
                "Запускаем отключение через ADB для 12 пакетов…");
        expect("Disabling (2/12):\ncom.example.app", "Отключение (2/12):\ncom.example.app");
        expect("Enabling (1/4):\ncom.example.app", "Включение (1/4):\ncom.example.app");
        expect("None of the packages listed in Sber 50U4123B (sber-50u4123b) are installed on this TV.\n\n"
                        + "Would you like to scan your TV's installed apps over ADB to find the closest matching TV model?",
                "На этом телевизоре не установлено ни одного пакета из списка Sber 50U4123B (sber-50u4123b).\n\n"
                        + "Просканировать установленные приложения через ADB, чтобы найти наиболее подходящую модель ТВ?");
        expect("Found saved disabled-app data (4 packages).\n\nRe-enable all 4 saved packages over ADB?",
                "Найдены сохранённые данные об отключённых приложениях (пакетов: 4).\n\n"
                        + "Повторно включить все сохранённые пакеты (4) через ADB?");
        expect("No saved disabled-app backup was found, and no TV model is selected.\n\n"
                        + "Please select your TV model first so Restore knows which packages to re-enable.",
                "Резервная копия отключённых приложений не найдена, модель телевизора не выбрана.\n\n"
                        + "Сначала выберите модель телевизора, чтобы определить, какие пакеты нужно включить.");
        expect("No saved disabled-app list was found.\n\n"
                        + "Restore will use your selected TV model (Sber 50U4123B) and re-enable its 8 packages over ADB.",
                "Сохранённый список отключённых приложений не найден.\n\n"
                        + "Для восстановления будет использована выбранная модель телевизора (Sber 50U4123B). "
                        + "Будут повторно включены пакеты: 8 через ADB.");
        expect("Restore Selected Model Profile (Sber 50U4123B — 8 packages)",
                "Восстановить профиль выбранной модели (Sber 50U4123B — пакетов: 8)");
        expect("Re-enable all packages in sber-50u4123b",
                "Повторно включить все пакеты профиля sber-50u4123b");
        expect("com.example.app is a protected core system package and cannot be disabled automatically.",
                "com.example.app — защищённый системный пакет, его нельзя отключить автоматически.");
        expect("The command was sent, but Android did not confirm that com.example.app is disabled.\n"
                        + "The restore list was not changed.",
                "Команда отправлена, но Android не подтвердил, что пакет com.example.app отключён.\n"
                        + "Список восстановления не изменён.");
        expect("com.example.app (com.example.app) is a critical system service required for your TV remote and Bluetooth connectivity.\n\n"
                        + "It cannot be disabled under any circumstances.",
                "com.example.app (com.example.app) — критическая системная служба, необходимая для работы пульта и Bluetooth.\n\n"
                        + "Отключать её нельзя ни при каких обстоятельствах.");
        expect("You are about to disable com.example.app (com.example.app).\n\n"
                        + "This is a manual operation. It may affect TV features. Continue only if you know what this package does.",
                "Вы собираетесь отключить com.example.app (com.example.app).\n\n"
                        + "Это действие выполняется вручную и может повлиять на функции телевизора. Продолжайте, только если знаете назначение этого пакета.");
        expect("Folder shuffle enabled: /storage/emulated/0/Pictures",
                "Смена изображений из папки включена: /storage/emulated/0/Pictures");
        expect("Lock Screen folder shuffle enabled: /storage/emulated/0/Pictures",
                "Смена изображений из папки для экрана блокировки включена: /storage/emulated/0/Pictures");
        expect("Custom 2", "Свой вариант 2");
        expect("✅ Disable Selected Packages (4)", "✅ Отключить выбранные пакеты (4)");
        expect("None of the packages listed in Sber 50U4123B are installed on this TV.",
                "На этом телевизоре не установлено ни одного пакета из списка модели Sber 50U4123B.");
        expect("Switch to Ирина", "Выбрать профиль «Ирина»");
        expect("Removed remap for Home.", "Удалено переназначение кнопки Home.");
        expect("⚠️ Security Warning:\n\n"
                        + "Keep Alive monitors and restores permissions if your TV turns them off in the background.\n\n"
                        + "Without WRITE_SECURE_SETTINGS, Keep Alive requires Network Debugging (ADB on port 5555) to stay enabled on your home network. For better security, run Pair / Repair ADB in Secure Mode first so WRITE_SECURE_SETTINGS is granted and port 5555 can be closed.\n\n"
                        + "Enable Keep Alive anyway?",
                "⚠️ Предупреждение безопасности:\n\n"
                        + "Keep Alive отслеживает разрешения и восстанавливает их, если телевизор отключает их в фоновом режиме.\n\n"
                        + "Без WRITE_SECURE_SETTINGS для работы Keep Alive должна оставаться включённой сетевая отладка (ADB на порту 5555) в домашней сети. Для большей безопасности сначала выполните «Подключить / восстановить ADB» в безопасном режиме, чтобы выдать WRITE_SECURE_SETTINGS и закрыть порт 5555.\n\n"
                        + "Всё равно включить Keep Alive?");
        expect("WARNING: Manual Debloat lists ALL system apps installed on your TV.\n\n"
                        + "• Only disable packages if you know exactly what you are doing.\n"
                        + "• Disabling core Android system services, TV input/tuner services, or Settings packages can break HDMI inputs, Wi-Fi, remote control, or cause a bootloop.\n\n"
                        + "Proceed at your own risk?",
                "ПРЕДУПРЕЖДЕНИЕ: в ручном режиме отображаются ВСЕ системные приложения.\n\n"
                        + "• Отключайте пакеты, только если точно знаете их назначение.\n"
                        + "• Отключение основных служб Android, служб ТВ-входов / тюнера или настроек может нарушить работу HDMI, Wi-Fi, пульта или привести к циклической перезагрузке.\n\n"
                        + "Продолжить на свой риск?");
        System.out.println("Dynamic Russian translation tests passed: " + checks);
    }
}
