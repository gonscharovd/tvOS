#!/usr/bin/env python3
"""Regression guards for bounded caches and retired media-shelf permissions."""
from pathlib import Path

ROOT = Path("src/com/techgenius/tvoslauncher")
settings = (ROOT / "SettingsActivity.java").read_text(encoding="utf-8")
main = (ROOT / "MainActivity.java").read_text(encoding="utf-8")
scene = (ROOT / "SceneHome.java").read_text(encoding="utf-8")
power = (ROOT / "PowerService.java").read_text(encoding="utf-8")
perms = (ROOT / "Perms.java").read_text(encoding="utf-8")
local_adb = (ROOT / "LocalAdb.java").read_text(encoding="utf-8")
control = (ROOT / "ControlPanelOverlay.java").read_text(encoding="utf-8")
manifest = Path("AndroidManifest.xml").read_text(encoding="utf-8")

assert "LruCache<String, Bitmap>(2 * 1024 * 1024)" in settings
assert "LruCache<String, Bitmap>(2 * 1024 * 1024)" in scene
assert "SCENE_THUMB_CACHE.evictAll()" in settings
assert "releaseTransientCaches()" in main
assert "logoCache.evictAll()" in scene
assert "buildKeepWatchingShelf" not in scene
assert "WatchCardData" not in scene
assert "loadWatchCardsForPackage" not in scene
assert "shelfThumbs" not in scene
assert "pkgWatchCache" not in scene
assert "shelfBox" not in scene and "hideShelf" not in scene
assert "refreshKeepWatching" not in scene and "onDockTileFocusForShelf" not in scene

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

assert not (ROOT / "MediaWatchService.java").exists()
for source in (main, scene, settings, power, perms, local_adb, control, manifest):
    assert "MediaWatchService" not in source
    assert "enabled_notification_listeners" not in source
    assert "NotificationListenerService" not in source
assert "Perms.toggleDnd(this)" in main
assert "Perms.toggleDnd(c)" in control
assert "setInterruptionFilter(" in perms
assert "requestLocation(Activity a)" in perms
assert "READ_TV_LISTINGS" not in manifest
assert "READ_EPG_DATA" not in manifest
assert "READ_TV_LISTINGS" not in main
assert "ensureTvListingsPermission" not in main
assert "refreshKeepWatching" not in main
assert "READ_TV_LISTINGS" not in local_adb
assert "grantVolumeLongPressPermissionIfNeeded(this, null)" in main
assert "SET_VOLUME_KEY_LONG_PRESS_LISTENER" in local_adb

print("Bounded caches, memory-trim hooks, retired media shelf and TV-listings permission removal passed.")
