#!/usr/bin/env python3
"""Regression guards for the grouped Settings navigation and preserved settings."""
from pathlib import Path

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

print("Settings navigation grouping, legacy routes and feature placement passed.")
