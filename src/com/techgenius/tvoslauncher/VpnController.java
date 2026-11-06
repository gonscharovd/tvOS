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
    private static final String HAPP_WIDGET_ACTION = HAPP_PACKAGE + ".action.widget.click";
    private static final ComponentName HAPP_WIDGET_RECEIVER =
            new ComponentName(HAPP_PACKAGE, HAPP_PACKAGE + ".receiver.WidgetProvider");

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
            ActivityInfo receiver = pm.getReceiverInfo(HAPP_WIDGET_RECEIVER, 0);
            if (receiver == null || !receiver.enabled || !receiver.exported) return false;

            Intent intent = new Intent(HAPP_WIDGET_ACTION);
            intent.setComponent(HAPP_WIDGET_RECEIVER);
            context.sendBroadcast(intent);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
