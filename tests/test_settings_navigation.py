#!/usr/bin/env python3
"""Regression guards for the simplified Russian-only Settings navigation."""
from pathlib import Path
import re

ROOT = Path("src/com/techgenius/tvoslauncher")
settings = (ROOT / "SettingsActivity.java").read_text(encoding="utf-8")

sidebar = settings.split("private static final String[][] SIDEBAR_SECTIONS = {", 1)[1].split("};", 1)[0]
expected = [
    '{LOOK_SCENES, "scene", "ВНЕШНИЙ ВИД"}',
    '{WEATHER, "cloud", ""}',
    '{HOME, "house", "ОСНОВНЫЕ НАСТРОЙКИ"}',
    '{ABOUT, "info", "ИНФОРМАЦИЯ"}',
    '{ADVANCED, "shield", "РАСШИРЕННЫЕ НАСТРОЙКИ"}',
]
positions = [sidebar.find(item) for item in expected]
assert all(pos >= 0 for pos in positions), "A settings section is missing"
assert positions == sorted(positions), "Settings sections are not in the requested order"
assert "CONTROL_PANEL" not in sidebar and "UPDATES" not in sidebar and "PROFILES" not in sidebar
assert "private String currentSection = HOME;" in settings

assert "void pageMainScreen()" in settings
assert "Звуки навигации" in settings
assert "Повторить первоначальную настройку" in settings
assert "Сбросить настройки testOS" in settings
assert "void pageLookAndScenes()" in settings and "pageLanguage();" not in settings
assert "Размер панели приложений" in settings
assert "void pageWeather()" in settings and "Кнопка погоды на главном экране" in settings
assert "void pageApps()" in settings and "Скрытые приложения" in settings
assert "void pageUpdates()" in settings
assert "void pageAbout()" in settings and "gonscharovd" in settings
assert "Сохранить настройки в память ТВ" in settings
assert "void pageAdvanced()" in settings
assert "Выбор лаунчера по умолчанию" in settings
assert "CleanYourTv.getSelectedModelSummary" in settings
assert "pageButtonMapper();" in settings
assert "Keep Alive (Auto-repair Accessibility)" in settings
assert "Ad-blocking Private DNS (AdGuard)" in settings
assert "ADB connection status" in settings and "Check permissions" in settings

for removed in ("void pageLanguage()", "void pageProfiles()", "void pageControlPanel()",
                "Classic Top Shelf suggestions", "confirmWatchSignOut(WatchCloud"):
    assert removed not in settings, f"Removed settings feature still present: {removed}"

advanced = settings.split("void pageAdvanced()", 1)[1].split("private volatile String advLaunchersSummary", 1)[0]
assert "Backup settings to Download" not in advanced
assert "Restore settings from folder" not in advanced
about = settings.split("void pageAbout()", 1)[1].split("private void resetAllAppDataAndReturnToSetup", 1)[0]
assert "pageUpdates();" in about
assert "Сохранить настройки в память ТВ" in about
assert "Восстановить настройки" in about

routes = settings.split("private String resolvePageExtra", 1)[1].split("@Override", 1)[0]
for route in ("return HOME;", "return LOOK_SCENES;", "return WEATHER;", "return ADVANCED;", "return ABOUT;"):
    assert route in routes

# Keep literal translation calls covered by the Russian dictionary.
i18n = (ROOT / "I18n.java").read_text(encoding="utf-8")
def java_unescape(value):
    value = re.sub(r'\\u([0-9a-fA-F]{4})', lambda m: chr(int(m.group(1), 16)), value)
    return value.replace(r'\\n', "\\n").replace(r'\\t', "\\t").replace(r'\\"', '"').replace(r'\\\\', "\\\\")
dictionary = {
    java_unescape(m.group(1))
    for m in re.finditer(r'add\\("((?:\\\\.|[^"\\\\])*)",', i18n)
}
literal_settings_strings = {
    java_unescape(m.group(1))
    for m in re.finditer(r'tr\\(\\s*"((?:\\\\.|[^"\\\\])*)"\\s*,', settings)
}
missing = sorted(literal_settings_strings - dictionary)
assert not missing, "Settings strings missing Russian translations: " + repr(missing)

print("Simplified settings navigation, section placement, legacy routes and Russian translation checks passed.")
