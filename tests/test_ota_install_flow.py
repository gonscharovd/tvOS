#!/usr/bin/env python3
"""Guard the OTA flow: always launch PackageInstaller and resume after permission grant."""
from pathlib import Path

updater = Path("src/com/techgenius/tvoslauncher/AppUpdater.java").read_text(encoding="utf-8")
settings = Path("src/com/techgenius/tvoslauncher/SettingsActivity.java").read_text(encoding="utf-8")
main = Path("src/com/techgenius/tvoslauncher/MainActivity.java").read_text(encoding="utf-8")

assert "LocalAdb.installApkViaAdb" not in updater, "OTA must not silently bypass the system installer"
assert "MAIN.post(() -> installWithPackageInstaller(activity, apkFile))" in updater
assert "KEY_PENDING_INSTALL_PATH" in updater
assert "resumePendingInstallIfAllowed(Activity activity)" in updater
assert "startActivity(permIntent)" in updater
assert "installWithPackageInstaller(activity, apkFile);" in updater
assert settings.count("AppUpdater.resumePendingInstallIfAllowed(this);") == 1
assert main.count("AppUpdater.resumePendingInstallIfAllowed(this);") == 1
assert "PackageInstaller.STATUS_PENDING_USER_ACTION" in updater
assert "PackageInstaller.STATUS_FAILURE" in updater

print("OTA install flow: PackageInstaller launch and permission-resume guards passed.")
