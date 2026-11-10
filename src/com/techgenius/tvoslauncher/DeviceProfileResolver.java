package com.techgenius.tvoslauncher;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reads Android platform identity and delegates vendor classification to DeviceProfile. */
final class DeviceProfileResolver {
    private DeviceProfileResolver() { }

    static DeviceProfile fromSystem(Context context) {
        Set<String> packages = new HashSet<>();
        boolean inventoryKnown = false;
        if (context != null) {
            try {
                PackageManager pm = context.getPackageManager();
                if (pm != null) {
                    List<ApplicationInfo> installed = pm.getInstalledApplications(0);
                    if (installed != null && !installed.isEmpty()) {
                        for (ApplicationInfo app : installed) {
                            if (app != null && app.packageName != null) packages.add(app.packageName);
                        }
                        inventoryKnown = !packages.isEmpty();
                    }
                }
            } catch (RuntimeException | LinkageError ignored) {
                // An incomplete package inventory must not authorize system-package debloating.
                inventoryKnown = false;
            }
        }
        return DeviceProfile.resolve(Build.MANUFACTURER, Build.BRAND, Build.MODEL,
                Build.DEVICE, Build.PRODUCT, packages, inventoryKnown);
    }
}
