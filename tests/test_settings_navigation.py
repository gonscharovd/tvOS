#!/usr/bin/env python3
"""Regression guards for the grouped Settings navigation and preserved settings."""
from pathlib import Path
import re

ROOT = Path("src/com/techgenius/tvoslauncher")
settings = (ROOT / "SettingsActivity.java").read_text(encoding="utf-8")

sidebar = settings.split("private static final String[][] SIDEBAR_SECTIONS = {", 1)[1].split("};", 1)[0]
expected = [
    '{HOME, "house"}',
    '{LOOK_SCENES, "scene"}',
    '{WEATHER, "cloud"}',
    '{CONTROL_PANEL, "controls"}',
    '{APPS, "apps"}',
    '{UPDATES, "update"}',
    '{ABOUT, "info"}',
    '{ADVANCED, "shield"}',
]
positions = [sidebar.find(item) for item in expected]
assert all(pos >= 0 for pos in positions), "A requested settings category is missing"
assert positions == sorted(positions), "Settings categories are not in the requested order"
assert "{BUTTON_MAPPER," not in sidebar
assert "private String currentSection = HOME;" in settings

assert "case HOME:" in settings and "pageMainScreen();" in settings
assert "case CONTROL_PANEL:" in settings and "pageControlPanel();" in settings
assert "void pageMainScreen()" in settings
assert "void pageControlPanel()" in settings
assert "void pageLookAndScenes()" in settings and "pageLanguage();" in settings
assert "void pageWeather()" in settings
assert "void pageApps()" in settings
assert "void pageUpdates()" in settings
assert "void pageAbout()" in settings

main_page = settings.split("void pageMainScreen()", 1)[1].split("void pageControlPanel()", 1)[0]
assert "Default home screen" in main_page
assert "pageProfiles();" in main_page
assert "Home screen weather shortcut" in main_page
assert "Classic Top Shelf suggestions" in main_page

apps_page = settings.split("void pageApps()", 1)[1].split("static final class HiddenAppItem", 1)[0]
assert "Home screen weather shortcut" not in apps_page
assert "Classic Top Shelf suggestions" not in apps_page

control_page = settings.split("void pageControlPanel()", 1)[1].split("void pageAdvanced()", 1)[0]
for action in ("ACTION_WIFI", "ACTION_BLUETOOTH", "ACTION_SOUND", "ACTION_INPUTS", "ACTION_AMBIENT"):
    assert f"ControlPanelOverlay.{action}" in control_page
assert "ControlPanelOverlay.getActionSummary(this, actionKey)" in control_page
assert 'openSubMenu("cp_app:" + actionKey)' in control_page
assert "clearAllCustomActions(this)" in control_page

advanced = settings.split("void pageAdvanced()", 1)[1].split("private volatile String advLaunchersSummary", 1)[0]
for feature in ("CleanYourTv.getSelectedModelSummary", "pageButtonMapper();",
                "Backup settings to Download", "Restore settings from folder",
                "DIAGNOSTICS & RECOVERY", "ADB connection status", "Check permissions"):
    assert feature in advanced, f"Advanced settings missing: {feature}"

about = settings.split("void pageAbout()", 1)[1].split("private void confirmWatchSignOut", 1)[0]
assert "BACKUP & RESTORE" not in about
assert "Reset settings to defaults" in about

routes = settings.split("private String resolvePageExtra", 1)[1].split("@Override", 1)[0]
for route in ('return CONTROL_PANEL;', 'return HOME;', 'return LOOK_SCENES;', 'return ADVANCED;'):
    assert route in routes


# Every new user-facing label and explanatory sentence introduced by the grouped
# navigation must have a direct Russian dictionary entry. I18n.s() intentionally
# ignores the legacy second (Turkish) argument, so missing keys otherwise fall
# back to English even though SettingsActivity.tr() appears to supply a fallback.
i18n = (ROOT / "I18n.java").read_text(encoding="utf-8")

def java_unescape(value):
    value = re.sub(r'\\u([0-9a-fA-F]{4})', lambda m: chr(int(m.group(1), 16)), value)
    return value.replace(r'\n', "\n").replace(r'\t', "\t").replace(r'\"', '"').replace(r'\\', "\\")

dictionary = {
    java_unescape(m.group(1))
    for m in re.finditer(r'add\("((?:\\.|[^"\\])*)",', i18n)
}
literal_settings_strings = {
    java_unescape(m.group(1))
    for m in re.finditer(r'tr\(\s*"((?:\\.|[^"\\])*)"\s*,', settings)
}
missing_russian = sorted(literal_settings_strings - dictionary)
assert not missing_russian, "Settings strings missing Russian translations: " + repr(missing_russian)

print("Settings navigation grouping, legacy routes, feature placement and complete literal Russian coverage passed.")


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
assert 'CLASSIC BACKGROUND' not in look_page
assert 'sceneCheckRow(spec, sceneIdx, order);' in look_page
assert 'scene-gradient:' in settings

setup = (ROOT / "SetupActivity.java").read_text(encoding="utf-8")
assert "LOOK_CLASSIC" not in setup
assert 'option(l, tr("Classic"' not in setup
assert "Classic Backgrounds" not in setup
