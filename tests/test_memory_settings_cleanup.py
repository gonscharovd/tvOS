#!/usr/bin/env python3
"""Regression guards for bounded artwork caches, Settings clarity and dead media hooks."""
from pathlib import Path

ROOT = Path("src/com/techgenius/tvoslauncher")
settings = (ROOT / "SettingsActivity.java").read_text(encoding="utf-8")
main = (ROOT / "MainActivity.java").read_text(encoding="utf-8")
scene = (ROOT / "SceneHome.java").read_text(encoding="utf-8")
power = (ROOT / "PowerService.java").read_text(encoding="utf-8")

assert "LruCache<String, Bitmap>(2 * 1024 * 1024)" in settings
assert "LruCache<String, Bitmap>(3 * 1024 * 1024)" in scene
assert "LruCache<String, Bitmap>(2 * 1024 * 1024)" in scene
assert "SCENE_THUMB_CACHE.evictAll()" in settings
assert "releaseTransientCaches()" in main
assert "shelfThumbs.evictAll()" in scene
assert "logoCache.evictAll()" in scene

assert "if (heroIndex != want || heroItems.isEmpty()) {" in main
assert "bdBlur.recycle()" in main
assert "logo.recycle()" in main

assert 'row(tr("Pair / Repair ADB connection"' not in settings
assert 'row(tr("Disabled apps backup"' not in settings
assert 'row(tr("Classic Top Shelf content"' not in settings
assert 'row(tr("Classic Top Shelf suggestions"' not in settings
assert "void pageControlPanel()" not in settings
assert "void pageProfiles()" not in settings
assert "void pageLanguage()" not in settings
assert "NuvioCloud" not in settings and "StremioCloud" not in settings
assert "pageUpdates();" in settings

perms = (ROOT / "Perms.java").read_text(encoding="utf-8")
local_adb = (ROOT / "LocalAdb.java").read_text(encoding="utf-8")
control = (ROOT / "ControlPanelOverlay.java").read_text(encoding="utf-8")
manifest = Path("AndroidManifest.xml").read_text(encoding="utf-8")
assert not (ROOT / "MediaWatchService.java").exists()
for source in (main, scene, settings, power, perms, local_adb, control, manifest):
    assert "MediaWatchService" not in source
    assert "enabled_notification_listeners" not in source
    assert "NotificationListenerService" not in source
assert "Perms.toggleDnd(this)" in main
assert "Perms.toggleDnd(c)" in control
assert "setInterruptionFilter(" in perms
assert "requestLocation(Activity a)" in perms
assert "WRITE_EPG_DATA" not in manifest

print("Bounded artwork caches, memory-trim hooks, Settings de-duplication and unused media-service removal passed.")
