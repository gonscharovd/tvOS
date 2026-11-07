#!/usr/bin/env python3
"""Regression checks for the Java CleanYourTV core-package protection regex."""

import json
import re
from pathlib import Path


SOURCE = Path("src/com/techgenius/tvoslauncher/CleanYourTv.java").read_text(encoding="utf-8")


def java_pattern(name: str) -> re.Pattern:
    match = re.search(
        rf"{re.escape(name)}\s*=\s*Pattern\.compile\((.*?)\);",
        SOURCE,
        re.DOTALL,
    )
    if not match:
        raise AssertionError(f"Could not find {name} in CleanYourTv.java")
    literals = re.findall(r'"((?:\\.|[^"\\])*)"', match.group(1))
    if not literals:
        raise AssertionError(f"No Java string literals found for {name}")
    return re.compile("".join(json.loads('"' + part + '"') for part in literals))


CORE_RE = java_pattern("CORE_RE")
CORE_OK = java_pattern("CORE_OK")


def blocked_by_core_regex(package: str) -> bool:
    return CORE_RE.fullmatch(package) is not None and CORE_OK.fullmatch(package) is None


def main() -> None:
    must_be_blocked = (
        "android",
        "com.android.server",
        "com.android.server.wm",
        "com.android.server.pm",
        "com.android.defcontainer",
        "com.android.vending",
        "com.android.systemui",
        "com.android.settings",
        "com.android.permissioncontroller",
        "com.google.android.gms",
    )
    for package in must_be_blocked:
        assert blocked_by_core_regex(package), f"Core package escaped protection: {package}"

    allowed_exceptions = (
        "com.android.providers.tv",
        "com.android.providers.calendar",
        "com.android.providers.contacts",
        "com.android.providers.userdictionary",
        "com.android.managedprovisioning",
    )
    for package in allowed_exceptions:
        assert not blocked_by_core_regex(package), f"CORE_OK exception unexpectedly blocked: {package}"

    protected_exact = set(re.findall(r'NEVER_DISABLE_EXACT\.add\("([^"]+)"\)', SOURCE))
    assert "ru.sberdevices.SberDevicesServiceSystem" in protected_exact
    assert "ru.sberdevices.starlauncher" in protected_exact
    assert "if (NEVER_DISABLE_EXACT.contains(pkg) || isCriticalProtectedService(pkg, null))" in SOURCE
    assert "if (disable && !isSafeToDisable(activity, pkg))" in SOURCE
    assert "allowAnySystemApp" not in SOURCE

    print(f"CleanYourTV protection: {len(must_be_blocked)} core packages, "
          f"{len(allowed_exceptions)} CORE_OK exceptions, Sber Device Owner/launcher guards passed.")


if __name__ == "__main__":
    main()
