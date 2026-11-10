#!/usr/bin/env python3
"""Static contract checks for the Android-backed CleanYourTV safety gate."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CLEAN = (ROOT / "src/com/techgenius/tvoslauncher/CleanYourTv.java").read_text(encoding="utf-8")
INSPECTOR = (ROOT / "src/com/techgenius/tvoslauncher/AndroidPackageSafetyInspector.java").read_text(encoding="utf-8")
POLICY = (ROOT / "src/com/techgenius/tvoslauncher/PackageSafetyPolicy.java").read_text(encoding="utf-8")
PERMS = (ROOT / "src/com/techgenius/tvoslauncher/Perms.java").read_text(encoding="utf-8")
LOCAL_ADB = (ROOT / "src/com/techgenius/tvoslauncher/LocalAdb.java").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    require("AndroidPackageSafetyInspector.create(c, AndroidHomeAppDetector.detect(c))" in CLEAN,
            "single-package disable must construct the Android safety inspector")
    require("return safety.evaluate(pkg).isSafeToDisable();" in CLEAN,
            "CleanYourTV must delegate disable decisions to the shared policy")
    require("AndroidPackageSafetyInspector safety = AndroidPackageSafetyInspector.create" in CLEAN,
            "list and batch operations must reuse a read-only safety snapshot")
    require("SAFE_DISABLE_CACHE" not in CLEAN,
            "package safety must not reuse stale package-name-only cache results")

    require("if (NEVER_DISABLE_EXACT.contains(pkg) || isCriticalProtectedService(pkg, null))" in CLEAN,
            "legacy exact protections must remain in the shared safety facts")
    require("CORE_RE.matcher(pkg).matches() && !CORE_OK.matcher(pkg).matches()" in CLEAN,
            "core Android protection must remain active")
    require("isSelectedModelProfileCandidate" in CLEAN,
            "system package profile membership must be checked together with OEM identity")

    for token in (
        "DevicePolicyManager", "getActiveAdmins()", "isDeviceOwnerApp(pkg)",
        "isProfileOwnerApp(pkg)", "FLAG_SYSTEM", "FLAG_UPDATED_SYSTEM_APP",
        "FLAG_PERSISTENT", "FIRST_APPLICATION_UID",
        "queryIntentServices", "queryIntentActivities", "checkPermission",
        "android.intent.action.MANAGE_APP_PERMISSIONS", "HomeAppDetector.Result",
        "DeviceProfileResolver.fromSystem",
    ):
        require(token in INSPECTOR, f"Android safety inspector missing {token}")

    require("UNKNOWN" in POLICY and "HOME_CANDIDATE" in POLICY
            and "UNKNOWN_OEM_SYSTEM_APPLICATION" in POLICY,
            "policy must model uncertainty and HOME/OEM states explicitly")
    require("PackageMutationCommandGuard.inspect(command)" in LOCAL_ADB
            and "packageMutationBlockReason(c, shellCmd, packageSafetySnapshot)" in LOCAL_ADB
            and "safety.evaluate(mutation.packageName).isSafeToDisable()" in LOCAL_ADB,
            "ADB transport must re-check every package mutation with the shared policy")
    require("UNSUPPORTED_MUTATION" in LOCAL_ADB and "DELETE" in LOCAL_ADB,
            "ADB transport must reject compound mutations and all package deletion")
    require("pm disable-user" not in PERMS,
            "Perms must not display a manual shell command that bypasses package safety")
    for path in (
        "src/com/techgenius/tvoslauncher/MainActivity.java",
        "src/com/techgenius/tvoslauncher/SettingsActivity.java",
        "src/com/techgenius/tvoslauncher/SetupActivity.java",
        "src/com/techgenius/tvoslauncher/PowerService.java",
    ):
        source = (ROOT / path).read_text(encoding="utf-8")
        require("pm disable-user" not in source and "pm uninstall" not in source
                and "deletePackage(" not in source,
                f"{path} must not add an alternate package mutation path")
    require("deletePackage(" not in CLEAN and "pm uninstall" not in CLEAN,
            "CleanYourTV must not delete packages")
    require("pm enable --user 0 " in CLEAN,
            "restore path must remain an enable-only recovery operation")
    require("pm disable-user --user 0 " in CLEAN,
            "the sole package-disable command must remain behind CleanYourTV's shared gate")

    print("CleanYourTV package-safety integration contract passed.")


if __name__ == "__main__":
    main()
