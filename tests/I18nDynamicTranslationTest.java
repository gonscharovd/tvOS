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
                        + "Сохранено в списке восстановления: 3 пакетов.");
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

        System.out.println("Dynamic Russian translation tests passed: " + checks);
    }
}
