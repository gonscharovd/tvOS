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
assert 'row(tr("Classic Top Shelf suggestions"' in settings

for source in (main, scene, settings, power):
    assert "MediaWatchService" not in source

print("Bounded artwork caches, memory-trim hooks, Settings de-duplication and dead media-hook cleanup passed.")
