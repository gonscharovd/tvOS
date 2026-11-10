package com.techgenius.tvoslauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.app.role.RoleManager;
import android.content.Intent;
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
        ComponentName resolvedHome = null;
        boolean candidatesQuerySucceeded = false;
        boolean selectionQuerySucceeded = false;
        Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addCategory(Intent.CATEGORY_DEFAULT);
        try {
            resolvedHomes.addAll(pm.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY));
            candidatesQuerySucceeded = true;
        } catch (RuntimeException | LinkageError ignored) {
            // A restricted or broken PackageManager must not be replaced with a guessed package name.
        }
        try {
            resolvedHome = pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
            selectionQuerySucceeded = true;
        } catch (RuntimeException | LinkageError ignored) {
            // A resolver activity is not treated as the selected HOME below.
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
        if (selectionQuerySucceeded && resolvedHome != null) {
            // resolveActivity may return ResolverActivity when the user has not selected a default.
            // Only accept it when it is one of the actual HOME candidates discovered above.
            for (HomeAppDetector.Candidate candidate : candidates) {
                String resolvedClass = resolvedHome.getClassName() == null
                        ? "" : resolvedHome.getClassName().toLowerCase(java.util.Locale.US);
                boolean resolverComponent = resolvedClass.contains("resolveractivity")
                        || resolvedClass.contains("fallbackhome")
                        || resolvedClass.contains("chooseractivity");
                if (!resolverComponent && candidate.packageName.equals(resolvedHome.getPackageName())) {
                    preferredPackage = resolvedHome.getPackageName();
                    preferredActivity = resolvedHome.getClassName();
                    break;
                }
            }
        }

        return HomeAppDetector.evaluate(Build.VERSION.SDK_INT, roleAvailable, roleQuerySucceeded,
                roleHolders, preferredPackage, preferredActivity,
                candidatesQuerySucceeded, candidates, context.getPackageName());
    }
}
