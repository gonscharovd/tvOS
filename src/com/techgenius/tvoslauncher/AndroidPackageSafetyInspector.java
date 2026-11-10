package com.techgenius.tvoslauncher;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Android-backed evidence collector for PackageSafetyPolicy.
 * It performs read-only PackageManager / DevicePolicyManager inspection; it never executes shell.
 */
final class AndroidPackageSafetyInspector {
    private static final String APK_MIME = "application/vnd.android.package-archive";

    private static final String[] CRITICAL_PERMISSIONS = {
            "android.permission.STATUS_BAR_SERVICE",
            "android.permission.EXPAND_STATUS_BAR",
            "android.permission.GRANT_RUNTIME_PERMISSIONS",
            "android.permission.REVOKE_RUNTIME_PERMISSIONS",
            "android.permission.ADJUST_RUNTIME_PERMISSIONS_POLICY",
            "android.permission.GET_RUNTIME_PERMISSIONS",
            "android.permission.MANAGE_PERMISSION_APPS",
            "android.permission.MANAGE_APP_OPS_MODES",
            "android.permission.MANAGE_ROLE_HOLDERS",
            "android.permission.REBOOT",
            "android.permission.DEVICE_POWER",
            "android.permission.SHUTDOWN",
            "android.permission.MASTER_CLEAR",
            "android.permission.RECOVERY",
            "android.permission.UPDATE_PACKAGES",
            "android.permission.INSTALL_PACKAGES",
            "android.permission.DELETE_PACKAGES",
            "android.permission.MANAGE_USERS",
            "android.permission.MANAGE_DEVICE_ADMINS",
            "android.permission.WRITE_SECURE_SETTINGS",
            "android.permission.CONNECTIVITY_INTERNAL",
            "android.permission.NETWORK_STACK",
            "android.permission.NETWORK_SETTINGS",
            "android.permission.MANAGE_NETWORK_POLICY",
            "android.permission.BLUETOOTH_PRIVILEGED",
            "android.permission.TV_INPUT_HARDWARE",
            "android.permission.BIND_TV_INPUT",
            "android.permission.BIND_INPUT_METHOD",
            "android.permission.BIND_DEVICE_ADMIN",
            "android.permission.BIND_ACCESSIBILITY_SERVICE",
            "android.permission.BIND_VPN_SERVICE",
            "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
            "android.permission.BIND_QUICK_SETTINGS_TILE",
            "android.permission.BIND_WALLPAPER",
            "android.permission.BIND_DREAM_SERVICE",
            "android.permission.BIND_PRINT_SERVICE"
    };

    private static final String[] CRITICAL_SERVICE_ACTIONS = {
            "android.media.tv.TvInputService",
            "android.view.InputMethod",
            "android.accessibilityservice.AccessibilityService",
            "android.app.action.DEVICE_ADMIN_ENABLED",
            "android.net.VpnService",
            "android.service.notification.NotificationListenerService",
            "android.service.quicksettings.action.QS_TILE",
            "android.service.wallpaper.WallpaperService",
            "android.service.dreams.DreamService",
            "android.service.voice.VoiceInteractionService",
            "android.service.textservice.SpellCheckerService"
    };

    private final Context context;
    private final PackageManager packageManager;
    private final DevicePolicyManager devicePolicyManager;
    private final HomeAppDetector.Result home;
    private final DeviceProfile deviceProfile;
    private final Set<String> activeAdminPackages;
    private final Set<String> criticalRolePackages;
    private final boolean devicePolicyQueryKnown;
    private final boolean criticalRoleQueryKnown;
    private AndroidPackageSafetyInspector(Context context, PackageManager pm,
            DevicePolicyManager dpm, HomeAppDetector.Result home, DeviceProfile profile,
            Set<String> activeAdmins, Set<String> criticalRoles, boolean dpmKnown,
            boolean roleKnown) {
        this.context = context;
        this.packageManager = pm;
        this.devicePolicyManager = dpm;
        this.home = home;
        this.deviceProfile = profile;
        this.activeAdminPackages = activeAdmins;
        this.criticalRolePackages = criticalRoles;
        this.devicePolicyQueryKnown = dpmKnown;
        this.criticalRoleQueryKnown = roleKnown;
    }

