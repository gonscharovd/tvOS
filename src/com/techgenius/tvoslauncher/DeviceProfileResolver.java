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
        if (context != null) {
            try {
                PackageManager pm = context.getPackageManager();
                List<ApplicationInfo> installed = pm.getInstalledApplications(0);
                if (installed != null) {
                    for (ApplicationInfo app : installed) {
                        if (app != null && app.packageName != null) packages.add(app.packageName);
                    }
                }
            } catch (RuntimeException ignored) {
                // Package visibility or OEM PackageManager restrictions must not block startup.
            }
        }
        return DeviceProfile.resolve(Build.MANUFACTURER, Build.BRAND, Build.MODEL,
                Build.DEVICE, Build.PRODUCT, packages);
    }
}
