package com.techgenius.tvoslauncher;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;

/**
 * Small launcher utilities retained for safe app launching and cached artwork.
 * The former recommendations, news feeds and streaming-service discovery engine has been removed.
 */
final class Suggestions {
    static final class Provider {
        final int id;
        final String name, pkg, jwShort, jwTech;
        Provider(int id, String name, String pkg, String jwShort, String jwTech) {
            this.id = id;
            this.name = name;
            this.pkg = pkg;
            this.jwShort = jwShort;
            this.jwTech = jwTech;
        }
    }

    static final class Item {
        int id, providerId;
        String type = "", title = "", overview = "", backdrop = "", logo = "", year = "", genres = "";
        String link = "", pkg = "", key = "";
        float progress = -1f;
        int remainingMin = -1;
        double rating;
        long published;

        Provider provider() { return null; }
        Provider provider(Context c) { return null; }
    }

    static boolean installed(Context c, String pkg) {
        if (c == null || pkg == null || pkg.isEmpty()) return false;
        try {
            c.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    static boolean start(Context c, Intent i) {
        if (c == null || i == null) return false;
        try {
            c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            return false;
        }
    }

    static Intent view(String url, String pkg) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        return pkg == null || pkg.isEmpty() ? i : i.setPackage(pkg);
    }

    static void open(Context c, Item item) {
        // No recommendation catalogue is present in the single-user Russian build.
    }

    static String logoFor(Context c, Item item) { return ""; }

        static Bitmap image(Context c, String path, String size, int maxWidth) {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("file://")) {
            path = Uri.parse(path).getPath();
            if (path == null || path.isEmpty()) return null;
        }
        if (path.startsWith("/")) {
            return decodeLocal(path, maxWidth);
        }
        if (path.startsWith("content://") || path.startsWith("android.resource://")) {
            try (InputStream in = c.getContentResolver().openInputStream(Uri.parse(path))) {
                if (in == null) return null;
                byte[] data = readAll(in);
                if (data == null || data.length == 0) return null;
                BitmapFactory.Options o = bounds(data, maxWidth);
                return BitmapFactory.decodeByteArray(data, 0, data.length, o);
            } catch (Exception e) {
                return null;
            }
        }
        if (!path.startsWith("http://") && !path.startsWith("https://")) return null;
        try {
            File dir = new File(c.getCacheDir(), "artwork");
            dir.mkdirs();
            File f = new File(dir, "u" + Integer.toHexString(path.hashCode()) + ".img");
            if (!f.exists()) {
                HttpURLConnection con = (HttpURLConnection) new URL(path).openConnection();
                con.setInstanceFollowRedirects(true);
                con.setConnectTimeout(8000);
                con.setReadTimeout(15000);
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android TV) AppleWebKit/537.36 Chrome/122 Safari/537.36");
                try {
                    if (con.getResponseCode() != 200) return null;
                    byte[] data = readAll(con.getInputStream());
                    File tmp = new File(dir, f.getName() + ".part");
                    try (OutputStream os = new FileOutputStream(tmp)) { os.write(data); }
                    if (!tmp.renameTo(f)) tmp.delete();
                } finally {
                    con.disconnect();
                }
            }
            return decodeLocal(f.getPath(), maxWidth);
        } catch (Exception e) {
            return null;
        }
    }

    private static BitmapFactory.Options bounds(byte[] data, int maxWidth) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, o);
        o.inSampleSize = 1;
        while (o.outWidth / (o.inSampleSize * 2) >= maxWidth) o.inSampleSize *= 2;
        o.inJustDecodeBounds = false;
        return o;
    }

    private static Bitmap decodeLocal(String path, int maxWidth) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        o.inSampleSize = 1;
        while (o.outWidth / (o.inSampleSize * 2) >= maxWidth) o.inSampleSize *= 2;
        o.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, o);
    }

        static byte[] readAll(InputStream in) throws Exception {
        try (InputStream is = in) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[16384];
            int n;
            while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
            return bos.toByteArray();
        }
    }
}
