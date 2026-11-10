package com.techgenius.tvoslauncher;

import java.util.regex.Pattern;

/**
 * Pure-Java, fail-closed policy for package disable operations.
 *
 * A device profile is evidence that a package belongs to a curated model list; it is never
 * sufficient by itself to authorize disabling a system package.
 */
public final class PackageSafetyPolicy {
    public enum State { SAFE_TO_DISABLE, PROTECTED, UNKNOWN }

    public enum HomeState { SAFE_NON_HOME, SELECTED_HOME, HOME_CANDIDATE, UNKNOWN }

    public enum Reason {
        NONE,
        INVALID_PACKAGE_NAME,
        OWN_APPLICATION,
        LEGACY_HARD_PROTECTION,
        CORE_ANDROID_PROTECTION,
        SELECTED_HOME,
        HOME_CANDIDATE,
        HOME_STATE_UNKNOWN,
        PACKAGE_INFO_UNKNOWN,
        DEVICE_OWNER_QUERY_UNKNOWN,
        DEVICE_OWNER_OR_ACTIVE_ADMIN,
        CRITICAL_ROLE_QUERY_UNKNOWN,
        CRITICAL_SYSTEM_ROLE,
        CRITICAL_PERMISSION_QUERY_UNKNOWN,
        CRITICAL_PERMISSION,
        SYSTEM_UID,
        PRIVILEGED_SYSTEM_APPLICATION,
        PERSISTENT_SYSTEM_APPLICATION,
        UNKNOWN_OEM_SYSTEM_APPLICATION,
        SYSTEM_PACKAGE_NOT_IN_SELECTED_PROFILE,
        REVIEWED_PROFILE_SYSTEM_APPLICATION,
        NON_SYSTEM_APPLICATION
    }

    public static final class Facts {
        public final String packageName;
        public final boolean packageNameValid;
        public final boolean ownApplication;
        public final boolean legacyHardProtected;
        public final boolean coreAndroidProtected;
        public final HomeState homeState;
        public final boolean packageInfoKnown;
        public final boolean systemApplication;
        public final boolean privilegedApplication;
        public final boolean persistentApplication;
        public final boolean systemUid;
        public final boolean deviceOwnerQueryKnown;
        public final boolean deviceOwnerOrActiveAdmin;
        public final boolean criticalRoleQueryKnown;
        public final boolean criticalSystemRole;
        public final boolean criticalPermissionQueryKnown;
        public final boolean criticalPermission;
        public final boolean knownOem;
        public final boolean inSelectedModelProfile;

        public Facts(String packageName, boolean packageNameValid, boolean ownApplication,
                     boolean legacyHardProtected, boolean coreAndroidProtected, HomeState homeState,
                     boolean packageInfoKnown, boolean systemApplication, boolean privilegedApplication,
                     boolean persistentApplication, boolean systemUid, boolean deviceOwnerQueryKnown,
                     boolean deviceOwnerOrActiveAdmin, boolean criticalRoleQueryKnown,
                     boolean criticalSystemRole, boolean criticalPermissionQueryKnown,
                     boolean criticalPermission, boolean knownOem, boolean inSelectedModelProfile) {
            this.packageName = packageName;
            this.packageNameValid = packageNameValid;
            this.ownApplication = ownApplication;
            this.legacyHardProtected = legacyHardProtected;
            this.coreAndroidProtected = coreAndroidProtected;
            this.homeState = homeState == null ? HomeState.UNKNOWN : homeState;
            this.packageInfoKnown = packageInfoKnown;
            this.systemApplication = systemApplication;
            this.privilegedApplication = privilegedApplication;
            this.persistentApplication = persistentApplication;
            this.systemUid = systemUid;
            this.deviceOwnerQueryKnown = deviceOwnerQueryKnown;
            this.deviceOwnerOrActiveAdmin = deviceOwnerOrActiveAdmin;
            this.criticalRoleQueryKnown = criticalRoleQueryKnown;
            this.criticalSystemRole = criticalSystemRole;
            this.criticalPermissionQueryKnown = criticalPermissionQueryKnown;
            this.criticalPermission = criticalPermission;
            this.knownOem = knownOem;
            this.inSelectedModelProfile = inSelectedModelProfile;
        }
    }

