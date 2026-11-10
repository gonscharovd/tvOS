package com.techgenius.tvoslauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.IntentFilter;
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
        boolean candidatesQuerySucceeded = false;
        boolean selectionQuerySucceeded = false;
        List<IntentFilter> preferredFilters = new ArrayList<>();
        List<ComponentName> preferredActivities = new ArrayList<>();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addCategory(Intent.CATEGORY_DEFAULT);
        try {
            int homeFlags = PackageManager.MATCH_DEFAULT_ONLY | PackageManager.MATCH_DISABLED_COMPONENTS;
            resolvedHomes.addAll(pm.queryIntentActivities(homeIntent, homeFlags));
            candidatesQuerySucceeded = true;
        } catch (RuntimeException | LinkageError ignored) {
            // A restricted or broken PackageManager must not be replaced with a guessed package name.
        }
        try {
            // Preferred activities represent an explicit system choice. resolveActivity() can return
            // a high-priority candidate (including testOS) even when the user has not selected it.
            pm.getPreferredActivities(preferredFilters, preferredActivities, null);
            selectionQuerySucceeded = true;
        } catch (RuntimeException | LinkageError ignored) {
            // Without an explicit preferred HOME, do not infer the selected app from candidate order.
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
                } catch (PackageManager.NameNotFoundException | RuntimeException | LinkageError ignored) {
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
                    roleQuerySucceeded = true;
                    // Public RoleManager API only tells this app whether it holds ROLE_HOME.
                    if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                        roleHolders = Collections.singletonList(context.getPackageName());
                    } else {
                        roleHolders = Collections.emptyList();
                    }
                }
            } catch (RuntimeException | LinkageError ignored) {
                // PackageManager's preferred HOME remains a fallback if RoleManager is unavailable.
            }
        }

        String preferredPackage = null;
        String preferredActivity = null;
        if (selectionQuerySucceeded) {
            List<ComponentName> selectedHomes = new ArrayList<>();
            int count = Math.min(preferredFilters.size(), preferredActivities.size());
            for (int i = 0; i < count; i++) {
                IntentFilter filter = preferredFilters.get(i);
                ComponentName component = preferredActivities.get(i);
                if (filter == null || component == null
                        || !filter.hasAction(Intent.ACTION_MAIN)
                        || !filter.hasCategory(Intent.CATEGORY_HOME)) continue;
                String className = component.getClassName() == null
                        ? "" : component.getClassName().toLowerCase(java.util.Locale.US);
                if (className.contains("resolveractivity") || className.contains("fallbackhome")
                        || className.contains("chooseractivity")) continue;
                if (!selectedHomes.contains(component)) selectedHomes.add(component);
            }
            // Multiple distinct preferred HOME components are ambiguous. Do not choose the first.
            if (selectedHomes.size() == 1) {
                preferredPackage = selectedHomes.get(0).getPackageName();
                preferredActivity = selectedHomes.get(0).getClassName();
            }
        }

        return HomeAppDetector.evaluate(Build.VERSION.SDK_INT, roleAvailable, roleQuerySucceeded,
                roleHolders, preferredPackage, preferredActivity,
                candidatesQuerySucceeded, candidates, context.getPackageName());
    }
}
