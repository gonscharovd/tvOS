package com.techgenius.tvoslauncher;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

/**
 * Permissions, asked for on screen wherever the TV allows it; adb is only the last resort.
 * - Location (Wi-Fi name): the normal runtime prompt.
 * - Do Not Disturb: Android's dedicated notification-policy access screen.
 */
final class Perms {
    static final int REQ_LOCATION = 41;

    private Perms() {
    }

    static boolean hasLocation(Activity a) {
        try {
            return a != null && a.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }

    static void requestLocation(Activity a) {
        if (a != null && !hasLocation(a)) {
            a.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
        }
    }

    static Intent accessibilityScreen(Activity a) {
        if (a == null) return null;
        try {
            Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            ComponentName cn = i.resolveActivity(a.getPackageManager());
            if (cn == null || cn.getClassName() == null || cn.getClassName().contains("Stub")) {
                return null;
            }
            return i;
        } catch (Throwable t) {
            return null;
        }
    }

    static boolean hasDndAccess(android.content.Context a) {
        if (a == null) return false;
        try {
            NotificationManager nm = a.getSystemService(NotificationManager.class);
            return nm != null && nm.isNotificationPolicyAccessGranted();
        } catch (Throwable t) {
            return false;
        }
    }

    static void requestDndAccess(Activity a) {
        if (a == null) return;
        if (hasDndAccess(a)) return;
        if (tryStart(a, new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))) return;
        if (tryStart(a, new Intent(Settings.ACTION_SETTINGS))) return;
        lastResort(a, I18n.s(a, "Do Not Disturb access", "Доступ к режиму «Не беспокоить»"),
                "adb shell cmd notification allow_dnd " + a.getPackageName());
    }

