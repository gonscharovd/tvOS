package com.techgenius.tvoslauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/**
 * Small, Android-9-compatible bridge to Happ's exported widget toggle and system VPN state.
 * The indicator reflects an active Android VPN transport; it does not assume that a button
 * press succeeded.
 */
final class VpnController {
    static final String HAPP_PACKAGE = "su.happ.proxyutility";
    private static final String[] HAPP_PACKAGES = {
            "su.happ.proxyutility", "com.happproxy"
    };

    private VpnController() { }

    static boolean isVpnActive(Context context) {
        if (context == null) return false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            Network[] networks = cm.getAllNetworks();
            if (networks == null) return false;
            for (Network network : networks) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return true;
            }
        } catch (Throwable ignored) { }
        return false;
    }

    /**
     * Sends the same explicit widget-toggle broadcast used by Android's AppWidget host.
     * Returns false if the Happ receiver is missing or is not exported to other applications.
     */
    static void toggleHappWithFeedback(Context context) {
        boolean sent = toggleHapp(context);
        String message = sent
                ? I18n.s(context, "VPN toggle sent to Happ", "Happ VPN anahtarı gönderildi")
                : I18n.s(context, "Happ VPN toggle is unavailable", "Happ VPN geçişi kullanılamıyor");
        android.widget.Toast.makeText(context, message,
                sent ? android.widget.Toast.LENGTH_SHORT : android.widget.Toast.LENGTH_LONG).show();
    }

    static boolean toggleHapp(Context context) {
        if (context == null) return false;
        try {
            PackageManager pm = context.getPackageManager();
            for (String pkg : HAPP_PACKAGES) {
                ComponentName receiverName = new ComponentName(pkg, pkg + ".receiver.WidgetProvider");
                try {
                    ActivityInfo receiver = pm.getReceiverInfo(receiverName, 0);
                    if (receiver == null || !receiver.enabled || !receiver.exported) continue;
                    Intent intent = new Intent(pkg + ".action.widget.click");
                    intent.setComponent(receiverName);
                    context.sendBroadcast(intent);
                    return true;
                } catch (PackageManager.NameNotFoundException ignored) {
                    // Try the other official Happ package variant.
                }
            }
            return false;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