    public static final class Decision {
        public final State state;
        public final Reason reason;

        private Decision(State state, Reason reason) {
            this.state = state;
            this.reason = reason;
        }

        public boolean isSafeToDisable() {
            return state == State.SAFE_TO_DISABLE;
        }
    }

    private static final Pattern PACKAGE_NAME =
            Pattern.compile("^[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+$");

    private PackageSafetyPolicy() { }

    public static boolean isValidPackageName(String packageName) {
        return packageName != null && PACKAGE_NAME.matcher(packageName).matches();
    }

    public static Decision evaluate(Facts f) {
        if (f == null) return unknown(Reason.PACKAGE_INFO_UNKNOWN);
        if (!f.packageNameValid || !isValidPackageName(f.packageName)) {
            return protectedResult(Reason.INVALID_PACKAGE_NAME);
        }
        if (f.ownApplication) return protectedResult(Reason.OWN_APPLICATION);
        if (f.legacyHardProtected) return protectedResult(Reason.LEGACY_HARD_PROTECTION);
        if (f.coreAndroidProtected) return protectedResult(Reason.CORE_ANDROID_PROTECTION);

        if (f.homeState == HomeState.SELECTED_HOME) return protectedResult(Reason.SELECTED_HOME);
        if (f.homeState == HomeState.HOME_CANDIDATE) return protectedResult(Reason.HOME_CANDIDATE);
        if (f.homeState != HomeState.SAFE_NON_HOME) return unknown(Reason.HOME_STATE_UNKNOWN);

        if (!f.packageInfoKnown) return unknown(Reason.PACKAGE_INFO_UNKNOWN);
        if (!f.deviceOwnerQueryKnown) return unknown(Reason.DEVICE_OWNER_QUERY_UNKNOWN);
        if (f.deviceOwnerOrActiveAdmin) return protectedResult(Reason.DEVICE_OWNER_OR_ACTIVE_ADMIN);
        if (!f.criticalRoleQueryKnown) return unknown(Reason.CRITICAL_ROLE_QUERY_UNKNOWN);
        if (f.criticalSystemRole) return protectedResult(Reason.CRITICAL_SYSTEM_ROLE);
        if (!f.criticalPermissionQueryKnown) return unknown(Reason.CRITICAL_PERMISSION_QUERY_UNKNOWN);
        if (f.criticalPermission) return protectedResult(Reason.CRITICAL_PERMISSION);

        if (f.systemUid) return protectedResult(Reason.SYSTEM_UID);
        if (!f.systemApplication) return safe(Reason.NON_SYSTEM_APPLICATION);
        if (f.privilegedApplication) return protectedResult(Reason.PRIVILEGED_SYSTEM_APPLICATION);
        if (f.persistentApplication) return protectedResult(Reason.PERSISTENT_SYSTEM_APPLICATION);
        if (!f.knownOem) return unknown(Reason.UNKNOWN_OEM_SYSTEM_APPLICATION);
        if (!f.inSelectedModelProfile) return unknown(Reason.SYSTEM_PACKAGE_NOT_IN_SELECTED_PROFILE);

        return safe(Reason.REVIEWED_PROFILE_SYSTEM_APPLICATION);
    }

    private static Decision safe(Reason reason) {
        return new Decision(State.SAFE_TO_DISABLE, reason);
    }

    private static Decision protectedResult(Reason reason) {
        return new Decision(State.PROTECTED, reason);
    }

    private static Decision unknown(Reason reason) {
        return new Decision(State.UNKNOWN, reason);
    }
}
