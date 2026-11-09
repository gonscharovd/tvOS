#!/usr/bin/env python3
"""Keep Linux/Windows build version defaults and release numbering aligned."""

import os
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read_properties(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        key, separator, value = line.partition("=")
        assert separator, f"Invalid version property: {raw_line!r}"
        values[key.strip()] = value.strip()
    return values


version = read_properties(ROOT / "version.properties")
build_ps = (ROOT / "build.ps1").read_text(encoding="utf-8")
build_sh = (ROOT / "build.sh").read_text(encoding="utf-8")
ps_helper = (ROOT / "tools/version.ps1").read_text(encoding="utf-8")
ps_test = (ROOT / "tests/test_build_contract.ps1").read_text(encoding="utf-8")
workflow = (ROOT / ".github/workflows/build-apk.yml").read_text(encoding="utf-8")
release_workflow = (ROOT / ".github/workflows/release.yml").read_text(encoding="utf-8")
updater = (ROOT / "src/com/techgenius/tvoslauncher/AppUpdater.java").read_text(encoding="utf-8")

# Canonical defaults are the planned next development target, not a published release.
assert version["DEFAULT_VERSION_CODE"] == "89"
assert version["DEFAULT_VERSION_NAME"] == "2.1.74"
assert version["RELEASE_VERSION_CODE_BASE"] == "15"
assert version["RELEASE_VERSION_PREFIX"] == "2.1."

# Verify Bash reads the canonical source and that environment overrides win.
assert 'source "$PWD/version.properties"' in build_sh
assert 'VERSION_CODE="${VERSION_CODE:-$DEFAULT_VERSION_CODE}"' in build_sh
assert 'VERSION_NAME="${VERSION_NAME:-$DEFAULT_VERSION_NAME}"' in build_sh
assert '--version-code "$VERSION_CODE" --version-name "$VERSION_NAME"' in build_sh
bash_probe = subprocess.run(
    [
        "bash",
        "-c",
        'source ./version.properties; printf "%s|%s\\n" "${VERSION_CODE:-$DEFAULT_VERSION_CODE}" "${VERSION_NAME:-$DEFAULT_VERSION_NAME}"',
    ],
    cwd=ROOT,
    env={**os.environ, "VERSION_CODE": "123", "VERSION_NAME": "2.1.123-test"},
    check=True,
    capture_output=True,
    text=True,
)
assert bash_probe.stdout.strip() == "123|2.1.123-test"
bash_defaults = subprocess.run(
    [
        "bash",
        "-c",
        'source ./version.properties; printf "%s|%s\\n" "$DEFAULT_VERSION_CODE" "$DEFAULT_VERSION_NAME"',
    ],
    cwd=ROOT,
    check=True,
    capture_output=True,
    text=True,
)
assert bash_defaults.stdout.strip() == "89|2.1.74"

# Verify PowerShell delegates parsing to a testable helper and keeps env overrides.
assert 'tools/version.ps1' in build_ps
assert "Get-TestOSBuildVersion" in build_ps
assert "DEFAULT_VERSION_CODE" in ps_helper and "DEFAULT_VERSION_NAME" in ps_helper
assert "$env:VERSION_CODE" in ps_helper and "$env:VERSION_NAME" in ps_helper
assert "VersionCodeOverride" in ps_test and "VersionNameOverride" in ps_test

# Both workflows consume the same defaults; release numbering retains BASE + PATCH.
assert "source ./version.properties" in workflow
assert 'VERSION_CODE=${VERSION_CODE:-$DEFAULT_VERSION_CODE}' in workflow
assert 'VERSION_NAME=${VERSION_NAME:-$DEFAULT_VERSION_NAME}' in workflow
assert "shell: pwsh" in workflow and "tests/test_build_contract.ps1" in workflow
assert "build-windows:" in workflow and "runs-on: windows-latest" in workflow
assert "run: ./build.ps1" in workflow
assert "APK package/version does not match shared version contract" in workflow
assert "source ./version.properties" in release_workflow
assert "RELEASE_VERSION_CODE_BASE + PATCH" in release_workflow
assert "RELEASE_VERSION_PREFIX" in release_workflow
assert "shell: pwsh" in release_workflow and "tests/test_build_contract.ps1" in release_workflow
assert int(version["RELEASE_VERSION_CODE_BASE"]) + 73 == 88
assert int(version["RELEASE_VERSION_CODE_BASE"]) + 74 == 89

# Preserve output, asset and signing contracts.
assert "build" + chr(92) + "tvos-home.apk" in build_ps
assert "build/tvos-home.apk" in build_sh
assert "build/testOS.apk" not in build_sh
assert "tools/prepare_assets.py" in build_ps and "tools/prepare_assets.py" in build_sh
assert "build" + chr(92) + "assets" in build_ps
assert "-A build/assets" in build_sh
assert "ASSET_OPTIMIZE" in build_ps and "ASSET_OPTIMIZE" in build_sh
assert "ASSET_PROFILE" in build_ps and "ASSET_PROFILE" in build_sh
assert "path: build/tvos-home.apk" in workflow
assert 'ASSET_OPTIMIZE: "1"' in workflow
assert 'ASSET_PROFILE: "sber"' in workflow
assert "fonttools" in workflow and "test_sber_font_subsetting.py" in workflow
assert "test_asset_optimizer.py" in workflow
assert "tvos-home.apk" in updater

for name, source in (("PowerShell", build_ps), ("Bash", build_sh)):
    for setting in (
        "RELEASE_KEYSTORE",
        "RELEASE_KEYSTORE_PASS",
        "RELEASE_KEY_ALIAS",
        "DEBUG_KEYSTORE",
        "DEBUG_KEY_ALIAS",
    ):
        assert setting in source, f"{name} build does not support {setting}"

print("Build contract: shared version source, environment overrides, release formula, output and signing controls passed.")
