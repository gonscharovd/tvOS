#!/usr/bin/env python3
"""Keep Windows/Linux build outputs and signing controls aligned."""

from pathlib import Path

build_ps = Path("build.ps1").read_text(encoding="utf-8")
build_sh = Path("build.sh").read_text(encoding="utf-8")
workflow = Path(".github/workflows/build-apk.yml").read_text(encoding="utf-8")
updater = Path("src/com/techgenius/tvoslauncher/AppUpdater.java").read_text(encoding="utf-8")

assert "$env:VERSION_CODE" in build_ps and "$env:VERSION_NAME" in build_ps
assert "${VERSION_CODE:-" in build_sh and "${VERSION_NAME:-" in build_sh
assert 'else { "85" }' in build_ps and 'else { "2.1.70" }' in build_ps
assert "${VERSION_CODE:-85}" in build_sh and "${VERSION_NAME:-2.1.70}" in build_sh

assert 'build\\tvos-home.apk' in build_ps
assert "build/tvos-home.apk" in build_sh
assert "build/testOS.apk" not in build_sh
assert "path: build/tvos-home.apk" in workflow
assert "tvos-home.apk" in updater
assert "Prepare persistent preview signing key" in workflow
assert "VERSION_NAME=2.1.70" in workflow and "VERSION_CODE=85" in workflow
assert "testOS-preview-signed" in workflow
assert "5dbf8846d9bb76a764928da8c8a833a837b44ba00f4a7c0b60dc77490319b10a" in workflow
assert "softprops/action-gh-release" not in workflow

for name, source in (("PowerShell", build_ps), ("Bash", build_sh)):
    for setting in ("RELEASE_KEYSTORE", "RELEASE_KEYSTORE_PASS", "RELEASE_KEY_ALIAS",
                    "DEBUG_KEYSTORE", "DEBUG_KEY_ALIAS"):
        assert setting in source, f"{name} build does not support {setting}"

print("Build contract: version overrides, canonical APK path, and signing controls passed.")
