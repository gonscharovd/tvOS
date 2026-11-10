#!/usr/bin/env python3
"""Static contract checks for the Android-backed CleanYourTV safety gate."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CLEAN = (ROOT / "src/com/techgenius/tvoslauncher/CleanYourTv.java").read_text(encoding="utf-8")
INSPECTOR = (ROOT / "src/com/techgenius/tvoslauncher/AndroidPackageSafetyInspector.java").read_text(encoding="utf-8")
POLICY = (ROOT / "src/com/techgenius/tvoslauncher/PackageSafetyPolicy.java").read_text(encoding="utf-8")
PERMS = (ROOT / "src/com/techgenius/tvoslauncher/Perms.java").read_text(encoding="utf-8")
LOCAL_ADB = (ROOT / "src/com/techgenius/tvoslauncher/LocalAdb.java").read_text(encoding="utf-8")
SCENE_HOME = (ROOT / "src/com/techgenius/tvoslauncher/SceneHome.java").read_text(encoding="utf-8")
MANIFEST = (ROOT / "AndroidManifest.xml").read_text(encoding="utf-8")
PROFILE = (ROOT / "src/com/techgenius/tvoslauncher/DeviceProfile.java").read_text(encoding="utf-8")
RESOLVER = (ROOT / "src/com/techgenius/tvoslauncher/DeviceProfileResolver.java").read_text(encoding="utf-8")
HOME_ADAPTER = (ROOT / "src/com/techgenius/tvoslauncher/AndroidHomeAppDetector.java").read_text(encoding="utf-8")
GUARD = (ROOT / "src/com/techgenius/tvoslauncher/PackageMutationCommandGuard.java").read_text(encoding="utf-8")


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
    for token in ("queryBroadcastReceivers", "Intent.ACTION_BOOT_COMPLETED",
                  "Intent.ACTION_LOCKED_BOOT_COMPLETED", "systemBootReceiverPackages.contains(pkg)",
                  "android.permission.BLUETOOTH_STACK"):
        require(token in INSPECTOR, f"Android safety inspector missing {token}")
    require("PackageManager.MATCH_DISABLED_COMPONENTS" in HOME_ADAPTER,
            "HOME detection must include disabled recovery candidates")
    require("packageInventoryKnown" in PROFILE
            and "conflicting-build-and-package-oem-evidence" in PROFILE,
            "device profile must expose incomplete and conflicting OEM evidence")
    require("installed != null && !installed.isEmpty()" in RESOLVER,
            "empty PackageManager inventory must not be considered complete")
    require("deviceProfile.packageInventoryKnown" in INSPECTOR
            and 'deviceProfile.evidence.contains("build-properties")' in INSPECTOR,
            "package-only or incomplete OEM identity must not authorize system debloating")


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
    require("REQUEST_DELETE_PACKAGES" in MANIFEST,
            "user-app uninstall via Android confirmation requires the declared request permission")
    require("CleanYourTv.isSafeToDelete(host, a.pkg)" in SCENE_HOME
            and "Intent.ACTION_DELETE" in SCENE_HOME,
            "SceneHome user-app uninstall must be gated by the shared deletion policy")
    forbidden_delete_api = ("deletePackage(", "ACTION_UNINSTALL_PACKAGE",
                            "setApplicationEnabledSetting(", "setPackagesSuspended(",
                            "setApplicationHidden(")
    for source_path in (ROOT / "src").rglob("*.java"):
        source = source_path.read_text(encoding="utf-8")
        for token in forbidden_delete_api:
            require(token not in source, f"{source_path} must not introduce {token}")
        if "Intent.ACTION_DELETE" in source:
            require(source_path.name == "SceneHome.java"
                    and "CleanYourTv.isSafeToDelete(host, a.pkg)" in source,
                    f"{source_path} must gate ACTION_DELETE through the shared policy")
    require("deletePackage(" not in CLEAN and "pm uninstall" not in CLEAN,
            "CleanYourTV must not directly delete packages")
    require("pm enable --user 0 " in CLEAN,
            "restore path must remain an enable-only recovery operation")
    require("PackageSafetyPolicy.isValidPackageName(pkg)" in CLEAN,
            "restore data must be validated before it is interpolated into shell commands")
    require("Type.ENABLE" in GUARD and "ENABLE_COMMAND" in GUARD,
            "shell boundary must parse exact restore commands and reject compound enable commands")
    require("pm disable-user --user 0 " in CLEAN,
            "the sole package-disable command must remain behind CleanYourTV's shared gate")

    print("CleanYourTV package-safety integration contract passed.")


if __name__ == "__main__":
    main()
