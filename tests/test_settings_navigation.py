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

# TV-remote navigation must stay within the two-pane Settings layout.
navigation = settings.split("public boolean dispatchKeyEvent", 1)[1].split(
    "private View findAdjacentFocusable", 1
)[0]
for key in ("KEYCODE_DPAD_UP", "KEYCODE_DPAD_DOWN", "KEYCODE_DPAD_LEFT",
            "KEYCODE_DPAD_RIGHT", "KEYCODE_BACK", "requestFocus()",
            "findAdjacentFocusable", "focusFirstRow()", "closeSubMenu()"):
    assert key in navigation, f"D-pad navigation is missing {key}"

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
assert "CleanYourTv.getSelectedModelSummary" in settings and "Очистить телевизор" in settings
assert "pageButtonMapper();" in settings
assert "Автовосстановление службы пульта" in settings
assert "AdGuard DNS" in settings
assert "Состояние подключения" in settings and "Просмотреть выданные разрешения" in settings

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

def java_unescape(value):
    value = re.sub(r'\\u([0-9a-fA-F]{4})', lambda m: chr(int(m.group(1), 16)), value)
    escapes = {"n": "\n", "r": "\r", "t": "\t", '"': '"', "'": "'", "\\": "\\"}
    return re.sub(r'\\([nrt"\'\\])', lambda m: escapes[m.group(1)], value)

i18n = (ROOT / "I18n.java").read_text(encoding="utf-8")
dictionary = {
    java_unescape(m.group(1))
    for m in re.finditer(r'add\("((?:\\.|[^"\\])*)",', i18n)
}
literal_settings_strings = {
    java_unescape(m.group(1))
    for m in re.finditer(r'tr\(\s*"((?:\\.|[^"\\])*)"\s*,', settings)
}
missing = sorted(literal_settings_strings - dictionary)
assert not missing, "Settings strings missing Russian translations: " + repr(missing)


# Single-user and Russian-only build: obsolete profile UI and media catalogue code are absent.
setup = (ROOT / "SetupActivity.java").read_text(encoding="utf-8")
main = (ROOT / "MainActivity.java").read_text(encoding="utf-8")
scenes = (ROOT / "Scenes.java").read_text(encoding="utf-8")
suggestions = (ROOT / "Suggestions.java").read_text(encoding="utf-8")
search = (ROOT / "Search.java").read_text(encoding="utf-8")
search_panel = (ROOT / "SearchPanel.java").read_text(encoding="utf-8")
assert not (ROOT / "Profiles.java").exists(), "Obsolete multi-profile model remains"
assert "Profiles." not in main and "Profiles." not in scenes and "Profiles." not in settings
assert "showProfilePicker" not in main and "profileMenu(" not in main
assert "promptAddPerson" not in settings
assert "SingleUserPrefs.userName(c)" in scenes
assert "SingleUserPrefs.homePrefs(this)" in main
assert "steps.add(this::profile)" not in setup
assert "void profile()" not in setup and "void homeContent()" not in setup
assert 'by.setText("by gonscharovd")' in setup
for removed_file in ("News.java", "NuvioCloud.java", "StremioCloud.java", "WatchCloud.java", "WatchPopup.java"):
    assert not (ROOT / removed_file).exists(), f"Obsolete source remains: {removed_file}"
for removed in ("Cinemeta", "stremio", "nuvio", "SHELF_RECS", "SHELF_NEWS", "fetch("):
    assert removed.lower() not in suggestions.lower(), f"Removed feed engine remains: {removed}"
for removed in ("Cinemeta", "stremio", "nuvio"):
    assert removed.lower() not in search.lower(), f"Removed streaming integration remains in Search: {removed}"
assert "TURKISH" not in search_panel and "HINDI" not in search_panel
assert "RUSSIAN" in search_panel and "РУС" in search_panel and "ENG" in search_panel
assert not (Path("res/values-tr") / "strings.xml").exists()

print("Simplified settings navigation, Russian-only keyboard, single-user mode and removed media integrations passed.")


# Night Scenes is the only home mode. Former Classic wallpapers are now
# first-class selectable entries in the rotating scene gallery.
scenes = (ROOT / "Scenes.java").read_text(encoding="utf-8")
assert 'return LOOK_SCENES;' in scenes
assert 'public static boolean isScenesLook(Context c) {\n        return true;' in scenes
assert 'LOOK_CLASSIC' not in scenes
for scene_id in (
    'photo:tessellated-pavement.jpg', 'photo:grose-valley.jpg',
    'photo:albury-storm.jpg', 'photo:aresquiers-beach.jpg',
    'photo:vang-vieng-sunset.jpg', 'grad:aurora', 'grad:midnight',
    'grad:ocean', 'grad:sunset', 'grad:graphite',
):
    assert scene_id in scenes, f"Former Classic background missing from Night Scenes: {scene_id}"
assert 'migrateClassicBackgroundsToNightGallery' in scenes
look_page = settings.split("void pageLookAndScenes()", 1)[1].split("void pageApps()", 1)[0]
assert 'choiceRow(' not in look_page
assert 'КЛАССИЧЕСКИЙ ФОН' not in look_page
assert 'sceneCheckRow(spec, sceneIdx, order);' in look_page
assert 'scene-gradient:' in settings

setup = (ROOT / "SetupActivity.java").read_text(encoding="utf-8")
assert "LOOK_CLASSIC" not in setup
assert 'option(l, tr("Classic"' not in setup
assert "Classic Backgrounds" not in setup