    static AndroidPackageSafetyInspector create(Context context, HomeAppDetector.Result home) {
        if (context == null) return unknownSession();
        PackageManager pm;
        try {
            pm = context.getPackageManager();
        } catch (RuntimeException | LinkageError e) {
            return unknownSession();
        }
        if (pm == null) return unknownSession();

        DevicePolicyManager dpm = null;
        Set<String> admins = new HashSet<>();
        boolean dpmKnown = false;
        try {
            dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (dpm != null) {
                List<ComponentName> active = dpm.getActiveAdmins();
                if (active != null) {
                    for (ComponentName component : active) {
                        if (component != null && component.getPackageName() != null) {
                            admins.add(component.getPackageName());
                        }
                    }
                }
                dpmKnown = true;
            }
        } catch (RuntimeException | LinkageError ignored) {
            dpmKnown = false;
        }

        Set<String> criticalRoles = new HashSet<>();
        boolean roleKnown = collectCriticalRoles(pm, criticalRoles);
        DeviceProfile profile = null;
        try {
            profile = DeviceProfileResolver.fromSystem(context);
        } catch (RuntimeException | LinkageError ignored) {
            // Unknown device identity is deliberately retained as an unknown profile.
        }

        return new AndroidPackageSafetyInspector(context, pm, dpm, home, profile,
                admins, criticalRoles, dpmKnown, roleKnown);
    }

    private static AndroidPackageSafetyInspector unknownSession() {
        return new AndroidPackageSafetyInspector(null, null, null, null, null,
                Collections.<String>emptySet(), Collections.<String>emptySet(),
                false, false);
    }

