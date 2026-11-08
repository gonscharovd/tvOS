package com.techgenius.tvoslauncher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** testOS home host: Night Scenes, application dock, overlays and lifecycle coordination. */
public class MainActivity extends Activity {

    static final String SETTINGS_KEY = "special:settings";
    static final String INPUTS_KEY = "special:inputs";

    static final String PREF_ORDER = "order";
    static final String PREF_HIDDEN = "hidden";
    static final String PREF_OTHER = "other_apps";
    static final String TV_SETTINGS = "com.android.tv.settings";

    /** Packages placed first (in this order) until the user rearranges the grid. */
    static final List<String> DEFAULT_ORDER = Arrays.asList(
            "com.netflix.ninja",
            "com.amazon.amazonvideo.livingroom",
            "org.smarttube.stable",
            "in.startv.hotstar",
            "com.apple.atve.androidtv.appletv",
            "com.spotify.tv.android",
            "com.mitv.tvhome.mitvplus",
            "com.google.android.youtube.tv",
            "org.videolan.vlc",
            "io.github.jqssun.airplay");

    static final PathInterpolator EASE = new PathInterpolator(0.2f, 0f, 0f, 1f);
    /** tvOS focused icon is ~1.2x its resting size (303x183 vs 248x148 px in Apple's reference). */

    /** Focus lift with a slight spring, like tvOS. */
    static final android.view.animation.OvershootInterpolator LIFT =
            new android.view.animation.OvershootInterpolator(1.3f);

    static class AppEntry {
        String key, pkg, label, defaultLabel;
        Drawable banner, icon;
        Intent launch;
        int color;
        boolean system;
    }

    static SharedPreferences customNamePrefs(Context c) {
        return c.getSharedPreferences("app_custom_names", Context.MODE_PRIVATE);
    }

    public static String getCustomAppName(Context c, String key, String pkg, String fallback) {
        SharedPreferences sp = customNamePrefs(c);
        if (key != null && !key.isEmpty()) {
            String byKey = sp.getString("key:" + key, null);
            if (byKey != null && !byKey.trim().isEmpty()) return byKey.trim();
        }
        if (pkg != null && !pkg.isEmpty()) {
            String byPkg = sp.getString("pkg:" + pkg, null);
            if (byPkg != null && !byPkg.trim().isEmpty()) return byPkg.trim();
        }
        return fallback;
    }

    public static boolean hasCustomAppName(Context c, String key, String pkg) {
        SharedPreferences sp = customNamePrefs(c);
        if (key != null && !key.isEmpty() && sp.contains("key:" + key)) return true;
        return pkg != null && !pkg.isEmpty() && sp.contains("pkg:" + pkg);
    }

    public static void setCustomAppName(Context c, String key, String pkg, String customName) {
        SharedPreferences.Editor ed = customNamePrefs(c).edit();
        String trimmed = customName == null ? "" : customName.trim();
        if (trimmed.isEmpty()) {
            if (key != null && !key.isEmpty()) ed.remove("key:" + key);
            if (pkg != null && !pkg.isEmpty()) ed.remove("pkg:" + pkg);
        } else {
            if (key != null && !key.isEmpty()) ed.putString("key:" + key, trimmed);
            if (pkg != null && !pkg.isEmpty() && !pkg.equals(c.getPackageName())) {
                ed.putString("pkg:" + pkg, trimmed);
            }
        }
        ed.apply();
    }

    /** Scroll position is driven by focus changes, not by ScrollView's own key handling. */
    SharedPreferences prefs;
    final Map<String, AppEntry> all = new LinkedHashMap<>();
    /** Visible home apps, in user order. */
    final List<AppEntry> apps = new ArrayList<>();
    /** Apps moved to "Other Apps" (not on first dock row / main grid, reachable from Other Apps button). */
    final List<AppEntry> otherApps = new ArrayList<>();
    /** Other apps including any dock overflow when the dock is enlarged (> 100%). */

    /** Apps the user hid; reachable through Launcher Settings > Apps > Hidden apps. */

    /** What the grid currently shows: home apps (+ folder tile) or the folder's contents. */

    final Handler handler = new Handler(Looper.getMainLooper());

    FrameLayout root;

    /** Top shelf carousel: two crossfading backdrops + info panel. */

    /** tvOS-style capsule button: glass when idle, solid white with dark text when focused. */

    /** What Liquid Glass samples: a blurred copy of the wallpaper or of the carousel artwork. */

    final LiquidGlass.Backdrop glassSource = () -> glassBg;

    boolean returningFromSettings;
    final java.util.concurrent.ExecutorService io = java.util.concurrent.Executors.newSingleThreadExecutor();

    float unit = 2f;
    Bitmap glassBg;
    int screenW, screenH, dockOuter;

    boolean resumed;
    boolean swallowUp;

    BroadcastReceiver pkgReceiver;

    /** Full-screen card that zooms out of a tile when an app is launched. */
    View launchOverlay;

    static MainActivity activeInstance;
    SceneHome sceneHome;
    String activeLang;

    final Runnable reloadRun = this::reload;

    private void cleanupRemovedFeatureData() {
        android.content.SharedPreferences marker = getSharedPreferences("feature_cleanup", MODE_PRIVATE);
        if (marker.getBoolean("recommendations_news_cloud_removed_v1", false)) return;
        for (String name : new String[]{"suggestions", "nuvio_cloud", "stremio_cloud", "watch_cloud"}) {
            getSharedPreferences(name, MODE_PRIVATE).edit().clear().apply();
        }
        try {
            SingleUserPrefs.homePrefs(this).edit()
                    .remove("shelf_mode").remove("cw_enabled").remove("shelf")
                    .remove("top_shelf_tab").apply();
        } catch (Throwable ignored) { }
        deleteLegacyFeatureFiles(new java.io.File(getCacheDir(), "tmdb"));
        deleteLegacyFeatureFiles(new java.io.File(getFilesDir(), "continue.json"));
        marker.edit().putBoolean("recommendations_news_cloud_removed_v1", true).apply();
    }

    private void deleteLegacyFeatureFiles(java.io.File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            java.io.File[] children = file.listFiles();
            if (children != null) for (java.io.File child : children) deleteLegacyFeatureFiles(child);
        }
        try { file.delete(); } catch (Throwable ignored) { }
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        activeInstance = this;
        cleanupRemovedFeatureData();
        getWindow().setBackgroundDrawable(null);
        prefs = SingleUserPrefs.homePrefs(this);
        String initLang = getIntent() != null ? getIntent().getStringExtra("lang") : null;
        if (initLang != null && !initLang.isEmpty()) I18n.setLang(this, initLang);
        activeLang = I18n.getLang(this);

        android.graphics.Point size = Ui.screen(this);
        screenW = size.x;
        screenH = size.y;
        unit = Ui.unit(this);
        dockOuter = dp(18);