    static void toggleDnd(android.content.Context c) {
        if (c == null) return;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm == null) return;
        if (!hasDndAccess(c)) {
            if (c instanceof Activity) {
                requestDndAccess((Activity) c);
            } else {
                try {
                    c.startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Throwable ignored) {
                }
            }
            return;
        }
        boolean on = nm.getCurrentInterruptionFilter() > NotificationManager.INTERRUPTION_FILTER_ALL;
        try {
            nm.setInterruptionFilter(on
                    ? NotificationManager.INTERRUPTION_FILTER_ALL
                    : NotificationManager.INTERRUPTION_FILTER_PRIORITY);
        } catch (SecurityException e) {
            if (c instanceof Activity) requestDndAccess((Activity) c);
        }
    }

    /** Starts a system screen if the TV really has it (many TVs map settings actions to empty stubs). */
    private static boolean tryStart(Activity a, Intent i) {
        if (a == null || i == null) return false;
        try {
            ComponentName cn = i.resolveActivity(a.getPackageManager());
            if (cn == null || cn.getClassName() == null || cn.getClassName().contains("Stub")) return false;
            a.startActivity(i);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    // ---------------------------------------------------------------- default home screen

    /**
     * Reports whether another enabled system HOME candidate exists.
     * Unknown package visibility is treated conservatively, never as "all stock launchers disabled".
     */
    static boolean hasEnabledStockLauncher(android.content.Context c) {
        if (c == null) return true;
        try {
            return AndroidHomeAppDetector.detect(c)
                    .hasEnabledSystemHomeCandidate(c.getPackageName());
        } catch (Throwable ignored) {
            return true;
        }
    }

    static boolean isDefaultHome(Activity a) {
        return isDefaultHome((android.content.Context) a);
    }

    static boolean isDefaultHome(android.content.Context c) {
        if (c == null) return false;
        try {
            // Preserve the existing accessibility remap semantics: the HOME key can open testOS
            // even when Android's selected HOME role belongs to another launcher.
            if (ButtonMapper.isHomeFixEnabled(c) && ButtonMapper.isServiceEnabled(c)) {
                return true;
            }
            return AndroidHomeAppDetector.detect(c).isCurrentHome(c.getPackageName());
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Become the home screen: offers reliable methods for all Android TVs including TCL & Philips
     * Android 11/12 TVs where standard RoleManager / Home Settings UI is stripped or ignored.
     */
    static void requestDefaultHome(Activity a) {
        if (a == null) return;
        // If inside SettingsActivity or SetupActivity, open the native Default Home setup sub-menu
        if (a instanceof SettingsActivity) {
            ((SettingsActivity) a).openDefaultHomeSubMenu();
            return;
        }
        if (a instanceof SetupActivity) {
            ((SetupActivity) a).openDefaultHomeSubMenu();
            return;
        }

        String[] items = {
            I18n.s(a, "Set Default via Local ADB (Recommended)", "Yerel ADB ile Varsayılan Yap (Önerilen)"),
            I18n.s(a, "Review Other System HOME Apps", "Diğer Sistem HOME Uygulamalarını İncele"),
            I18n.s(a, "Enable Home Button Fix (Accessibility Remap)", "Ana Ekran Tuşu Düzeltmesini Aç (Erişilebilirlik)"),
            I18n.s(a, "Open Android Default Home Settings", "Android Varsayılan Ana Ekran Ayarlarını Aç")
        };

        new AlertDialog.Builder(a, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(I18n.s(a, "Set Default Home Launcher", "Varsayılan Ana Ekran Yap"))
                .setItems(items, (d, which) -> {
                    switch (which) {
                        case 0:
                            LocalAdb.setDefaultLauncherViaAdb(a, (ok, msg) -> a.runOnUiThread(() -> {
                                if (a.isFinishing()) return;
                                if (ok) {
                                    android.widget.Toast.makeText(a,
                                            I18n.s(a, "Default home launcher set via ADB!", "Varsayılan ana ekran ADB ile ayarlandı!"),
                                            android.widget.Toast.LENGTH_SHORT).show();
                                    if (hasEnabledStockLauncher(a)) {
                                        CleanYourTv.showDisableSystemLaunchersDialog(a, null);
                                    }
                                } else {
                                    lastResort(a, I18n.s(a, "Home screen", "Ana ekran"),
                                            "adb shell cmd role add-role-holder --user 0 android.app.role.HOME com.techgenius.tvoslauncher\n"
                                            + "adb shell cmd package set-home-activity com.techgenius.tvoslauncher/.MainActivity\n"
                                            + "adb shell pm disable-user --user 0 com.google.android.tvlauncher");
                                }
                            }));
                            break;
                        case 1:
                            CleanYourTv.showDisableSystemLaunchersDialog(a, null);
                            break;
                        case 2:
                            ButtonMapper.setHomeFixEnabled(a, true);
                            if (!ButtonMapper.isServiceEnabled(a)) {
                                if (LocalAdb.hasWriteSecureSettings(a) && LocalAdb.repairPermissionsViaSecureSettings(a)) {
                                    android.widget.Toast.makeText(a,
                                            I18n.s(a, "Home Button Fix & Accessibility enabled!", "Ana Ekran Tuşu Düzeltmesi ve Erişilebilirlik etkinleştirildi!"),
                                            android.widget.Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Intent acc = accessibilityScreen(a);
                                if (acc != null && tryStart(a, acc)) {
                                    return;
                                }
                                LocalAdb.grantAllPermissions(a, null);
                            }
                            android.widget.Toast.makeText(a,
                                    I18n.s(a, "Home Button Fix enabled!", "Ana Ekran Tuşu Düzeltmesi etkinleştirildi!"),
                                    android.widget.Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            openSystemHomeSettingsUi(a);
                            break;
                    }
                })
                .setNegativeButton(I18n.s(a, "Cancel", "İptal"), null)
                .show();
    }

    static void openSystemHomeSettingsUi(Activity a) {
        if (a == null) return;
        if (tryStart(a, new Intent(Settings.ACTION_HOME_SETTINGS))) return;
        if (Build.VERSION.SDK_INT >= 24 && tryStart(a, new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))) return;
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                android.app.role.RoleManager rm = a.getSystemService(android.app.role.RoleManager.class);
                if (rm != null && rm.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME)
                        && !rm.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)) {
                    Intent roleIntent = rm.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME);
                    if (roleIntent != null) {
                        a.startActivityForResult(roleIntent, 42);
                        return;
                    }
                }
            } catch (Throwable ignored) { }
        }
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        if (tryStart(a, Intent.createChooser(home, I18n.s(a, "Select Home App", "Ana Ekran Uygulamasını Seçin")))) return;
        tryStart(a, new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + a.getPackageName())));
    }

    /** Only when the TV has no screen for it: explain plainly, with the command for whoever installs the TV. */
    private static void lastResort(Activity a, String what, String cmd) {
        new AlertDialog.Builder(a, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(what)
                .setMessage(I18n.s(a,
                        "This TV's settings have no switch for this permission. Whoever set up the TV can turn it on once from a computer on the same network:\n\n",
                        "Bu TV'nin ayarlar\u0131nda bu izin i\u00e7in bir se\u00e7enek yok. TV'yi kuran ki\u015fi ayn\u0131 a\u011fdaki bir bilgisayardan bir kez etkinle\u015ftirebilir:\n\n") + cmd)
                .setPositiveButton(I18n.s(a, "OK", "Tamam"), null)
                .setNeutralButton(I18n.s(a, "App info", "Uygulama bilgisi"), (d, w) -> tryStart(a, new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + a.getPackageName()))))
                .show();
    }
}
