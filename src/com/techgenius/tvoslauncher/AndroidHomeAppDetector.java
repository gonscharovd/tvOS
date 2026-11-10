package com.techgenius.tvoslauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.app.role.RoleManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Android PackageManager / RoleManager adapter for the vendor-neutral HOME policy model. */
final class AndroidHomeAppDetector {
    private AndroidHomeAppDetector() { }

    static HomeAppDetector.Result detect(Context context) {
        if (context == null) {
            return HomeAppDetector.evaluate(Build.VERSION.SDK_INT, false, false,
                    null, null, null, false, Collections.<HomeAppDetector.Candidate>emptyList(), "");
        }

        PackageManager pm;
        try {
            pm = context.getPackageManager();
        } catch (RuntimeException | LinkageError e) {
            return HomeAppDetector.evaluate(Build.VERSION.SDK_INT, false, false,
                    null, null, null, false, Collections.<HomeAppDetector.Candidate>emptyList(),
                    context.getPackageName());
        }
        if (pm == null) {
            return HomeAppDetector.evaluate(Build.VERSION.SDK_INT, false, false,
                    null, null, null, false, Collections.<HomeAppDetector.Candidate>emptyList(),
                    context.getPackageName());
        }

        List<ResolveInfo> resolvedHomes = new ArrayList<>();
        ComponentName preferredHome = null;
        boolean candidatesQuerySucceeded = false;
        try {
            preferredHome = pm.getHomeActivities(resolvedHomes);
            candidatesQuerySucceeded = true;
        } catch (RuntimeException | LinkageError ignored) {
            // A restricted or broken PackageManager must not be replaced with a guessed package name.
        }

        List<HomeAppDetector.Candidate> candidates = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolvedHomes) {
            if (resolveInfo == null) continue;
            ActivityInfo activity = resolveInfo.activityInfo;
            if (activity == null || activity.packageName == null || activity.packageName.isEmpty()) continue;

            ApplicationInfo app = activity.applicationInfo;
            if (app == null) {
                try {
                    app = pm.getApplicationInfo(activity.packageName, PackageManager.MATCH_DISABLED_COMPONENTS);
                } catch (PackageManager.NameNotFoundException | RuntimeException ignored) {
                    app = null;
                }
            }

            Boolean systemApp = null;
            Boolean enabled = null;
            if (app != null) {
                systemApp = (app.flags & (ApplicationInfo.FLAG_SYSTEM
                        | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
                enabled = app.enabled && activity.enabled;
            }
            candidates.add(new HomeAppDetector.Candidate(
                    activity.packageName, activity.name, systemApp, enabled));
        }

        boolean roleAvailable = false;
        boolean roleQuerySucceeded = false;
        List<String> roleHolders = null;
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                RoleManager roleManager = context.getSystemService(RoleManager.class);
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    roleAvailable = true;
                    roleHolders = roleManager.getRoleHolders(RoleManager.ROLE_HOME);
                    roleQuerySucceeded = roleHolders != null;
                }
            } catch (RuntimeException | LinkageError ignored) {
                // PackageManager's preferred HOME remains a fallback if RoleManager is unavailable.
            }
        }

        return HomeAppDetector.evaluate(Build.VERSION.SDK_INT, roleAvailable, roleQuerySucceeded,
                roleHolders,
                preferredHome == null ? null : preferredHome.getPackageName(),
                preferredHome == null ? null : preferredHome.getClassName(),
                candidatesQuerySucceeded, candidates, context.getPackageName());
    }
}