        Clicks.init(this);
        java.util.concurrent.Future<?> appsTask = io.submit(this::loadApps);
        buildChrome();
        try {
            appsTask.get();
        } catch (Exception e) {
            loadApps();
        }
        sceneHome.rebuildDock();
        sceneHome.focusDock(prefs.getInt("last_dock_idx", 0));
        registerPackageReceiver();
        wakeReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                AirPlayGuard.check(c, true);
                LocalAdb.scheduleKeepAliveRepair(c, 1500L);
            }
        };
        registerReceiver(wakeReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        LocalAdb.ensureKeepAliveWatchdog(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) return;
        if (intent.hasExtra("dock_scale")) {
            Scenes.setDockScalePercent(this, intent.getIntExtra("dock_scale", 100));
            if (sceneHome != null) sceneHome.rebuildDock();
        }
        String lang = intent.getStringExtra("lang");
        if (lang != null && !lang.isEmpty()) {
            I18n.setLang(this, lang);
            activeLang = I18n.getLang(this);
            recreate();
            return;
        }
        String panel = intent.getStringExtra("panel");
        if (panel != null) {
            dropPicker();
            pickerLayer = null;
            popoverReturn = null;
            closeMenu();
            RecentAppsOverlay.dismiss();
            ControlPanelOverlay.dismiss();
            if (panel.equals("cc")) showControlCenter();
            else if (panel.equals("recents")) RecentAppsOverlay.showInActivity(this);
            else if (panel.equals("inputs")) ControlPanelOverlay.openInputs(this, null);
            else if (panel.equals("weather")) showWeather();
            else if (panel.equals("settings")) showSettingsChooser();
            else if (panel.equals("search")) showSearch(intent.getStringExtra("q"));
            else if (panel.equals("ambient")) {
                setIntent(intent);
                enterAmbientShowcase();
            } else if (panel.equals("lock")) {
                boolean ext = intent.getBooleanExtra("lock_from_external", false);
                setIntent(intent);
                enterLockScreen(ext);
            }
            return;
        }
        if (ambientShowcase) exitAmbientShowcase();
        dropPicker();
        pickerLayer = null;
        popoverReturn = null;
        closeMenu();
        if (sceneHome != null && sceneHome.isSheetOpen()) sceneHome.closeSheet();
        if (sceneHome != null) {
            if (sceneHome.isDockMoveActive()) sceneHome.endDockMove();
            sceneHome.focusDock(sceneHome.focusedDockIdx);
        }
        resetIdle();
        RecentAppsOverlay.dismiss();
        ControlPanelOverlay.dismiss();
    }

    /** A different display, resolution or density: rebuild so every size is recomputed. */
    @Override
    public void onConfigurationChanged(android.content.res.Configuration c) {
        super.onConfigurationChanged(c);
        android.graphics.Point size = Ui.screen(this);
        if (size.x != screenW || size.y != screenH || Ui.unit(this) != unit) recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUpdater.resumePendingInstallIfAllowed(this);
        if (!I18n.getLang(this).equals(activeLang)) {
            recreate();
            return;
        }
        resumed = true;
        activeInstance = this;
        clearLaunchDim();
        boolean wasReturningFromSettings = returningFromSettings;
        returningFromSettings = false;
        if (wasReturningFromSettings) {
            loadApps();
            sceneHome.rebuildDock();
        }
        LocalAdb.grantVolumeLongPressPermissionIfNeeded(this, null);
        if (sceneHome != null) {
            sceneHome.sceneView.syncSettings();
            sceneHome.sceneView.start();
            sceneHome.refreshVpnIndicator();
            sceneHome.refreshMoonBtn();
            sceneHome.refreshWeatherBtn();
            if (sceneHome.dockTiles.isEmpty()) sceneHome.rebuildDock();
            if (pickerLayer == null && menuLayer == null && !sceneHome.isSheetOpen()) {
                sceneHome.focusDock(sceneHome.focusedDockIdx);
            }
        }
        if (!SetupActivity.done(this)) {
            startActivity(new Intent(this, SetupActivity.class));
            return;
        }
        if (getSharedPreferences("screensaver", MODE_PRIVATE).getBoolean("allow", true)) {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        updateWeather();
        resetIdle();
        handler.removeCallbacks(weatherTick);
        handler.post(weatherTick);
        LocalAdb.ensureKeepAliveWatchdog(this);
        LocalAdb.scheduleKeepAliveRepair(this, 1000L);
        AppUpdater.checkAutoUpdateIfNeeded(this);
        SettingsActivity.prewarmCache(getApplicationContext());
        if (ambientShowcase || (getIntent() != null && "ambient".equals(getIntent().getStringExtra("panel")))) {
            if (getIntent() != null) getIntent().removeExtra("panel");
            enterAmbientShowcase();
        } else if (getIntent() != null && "lock".equals(getIntent().getStringExtra("panel"))) {
            boolean ext = getIntent().getBooleanExtra("lock_from_external", false);
            getIntent().removeExtra("panel");
            getIntent().removeExtra("lock_from_external");
            enterLockScreen(ext);
        }
        if (SetupActivity.done(this) && !Perms.isDefaultHome(this) && !prefs.getBoolean("asked_default_home", false)) {
            prefs.edit().putBoolean("asked_default_home", true).apply();
            new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setTitle(I18n.s(this, "Make testOS your home screen?", "testOS ana ekranınız yapılsın mı?"))
                    .setMessage(I18n.s(this, "The Home button on the remote will open testOS.", "Kumandadaki Ana Ekran düğmesi testOS'ı açacaktır."))
                    .setPositiveButton(I18n.s(this, "Make Home Screen", "Ana Ekran Yap"), (d, w) -> Perms.requestDefaultHome(this))
                    .setNegativeButton(I18n.s(this, "Not Now", "Şimdi Değil"), null).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        resumed = false;
        if (ambientShowcase && android.os.SystemClock.uptimeMillis() - ambientEnterTime > 1000L) {
            exitAmbientShowcase();
        }
        if (sceneHome != null && !ambientShowcase) sceneHome.sceneView.stop();
        handler.removeCallbacks(weatherTick);
        handler.removeCallbacks(lockIdleRunnable);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_UI_HIDDEN && sceneHome != null) {
            sceneHome.releaseTransientCaches();
            sceneHome.sceneView.releaseCache();
        }
        if (level >= TRIM_MEMORY_BACKGROUND && !resumed && sceneHome != null) {
            sceneHome.sceneView.releaseCache();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pkgReceiver != null) unregisterReceiver(pkgReceiver);
        if (wakeReceiver != null) unregisterReceiver(wakeReceiver);
        handler.removeCallbacksAndMessages(null);
    }

    // ---------------------------------------------------------------- layout

    void buildChrome() {
        root = new FrameLayout(this);
        root.setBackground(null);
        sceneHome = new SceneHome(this);
        root.addView(sceneHome.sceneView, match());
        root.addView(sceneHome.overlay, match());
        setContentView(root);
    }

    /** tvOS-style ambient colour field: soft glows of indigo, teal, magenta and amber on navy. */

    void invalidateGlass() {
        if (sceneHome != null) sceneHome.invalidateSceneGlass();
        if (menuLayer != null && menuLayer.getChildCount() > 0) menuLayer.getChildAt(0).invalidate();
    }

    /** The bottom darkening that keeps the dock and labels legible, painted into the image. */
    static Bitmap makeWallpaperBase(String style) {
        int w = 192, h = 108;
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        int base;
        int[] colors;
        switch (style) {
            case "midnight":
                base = 0xFF04060F;
                colors = new int[]{0xFF1B2A6B, 0xFF0F3D63, 0xFF2B1B5E, 0xFF14254F, 0xFF22306E};
                break;
            case "sunset":
                base = 0xFF140812;
                colors = new int[]{0xFFB0412E, 0xFF8E2C6E, 0xFFD08A2E, 0xFF6B2A8E, 0xFFC0563A};
                break;
            case "ocean":
                base = 0xFF031418;
                colors = new int[]{0xFF0C6E86, 0xFF0B8C76, 0xFF1A4FA0, 0xFF0E5E6E, 0xFF2A7FB0};
                break;
            case "graphite":
                base = 0xFF0C0C0E;
                colors = new int[]{0xFF3A3A42, 0xFF2A2C34, 0xFF4A4A52, 0xFF26262C, 0xFF34363E};
                break;
            default:
                base = 0xFF080C1E;
                colors = new int[]{0xFF4B2DB0, 0xFF0B7F8C, 0xFFB02F6E, 0xFFC27A2C, 0xFF2150C0};
        }
        c.drawColor(base);
        float[][] blobs = {      // centre x, centre y, radius (fractions of width)
                {0.14f, 0.18f, 0.62f},
                {0.88f, 0.22f, 0.50f},
                {0.72f, 0.92f, 0.55f},
                {0.20f, 0.95f, 0.42f},
                {0.50f, 0.45f, 0.38f}};
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        for (int i = 0; i < colors.length; i++) {
            int col = colors[i];
            p.setShader(new RadialGradient(blobs[i][0] * w, blobs[i][1] * h, blobs[i][2] * w,
                    new int[]{col & 0xD9FFFFFF, col & 0x59FFFFFF, col & 0x00FFFFFF},
                    new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, p);
        }
        return b;
    }

    /** Round profile avatar: indigo-to-pink gradient with a person glyph. */
    Bitmap avatarBitmap(int size) {
        Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setShader(new LinearGradient(0, 0, 0, size, 0xFFA8A8B0, 0xFF838389, Shader.TileMode.CLAMP));
        float r = size / 2f;
        c.drawCircle(r, r, r, p);
        p.setShader(null);
        p.setColor(0xF2FFFFFF);
        c.drawCircle(r, size * 0.40f, size * 0.17f, p);
        c.save();
        android.graphics.Path clip = new android.graphics.Path();
        clip.addCircle(r, r, r, android.graphics.Path.Direction.CW);
        c.clipPath(clip);
        c.drawOval(size * 0.20f, size * 0.64f, size * 0.80f, size * 1.10f, p);
        c.restore();
        return b;
    }

    /** System/utility apps that get a drawn tvOS-style card instead of their Android art. */
    static final Set<String> TVOS_CARD_PKGS = new HashSet<>(Arrays.asList(
            TV_SETTINGS, "com.android.vending", "com.mitv.dream",
            "com.spotify.tv.android", "com.spotify.music"));

    /**
     * tvOS-style card: soft vertical gradient with a top sheen and the app's glyph centred.
     * Settings gets the silver card with a drawn gear, like tvOS.
     */
    Bitmap tvosCard(AppEntry e) {
        int w = 360, h = 216;
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        boolean settings = TV_SETTINGS.equals(e.pkg);
        boolean playStore = "com.android.vending".equals(e.pkg);
        boolean spotify = "com.spotify.tv.android".equals(e.pkg) || "com.spotify.music".equals(e.pkg);
        int top, bottom;
        if (settings) {                                  // tvOS Settings: silver
            top = 0xFFE9E9EE;
            bottom = 0xFFAFAFB8;
        } else if (playStore) {                          // Play Store: clean white background
            top = 0xFFFFFFFF;
            bottom = 0xFFF2F2F7;
        } else if (spotify) {                            // Spotify: inverted dark background + green logo
            top = 0xFF1E1E22;
            bottom = 0xFF121214;
        } else {                                         // vivid gradient in the app's own hue
            float[] hsv = new float[3];
            Color.colorToHSV(e.color, hsv);
            float sat = hsv[1] < 0.15f ? 0.12f : 0.7f;   // greys stay neutral
            top = Color.HSVToColor(new float[]{hsv[0], sat * 0.85f, 0.92f});
            bottom = Color.HSVToColor(new float[]{(hsv[0] + 8f) % 360f, Math.min(1f, sat * 1.15f), sat < 0.2f ? 0.42f : 0.68f});
        }
        p.setShader(new LinearGradient(0, 0, 0, h, top, bottom, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);

        if (settings) {
            drawGear(c, w / 2f, h / 2f, h * 0.27f, 0xFF4A4A52, 0xFFD2D2D9);
            return b;
        }
        if (playStore) {
            if (Build.VERSION.SDK_INT >= 26 && e.icon instanceof android.graphics.drawable.AdaptiveIconDrawable) {
                Drawable fg = ((android.graphics.drawable.AdaptiveIconDrawable) e.icon).getForeground();
                if (fg != null) {
                    int sz = (int) (h * 0.82f);
                    Rect old = fg.copyBounds();
                    fg.setBounds((w - sz) / 2, (h - sz) / 2, (w + sz) / 2, (h + sz) / 2);
                    fg.draw(c);
                    fg.setBounds(old);
                    return b;
                }
            }
            if (e.icon != null) {
                int sz = (int) (h * 0.50f);
                Rect old = e.icon.copyBounds();
                e.icon.setBounds((w - sz) / 2, (h - sz) / 2, (w + sz) / 2, (h + sz) / 2);
                e.icon.draw(c);
                e.icon.setBounds(old);
            }
            return b;
        }
        if (spotify) {
            Bitmap spLogo = Scenes.loadAssetBitmap(this, "app-logos/com.spotify.tv.android.png", 192, 192);
            if (spLogo != null) {
                int sz = (int) (h * 0.54f);
                float left = (w - sz) / 2f, topY = (h - sz) / 2f;
                c.save();
                android.graphics.Path oval = new android.graphics.Path();
                oval.addOval(left, topY, left + sz, topY + sz, android.graphics.Path.Direction.CW);
                c.clipPath(oval);
                c.drawBitmap(spLogo, null, new RectF(left, topY, left + sz, topY + sz),
                        new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                c.restore();
                spLogo.recycle();
                return b;
            }
        }
        Bitmap glyph = whiteGlyph(e, (int) (h * 0.5f));
        if (glyph != null) {
            // Soft drop shadow under the glyph, like tvOS system icons.
            Paint sp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            sp.setColorFilter(new android.graphics.PorterDuffColorFilter(0x38000000, android.graphics.PorterDuff.Mode.SRC_IN));
            sp.setMaskFilter(new android.graphics.BlurMaskFilter(h * 0.025f, android.graphics.BlurMaskFilter.Blur.NORMAL));
            float x = (w - glyph.getWidth()) / 2f, y = (h - glyph.getHeight()) / 2f;
            c.drawBitmap(glyph.extractAlpha(), x, y + h * 0.015f, sp);
            c.drawBitmap(glyph, x, y, new Paint(Paint.FILTER_BITMAP_FLAG));
        } else if (e.icon != null) {
            int sz = (int) (h * 0.46f);
            Rect old = e.icon.copyBounds();
            e.icon.setBounds((w - sz) / 2, (h - sz) / 2, (w + sz) / 2, (h + sz) / 2);
            e.icon.draw(c);
            e.icon.setBounds(old);
        }
        return b;
    }

    /**
     * The app's glyph as a flat white shape (tvOS system-icon style): uses the adaptive icon's
     * foreground layer when there is one. Returns null for full-bleed artwork that can't be
     * reduced to a glyph, so it is shown as-is instead of becoming a white blob.
     */
    Bitmap whiteGlyph(AppEntry e, int size) {
        if (e.icon == null) return null;
        Drawable d = e.icon;
        float zoom = 1f;
        if (d instanceof android.graphics.drawable.AdaptiveIconDrawable) {
            Drawable fg = ((android.graphics.drawable.AdaptiveIconDrawable) d).getForeground();
            if (fg != null) {
                d = fg;
                zoom = 1.5f;  // adaptive foregrounds keep the glyph in the centre 2/3
            }
        }
        int big = Math.round(size * zoom);
        Bitmap full = Bitmap.createBitmap(big, big, Bitmap.Config.ARGB_8888);
        Rect old = d.copyBounds();
        d.setBounds(0, 0, big, big);
        d.draw(new Canvas(full));
        d.setBounds(old);
        Bitmap g = Bitmap.createBitmap(full, (big - size) / 2, (big - size) / 2, size, size);
        int[] px = new int[size * size];
        g.getPixels(px, 0, size, 0, 0, size, size);
        int solid = 0;
        for (int v : px) if (Color.alpha(v) > 128) solid++;
        if (solid > px.length * 0.6f || solid < px.length * 0.02f) return null;
        for (int i = 0; i < px.length; i++) px[i] = (Color.alpha(px[i]) << 24) | 0xFFFFFF;
        Bitmap out = Bitmap.createBitmap(px, size, size, Bitmap.Config.ARGB_8888);
        if (g != full) g.recycle();
        full.recycle();
        return out;
    }

    /** tvOS Settings-style gear: toothed ring with a hollow centre. */
    static void drawGear(Canvas c, float cx, float cy, float r, int color, int holeColor) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        int teeth = 12;
        float tw = r * 0.30f, th = r * 0.30f;
        for (int i = 0; i < teeth; i++) {
            c.save();
            c.rotate(i * 360f / teeth, cx, cy);
            c.drawRoundRect(cx - tw / 2, cy - r - th * 0.55f, cx + tw / 2, cy - r + th, tw * 0.25f, tw * 0.25f, p);
            c.restore();
        }
        c.drawCircle(cx, cy, r, p);
        p.setColor(holeColor);
        c.drawCircle(cx, cy, r * 0.42f, p);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(r * 0.08f);
        c.drawCircle(cx, cy, r * 0.62f, p);
    }

    /** Frosted glass dock (tvOS): lush rounded corner radius, translucent fill, sheen and rim. */

    /** Fine monochrome noise, tiled over glass to give it a frosted texture. */
    /**
     * Liquid Glass (iOS/tvOS 26), drawn from what is actually behind the surface: a blurred,
     * colour-boosted copy of the backdrop aligned to the surface's screen position; a band along
     * the edges that magnifies it (lensing, like thick glass); a light tint; and a specular rim
     * that is bright top-left and softer bottom-right. No grain.
     */
    static class LiquidGlass extends Drawable {
        interface Backdrop {
            Bitmap get();
        }

        final float r, density;
        final Backdrop backdrop;
        final int screenW, screenH;
        final int baseTint;
        float lens;
        float rimStrength = 1f;
        // Every layer is a single rounded-rect draw (shader-filled), which the GPU does cheaply;
        // there is no clipping and no off-screen/software rendering at draw time.
        final Paint body = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        final Paint lens1 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        final Paint lens2 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
        final android.graphics.RectF box = new android.graphics.RectF(), tmp = new android.graphics.RectF();
        final android.graphics.Matrix m = new android.graphics.Matrix();
        final int[] loc = new int[2];
        Bitmap shaderFor;
        android.graphics.BitmapShader shader1, shader2, shader3;

        LiquidGlass(float r, int tint, Backdrop backdrop, int screenW, int screenH, float density) {
            this.r = r;
            this.baseTint = tint;
            this.backdrop = backdrop;
            this.screenW = screenW;
            this.screenH = screenH;
            this.density = density;
            fill.setColor(tint);
            lens1.setStyle(Paint.Style.STROKE);
            lens2.setStyle(Paint.Style.STROKE);
            lens1.setAlpha(150);
            lens2.setAlpha(150);
            rim.setStyle(Paint.Style.STROKE);
            rim.setStrokeWidth(1.1f * density);
        }

        boolean lit, adaptive;
        int adaptiveTint, minBlack;
        Bitmap lumFor, matFor;
        int lumX = Integer.MIN_VALUE, lumY;
        int matLocX = Integer.MIN_VALUE, matLocY = Integer.MIN_VALUE;
        float matBoxW = -1f, matBoxH = -1f;

        /** Never lighter than this much black: for text-heavy tiles (Control Center). */
        LiquidGlass darker(int alpha) {
            minBlack = alpha;
            return this;
        }
        final int[] sample = new int[64];

        /**
         * Glass for small controls (Liquid Glass "regular"): the tint follows the
         * brightness behind it \u2014 darker over bright artwork so white text stays legible, a light
         * frost over dark artwork so the control stays visible.
         */
        LiquidGlass adaptive() {
            adaptive = true;
            return this;
        }

        void setLit(boolean lit) {
            this.lit = lit;
            fill.setColor(lit ? 0x52FFFFFF : adaptive && adaptiveTint != 0 ? adaptiveTint : baseTint);
            invalidateSelf();
        }

        /** Average luminance (0..1) of the backdrop under this surface, from 4x4 samples. */
        float luminanceBehind(Bitmap b) {
            float sx = b.getWidth() / (float) screenW, sy = b.getHeight() / (float) screenH;
            int x0 = Math.max(0, (int) ((loc[0] + box.left) * sx)), y0 = Math.max(0, (int) ((loc[1] + box.top) * sy));
            int x1 = Math.min(b.getWidth() - 1, (int) ((loc[0] + box.right) * sx)), y1 = Math.min(b.getHeight() - 1, (int) ((loc[1] + box.bottom) * sy));
            if (x1 < x0 || y1 < y0) return 0.3f;
            float sum = 0;
            for (int j = 0; j < 4; j++) {
                int py = y0 + (y1 - y0) * j / 3;
                for (int i = 0; i < 4; i++) {
                    int px = b.getPixel(x0 + (x1 - x0) * i / 3, py);
                    sum += (0.2126f * Color.red(px) + 0.7152f * Color.green(px) + 0.0722f * Color.blue(px)) / 255f;
                }
            }
            return sum / 16f;
        }

        static int tintFor(float lum) {
            if (lum >= 0.45f) {  // bright: darken, up to 36% black
                int a = Math.round(0x1F + (Math.min(lum, 0.85f) - 0.45f) / 0.4f * (0x5C - 0x1F));
                return a << 24;
            }
            int a = Math.round((0.45f - Math.max(lum, 0.05f)) / 0.4f * 0x24);  // dark: light frost
            return (a << 24) | 0xFFFFFF;
        }

        int rimA(int a) {
            return (Math.round(a * rimStrength) << 24) | 0xFFFFFF;
        }

        @Override protected void onBoundsChange(Rect b) {
            box.set(b);
            matBoxW = -1f;
            float h = box.height();
            glow.setShader(new LinearGradient(0, box.top, 0, box.top + h * 0.5f,
                    0x1FFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
            rim.setShader(new LinearGradient(box.left, box.top, box.right, box.bottom,
                    new int[]{rimA(0xD9), rimA(0x40), rimA(0x14), rimA(0x26), rimA(0x8C)},
                    new float[]{0f, 0.22f, 0.5f, 0.78f, 1f}, Shader.TileMode.CLAMP));
            float l = Math.min(lens, Math.min(box.width(), box.height()) / 3f);
            lens1.setStrokeWidth(l);
        }

        /** Maps the full-screen backdrop onto this surface's on-screen spot, optionally magnified. */
        void place(android.graphics.BitmapShader sh, Bitmap b, float zoom) {
            m.reset();
            m.setScale(screenW / (float) b.getWidth(), screenH / (float) b.getHeight());
            m.postTranslate(box.left - loc[0], box.top - loc[1]);
            if (zoom != 1f) m.postScale(zoom, zoom, box.centerX(), box.centerY());
            sh.setLocalMatrix(m);
        }

        @Override public void draw(Canvas c) {
            Bitmap b = backdrop.get();
            Object cb = getCallback();
            if (cb instanceof View) ((View) cb).getLocationOnScreen(loc);
            if (b != null && !b.isRecycled()) {
                if (b != shaderFor) {
                    shader1 = new android.graphics.BitmapShader(b, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    if (!adaptive && lens > 0) {
                        shader2 = new android.graphics.BitmapShader(b, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                        lens1.setShader(shader2);
                    }
                    body.setShader(shader1);
                    shaderFor = b;
                    matFor = null;
                }
                if (adaptive && (b != lumFor || Math.abs(loc[0] - lumX) > 32 || Math.abs(loc[1] - lumY) > 32)) {
                    lumFor = b;
                    lumX = loc[0];
                    lumY = loc[1];
                    int t = tintFor(luminanceBehind(b));
                    if (minBlack > 0) {
                        boolean black = (t & 0xFFFFFF) == 0;
                        t = (black ? Math.max(t >>> 24, minBlack) : minBlack) << 24;
                    }
                    if (t != adaptiveTint) {
                        adaptiveTint = t;
                        if (!lit) fill.setColor(t);
                    }
                }
                if (b != matFor || loc[0] != matLocX || loc[1] != matLocY
                        || box.width() != matBoxW || box.height() != matBoxH) {
                    matFor = b;
                    matLocX = loc[0];
                    matLocY = loc[1];
                    matBoxW = box.width();
                    matBoxH = box.height();
                    place(shader1, b, 1f);
                    if (!adaptive && lens > 0 && shader2 != null) {
                        place(shader2, b, 1.04f);
                    }
                }
                c.drawRoundRect(box, r, r, body);
                if (!adaptive && lens > 0 && shader2 != null) {
                    float w1 = lens1.getStrokeWidth();
                    tmp.set(box.left + w1 / 2, box.top + w1 / 2, box.right - w1 / 2, box.bottom - w1 / 2);
                    c.drawRoundRect(tmp, r - w1 / 2, r - w1 / 2, lens1);
                }
            }
            c.drawRoundRect(box, r, r, fill);
            c.drawRoundRect(box, r, r, glow);
            float in = rim.getStrokeWidth() / 2f;
            c.drawRoundRect(box.left + in, box.top + in, box.right - in, box.bottom - in, r - in, r - in, rim);
        }

        @Override public void setAlpha(int a) { }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { }
        @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    }

    /**
     * Frosted Liquid Glass surface: tinted luminosity lift, fine frost grain, a soft inner glow
     * at the top, gentle shade at the bottom, and a rim that catches light on the top edge and
     * fades down the sides (no uniform outline).
     */
    /** Liquid-glass surface: translucent fill, top sheen and a bright hairline rim. */
    /** Outline whose corner radius can be changed while animating (radius[0]). */
    ViewOutlineProvider roundedOutline(float[] r) {
        return new ViewOutlineProvider() {
            @Override public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), r[0]);
            }
        };
    }

    // ---------------------------------------------------------------- suggestions carousel

    /** Title logo (or title), meta line, overview, "Open in \u2026" capsule and page dots, bottom-left of the shelf. */
    /**
     * Darkening for legibility (left side for the text, bottom for the dock), painted into the
     * artwork once when it loads, so the GPU draws one full-screen image instead of three layers.
     */
    /** A soft shade along the top so the time and status icons stay legible over bright artwork. */
    /** Info block sits just above the dock while a dock app is focused... */
    /** ...and the page dots sit centred between the info and the dock. */
    /** tvOS-style top tab bar: a glass capsule, top centre, with two text tabs. */
    /** Selected tab: soft glass pill; white with dark text while the tab bar has focus (tvOS). */
    /** Continue Watching is intentionally disabled in this device-specific build. */
    /** The tab bar only appears when there are two things to switch between. */
    /** Applies the profile's choices: hides the tab bar and picks the one remaining mode. */
    /** Down from the status row: the tab bar, or (when it's hidden) the carousel or the dock. */
    /** Up from the shelf/dock: the tab bar, or the status row when the tab bar is hidden. */
    /** Legacy hero content is disabled in this build. */
    String lastCwSig = "";

    /** Obsolete carousel mixing logic retained temporarily for source compatibility. */
    /** Resume first: up to six recent Continue Watching titles, then news and recommendations. */


    /** Carousel vs launcher background: crossfade between them. */
    /** dir: +1 next, -1 previous, 0 first show (no slide). */
    /** Title/meta/overview for an item, applied in the same frame as its artwork. */
    /**
     * Artwork transition (tvOS carousel): the new image slides in from the side you pressed and
     * fades up over the old one, which drifts the other way and fades out.
     */
    /** Carousel focused: the dock slides away and the artwork takes the whole screen (tvOS Top Shelf). */

    /** The search screen while it is open (its layer is also pickerLayer, so Home and idle treat it as an overlay). */
    SearchPanel search;

    FrameLayout pickerLayer;
    BroadcastReceiver wakeReceiver;

    /** Top shelf content: "suggest" (carousel) or "continue" (Continue Watching). */

    /** Moves the highlight between Resume and Remove (the carousel keeps focus). */
    /** Carousel and capsule navigation: LEFT/RIGHT browse, OK opens, UP/DOWN move between capsule, carousel and dock. */
    // ---------------------------------------------------------------- focus sway

    /** Slow elliptical tilt of the focused card with the highlight drifting against it. */
    // ---------------------------------------------------------------- launch

    View launchDimView;

    /**
     * Immediately halts all launcher animations and timers before starting an external app
     * (cleanyourtv style) so 100% of CPU/GPU goes to the opening app and WindowManager transition.
     */
    void prepareForAppLaunch() {
        if (sceneHome != null) sceneHome.sceneView.stop();
        handler.removeCallbacks(weatherTick);
        handler.removeCallbacks(lockIdleRunnable);
    }

    void showLaunchDim() {
        prepareForAppLaunch();
    }

    void clearLaunchDim() {
        if (root != null) {
            root.animate().cancel();
            root.setScaleX(1f);
            root.setScaleY(1f);
        }
        if (launchDimView != null) {
            launchDimView.animate().cancel();
            launchDimView.setAlpha(0f);
            launchDimView.setVisibility(View.GONE);
        }
    }

    /**
     * Launches the selected app immediately using cleanyourtv's zero-overhead window transition.
     */
    void launchAnimated(View tile, AppEntry a) {
        if (INPUTS_KEY.equals(a.key)) {
            ControlPanelOverlay.openInputs(this, () -> showInputs(tile));
            return;
        }
        if (a == null || a.launch == null) return;
        try {
            prepareForAppLaunch();
            startActivity(a.launch);
            overridePendingTransition(R.anim.app_open_enter, R.anim.app_open_exit);
            final String pkg = a.pkg;
            io.execute(() -> noteLaunched(pkg));
        } catch (ActivityNotFoundException | SecurityException e) {
            if (Scenes.isScenesLook(this) && sceneHome != null) sceneHome.sceneView.start();
            toast(I18n.s(this, "Can't open " + a.label, a.label + " açılamıyor"));
        }
    }

    /** Coming back to Home after a launch: the card fades away and the grid zooms back in. */
    /**
     * The colour the app paints first: its Android 12 splash background, else its window
     * background. Ending the zoom on this colour makes the handoff to the app seamless.
     */
    int launchBackground(AppEntry a, int fallback) {
        ComponentName cn = a.launch.getComponent();
        if (cn == null) return fallback;
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(cn, 0);
            Context pc = createPackageContext(cn.getPackageName(), 0);
            Resources.Theme theme = pc.getResources().newTheme();
            theme.applyStyle(ai.getThemeResource(), true);
            int[] attrs = Build.VERSION.SDK_INT >= 31
                    ? new int[]{android.R.attr.windowSplashScreenBackground, android.R.attr.windowBackground}
                    : new int[]{android.R.attr.windowBackground};
            TypedArray ta = theme.obtainStyledAttributes(attrs);
            try {
                for (int i = 0; i < attrs.length; i++) {
                    TypedValue v = ta.peekValue(i);
                    if (v == null) continue;
                    int color = 0;
                    if (v.type >= TypedValue.TYPE_FIRST_COLOR_INT && v.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                        color = v.data;
                    } else {
                        Drawable d = ta.getDrawable(i);
                        if (d instanceof ColorDrawable) color = ((ColorDrawable) d).getColor();
                    }
                    if (Color.alpha(color) > 200) return color | 0xFF000000;
                }
            } finally {
                ta.recycle();
            }
        } catch (Exception e) {
            // Theme unreadable: keep the banner colour.
        }
        return fallback;
    }

    static int colorDistance(int x, int y) {
        return Math.abs(Color.red(x) - Color.red(y)) + Math.abs(Color.green(x) - Color.green(y))
                + Math.abs(Color.blue(x) - Color.blue(y));
    }

    static int blendArgb(int x, int y, float t) {
        return Color.argb(
                (int) (Color.alpha(x) + (Color.alpha(y) - Color.alpha(x)) * t),
                (int) (Color.red(x) + (Color.red(y) - Color.red(x)) * t),
                (int) (Color.green(x) + (Color.green(y) - Color.green(x)) * t),
                (int) (Color.blue(x) + (Color.blue(y) - Color.blue(x)) * t));
    }

    static int blend(int x, int y, float t) {
        return Color.rgb(
                (int) (Color.red(x) + (Color.red(y) - Color.red(x)) * t),
                (int) (Color.green(x) + (Color.green(y) - Color.green(x)) * t),
                (int) (Color.blue(x) + (Color.blue(y) - Color.blue(x)) * t));
    }

    /** Colour along the banner's edges, so the zoomed card blends with the banner art. */
    int edgeColor(AppEntry a) {
        Bitmap b = backdropSharp(a, 96, 54);
        int w = b.getWidth() - 3, h = b.getHeight() - 3;
        int[] px = {b.getPixel(2, 2), b.getPixel(w, 2), b.getPixel(2, h), b.getPixel(w, h),
                b.getPixel(w / 2, 2), b.getPixel(w / 2, h)};
        int r = 0, g = 0, bl = 0;
        for (int p : px) {
            r += Color.red(p);
            g += Color.green(p);
            bl += Color.blue(p);
        }
        b.recycle();
        return Color.rgb(r / px.length, g / px.length, bl / px.length);
    }

    /** Up Next strip: one thumbnail per entry with a progress bar; the current one is lifted. */
    /** Banner rendered sharp, minus a hairline border so anti-aliased edges don't get extended. */
    /**
     * The launch card, drawn in one pass so nothing distorts: a rounded rect that grows from the
     * card's on-screen rect to full screen while its corners flatten. Banner apps draw the banner
     * centre-cropped exactly like the card, with its edge pixels clamped outward, so the whole
     * screen continues the card's own background; the logo settles at ~42% of the screen.
     */
    static class LaunchCard extends Drawable {
        final RectF card;
        final float r0;
        final Bitmap art;
        final Drawable icon;
        final int color0, color1;
        final Paint artPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        final android.graphics.Matrix m = new android.graphics.Matrix();
        final android.graphics.Path clip = new android.graphics.Path();
        final RectF o = new RectF(), br = new RectF();
        float f;

        LaunchCard(RectF card, float r0, Bitmap art, Drawable icon, int color0, int color1) {
            this.card = card;
            this.r0 = r0;
            this.art = art;
            this.icon = icon;
            this.color0 = color0;
            this.color1 = color1;
            if (art != null) {
                artPaint.setShader(new android.graphics.BitmapShader(art, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            }
        }

        void setProgress(float f) {
            this.f = f;
            invalidateSelf();
        }

        static float lerp(float a, float b, float t) {
            return a + (b - a) * t;
        }

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float W = b.width(), H = b.height();
            o.set(lerp(card.left, 0, f), lerp(card.top, 0, f), lerp(card.right, W, f), lerp(card.bottom, H, f));
            float rad = r0 * (1f - f);
            if (art != null) {
                // Banner rect: starts as the card itself, ends centred at 42% of the screen.
                float bw = lerp(card.width(), W * 0.42f, f), bh = lerp(card.height(), H * 0.42f, f);
                br.set(o.centerX() - bw / 2, o.centerY() - bh / 2, o.centerX() + bw / 2, o.centerY() + bh / 2);
                float scale = Math.max(br.width() / art.getWidth(), br.height() / art.getHeight());
                m.setScale(scale, scale);
                m.postTranslate(br.centerX() - art.getWidth() * scale / 2, br.centerY() - art.getHeight() * scale / 2);
                artPaint.getShader().setLocalMatrix(m);
                c.drawRoundRect(o, rad, rad, artPaint);
            } else {
                float t = Math.max(0f, Math.min(1f, (f - 0.55f) / 0.45f));
                fill.setColor(blend(color0, color1, t));
                c.drawRoundRect(o, rad, rad, fill);
                if (icon != null) {
                    float s = lerp(card.height() * 0.55f, H * 0.27f, f);
                    Rect old = icon.copyBounds();
                    icon.setBounds((int) (o.centerX() - s / 2), (int) (o.centerY() - s / 2),
                            (int) (o.centerX() + s / 2), (int) (o.centerY() + s / 2));
                    icon.draw(c);
                    icon.setBounds(old);
                }
            }
        }

        @Override public void setAlpha(int a) { }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { }
        @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    }

    Drawable copyOf(Drawable d) {
        Drawable.ConstantState cs = d.getConstantState();
        return cs != null ? cs.newDrawable(getResources()).mutate() : d;
    }

    // ---------------------------------------------------------------- focus & shelf

    ObjectAnimator scrollAnim;

    /**
     * Row tops come from the fixed grid geometry rather than getTop(), so this also works right
     * after buildGrid() (e.g. while moving a card), before the new rows have been laid out.
     * Uses an explicit ObjectAnimator on scrollY instead of ScrollView.smoothScrollTo because
     * Android's ScrollView aborts smooth scroll and jumps immediately if called twice within 250ms.
     */
    void goTop() {
        if (sceneHome != null) {
            if (sceneHome.isSheetOpen()) sceneHome.closeSheet();
            sceneHome.focusDock(sceneHome.focusedDockIdx);
        }
    }

    // ---------------------------------------------------------------- folder of hidden apps

    /** Folder card: dark glass gradient, 3x3 app-grid glyph, title and hidden-app count. */
    /** Quick zoom-through between home and folder, so it reads as "opening" the folder. */
    // ---------------------------------------------------------------- keys

    @Override
    public boolean dispatchKeyEvent(KeyEvent ev) {
        if (ambientShowcase) {
            if (ev == null) return true;
            int action = ev.getAction();
            int code = ev.getKeyCode();
            if (action == KeyEvent.ACTION_DOWN) {
                if (android.os.SystemClock.uptimeMillis() - ambientEnterTime > 600L && ev.getRepeatCount() == 0) {
                    ambientDownKeys.add(code);
                }
                return true;
            }
            if (action == KeyEvent.ACTION_UP) {
                if (ambientDownKeys.remove(code)) exitAmbientShowcase();
                return true;
            }
            return true;
        }
        if (PowerService.instance == null && ButtonMapper.handleKeyEvent(this, ev)) return true;
        if (launchOverlay != null) return true;
        if (swallowUp && ev.getAction() == KeyEvent.ACTION_UP) {
            swallowUp = false;
            return true;
        }
        resetIdle();
        if (!replayingBack) navSound(this, ev);
        if (search != null && pickerLayer == search.layer && menuLayer == null) {
            return search.dispatch(ev) || super.dispatchKeyEvent(ev);
        }
        boolean dockMoving = sceneHome != null && sceneHome.isDockMoveActive();
        if (pickerLayer == null && menuLayer == null && !dockMoving && keyboardSearch(ev)) return true;
        if (pickerLayer == null && menuLayer == null && !dockMoving && holdBackSearch(ev)) return true;
        if (ev.getKeyCode() == KeyEvent.KEYCODE_SEARCH && pickerLayer == null && menuLayer == null && !dockMoving) {
            if (ev.getAction() == KeyEvent.ACTION_UP) showSearch(null);
            return true;
        }
        if (pickerLayer != null && menuLayer == null) {
            int key = ev.getKeyCode();
            boolean down = ev.getAction() == KeyEvent.ACTION_DOWN;
            if (key == KeyEvent.KEYCODE_BACK) {
                if (!down) closePicker();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_UP || key == KeyEvent.KEYCODE_DPAD_DOWN
                    || key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (down) pickerMove(key);
                return true;
            }
            return super.dispatchKeyEvent(ev);
        }
        if (menuLayer != null) {
            int key = ev.getKeyCode();
            if (key == KeyEvent.KEYCODE_BACK || key == KeyEvent.KEYCODE_MENU || key == KeyEvent.KEYCODE_ESCAPE) {
                if (ev.getAction() == KeyEvent.ACTION_UP) closeMenu();
                return true;
            }
            return super.dispatchKeyEvent(ev);
        }
        if (sceneHome != null && sceneHome.dispatchKey(ev)) return true;
        return super.dispatchKeyEvent(ev);
    }

    // ---------------------------------------------------------------- apps

    void loadApps() {
        all.clear();
        apps.clear();
        otherApps.clear();
        PackageManager pm = getPackageManager();
        Set<String> pkgs = new HashSet<>();
        addResolved(pm, Intent.CATEGORY_LEANBACK_LAUNCHER, pkgs, true);
        addResolved(pm, Intent.CATEGORY_LAUNCHER, pkgs, false);
        if (!pkgs.contains(TV_SETTINGS)) addSettingsEntry(pm);
        inputs = Inputs.list(this);
        if (Inputs.showCard(this) && (!inputs.isEmpty() || ControlPanelOverlay.hasCustomAction(this, ControlPanelOverlay.ACTION_INPUTS))) {
            addInputsEntry();
        }
        for (AppEntry e : all.values()) {
            if (CustomMedia.hasCustomAppIcon(this, e.key, e.pkg)) {
                Bitmap cIcon = CustomMedia.getCustomAppIcon(this, e.key, e.pkg);
                if (cIcon != null && !cIcon.isRecycled()) {
                    e.icon = new BitmapDrawable(getResources(), cIcon);
                    e.banner = new BitmapDrawable(getResources(), CustomMedia.buildClassicBannerFromCustomIcon(cIcon, e.color));
                    continue;
                }
            }
            if (e.banner == null || TVOS_CARD_PKGS.contains(e.pkg)) {
                e.banner = new BitmapDrawable(getResources(), tvosCard(e));
            }
        }

        Set<String> hidden = prefs.getStringSet(PREF_HIDDEN, Collections.emptySet());
        Set<String> other = prefs.getStringSet(PREF_OTHER, Collections.emptySet());
        Map<String, AppEntry> pool = new LinkedHashMap<>();
        for (AppEntry e : all.values()) {
            if (hidden.contains(e.key) || (e.pkg != null && hidden.contains(e.pkg))) continue;
            if (other.contains(e.key) || (e.pkg != null && other.contains(e.pkg))) otherApps.add(e);
            else pool.put(e.key, e);
        }
        Collections.sort(otherApps, (x, y) -> x.label.compareToIgnoreCase(y.label));

        for (String key : prefs.getString(PREF_ORDER, "").split(",")) {
            AppEntry e = pool.remove(key);
            if (e != null) apps.add(e);
        }
        List<AppEntry> rest = new ArrayList<>(pool.values());
        Collections.sort(rest, (x, y) -> {
            int px = priority(x), py = priority(y);
            return px != py ? Integer.compare(px, py) : x.label.compareToIgnoreCase(y.label);
        });
        apps.addAll(rest);
        // Inputs starts as the first card in the dock until it is moved.
        if (!Arrays.asList(prefs.getString(PREF_ORDER, "").split(",")).contains(INPUTS_KEY)) {
            for (int i = 0; i < apps.size(); i++) {
                if (INPUTS_KEY.equals(apps.get(i).key)) {
                    apps.add(0, apps.remove(i));
                    break;
                }
            }
        }
    }

    /** Fallback Settings tile for TVs whose settings app has no launcher entry. */
    void addSettingsEntry(PackageManager pm) {
        AppEntry s = new AppEntry();
        s.key = SETTINGS_KEY;
        s.pkg = TV_SETTINGS;
        s.defaultLabel = I18n.s(this, "Settings", "Ayarlar");
        s.label = getCustomAppName(this, s.key, s.pkg, s.defaultLabel);
        s.system = true;
        s.launch = new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            s.banner = pm.getApplicationBanner(s.pkg);
            s.icon = pm.getApplicationIcon(s.pkg);
        } catch (PackageManager.NameNotFoundException e) {
            s.icon = getDrawable(android.R.drawable.ic_menu_preferences);
        }
        s.color = 0xFF3A3A3C;
        all.put(s.key, s);
    }

    int priority(AppEntry e) {
        if (INPUTS_KEY.equals(e.key)) return -1;
        if (isSettings(e)) return 2000;
        int i = DEFAULT_ORDER.indexOf(e.pkg);
        return i >= 0 ? i : 1000;
    }

    boolean isSettings(AppEntry e) {
        return SETTINGS_KEY.equals(e.key) || TV_SETTINGS.equals(e.pkg);
    }

    List<Inputs.Input> inputs = new ArrayList<>();

    /** Inputs card: a graphite tvOS-style card with the input glyph; OK opens the input picker. */
    void addInputsEntry() {
        AppEntry e = new AppEntry();
        e.key = INPUTS_KEY;
        e.pkg = getPackageName();
        e.defaultLabel = I18n.s(this, "Inputs", "Girişler");
        e.label = getCustomAppName(this, e.key, null, e.defaultLabel);
        e.system = true;
        e.color = 0xFF48484A;
        int w = 360, h = 216;
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        p.setShader(new LinearGradient(0, 0, 0, h, 0xFF6E6E73, 0xFF2C2C2E, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        Bitmap g = Glyphs.get("input", (int) (h * 0.5f), Color.WHITE);
        c.drawBitmap(g, (w - g.getWidth()) / 2f, (h - g.getHeight()) / 2f, null);
        e.banner = new BitmapDrawable(getResources(), b);
        e.icon = e.banner;
        all.put(e.key, e);
    }

    void addResolved(PackageManager pm, String category, Set<String> pkgs, boolean leanback) {
        Intent q = new Intent(Intent.ACTION_MAIN).addCategory(category);
        for (ResolveInfo ri : pm.queryIntentActivities(q, 0)) {
            ActivityInfo ai = ri.activityInfo;
            if (ai.packageName.equals(getPackageName()) || pkgs.contains(ai.packageName)) continue;
            AppEntry e = new AppEntry();
            ComponentName cn = new ComponentName(ai.packageName, ai.name);
            e.pkg = ai.packageName;
            e.key = cn.flattenToShortString();
            e.defaultLabel = TV_SETTINGS.equals(e.pkg)
                    ? I18n.s(this, "Settings", "Ayarlar")
                    : ri.loadLabel(pm).toString();
            e.label = getCustomAppName(this, e.key, e.pkg, e.defaultLabel);
            e.banner = leanback ? ai.loadBanner(pm) : null;
            e.icon = ri.loadIcon(pm);
            e.color = tintFrom(e.icon);
            e.system = (ai.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            Intent launch = new Intent(Intent.ACTION_MAIN)
                    .setComponent(cn)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            if (leanback) launch.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
            e.launch = launch;
            all.put(e.key, e);
            pkgs.add(e.pkg);
        }
    }

    /** Average colour of the icon, darkened so white logos stay readable on top of it. */
    int tintFrom(Drawable d) {
        if (d == null) return 0xFF2C2C2E;
        Bitmap b = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
        Rect old = d.copyBounds();
        d.setBounds(0, 0, 16, 16);
        d.draw(new Canvas(b));
        d.setBounds(old);
        long r = 0, g = 0, bl = 0, n = 0;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int p = b.getPixel(x, y);
                if (Color.alpha(p) < 128) continue;
                r += Color.red(p);
                g += Color.green(p);
                bl += Color.blue(p);
                n++;
            }
        }
        b.recycle();
        if (n == 0) return 0xFF2C2C2E;
        float[] hsv = new float[3];
        Color.RGBToHSV((int) (r / n), (int) (g / n), (int) (bl / n), hsv);
        hsv[1] = Math.min(hsv[1], 0.8f);
        hsv[2] = Math.min(hsv[2], 0.5f);
        return Color.HSVToColor(hsv);
    }

    void reload() {
        loadApps();
        if (sceneHome != null) {
            sceneHome.rebuildDock();
            sceneHome.focusDock(Math.min(sceneHome.focusedDockIdx, Math.max(0, sceneHome.dockTiles.size() - 1)));
        }
    }

    void saveOrder() {
        StringBuilder sb = new StringBuilder();
        for (AppEntry e : apps) {
            if (sb.length() > 0) sb.append(',');
            sb.append(e.key);
        }
        prefs.edit().putString(PREF_ORDER, sb.toString()).apply();
    }

    void setOtherApp(AppEntry a, boolean toOther) {
        if (a == null) return;
        Set<String> other = new HashSet<>(prefs.getStringSet(PREF_OTHER, Collections.emptySet()));
        Set<String> hidden = new HashSet<>(prefs.getStringSet(PREF_HIDDEN, Collections.emptySet()));
        if (toOther) {
            other.add(a.key);
            hidden.remove(a.key);
            if (a.pkg != null) hidden.remove(a.pkg);
        } else {
            other.remove(a.key);
            if (a.pkg != null) other.remove(a.pkg);
            hidden.remove(a.key);
            if (a.pkg != null) hidden.remove(a.pkg);
        }
        prefs.edit().putStringSet(PREF_OTHER, other).putStringSet(PREF_HIDDEN, hidden).apply();
        loadApps();
        if (!toOther) ensureInFirstDockRow(a.key);
        if (sceneHome != null) {
            sceneHome.rebuildDock();
            sceneHome.focusDock(Math.min(sceneHome.focusedDockIdx, Math.max(0, sceneHome.dockTiles.size() - 1)));
        }
        toast(toOther
                ? I18n.s(this, a.label + " moved to Other Apps", a.label + " Diğer Uygulamalar'a taşındı")
                : I18n.s(this, a.label + " is back on Home", a.label + " Ana Ekran'a geri döndü"));
    }

    void ensureInFirstDockRow(String key) {
        if (key == null) return;
        int curIdx = -1;
        int dockPos = 0;
        int tenthRealIdx = -1;
        for (int idx = 0; idx < apps.size(); idx++) {
            AppEntry e = apps.get(idx);
            if (SETTINGS_KEY.equals(e.key) || TV_SETTINGS.equals(e.pkg)) continue;
            if (dockPos == 9) tenthRealIdx = idx;
            if (key.equals(e.key)) {
                curIdx = idx;
                if (dockPos < 10) return; // Already on first dock row!
            }
            dockPos++;
        }
        if (curIdx >= 0 && tenthRealIdx >= 0 && curIdx > tenthRealIdx) {
            AppEntry moved = apps.remove(curIdx);
            apps.add(tenthRealIdx, moved);
            saveOrder();
        }
    }

    void setHidden(AppEntry a, boolean hide) {
        if (a == null) return;
        Set<String> hidden = new HashSet<>(prefs.getStringSet(PREF_HIDDEN, Collections.emptySet()));
        Set<String> other = new HashSet<>(prefs.getStringSet(PREF_OTHER, Collections.emptySet()));
        if (hide) {
            hidden.add(a.key);
            other.remove(a.key);
            if (a.pkg != null) other.remove(a.pkg);
        } else {
            hidden.remove(a.key);
            if (a.pkg != null) hidden.remove(a.pkg);
            other.remove(a.key);
            if (a.pkg != null) other.remove(a.pkg);
        }
        prefs.edit().putStringSet(PREF_HIDDEN, hidden).putStringSet(PREF_OTHER, other).apply();
        loadApps();
        if (sceneHome != null) {
            sceneHome.rebuildDock();
            sceneHome.focusDock(Math.min(sceneHome.focusedDockIdx, Math.max(0, sceneHome.dockTiles.size() - 1)));
        }
        toast(hide
                ? I18n.s(this, a.label + " hidden (Settings > Apps > Hidden apps)",
                        a.label + " gizlendi (Ayarlar > Uygulamalar > Gizlenen uygulamalar)")
                : I18n.s(this, a.label + " is back on Home", a.label + " Ana Ekran'a geri döndü"));
    }

    void registerPackageReceiver() {
        pkgReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                handler.removeCallbacks(reloadRun);
                handler.postDelayed(reloadRun, 600);
            }
        };
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_PACKAGE_ADDED);
        f.addAction(Intent.ACTION_PACKAGE_REMOVED);
        f.addAction(Intent.ACTION_PACKAGE_CHANGED);
        f.addAction(Intent.ACTION_PACKAGE_REPLACED);
        f.addDataScheme("package");
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(pkgReceiver, f, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(pkgReceiver, f);
        }
    }

    // ---------------------------------------------------------------- options

    void promptRenameApp(AppEntry a, Runnable onDone) {
        if (a == null) return;
        android.widget.EditText in = new android.widget.EditText(this);
        in.setSingleLine(true);
        in.setText(a.label != null ? a.label : "");
        in.setSelection(in.getText().length());
        FrameLayout box = new FrameLayout(this);
        int pad = dp(16);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(in);

        String fallback = (a.defaultLabel != null && !a.defaultLabel.isEmpty()) ? a.defaultLabel : a.label;
        AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(I18n.s(this, "Rename App", "Uygulamayı Yeniden Adlandır"))
                .setView(box)
                .setPositiveButton(I18n.s(this, "Save", "Kaydet"), (d, w) -> {
                    String n = in.getText().toString().trim();
                    if (n.length() > 32) n = n.substring(0, 32);
                    if (n.isEmpty() || (fallback != null && n.equals(fallback))) {
                        setCustomAppName(this, a.key, a.pkg, "");
                        a.label = fallback;
                    } else {
                        setCustomAppName(this, a.key, a.pkg, n);
                        a.label = n;
                    }
                    if (onDone != null) onDone.run();
                })
                .setNegativeButton(I18n.s(this, "Cancel", "İptal"), (d, w) -> {
                    if (onDone != null) onDone.run();
                })
                .setOnCancelListener(d -> {
                    if (onDone != null) onDone.run();
                });

        if (hasCustomAppName(this, a.key, a.pkg)) {
            b.setNeutralButton(I18n.s(this, "Reset", "Sıfırla"), (d, w) -> {
                setCustomAppName(this, a.key, a.pkg, "");
                if (fallback != null) a.label = fallback;
                if (onDone != null) onDone.run();
            });
        }

        AlertDialog dlg = b.show();
        Ui.setupDialogEditText(in, dlg);
        int[] btnIds = {
                android.content.DialogInterface.BUTTON_POSITIVE,
                android.content.DialogInterface.BUTTON_NEUTRAL,
                android.content.DialogInterface.BUTTON_NEGATIVE
        };
        int hMargin = dp(6);
        int vMargin = dp(5);
        for (int id : btnIds) {
            android.widget.Button btn = dlg.getButton(id);
            if (btn != null) {
                btn.setPadding(dp(14), dp(8), dp(14), dp(8));
                ViewGroup.LayoutParams lp = btn.getLayoutParams();
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                    mlp.leftMargin = hMargin;
                    mlp.rightMargin = hMargin;
                    mlp.topMargin = vMargin;
                    mlp.bottomMargin = vMargin;
                    btn.setLayoutParams(mlp);
                }
            }
        }
        in.requestFocus();
    }

    void promptChangeAppIcon(AppEntry a, Runnable onDone) {
        if (a == null) return;
        boolean hasCustom = CustomMedia.hasCustomAppIcon(this, a.key, a.pkg);
        String resetLabel = hasCustom
                ? I18n.s(this, "Reset to default icon", "Varsayılan simgeye sıfırla")
                : null;
        Runnable onReset = hasCustom ? () -> {
            CustomMedia.clearCustomAppIcon(this, a.key, a.pkg);
            if (sceneHome != null) sceneHome.invalidateAppLogoCache(a);
            toast(I18n.s(this, "App icon reset to default", "Uygulama simgesi varsayılana sıfırlandı"));
            if (onDone != null) onDone.run();
        } : null;

        CustomMedia.showImageFilePicker(
                this,
                I18n.s(this, "Change app icon", "Uygulama simgesini değiştir") + " — " + a.label,
                true,
                512,
                512,
                resetLabel,
                onReset,
                bmp -> CustomMedia.showAppIconEditor(
                        this,
                        a.label,
                        bmp,
                        a.color,
                        editedBmp -> {
                            if (CustomMedia.setCustomAppIcon(this, a.key, a.pkg, editedBmp)) {
                                if (sceneHome != null) sceneHome.invalidateAppLogoCache(a);
                                toast(I18n.s(this, "App icon updated", "Uygulama simgesi güncellendi"));
                                if (onDone != null) onDone.run();
                            }
                        }));
    }

    /** Small chooser that keeps launcher settings separate from the TV's own settings. */
    void showSettingsChooser() {
        dropPicker();
        pickerLayer = null;
        popoverReturn = null;

        String[] choices = {
                I18n.s(this, "testOS Settings", "Настройки testOS"),
                I18n.s(this, "TV Settings", "Настройки телевизора")
        };
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(I18n.s(this, "Settings", "Настройки"))
                .setItems(choices, (dialog, which) -> {
                    returningFromSettings = true;

                    if (which == 0) {
                        startActivity(new Intent(this, SettingsActivity.class));
                    } else {
                        Intent tvSettings = new Intent(Settings.ACTION_SETTINGS);
                        if (!Suggestions.start(this, tvSettings)) {
                            try { startActivity(tvSettings); } catch (Exception ignored) { }
                        }
                    }
                })
                .setNegativeButton(I18n.s(this, "Cancel", "Отмена"), null)
                .show();
    }

    // ---------------------------------------------------------------- Control Center

    /**
     * Control Center: full-width Wi-Fi, Bluetooth, VPN and Sleep Timer rows,
     * with paired RAM / Recent Apps and Launcher / TV Settings shortcuts.
     */
    void showControlCenter() {
        ControlPanelOverlay.dismiss();
        dropPicker();
        popoverReturn = sceneHome != null ? sceneHome.moonBtn : null;
        FrameLayout layer = new FrameLayout(this);
        layer.setClickable(true);
        layer.setClipChildren(false);
        layer.setTag("control_panel");
        addBlurredBackdrop(layer, 0x8C000000);

        List<View> cells = new ArrayList<>();
        FrameLayout panel = ControlPanelOverlay.buildControlPanelView(
                this,
                screenW,
                screenH,
                unit,
                glassSource,
                cells,
                () -> {
                    dropPicker();
                    pickerLayer = null;
                    popoverReturn = null;
                    VpnController.toggleHappWithFeedback(this);
                },
                () -> {
                    dropPicker();
                    pickerLayer = null;
                    popoverReturn = null;
                    returningFromSettings = true;
                    startActivity(new Intent(this, SettingsActivity.class));
                },
                () -> {
                    dropPicker();
                    pickerLayer = null;
                    popoverReturn = null;
                    ControlPanelOverlay.openTvSettings(this);
                });

        FrameLayout.LayoutParams plp = (FrameLayout.LayoutParams) panel.getLayoutParams();
        plp.gravity = Gravity.TOP | Gravity.END;
        plp.topMargin = dp(58);
        plp.rightMargin = dockOuter;
        layer.addView(panel, plp);

        root.addView(layer, match());
        pickerLayer = layer;
        pickerCells = cells;
        layer.setAlpha(0f);
        panel.setTranslationY(-dp(10));
        layer.animate().alpha(1f).setDuration(200).withEndAction(() -> hideUnderOverlay(layer)).start();
        panel.animate().translationY(0).setDuration(260).setInterpolator(EASE).start();
        if (!cells.isEmpty()) cells.get(0).requestFocus();
    }

    interface Status {
        String get();
    }

    /**
     * One Control Center tile: glass with glyph (+ title / live status). Square tiles stack the
     * glyph over the title; capsules put the glyph left of two lines; circles are glyph-only.
     * Focus: the glass lights up and the tile lifts.
     */
    View ccTile(FrameLayout panel, int x, int y, int w, int h, String glyph, String title, Status status,
                boolean square, View.OnClickListener click) {
        boolean circle = title == null;
        float radius = circle ? h / 2f : square ? dp(26) : h / 2f;
        LiquidGlass glass = new LiquidGlass(radius, 0x24FFFFFF, glassSource, screenW, screenH, unit).adaptive().darker(0x59);
        glass.lens = dp(7);
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(square ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        t.setGravity(Gravity.CENTER);
        t.setBackground(glass);
        t.setFocusable(true);
        t.setClickable(true);
        t.setPadding(circle ? 0 : dp(16), 0, circle ? 0 : dp(14), 0);
        int gs = square ? dp(54) : circle ? dp(30) : dp(28);
        ImageView gi = new ImageView(this);
        gi.setImageBitmap(Glyphs.get(glyph, gs, glyph.equals("wifi") ? 0xFF0A84FF : Color.WHITE));
        t.addView(gi, new LinearLayout.LayoutParams(gs, gs));
        TextView sub = null;
        TextView[] titleRef = {null};
        if (!circle) {
            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setGravity(square ? Gravity.CENTER_HORIZONTAL : Gravity.START);
            TextView tt = text(square ? 13.5f : 13f, Ui.font(this, "medium"), 1f);
            titleRef[0] = tt;
            tt.setText(title);
            tt.setShadowLayer(dp(4), 0, dp(1), 0x99000000);
            tt.setMaxLines(2);
            texts.addView(tt);
            if (status != null) {
                sub = text(11.5f, Ui.font(this, "regular"), 0.85f);
                sub.setShadowLayer(dp(4), 0, dp(1), 0x99000000);
                sub.setText(status.get());
                sub.setSingleLine(true);
                sub.setEllipsize(TextUtils.TruncateAt.END);
                texts.addView(sub);
            }
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(square ? ViewGroup.LayoutParams.WRAP_CONTENT : 0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, square ? 0 : 1f);
            if (square) tlp.topMargin = dp(12);
            else tlp.leftMargin = dp(10);
            t.addView(texts, tlp);
        }
        TextView subRef = sub;
        // Focused (tvOS): the tile turns solid white with a dark glyph and text.
        GradientDrawable white = new GradientDrawable();
        white.setCornerRadius(radius);
        white.setColor(0xF7FFFFFF);
        int glyphColor = glyph.equals("wifi") ? 0xFF0A84FF : Color.WHITE;
        t.setOnFocusChangeListener((v, has) -> {
            t.setBackground(has ? white : glass);
            gi.setImageBitmap(Glyphs.get(glyph, gs, has ? (glyph.equals("wifi") ? 0xFF0A84FF : 0xFF111114) : glyphColor));
            TextView tt = titleRef[0];
            if (tt != null) {
                tt.setTextColor(has ? 0xFF111114 : Color.WHITE);
                tt.setShadowLayer(has ? 0 : dp(4), 0, has ? 0 : dp(1), 0x99000000);
            }
            if (subRef != null) {
                subRef.setTextColor(has ? 0xB3111114 : Color.WHITE);
                subRef.setShadowLayer(has ? 0 : dp(4), 0, has ? 0 : dp(1), 0x99000000);
            }
            if (memBarFill != null && memBarFill.getParent() != null && v == ((View) memBarFill.getParent()).getParent().getParent()) {
                ((GradientDrawable) ((View) memBarFill.getParent()).getBackground()).setColor(has ? 0x33111114 : 0x40FFFFFF);
                ((GradientDrawable) memBarFill.getBackground()).setColor(has ? 0xFF111114 : (memoryUsed() > 0.85f ? 0xFFFF9F0A : Color.WHITE));
            }
            v.animate().scaleX(has ? 1.06f : 1f).scaleY(has ? 1.06f : 1f)
                    .setDuration(200).setInterpolator(LIFT).start();
        });
        t.setOnClickListener(v -> {
            click.onClick(v);
            if (subRef != null && status != null) subRef.setText(status.get());
        });
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
        lp.leftMargin = x;
        lp.topMargin = y;
        panel.addView(t, lp);
        return t;
    }

    // ---------------------------------------------------------------- memory

    float memoryUsed() {
        android.app.ActivityManager am = getSystemService(android.app.ActivityManager.class);
        android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.totalMem > 0 ? 1f - mi.availMem / (float) mi.totalMem : 0f;
    }

    String memoryState() {
        return Math.round(memoryUsed() * 100) + I18n.s(this, "% used  \u00b7  Clean", "% kullanımda  \u00b7  Temizle");
    }

    View memBarFill;

    /** A thin usage bar under the Memory tile's text. */
    void addMemoryBar(View tile) {
        if (!(tile instanceof LinearLayout) || ((LinearLayout) tile).getChildCount() < 2) return;
        View texts = ((LinearLayout) tile).getChildAt(1);
        if (!(texts instanceof LinearLayout)) return;
        FrameLayout track = new FrameLayout(this);
        GradientDrawable tb = new GradientDrawable();
        tb.setCornerRadius(dp(2));
        tb.setColor(0x40FFFFFF);
        track.setBackground(tb);
        memBarFill = new View(this);
        GradientDrawable fb = new GradientDrawable();
        fb.setCornerRadius(dp(2));
        fb.setColor(Color.WHITE);
        memBarFill.setBackground(fb);
        track.addView(memBarFill, new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(78), dp(4));
        lp.topMargin = dp(4);
        ((LinearLayout) texts).addView(track, lp);
        track.post(this::updateMemoryBar);
    }

    void updateMemoryBar() {
        if (memBarFill == null || memBarFill.getParent() == null) return;
        View track = (View) memBarFill.getParent();
        float used = memoryUsed();
        ViewGroup.LayoutParams lp = memBarFill.getLayoutParams();
        lp.width = Math.round(track.getWidth() * used);
        memBarFill.setLayoutParams(lp);
        ((GradientDrawable) memBarFill.getBackground()).setColor(used > 0.85f ? 0xFFFF9F0A : Color.WHITE);
    }

    /** Apps opened from Home recently, newest first (Android hides other apps' processes). */
    void noteLaunched(String pkg) {
        if (pkg == null || pkg.equals(getPackageName())) return;
        RecentAppsOverlay.recordAppUsed(this, pkg);
        List<String> l = new ArrayList<>(Arrays.asList(getSharedPreferences("clean", MODE_PRIVATE).getString("recent", "").split(",")));
        l.remove(pkg);
        l.remove("");
        l.add(0, pkg);
        while (l.size() > 12) l.remove(l.size() - 1);
        getSharedPreferences("clean", MODE_PRIVATE).edit().putString("recent", String.join(",", l)).apply();
    }

    List<String> recentApps() {
        List<String> l = new ArrayList<>();
        for (String p : getSharedPreferences("clean", MODE_PRIVATE).getString("recent", "").split(",")) {
            if (!p.isEmpty() && Suggestions.installed(this, p)) l.add(p);
        }
        return l;
    }

    /**
     * Clean: close the apps you've opened since the last clean. With testOS's accessibility switch on,
     * it presses Force stop on each app's info page (what you'd do by hand); otherwise Android is
     * asked to drop their background processes. The memory freed is measured.
     */
    void cleanMemory() {
        android.app.ActivityManager am = getSystemService(android.app.ActivityManager.class);
        List<String> apps = recentApps();
        if (apps.isEmpty()) {
            toast(I18n.s(this, "No apps to close", "Kapatılacak uygulama yok"));
            return;
        }
        long before = availMem(am);
        for (String pkg : apps) {
            try {
                am.killBackgroundProcesses(pkg);
            } catch (Exception ignored) {
                // not allowed for this one
            }
        }
        Runnable done = () -> {
            getSharedPreferences("clean", MODE_PRIVATE).edit().remove("recent").apply();
            long freed = Math.max(0, availMem(am) - before) / (1024 * 1024);
            updateMemoryBar();
            toast(I18n.s(this,
                    "Closed " + apps.size() + (apps.size() == 1 ? " app" : " apps") + (freed > 0 ? " \u00b7 freed " + freed + " MB" : ""),
                    apps.size() + " uygulama kapatıldı" + (freed > 0 ? " \u00b7 " + freed + " MB boşaltıldı" : "")));
        };
        if (PowerService.instance != null) {
            closePicker();
            PowerService.forceStop(this, apps, () -> handler.postDelayed(done, 600));
        } else {
            handler.postDelayed(done, 800);
        }
    }

    long availMem(android.app.ActivityManager am) {
        android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    String wifiName() {
        try {
            android.net.ConnectivityManager cm = getSystemService(android.net.ConnectivityManager.class);
            android.net.NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (nc == null) return I18n.s(this, "Not Connected", "Bağlı Değil");
            if (nc.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)) return "Ethernet";
            if (!nc.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)) return I18n.s(this, "Connected", "Bağlı");
            android.net.wifi.WifiManager wm = getApplicationContext().getSystemService(android.net.wifi.WifiManager.class);
            String ssid = wm.getConnectionInfo().getSSID();
            if (ssid == null || ssid.contains("unknown ssid")) return I18n.s(this, "Connected", "Bağlı");
            return ssid.replace("\"", "");
        } catch (Exception e) {
            return I18n.s(this, "Connected", "Bağlı");
        }
    }

    String dndState() {
        android.app.NotificationManager nm = getSystemService(android.app.NotificationManager.class);
        return nm.getCurrentInterruptionFilter() > android.app.NotificationManager.INTERRUPTION_FILTER_ALL
                ? I18n.s(this, "On", "Açık") : I18n.s(this, "Off", "Kapalı");
    }

    void toggleDnd() { Perms.toggleDnd(this); }

    String audioOutput() {
        android.media.AudioManager am = getSystemService(android.media.AudioManager.class);
        for (android.media.AudioDeviceInfo d : am.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS)) {
            switch (d.getType()) {
                case android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP: return d.getProductName() + "";
                case android.media.AudioDeviceInfo.TYPE_HDMI_ARC:
                case android.media.AudioDeviceInfo.TYPE_HDMI_EARC: return "HDMI ARC";
                default: break;
            }
        }
        return I18n.s(this, "TV Speakers", "TV Hoparlörü");
    }

    String timerState() {
        int left = SleepReceiver.timerLeft(this);
        return left > 0 ? left + I18n.s(this, " min left", " dk kaldı") : I18n.s(this, "Off", "Kapalı");
    }

    /** Off \u2192 15 \u2192 30 \u2192 60 \u2192 90 \u2192 Off. */
    void cycleSleepTimer() {
        int left = SleepReceiver.timerLeft(this);
        int[] steps = {15, 30, 60, 90};
        int next = steps[0];
        if (left > 0) {
            next = 0;
            for (int st : steps) if (st > left + 1) { next = st; break; }
        }
        SleepReceiver.setTimer(this, next);
        if (sceneHome != null) sceneHome.refreshMoonBtn();
    }


    // ---------------------------------------------------------------- inputs

    /** Input picker under the status row: Control Center capsules, two columns. */
    void showInputs(View from) {
        List<Inputs.Input> list = Inputs.list(this);
        if (list.isEmpty()) {
            toast(I18n.s(this, "This TV has no inputs", "Bu TV'de giriş bulunmuyor"));
            return;
        }
        dropPicker();
        popoverReturn = from;
        FrameLayout layer = new FrameLayout(this);
        layer.setClickable(true);
        layer.setClipChildren(false);
        View shade = new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,
                new int[]{0x99000000, 0x40000000, 0x00000000}));
        layer.addView(shade, new FrameLayout.LayoutParams(screenW * 3 / 5, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END));

        int col = dp(141), gap = dp(13), cap = (col - gap) / 2;
        FrameLayout panel = new FrameLayout(this);
        panel.setClipChildren(false);
        List<View> cells = new ArrayList<>();
        TextView title = text(13, Ui.font(this, "semibold"), 0.75f);
        title.setText(I18n.s(this, "Inputs", "Girişler"));
        panel.addView(title, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(20)));
        int top = dp(26);
        for (int i = 0; i < list.size(); i++) {
            Inputs.Input in = list.get(i);
            int x = (i % 2) * (col + gap), y = top + (i / 2) * (cap + gap);
            cells.add(ccTile(panel, x, y, col, cap, Inputs.glyph(in), in.label,
                    in.disconnected ? () -> I18n.s(this, "Not connected", "Bağlı değil") : null, false, v -> {
                        closePicker();
                        if (!Inputs.open(this, in)) toast(I18n.s(this, "Can't switch to " + in.label, in.label + " girişine geçilemiyor"));
                    }));
        }
        int rows = (list.size() + 1) / 2;
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(2 * col + gap, top + rows * cap + (rows - 1) * gap,
                Gravity.TOP | Gravity.END);
        plp.topMargin = dp(72);
        plp.rightMargin = dockOuter;
        layer.addView(panel, plp);

        root.addView(layer, match());
        pickerLayer = layer;
        pickerCells = cells;
        layer.setAlpha(0f);
        panel.setTranslationY(-dp(10));
        layer.animate().alpha(1f).setDuration(200).start();
        panel.animate().translationY(0).setDuration(260).setInterpolator(EASE).start();
        cells.get(0).requestFocus();
    }

    // ---------------------------------------------------------------- connectivity recovery

    int netState = -1;

    /** Detect connectivity recovery for services; no status-row UI remains. */
    void updateNet() {
        if (sceneHome != null) sceneHome.refreshVpnIndicator();
        android.net.ConnectivityManager cm = getSystemService(android.net.ConnectivityManager.class);
        android.net.Network active = cm != null ? cm.getActiveNetwork() : null;
        android.net.NetworkCapabilities nc = cm != null && active != null ? cm.getNetworkCapabilities(active) : null;
        int state = nc == null ? 0
                : nc.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED) ? 2 : 1;
        boolean back = netState == 0 || netState == 1;
        if (state == netState) return;
        netState = state;
        if (state == 2) AirPlayGuard.check(this, back);
        if (state == 2 && back) Weather.refresh(this, true, this::updateWeather);
    }

    /** Follows the default network; Android marks it "validated" once the internet is reachable. */

    /**
     * Apple-style network symbol, white only: Wi-Fi as a dot-wedge and two bands lit by signal
     * strength (unlit at 30%), or Ethernet; "!" beside it without internet (wifi.exclamationmark)
     * and a slash through dimmed bands with no network (wifi.slash).
     */
    /** SF-style Wi-Fi: a wedge and two equal bands fanning 90 degrees from a point near the bottom. */
    /** Ethernet: three solid nodes joined by lines, drawn bolder to match the Wi-Fi weight. */
    /** Android TV's system click on D-pad moves, select and Back (Settings -> Home -> Navigation sounds). */
    static void navSound(Context c, KeyEvent ev) {
        if (ev.getAction() != KeyEvent.ACTION_DOWN) return;
        switch (ev.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Clicks.move(c, ev.getKeyCode());
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (ev.getRepeatCount() == 0) Clicks.select(c);
                break;
            case KeyEvent.KEYCODE_BACK:
                if (ev.getRepeatCount() == 0) Clicks.back(c);
                break;
            default:
                break;
        }
    }

    /** Refresh button: update weather data. */
    // ---------------------------------------------------------------- built-in ambient mode and lock screen

    boolean ambientShowcase;
    boolean lockScreenActive;
    boolean lockScreenFromExternal;
    long ambientEnterTime;
    final Set<Integer> ambientDownKeys = new HashSet<>();

    public static void triggerBuiltInAmbient(Context c) {
        if (activeInstance != null && activeInstance.resumed) {
            activeInstance.runOnUiThread(() -> {
                activeInstance.lockScreenActive = false;
                activeInstance.lockScreenFromExternal = false;
                if (activeInstance.sceneHome != null) {
                    activeInstance.sceneHome.sceneView.setLockScreenMode(false);
                }
                activeInstance.enterAmbientShowcase();
            });
            return;
        }
        try {
            Intent i = new Intent(c, MainActivity.class)
                    .putExtra("panel", "ambient")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            c.startActivity(i);
        } catch (Exception ignored) {
        }
    }

    public static void triggerLockScreen(Context c) {
        if (activeInstance != null && activeInstance.resumed) {
            activeInstance.runOnUiThread(() -> activeInstance.enterLockScreen(false));
            return;
        }
        try {
            Intent i = new Intent(c, MainActivity.class)
                    .putExtra("panel", "lock")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            c.startActivity(i);
        } catch (Exception ignored) {
        }
    }

    void enterLockScreen(boolean fromExternal) {
        lockScreenActive = true;
        lockScreenFromExternal = fromExternal;
        if (sceneHome != null) {
            sceneHome.sceneView.setLockScreenMode(true);
        }
        enterAmbientShowcase();
    }

    /**
     * Built-in Ambient Mode fallback when the TV has no external Ambient Mode app installed:
     * hides the dock and Control Panel icon so the background scene, giant glass clock,
     * date, weather, and greeting are showcased full-screen. Any key press restores the UI.
     */
    void enterAmbientShowcase() {
        ControlPanelOverlay.dismiss();
        dropPicker();
        pickerLayer = null;
        popoverReturn = null;
        closeMenu();
        if (sceneHome != null && sceneHome.isSheetOpen()) sceneHome.closeSheet();
        handler.removeCallbacks(lockIdleRunnable);
        ambientShowcase = true;
        ambientEnterTime = android.os.SystemClock.uptimeMillis();
        ambientDownKeys.clear();
        if (root != null) root.setKeepScreenOn(true);
        if (sceneHome != null) {
            sceneHome.sceneView.setVisibility(View.VISIBLE);
            sceneHome.sceneView.syncSettings();
            sceneHome.sceneView.start();
            updateWeather();
            sceneHome.overlay.animate().cancel();
            sceneHome.overlay.animate().alpha(0f).setDuration(320).setInterpolator(EASE)
                    .withEndAction(() -> {
                        if (ambientShowcase && sceneHome != null) sceneHome.overlay.setVisibility(View.INVISIBLE);
                    }).start();
        }
    }

    void exitAmbientShowcase() {
        if (!ambientShowcase) return;
        ambientShowcase = false;
        boolean wasLock = lockScreenActive;
        boolean wasExternal = lockScreenFromExternal;
        lockScreenActive = false;
        lockScreenFromExternal = false;
        if (sceneHome != null && wasLock) sceneHome.sceneView.setLockScreenMode(false);
        if (root != null) root.setKeepScreenOn(false);
        if (sceneHome != null) {
            sceneHome.overlay.animate().cancel();
            sceneHome.overlay.setVisibility(View.VISIBLE);
            sceneHome.overlay.setAlpha(0f);
            sceneHome.overlay.animate().alpha(1f).setDuration(260).setInterpolator(EASE).start();
            sceneHome.focusDock(sceneHome.focusedDockIdx);
        }
        resetIdle();
        if (wasLock && wasExternal) moveTaskToBack(true);
    }

    /** Seconds without input before the dock slides away (Settings -> Home); 0 = never. */

    final Runnable lockIdleRunnable = () -> {
        if (resumed && !ambientShowcase) {
            enterLockScreen(false);
        }
    };

    void resetIdle() {
        handler.removeCallbacks(lockIdleRunnable);
        int lockMin = Scenes.getLockIdleMinutes(this);
        if (lockMin > 0 && resumed && !ambientShowcase) {
            handler.postDelayed(lockIdleRunnable, lockMin * 60_000L);
        }
    }

    /**
     * No input for a while: the dock and grid slide down off screen, the weather card and tab bar
     * fade, and the carousel keeps playing full screen with its title and summary at the bottom.
     */
    // ---------------------------------------------------------------- weather

    final Runnable weatherTick = new Runnable() {
        @Override public void run() {
            Weather.refresh(MainActivity.this, false, MainActivity.this::updateWeather);
            updateWeather();
            if (++tickCount % 15 == 0) AirPlayGuard.check(MainActivity.this, false);
            LocalAdb.checkAndRepairPermissionsIfNeeded(MainActivity.this, false);
            updateNet();
            handler.postDelayed(this, 60 * 1000L);
        }
    };

    /**
     * Once a minute: fetch when the forecast is due (every 15 minutes by default), move "Now" on,
     * and pick up Wi-Fi signal changes.
     */
    int tickCount;
    /** Shows the cached forecast (hidden when weather is off or nothing has loaded yet). */
    void updateWeather() {
        if (sceneHome == null) return;
        Weather.Data data = Weather.enabled(this) ? Weather.cached(this) : null;
        sceneHome.refreshWeatherBtn();
        if (data != null) {
            sceneHome.sceneView.setWeather(data.temp + "\u00b0", Weather.describe(this, data.code, data.day), data.code);
        } else {
            sceneHome.sceneView.setWeather(null, null, -1);
        }
    }

    /** Opens the full forecast from the Night Scenes weather shortcut. */
    void showWeather() {
        Weather.Data d = Weather.cached(this);
        if (d == null) {
            if (Weather.enabled(this)) {
                Weather.refresh(this, true, () -> {
                    updateWeather();
                    if (Weather.cached(this) != null) {
                        showWeather();
                    }
                });
            }
            return;
        }
        dropPicker();
        popoverReturn = sceneHome != null && sceneHome.weatherBtn != null
                && sceneHome.weatherBtn.getVisibility() == View.VISIBLE ? sceneHome.weatherBtn : null;
        FrameLayout layer = new FrameLayout(this);
        layer.setClickable(true);
        layer.setClipChildren(false);
        View shade = new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xA6000000, 0x4D000000, 0x00000000}));
        layer.addView(shade, new FrameLayout.LayoutParams(screenW * 3 / 5, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START));

        WeatherView panel = new WeatherView(this, true, unit);
        LiquidGlass glass = new LiquidGlass(dp(26), 0x33000000, glassSource, screenW, screenH, unit).adaptive();
        glass.lens = dp(7);
        panel.setBackground(glass);
        panel.setFocusable(true);
        panel.setData(d);
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(dp(WeatherView.FULL_W), dp(WeatherView.FULL_H),
                Gravity.TOP | Gravity.START);
        plp.topMargin = dp(24);
        plp.leftMargin = dockOuter;
        layer.addView(panel, plp);

        root.addView(layer, match());
        pickerLayer = layer;
        pickerCells = new ArrayList<>();
        pickerCells.add(panel);

        layer.setAlpha(0f);
        panel.setTranslationY(-dp(10));
        layer.animate().alpha(1f).setDuration(200).start();
        panel.animate().translationY(0).setDuration(260).setInterpolator(EASE).start();
        panel.requestFocus();
        Weather.refresh(this, false, () -> {
            Weather.Data nd = Weather.cached(this);
            if (nd != null && panel.isAttachedToWindow()) panel.setData(nd);
            updateWeather();
        });
    }

    // ---------------------------------------------------------------- profiles

    List<View> pickerCells = new ArrayList<>();

    /**
     * tvOS 26 profile switcher (per Apple's UI): a popover under the top-right status area with a
     * two-column grid of round avatars, names in soft grey, a green check on the current profile,
     * and a glass "+" Add User. Content stays visible behind it.
     */

    /**
     * The whole Home Screen (artwork, dock, cards, status row) blurred and dimmed behind an overlay:
     * a quarter-size snapshot taken once when the overlay opens, then blurred off the GPU path.
     */
    void addBlurredBackdrop(FrameLayout layer, int dim) {
        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        boolean bakedDim = false;
        try {
            int w = Math.max(1, screenW / 4), h = Math.max(1, screenH / 4);
            Bitmap snap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(snap);
            c.scale(w / (float) screenW, h / (float) screenH);
            root.draw(c);
            Bitmap blurred = Ui.blur(snap, w / 2, h / 2, 4, 3);
            snap.recycle();
            if (blurred != null) {
                new Canvas(blurred).drawColor(dim);
                iv.setImageBitmap(blurred);
                bakedDim = true;
            }
        } catch (Throwable e) {
            Bitmap bg = glassSource.get();
            if (bg != null) iv.setImageBitmap(bg);
        }
        layer.addView(iv, match());
        if (!bakedDim) {
            View v = new View(this);
            v.setBackgroundColor(dim);
            layer.addView(v, match());
        }
    }

    /**
     * Full-screen profile switcher (Apple TV "Who's Watching?"): the Home Screen blurred and dimmed
     * behind, a centred title and a centred row of large avatars with a green check on the current
     * one, plus Add User.
     */
    // ---------------------------------------------------------------- Search

    /** Opens search over the blurred Home Screen; q (adb hook, voice) starts with that text. */
    void showSearch(String q) {
        dropPicker();
        closeMenu();
        popoverReturn = getCurrentFocus();
        if (popoverReturn == null && sceneHome != null) popoverReturn = sceneHome.moonBtn;
        search = new SearchPanel(this);
        pickerLayer = search.layer;
        pickerCells = new ArrayList<>();
        root.addView(search.layer, match());
        search.firstKey().requestFocus();  // only possible once the layer is attached
        SearchPanel opened = search;
        search.layer.setAlpha(0f);
        // Once search covers the screen, the Home layers under it stop drawing (the backdrop is a snapshot).
        search.layer.animate().alpha(1f).setDuration(200).withEndAction(() -> {
            if (search != opened) return;
            hiddenUnderSearch.clear();
            for (int i = 0; i < root.getChildCount(); i++) {
                View c = root.getChildAt(i);
                if (c != opened.layer && c != launchOverlay && c.getVisibility() == View.VISIBLE) {
                    c.setVisibility(View.INVISIBLE);
                    hiddenUnderSearch.add(c);
                }
            }
        }).start();
        if (q != null && !q.isEmpty()) search.setQuery(q);
    }

    /** How long Back is held on Home to open Search. */
    static final long BACK_HOLD_MS = 1000;

    /** Back pressed on Home and held for BACK_HOLD_MS: open Search (the release is swallowed). */
    final Runnable backHold = () -> {
        backDown = null;
        swallowUp = true;
        showSearch(null);
    };
    KeyEvent backDown;
    boolean replayingBack;

    /**
     * The remote shortcut for Search: hold Back on Home. A short press must still behave exactly
     * as before, so Back is held back until it is released (or held long enough), then replayed.
     */
    boolean holdBackSearch(KeyEvent ev) {
        if (ev.getKeyCode() != KeyEvent.KEYCODE_BACK || replayingBack) return false;
        if (ev.getAction() == KeyEvent.ACTION_DOWN) {
            if (ev.getRepeatCount() == 0) {
                backDown = new KeyEvent(ev);
                handler.removeCallbacks(backHold);
                handler.postDelayed(backHold, BACK_HOLD_MS);
            }
            return true;
        }
        handler.removeCallbacks(backHold);
        KeyEvent d = backDown;
        backDown = null;
        if (d == null) return true;
        replayingBack = true;
        try {
            dispatchKeyEvent(d);
            dispatchKeyEvent(ev);
        } finally {
            replayingBack = false;
        }
        return true;
    }

    /**
     * Keyboard shortcuts on Home (a USB/Bluetooth keyboard, not the remote): typing a letter or
     * digit opens Search with it typed; "/", Ctrl+F and Ctrl+K open it empty.
     */
    boolean keyboardSearch(KeyEvent ev) {
        if (ev.getAction() != KeyEvent.ACTION_DOWN || ev.getRepeatCount() > 0) return false;
        android.view.InputDevice dev = ev.getDevice();
        if (dev == null || dev.getKeyboardType() != android.view.InputDevice.KEYBOARD_TYPE_ALPHABETIC) return false;
        int k = ev.getKeyCode();
        boolean shortcut = (ev.isCtrlPressed() && (k == KeyEvent.KEYCODE_F || k == KeyEvent.KEYCODE_K))
                || (k == KeyEvent.KEYCODE_SLASH && !ev.isCtrlPressed() && !ev.isAltPressed());
        int ch = ev.isCtrlPressed() || ev.isAltPressed() || ev.isMetaPressed() ? 0 : ev.getUnicodeChar();
        if (!shortcut && !Character.isLetterOrDigit(ch)) return false;
        swallowUp = true;  // the key's release must not reach the search screen
        showSearch(shortcut ? null : String.valueOf((char) ch).toLowerCase());
        return true;
    }

    void closeSearch() {
        if (search == null) return;
        FrameLayout l = search.layer;
        releaseSearch();
        if (pickerLayer == l) {
            pickerLayer = null;
            l.animate().alpha(0f).setDuration(200).withEndAction(() -> root.removeView(l)).start();
        }
        if (popoverReturn != null && popoverReturn.isAttachedToWindow()) popoverReturn.requestFocus();
        else goTop();
        popoverReturn = null;
    }

    final List<View> hiddenUnderSearch = new ArrayList<>();

    void hideUnderOverlay(FrameLayout activeLayer) {
        if (pickerLayer != activeLayer || activeLayer == null) return;
        hiddenUnderSearch.clear();
        for (int i = 0; i < root.getChildCount(); i++) {
            View c = root.getChildAt(i);
            if (c != activeLayer && c != launchOverlay && c.getVisibility() == View.VISIBLE) {
                c.setVisibility(View.INVISIBLE);
                hiddenUnderSearch.add(c);
            }
        }
    }

    void restoreUnderOverlay() {
        for (View c : hiddenUnderSearch) {
            if (c != null) c.setVisibility(View.VISIBLE);
        }
        hiddenUnderSearch.clear();
    }

    void releaseSearch() {
        restoreUnderOverlay();
        if (search == null) return;
        search.release();
        search = null;
    }

    /** Removes whichever overlay is open (stopping search's work if it was search). */
    void dropPicker() {
        restoreUnderOverlay();
        if (pickerLayer == null) return;
        if (search != null && pickerLayer == search.layer) releaseSearch();
        root.removeView(pickerLayer);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (CustomMedia.onActivityResult(req, res, data)) return;
        if (req == SearchPanel.REQ_VOICE && res == RESULT_OK && search != null) search.onVoice(data);
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (req == SearchPanel.REQ_MIC && search != null) {
            search.onMicPermission(results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    /** Directional focus inside a popover: nearest cell whose centre lies in that direction. */
    boolean pickerMove(int keyCode) {
        if (pickerLayer != null && "control_panel".equals(pickerLayer.getTag())) {
            ControlPanelOverlay.moveControlPanelFocus(pickerCells, keyCode);
            return true;
        }
        View cur = getCurrentFocus();
        if (!pickerCells.contains(cur)) {
            if (!pickerCells.isEmpty()) pickerCells.get(0).requestFocus();
            return true;
        }
        int[] a = new int[2];
        cur.getLocationOnScreen(a);
        float ax = a[0] + cur.getWidth() / 2f, ay = a[1] + cur.getHeight() / 2f;
        View best = null;
        float bestScore = Float.MAX_VALUE;
        for (View v : pickerCells) {
            if (v == cur) continue;
            int[] b = new int[2];
            v.getLocationOnScreen(b);
            float dx = b[0] + v.getWidth() / 2f - ax, dy = b[1] + v.getHeight() / 2f - ay;
            float main, cross;
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT: main = -dx; cross = dy; break;
                case KeyEvent.KEYCODE_DPAD_RIGHT: main = dx; cross = dy; break;
                case KeyEvent.KEYCODE_DPAD_UP: main = -dy; cross = dx; break;
                default: main = dy; cross = dx; break;
            }
            if (main <= dp(4)) continue;
            float score = main + Math.abs(cross) * 2.2f;
            if (score < bestScore) {
                bestScore = score;
                best = v;
            }
        }
        if (best != null) best.requestFocus();
        return true;
    }

    /** Avatar (or glass +) with the name below; the current profile gets a green check. */
    View popoverReturn;

    void closePicker() {
        restoreUnderOverlay();
        if (pickerLayer == null) return;
        FrameLayout layer = pickerLayer;
        pickerLayer = null;
        layer.animate().alpha(0f).setDuration(220).withEndAction(() -> root.removeView(layer)).start();

        if (popoverReturn != null && popoverReturn.isAttachedToWindow()) popoverReturn.requestFocus();
        else goTop();
        popoverReturn = null;
    }

    // ---------------------------------------------------------------- context menu

    FrameLayout menuLayer;
    View menuReturnFocus;

    /**
     * tvOS-style context menu: a dark Liquid Glass panel beside the focused card, title on top,
     * rows below; the focused row becomes a white pill with dark text. Back closes it.
     */
    void showMenu(View anchor, String title, List<String> labels, List<Runnable> actions, int destructive) {
        closeMenu();
        menuReturnFocus = anchor;
        menuLayer = new FrameLayout(this);
        menuLayer.setBackgroundColor(0x00000000);
        menuLayer.setClickable(true);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(8), dp(12), dp(8), dp(10));
        // "Regular" Liquid Glass: heavier dimming so the menu text stays legible over anything.
        LiquidGlass glass = new LiquidGlass(dp(20), 0xB81A1A20, glassSource, screenW, screenH, unit);
        glass.lens = dp(8);
        panel.setBackground(glass);
        panel.setClipChildren(false);
        panel.setClipToPadding(false);

        TextView head = text(11, Ui.font(this, "semibold"), 0.55f);
        head.setText(title);
        head.setShadowLayer(0, 0, 0, 0);
        head.setPadding(dp(12), 0, dp(12), dp(8));
        head.setSingleLine(true);
        head.setEllipsize(TextUtils.TruncateAt.END);
        panel.addView(head);

        List<View> rows = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            int idx = i;
            TextView row = text(14, Ui.font(this, "medium"), 1f);
            row.setText(labels.get(i));
            row.setShadowLayer(0, 0, 0, 0);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setMinHeight(dp(34));
            row.setPadding(dp(12), dp(6), dp(12), dp(8));
            row.setLineSpacing(0f, 1.08f);
            row.setFocusable(true);
            row.setClickable(true);
            int base = i == destructive ? 0xFFFF6B5E : Color.WHITE;
            row.setTextColor(base);
            GradientDrawable pill = new GradientDrawable();
            pill.setCornerRadius(dp(12));
            pill.setColor(Color.TRANSPARENT);
            row.setBackground(pill);
            row.setOnFocusChangeListener((v, has) -> {
                pill.setColor(has ? 0xFFFFFFFF : Color.TRANSPARENT);
                row.setTextColor(has ? (idx == destructive ? 0xFFD7372A : 0xFF111114) : base);
            });
            row.setOnClickListener(v -> {
                closeMenu();
                actions.get(idx).run();
            });
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.topMargin = dp(2);
            panel.addView(row, rlp);
            rows.add(row);
        }
        // Wrap focus top <-> bottom and keep it inside the menu.
        for (int i = 0; i < rows.size(); i++) {
            View r = rows.get(i);
            r.setId(View.generateViewId());
        }
        for (int i = 0; i < rows.size(); i++) {
            View r = rows.get(i);
            r.setNextFocusUpId(rows.get((i - 1 + rows.size()) % rows.size()).getId());
            r.setNextFocusDownId(rows.get((i + 1) % rows.size()).getId());
            r.setNextFocusLeftId(r.getId());
            r.setNextFocusRightId(r.getId());
        }

        int w = dp(236);
        int[] loc = new int[2];
        anchor.getLocationInWindow(loc);
        float aw = anchor.getWidth() * anchor.getScaleX(), ah = anchor.getHeight() * anchor.getScaleY();
        int x = (int) (loc[0] + aw + dp(18));
        if (x + w > screenW - dp(24)) x = (int) (loc[0] - w - dp(18));
        x = Math.max(dp(24), x);
        panel.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED);
        int h = panel.getMeasuredHeight();
        int y = (int) (loc[1] + ah / 2 - h / 2f);
        y = Math.max(dp(24), Math.min(y, screenH - h - dp(24)));
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        plp.leftMargin = x;
        plp.topMargin = y;
        menuLayer.addView(panel, plp);
        root.addView(menuLayer, match());

        // Pop in from the card's side.
        panel.setPivotX(x > loc[0] ? 0 : w);
        panel.setPivotY(h / 2f);
        panel.setAlpha(0f);
        panel.setScaleX(0.9f);
        panel.setScaleY(0.9f);
        panel.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(220).setInterpolator(LIFT).start();
        menuLayer.animate().setDuration(220).start();
        menuLayer.setBackgroundColor(0x59000000);
        if (!rows.isEmpty()) rows.get(0).requestFocus();
    }

    void closeMenu() {
        if (menuLayer == null) return;
        FrameLayout m = menuLayer;
        menuLayer = null;
        m.animate().alpha(0f).setDuration(150).withEndAction(() -> root.removeView(m)).start();
        if (menuReturnFocus != null && menuReturnFocus.isAttachedToWindow()) menuReturnFocus.requestFocus();
        menuReturnFocus = null;
    }

    void startSafe(Intent i) {
        try {
            startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            toast("Not available on this TV");
        }
    }

    // ---------------------------------------------------------------- move mode

    /** Window position of every visible card, keyed by app, for the move animation. */
    /**
     * tvOS-style reflow: after the grid is rebuilt in its new order, every card starts at its old
     * on-screen position and glides to its new slot, so neighbours visibly make room.
     */
    // ---------------------------------------------------------------- helpers

    TextView text(float sp, Typeface tf, float alpha) {
        TextView t = new TextView(this);
        Ui.textSize(t, sp);
        t.setTypeface(tf);
        t.setTextColor(Color.WHITE);
        t.setAlpha(alpha);
        t.setShadowLayer(10, 0, 2, 0x99000000);
        return t;
    }

    FrameLayout.LayoutParams match() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /** Design units (1/960 of the screen width), so layout follows the display's resolution. */
    int dp(float v) {
        return Math.round(v * unit);
    }

    void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