    private static boolean collectCriticalRoles(PackageManager pm, Set<String> out) {
        try {
            int flags = PackageManager.MATCH_DISABLED_COMPONENTS;
            for (String action : CRITICAL_SERVICE_ACTIONS) {
                List<ResolveInfo> services = pm.queryIntentServices(new Intent(action), flags);
                if (services == null) return false;
                for (ResolveInfo info : services) {
                    ServiceInfo service = info == null ? null : info.serviceInfo;
                    if (service != null && service.packageName != null) out.add(service.packageName);
                }
            }

            List<Intent> activities = new ArrayList<>();
            activities.add(new Intent(Settings.ACTION_SETTINGS));
            // The permission-controller actions are hidden from the public SDK; resolving them
            // by their stable intent strings avoids hidden API calls while still discovering handlers.
            activities.add(new Intent("android.intent.action.MANAGE_APP_PERMISSIONS"));
            activities.add(new Intent("android.intent.action.MANAGE_PERMISSIONS"));
            activities.add(new Intent(Intent.ACTION_INSTALL_PACKAGE)
                    .setDataAndType(Uri.parse("content://testos.invalid/package.apk"), APK_MIME));
            activities.add(new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.parse("content://testos.invalid/package.apk"), APK_MIME));
            boolean permissionControllerActionFound = false;
            for (Intent intent : activities) {
                List<ResolveInfo> resolved = pm.queryIntentActivities(intent, flags);
                if (resolved == null) return false;
                if (("android.intent.action.MANAGE_APP_PERMISSIONS".equals(intent.getAction())
                        || "android.intent.action.MANAGE_PERMISSIONS".equals(intent.getAction()))
                        && !resolved.isEmpty()) {
                    permissionControllerActionFound = true;
                }
                for (ResolveInfo info : resolved) {
                    if (info != null && info.activityInfo != null
                            && info.activityInfo.packageName != null) {
                        out.add(info.activityInfo.packageName);
                    }
                }
            }
            return Build.VERSION.SDK_INT < 29 || permissionControllerActionFound;
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    PackageSafetyPolicy.Decision evaluate(String pkg) {
        if (context == null || packageManager == null || pkg == null) {
            return PackageSafetyPolicy.evaluate(facts(pkg, false, false, false, false,
                    false, false, false, false, false, false, false, false,
                    PackageSafetyPolicy.HomeState.UNKNOWN, null));
        }

        boolean valid = PackageSafetyPolicy.isValidPackageName(pkg);
        if (!valid) {
            return PackageSafetyPolicy.evaluate(facts(pkg, false, false, false, false,
                    false, false, false, false, false, false, false, false,
                    PackageSafetyPolicy.HomeState.SAFE_NON_HOME, null));
        }

        PackageSafetyPolicy.HomeState homeState = homeState(pkg);
        ApplicationInfo app;
        try {
            app = packageManager.getApplicationInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS);
        } catch (PackageManager.NameNotFoundException | RuntimeException | LinkageError e) {
            return PackageSafetyPolicy.evaluate(facts(pkg, false, false, false, false,
                    false, false, false, false, false, false, false, false, homeState));
        }

        boolean systemApp = (app.flags & (ApplicationInfo.FLAG_SYSTEM
                | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
        boolean persistent = (app.flags & ApplicationInfo.FLAG_PERSISTENT) != 0;
        boolean systemUid = app.uid < Process.FIRST_APPLICATION_UID;

        boolean ownerKnown = devicePolicyQueryKnown;
        boolean owner = activeAdminPackages.contains(pkg);
        if (ownerKnown && devicePolicyManager != null) {
            try {
                owner = owner || devicePolicyManager.isDeviceOwnerApp(pkg)
                        || devicePolicyManager.isProfileOwnerApp(pkg);
            } catch (RuntimeException | LinkageError ignored) {
                ownerKnown = false;
            }
        }

        boolean permissionKnown = true;
        boolean criticalPermission = false;
        try {
            for (String permission : CRITICAL_PERMISSIONS) {
                if (packageManager.checkPermission(permission, pkg) == PackageManager.PERMISSION_GRANTED) {
                    criticalPermission = true;
                    break;
                }
            }
        } catch (RuntimeException | LinkageError ignored) {
            permissionKnown = false;
        }

        boolean criticalRole = criticalRolePackages.contains(pkg);
        boolean roleKnown = criticalRoleQueryKnown;

        boolean knownOem = deviceProfile != null && deviceProfile.isKnownOem();
        boolean inSelectedProfile = knownOem
                && CleanYourTv.isSelectedModelProfileCandidate(context, pkg, deviceProfile);

        String label = "";
        try {
            CharSequence loaded = app.loadLabel(packageManager);
            if (loaded != null) label = loaded.toString();
        } catch (RuntimeException ignored) {
            // Package label is cosmetic; package name and platform facts remain authoritative.
        }
        PackageSafetyPolicy.Facts facts = facts(pkg, true, systemApp, persistent,
                systemUid, ownerKnown, owner, roleKnown, criticalRole, permissionKnown,
                criticalPermission, knownOem, inSelectedProfile, homeState, label);
        return PackageSafetyPolicy.evaluate(facts);
    }

    private PackageSafetyPolicy.HomeState homeState(String pkg) {
        if (home == null) return PackageSafetyPolicy.HomeState.UNKNOWN;
        if (home.isCurrentHome(pkg)) return PackageSafetyPolicy.HomeState.SELECTED_HOME;
        if (home.candidatesKnown && !home.candidates.isEmpty()) {
            for (HomeAppDetector.Candidate candidate : home.candidates) {
                if (candidate != null && pkg.equals(candidate.packageName)) {
                    return PackageSafetyPolicy.HomeState.HOME_CANDIDATE;
                }
            }
            if (home.currentHomeKnown) return PackageSafetyPolicy.HomeState.SAFE_NON_HOME;
        }
        return PackageSafetyPolicy.HomeState.UNKNOWN;
    }

    private PackageSafetyPolicy.Facts facts(String pkg, boolean packageInfoKnown,
            boolean systemApp, boolean persistent, boolean systemUid,
            boolean ownerKnown, boolean owner, boolean rolesKnown, boolean criticalRole,
            boolean permissionsKnown, boolean criticalPermission, boolean knownOem,
            boolean inProfile, PackageSafetyPolicy.HomeState homeState) {
        return facts(pkg, packageInfoKnown, systemApp, persistent, systemUid,
                ownerKnown, owner, rolesKnown, criticalRole, permissionsKnown,
                criticalPermission, knownOem, inProfile, homeState, null);
    }

    private PackageSafetyPolicy.Facts facts(String pkg, boolean packageInfoKnown,
            boolean systemApp, boolean persistent, boolean systemUid,
            boolean ownerKnown, boolean owner, boolean rolesKnown, boolean criticalRole,
            boolean permissionsKnown, boolean criticalPermission, boolean knownOem,
            boolean inProfile, PackageSafetyPolicy.HomeState homeState, String appLabel) {
        boolean valid = PackageSafetyPolicy.isValidPackageName(pkg);
        boolean own = context != null && pkg != null && pkg.equals(context.getPackageName());
        boolean legacyProtected = CleanYourTv.isLegacyHardProtectedPackage(pkg)
                || CleanYourTv.isCriticalProtectedService(pkg, appLabel);
        boolean coreProtected = CleanYourTv.isCoreAndroidProtectedPackage(pkg);
        return new PackageSafetyPolicy.Facts(pkg, valid, own, legacyProtected, coreProtected,
                homeState, packageInfoKnown, systemApp, persistent, systemUid,
                ownerKnown, owner, rolesKnown, criticalRole, permissionsKnown,
                criticalPermission, knownOem, inProfile);
    }
}
