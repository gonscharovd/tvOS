#!/usr/bin/env python3
"""Guard the settings backup against restoring volatile or security-sensitive state."""

from pathlib import Path

root = Path("src/com/techgenius/tvoslauncher")
media = (root / "CustomMedia.java").read_text(encoding="utf-8")
backup = media.split("public static String buildSettingsBackupJson", 1)[1].split(
    "private static boolean validBackupItem", 1
)[0]
restore = media.split("public static boolean applySettingsBackupJson", 1)[1].split(
    "public static void backupSettingsToDownload", 1
)[0]
writer = media.split("public static void backupSettingsToDownload", 1)[1].split(
    "public static void showRestoreSettingsPicker", 1
)[0]

for pref in (
    '"settings"', '"home"', '"home_default"', '"app_custom_names"',
    '"button_mapper"', '"control_panel"', '"weather"', '"inputs"',
    '"clicks"', '"custom_media"', '"screensaver"',
):
    assert pref in media.split("private static final String[] KNOWN_PREF_NAMES", 1)[1].split("};", 1)[0]

for unsafe in (
    '"profiles"', '"suggestions"', '"media_watch"', '"local_adb"',
    '"adb_state"', '"app_updater"', '"cleanyourtv"', '"cleanyourtv_prefs"',
    '"present"', '"search"', '"setup"',
):
    assert unsafe not in media.split("private static final String[] KNOWN_PREF_NAMES", 1)[1].split("};", 1)[0]

assert 'root.put("format", "tvos-launcher-backup")' in backup
assert 'root.put("version", 2)' in backup
assert 'root.put("package", c.getPackageName())' in backup
assert "getApplicationInfo().dataDir" not in backup
assert "shared_prefs" not in backup

assert '"tvos-launcher-backup".equals(root.optString("format", ""))' in restore
assert "version < 1 || version > 2" in restore
assert "sourcePackage" in restore and "c.getPackageName().equals(sourcePackage)" in restore
assert "2 * 1024 * 1024" in restore
assert "validBackupItem(item)" in restore
assert "isBackupPrefName(prefName)" in restore
assert ".clear()" not in restore
assert "editor.commit()" in restore
assert "Merge known settings instead of clearing the file" in restore

assert "android.util.AtomicFile" in writer
assert "atomic.startWrite()" in writer
assert "atomic.finishWrite(fos)" in writer
assert "atomic.failWrite(fos)" in writer
assert "file.length() > 2L * 1024L * 1024L" in media

print("Settings backup safety: allowlist, schema validation, size guard and atomic writes passed.")
