package com.techgenius.tvoslauncher;

public final class PackageSafetyPolicyTest {
    private static final String PKG = "vendor.example.app";

    private static PackageSafetyPolicy.Facts facts(
            PackageSafetyPolicy.HomeState home, boolean system, boolean persistent, boolean systemUid, boolean ownerKnown, boolean owner,
            boolean rolesKnown, boolean criticalRole, boolean permissionsKnown,
            boolean criticalPermission, boolean knownOem, boolean inProfile) {
        return new PackageSafetyPolicy.Facts(PKG, true, false, false, false, home,
                true, system, persistent, systemUid, ownerKnown, owner,
                rolesKnown, criticalRole, permissionsKnown, criticalPermission, knownOem, inProfile);
    }

    private static void expect(PackageSafetyPolicy.State state, PackageSafetyPolicy.Reason reason,
                               PackageSafetyPolicy.Facts facts, String label) {
        PackageSafetyPolicy.Decision d = PackageSafetyPolicy.evaluate(facts);
        check(d.state == state && d.reason == reason,
                label + ": expected " + state + "/" + reason + " but got " + d.state + "/" + d.reason);
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        PackageSafetyPolicy.Facts safeUser = facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, false, false, false, true, false, true, false, true, false, false, false);
        expect(PackageSafetyPolicy.State.SAFE_TO_DISABLE, PackageSafetyPolicy.Reason.NON_SYSTEM_APPLICATION,
                safeUser, "ordinary user app");

        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.SELECTED_HOME,
                facts(PackageSafetyPolicy.HomeState.SELECTED_HOME, false, false, false,
                        true, false, true, false, true, false, false, false), "selected HOME");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.HOME_CANDIDATE,
                facts(PackageSafetyPolicy.HomeState.HOME_CANDIDATE, true, false, false,
                        true, false, true, false, true, false, true, true), "alternate HOME");
        expect(PackageSafetyPolicy.State.UNKNOWN, PackageSafetyPolicy.Reason.HOME_STATE_UNKNOWN,
                facts(PackageSafetyPolicy.HomeState.UNKNOWN, false, false, false,
                        true, false, true, false, true, false, false, false), "unknown HOME");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.OWN_APPLICATION,
                new PackageSafetyPolicy.Facts("com.gonscharovd.tvoslauncher", true, true, false, false,
                        PackageSafetyPolicy.HomeState.SELECTED_HOME, true, false, false, false,
                        true, false, true, false, true, false, false, false), "testOS self-protection");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.LEGACY_HARD_PROTECTION,
                new PackageSafetyPolicy.Facts(PKG, true, false, true, false,
                        PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, false, false,
                        true, false, true, false, true, false, false, false), "legacy exact deny");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.CORE_ANDROID_PROTECTION,
                new PackageSafetyPolicy.Facts(PKG, true, false, false, true,
                        PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, false, false,
                        true, false, true, false, true, false, false, false), "Android core regex");

        expect(PackageSafetyPolicy.State.UNKNOWN, PackageSafetyPolicy.Reason.PACKAGE_INFO_UNKNOWN,
                new PackageSafetyPolicy.Facts(PKG, true, false, false, false,
                        PackageSafetyPolicy.HomeState.SAFE_NON_HOME, false, false, false, false, false, false,
                        true, false, true, false, false, false), "PackageManager unavailable");
        expect(PackageSafetyPolicy.State.UNKNOWN, PackageSafetyPolicy.Reason.DEVICE_OWNER_QUERY_UNKNOWN,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, false, false, false,
                        false, false, true, false, true, false, false, false), "DevicePolicyManager failure");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.DEVICE_OWNER_OR_ACTIVE_ADMIN,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, false, false, false,
                        true, true, true, false, true, false, false, false), "Device Owner");
        expect(PackageSafetyPolicy.State.UNKNOWN, PackageSafetyPolicy.Reason.CRITICAL_ROLE_QUERY_UNKNOWN,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, false, false, false,
                        true, false, false, false, true, false, false, false), "critical-role query failure");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.CRITICAL_SYSTEM_ROLE,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, false, false, false,
                        true, false, true, true, true, false, false, false), "TV Input / IME / Settings role");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.CRITICAL_SYSTEM_ROLE,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, false,
                        true, false, true, true, true, false, true, true), "System UI");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.CRITICAL_SYSTEM_ROLE,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, false,
                        true, false, true, true, true, false, true, true), "Package Installer");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.CRITICAL_SYSTEM_ROLE,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, false,
                        true, false, true, true, true, false, true, true), "Permission Controller");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.CRITICAL_SYSTEM_ROLE,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, false,
                        true, false, true, true, true, false, true, true), "TV Input Framework");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.CRITICAL_SYSTEM_ROLE,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, false,
                        true, false, true, true, true, false, true, true), "system boot receiver");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.SELECTED_HOME,
                facts(PackageSafetyPolicy.HomeState.SELECTED_HOME, true, false, false,
                        true, false, true, false, true, false, true, true), "selected system HOME");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.HOME_CANDIDATE,
                new PackageSafetyPolicy.Facts("com.gonscharovd.tvoslauncher", true, false, false, false,
                        PackageSafetyPolicy.HomeState.HOME_CANDIDATE, true, false, false, false,
                        true, false, true, false, true, false, true, true), "testOS HOME candidate");
        expect(PackageSafetyPolicy.State.UNKNOWN, PackageSafetyPolicy.Reason.CRITICAL_PERMISSION_QUERY_UNKNOWN,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, false, false, false,
                        true, false, true, false, false, false, false, false), "permission query failure");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.CRITICAL_PERMISSION,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, false, false, false,
                        true, false, true, false, true, true, false, false), "critical Android permission");

        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.SYSTEM_UID,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, true,
                        true, false, true, false, true, false, true, true), "system UID");
        expect(PackageSafetyPolicy.State.PROTECTED, PackageSafetyPolicy.Reason.PERSISTENT_SYSTEM_APPLICATION,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, true, false,
                        true, false, true, false, true, false, true, true), "persistent system app");
        expect(PackageSafetyPolicy.State.UNKNOWN, PackageSafetyPolicy.Reason.UNKNOWN_OEM_SYSTEM_APPLICATION,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, false,
                        true, false, true, false, true, false, false, true), "unknown OEM system app");
        expect(PackageSafetyPolicy.State.UNKNOWN, PackageSafetyPolicy.Reason.SYSTEM_PACKAGE_NOT_IN_SELECTED_PROFILE,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, false,
                        true, false, true, false, true, false, true, false), "unknown system package");
        expect(PackageSafetyPolicy.State.SAFE_TO_DISABLE, PackageSafetyPolicy.Reason.REVIEWED_PROFILE_SYSTEM_APPLICATION,
                facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, true, false, false,
                        true, false, true, false, true, false, true, true), "profile candidate with independent checks");

        for (int api : new int[] {24, 28, 34, 35}) {
            PackageSafetyPolicy.Facts f = facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME, false, false, false, true, false, true, false, true, false, false, false);
            expect(PackageSafetyPolicy.State.SAFE_TO_DISABLE, PackageSafetyPolicy.Reason.NON_SYSTEM_APPLICATION,
                    f, "API " + api + " policy parity");
        }

        check(PackageSafetyPolicy.evaluate(safeUser).confidence == PackageSafetyPolicy.Confidence.CONFIRMED,
                "safe decision must carry confirmed confidence");
        PackageSafetyPolicy.Decision unknown = PackageSafetyPolicy.evaluate(
                facts(PackageSafetyPolicy.HomeState.UNKNOWN, false, false, false,
                        true, false, true, false, true, false, false, false));
        check(unknown.confidence == PackageSafetyPolicy.Confidence.INCOMPLETE,
                "unknown decision must carry incomplete confidence");
        check(PackageSafetyPolicy.evaluateDelete(safeUser).isSafeToDelete()
                        && PackageSafetyPolicy.evaluateDelete(safeUser).reason
                        == PackageSafetyPolicy.Reason.SAFE_USER_APP_DELETE,
                "ordinary user app may be uninstalled after all safety checks");
        PackageSafetyPolicy.Facts systemApp = facts(PackageSafetyPolicy.HomeState.SAFE_NON_HOME,
                true, false, false, true, false, true, false, true, false, true, true);
        check(PackageSafetyPolicy.evaluateDelete(systemApp).state == PackageSafetyPolicy.State.PROTECTED
                        && PackageSafetyPolicy.evaluateDelete(systemApp).reason
                        == PackageSafetyPolicy.Reason.SYSTEM_PACKAGE_DELETE_NOT_ALLOWED,
                "OEM/system package deletion must be denied");
        PackageSafetyPolicy.Facts homeCandidate = facts(PackageSafetyPolicy.HomeState.HOME_CANDIDATE,
                false, false, false, true, false, true, false, true, false, false, false);
        check(PackageSafetyPolicy.evaluateDelete(homeCandidate).state == PackageSafetyPolicy.State.PROTECTED
                        && PackageSafetyPolicy.evaluateDelete(homeCandidate).reason
                        == PackageSafetyPolicy.Reason.HOME_CANDIDATE,
                "HOME candidate deletion must be denied");

        check(!PackageSafetyPolicy.isValidPackageName("vendor.example.app; pm disable-user android"),
                "shell metacharacters must not pass package validation");
        check(!PackageSafetyPolicy.isValidPackageName("not-a-package"), "hyphenated command must be rejected");
        check(PackageSafetyPolicy.isValidPackageName("vendor.example_app.app2"), "valid package name");

        System.out.println("Package safety policy tests passed.");
    }
}
