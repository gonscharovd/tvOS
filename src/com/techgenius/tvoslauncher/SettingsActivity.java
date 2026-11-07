package com.techgenius.tvoslauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * testOS Settings with four clear groups:
 * - Appearance: visual style, clock, dock and weather
 * - Basic settings: navigation sounds, hidden apps, setup and reset
 * - Information: app identity, OTA updates and local settings backup
 * - Advanced settings: HOME selection, remote mapping, ADB, permissions and TV cleanup
 *
 * The Control Panel remains available from the launcher; its separate customization screen
 * and all multi-user settings have been removed.
 */
public class SettingsActivity extends Activity {

    static final String ROOT = "root",
            LANGUAGE = "Language",
            LOCATION = "Location",
            WEATHER = "Weather",
            LOOK_SCENES = "Appearance and clock",
            CONTROL_PANEL = "Control panel",
            APPS = "Apps",
            PROFILES = "Profiles",
            BUTTON_MAPPER = "Button mapper",
            QUICK = "Quick buttons",
            ADVANCED = "Advanced",
            UPDATES = "Updates",
            ABOUT = "About",
            SHELF = "Top Shelf",
            INPUTS = "Inputs",
            HOME = "Home",
            PERMS = "Permissions";

    // Third column is an optional non-clickable group heading.
    private static final String[][] SIDEBAR_SECTIONS = {
        {LOOK_SCENES, "scene", "ВНЕШНИЙ ВИД"},
        {WEATHER, "cloud", ""},
        {HOME, "house", "ОСНОВНЫЕ НАСТРОЙКИ"},
        {ABOUT, "info", "ИНФОРМАЦИЯ"},
        {ADVANCED, "shield", "РАСШИРЕННЫЕ НАСТРОЙКИ"}
    };

    private TextView sideTitleView;
    private LinearLayout sideList;
    private TextView mainHeadView;
    private ScrollView scroller;
    private LinearLayout list;
    private TextView msgView;
    private TextView hintView;
    private ProgressBar updateDownloadProgress;
    private TextView updateDownloadStatus;
    private final android.os.Handler updateProgressHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable updateProgressTick = new Runnable() {
        @Override public void run() {
            if (isFinishing()) return;
            if (updateDownloadProgress != null && updateDownloadStatus != null) refreshUpdateDownloadProgress();
            if (AppUpdater.isDownloading()) updateProgressHandler.postDelayed(this, 350L);
        }
    };

    private final List<Runnable> refreshers = new ArrayList<>();
    private static final android.util.LruCache<String, Bitmap> SCENE_THUMB_CACHE =
            new android.util.LruCache<String, Bitmap>(2 * 1024 * 1024) {
                @Override protected int sizeOf(String key, Bitmap value) {
                    return value == null || value.isRecycled() ? 0 : Math.max(1, value.getAllocationByteCount());
                }
            };
    private static final java.util.concurrent.ExecutorService THUMB_EXEC =
            java.util.concurrent.Executors.newFixedThreadPool(2);
    private static volatile Bitmap sCachedBackdropBmp = null;
    private static volatile String sCachedBackdropKey = null;
    private static volatile Bitmap sPlaceholderThumb = null;
    private final Map<String, View> sideRowViews = new HashMap<>();
    private final Map<String, ImageView> sideIcons = new HashMap<>();
    private final Map<String, GradientDrawable> sideIconBgs = new HashMap<>();
    private final Map<String, TextView> sideLabels = new HashMap<>();
    private final Map<String, GradientDrawable> sidePillBgs = new HashMap<>();
    private final Map<String, Integer> sideRowStates = new HashMap<>();
    private boolean pendingSidebarRebuildScheduled = false;
    private final Runnable pendingSidebarRebuild = () -> {
        pendingSidebarRebuildScheduled = false;
        if (!isFinishing()) {
            rebuildMainRows();
        }
    };

    private String currentSection = HOME;

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            SCENE_THUMB_CACHE.evictAll();
            sCachedBackdropBmp = null;
            sCachedBackdropKey = null;
        }
    }
    private boolean inRows = false;
    private boolean rebuilding = false;
    private String liftedSceneId = null;
    private boolean showingHiddenApps = false;
    private String activeSubMenu = null;
    private final List<String> subMenuStack = new ArrayList<>();
    private final List<Integer> subMenuParentFocusIdx = new ArrayList<>();

    // Cached state for async right-side CleanYourTV sub-menus
    private static final int CYT_PAGE_SIZE = 40;
    private Bitmap cachedCheckWhite = null;
    private Bitmap cachedCheckDark = null;
    private boolean cytAutoContinueToDebloat = false;
    private String cytModelFilterQuery = "";
    private int cytAllModelsPage = 0;
    private List<CleanYourTv.ModelMatch> cytScannedMatches = null;
    private int cytScannedCount = 0;
    private boolean cytScanningApps = false;
    private List<CleanYourTv.DebloatItem> cytChecklistItems = null;
    private String cytChecklistSlug = "";
    private boolean cytChecklistLoading = false;
    private boolean cytChecklistIsGoogleTv = false;
    private boolean cytChecklistWifiOwnedBySetupwraith = false;
    private Set<String> cytCachedInstalledPkgs = new HashSet<>();
    private Set<String> cytCachedEnabledPkgs = new HashSet<>();
    private boolean cytLaunchersSyncedAdb = false;
    private List<CleanYourTv.DebloatItem> cytManualItems = null;
    private boolean cytManualLoading = false;
    private String cytManualFilterQuery = "";
    private int cytManualPage = 0;

    private String resolvePageExtra(String pg) {
        if (pg == null || pg.isEmpty()) return HOME;
        if (INPUTS.equals(pg) || CONTROL_PANEL.equals(pg) || "cp".equalsIgnoreCase(pg)
                || "control_panel".equalsIgnoreCase(pg) || "apps".equalsIgnoreCase(pg) || APPS.equals(pg)
                || "profiles".equalsIgnoreCase(pg) || "people".equalsIgnoreCase(pg) || PROFILES.equals(pg)
                || QUICK.equals(pg) || "quick".equalsIgnoreCase(pg)) return HOME;
        if (LOCATION.equals(pg) || WEATHER.equals(pg) || "weather".equalsIgnoreCase(pg)) return WEATHER;
        if (PERMS.equals(pg) || "bm".equalsIgnoreCase(pg) || "button_mapper".equalsIgnoreCase(pg)
                || BUTTON_MAPPER.equals(pg) || "advanced".equalsIgnoreCase(pg)) return ADVANCED;
        if (LOOK_SCENES.equals(pg) || "Look and scenes".equalsIgnoreCase(pg) || "look".equalsIgnoreCase(pg)
                || "scenes".equalsIgnoreCase(pg) || "language".equalsIgnoreCase(pg)) return LOOK_SCENES;
        if ("updates".equalsIgnoreCase(pg) || UPDATES.equals(pg) || "about".equalsIgnoreCase(pg)
                || "information".equalsIgnoreCase(pg) || ABOUT.equals(pg)) return ABOUT;
        return pg;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Clicks.init(this);
        String lang = getIntent().getStringExtra("lang");
        if (lang != null && !lang.isEmpty()) {
            I18n.setLang(this, lang);
        }
        String pg = getIntent().getStringExtra("page");
        if (pg != null && !pg.isEmpty()) {
            currentSection = resolvePageExtra(pg);
        }
        buildUi();
        selectSection(currentSection, false, 0);
        String sub = getIntent().getStringExtra("submenu");
        if (sub != null && !sub.isEmpty()) {
            openSubMenu(sub);
        }
        int scrollY = getIntent().getIntExtra("scroll_y", 0);
        if (scrollY > 0 && scroller != null) {
            scroller.postDelayed(() -> scroller.scrollTo(0, u(scrollY)), 200);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String lang = intent.getStringExtra("lang");
        if (lang != null && !lang.isEmpty()) {
            I18n.setLang(this, lang);
            updateSidebarTranslations();
        }
        String pg = intent.getStringExtra("page");
        if (pg != null && !pg.isEmpty()) {
            activeSubMenu = null;
            subMenuStack.clear();
            subMenuParentFocusIdx.clear();
            selectSection(resolvePageExtra(pg), false, 0);
        }
        String sub = intent.getStringExtra("submenu");
        if (sub != null && !sub.isEmpty()) {
            openSubMenu(sub);
        }
        int scrollY = intent.getIntExtra("scroll_y", 0);
        if (scrollY > 0 && scroller != null) {
            scroller.postDelayed(() -> scroller.scrollTo(0, u(scrollY)), 200);
        }
    }

    private String tr(String en, String trStr) {
        return I18n.s(this, en, trStr);
    }

    private int u(float dp) {
        return Ui.px(this, dp);
    }

    private String sectionTitle(String sec) {
        switch (sec) {
            case HOME: return "Основные настройки";
            case LOOK_SCENES: return "Внешний вид и часы";
            case LOCATION:
            case WEATHER: return "Погода";
            case ADVANCED: return "Расширенные настройки";
            case UPDATES:
            case ABOUT: return "Информация";
            default: return sec;
        }
    }

    private void buildUi() {
        getWindow().setBackgroundDrawable(null);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF0A080C);

        // Blurred scene backdrop with baked dark scrim (single full-screen pass instead of 4 layers)
        ImageView bgView = new ImageView(this);
        bgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bindSettingsBackdropAsync(bgView);
        root.addView(bgView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Two-column grid: left sidebar (276dp) + gap (34dp) + right content (1f)
        LinearLayout columns = new LinearLayout(this);
        columns.setOrientation(LinearLayout.HORIZONTAL);
        columns.setClipChildren(false);
        columns.setClipToPadding(false);
        columns.setPadding(u(44), u(28), u(48), u(22));
        root.addView(columns, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // ---------------- Left Sidebar (.set-side) ----------------
        LinearLayout sideCol = new LinearLayout(this);
        sideCol.setOrientation(LinearLayout.VERTICAL);
        sideCol.setClipChildren(false);
        sideCol.setClipToPadding(false);
        LinearLayout.LayoutParams sideLp = new LinearLayout.LayoutParams(u(288), ViewGroup.LayoutParams.MATCH_PARENT);
        sideLp.rightMargin = u(26);
        columns.addView(sideCol, sideLp);

        sideTitleView = new TextView(this);
        sideTitleView.setText(tr("Home settings", "Ana ekran ayarları"));
        sideTitleView.setTextColor(Color.WHITE);
        sideTitleView.setTypeface(Ui.font(this, "display"));
        sideTitleView.setLetterSpacing(-0.022f);
        Ui.textSize(sideTitleView, 24f);
        sideTitleView.setSingleLine(true);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stLp.leftMargin = u(16);
        stLp.topMargin = u(10);
        stLp.bottomMargin = u(16);
        sideCol.addView(sideTitleView, stLp);

        sideList = new LinearLayout(this);
        sideList.setOrientation(LinearLayout.VERTICAL);
        sideList.setClipChildren(false);
        sideList.setClipToPadding(false);
        sideList.setPadding(u(8), u(4), u(8), u(8));
        sideCol.addView(sideList, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        buildSidebarRows();

        // ---------------- Right Main Column (.set-main) ----------------
        LinearLayout mainCol = new LinearLayout(this);
        mainCol.setOrientation(LinearLayout.VERTICAL);
        mainCol.setClipChildren(true);
        mainCol.setClipToPadding(true);
        columns.addView(mainCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        mainHeadView = new TextView(this);
        mainHeadView.setTextColor(Color.WHITE);
        mainHeadView.setTypeface(Ui.font(this, "display"));
        mainHeadView.setLetterSpacing(-0.015f);
        Ui.textSize(mainHeadView, 20f);
        mainHeadView.setSingleLine(true);
        LinearLayout.LayoutParams mhLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mhLp.leftMargin = u(16);
        mhLp.topMargin = u(14);
        mhLp.bottomMargin = u(12);
        mainCol.addView(mainHeadView, mhLp);

        scroller = new ScrollView(this);
        scroller.setVerticalScrollBarEnabled(false);
        scroller.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroller.setClipChildren(true);
        scroller.setClipToPadding(true);
        scroller.setPadding(0, 0, 0, 0);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setClipChildren(false);
        list.setClipToPadding(false);
        list.setPadding(u(18), u(8), u(18), u(28));
        scroller.addView(list, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mainCol.addView(scroller, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        msgView = new TextView(this);
        msgView.setTextColor(0xFF8FD3FF);
        msgView.setTypeface(Ui.font(this, "medium"));
        Ui.textSize(msgView, 11.5f);
        msgView.setSingleLine(true);
        msgView.setVisibility(View.GONE);
        GradientDrawable msgBg = new GradientDrawable();
        msgBg.setCornerRadius(u(11));
        msgBg.setColor(0xD8181A24);
        msgBg.setStroke(Math.max(1, u(1)), 0x338FD3FF);
        msgView.setBackground(msgBg);
        msgView.setPadding(u(14), u(6), u(14), u(6));
        LinearLayout.LayoutParams msgLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        msgLp.leftMargin = u(18);
        msgLp.topMargin = u(6);
        mainCol.addView(msgView, msgLp);

        hintView = new TextView(this);
        hintView.setTextColor(0xB8FFFFFF);
        hintView.setTypeface(Ui.font(this, "medium"));
        Ui.textSize(hintView, 10.5f);
        hintView.setSingleLine(true);
        GradientDrawable hintBg = new GradientDrawable();
        hintBg.setCornerRadius(u(12));
        hintBg.setColor(0xD916161F);
        hintBg.setStroke(Math.max(1, u(1)), 0x26FFFFFF);
        hintView.setBackground(hintBg);
        hintView.setPadding(u(14), u(7), u(14), u(7));
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hintLp.leftMargin = u(18);
        hintLp.topMargin = u(8);
        mainCol.addView(hintView, hintLp);

        setContentView(root);
    }

    /** Creates or reuses the soft blurred scene + dock glow backdrop behind Home settings without stalling the UI thread. */
    private void bindSettingsBackdropAsync(final ImageView bgView) {
        List<String> enabled = Scenes.getEnabledSceneIds(this);
        final String sceneId = enabled.isEmpty() ? "night" : enabled.get(0);
        final String key = sceneId + ":" + (Scenes.isCustomId(sceneId) ? "custom" : "asset");
        Bitmap cached = sCachedBackdropBmp;
        if (cached != null && !cached.isRecycled() && key.equals(sCachedBackdropKey)) {
            bgView.setImageDrawable(new BitmapDrawable(getResources(), cached));
            return;
        }
        if (cached != null && !cached.isRecycled()) {
            bgView.setImageDrawable(new BitmapDrawable(getResources(), cached));
        }
        THUMB_EXEC.execute(() -> {
            Bitmap blurred = buildSettingsBackdrop(sceneId);
            if (blurred != null) {
                sCachedBackdropBmp = blurred;
                sCachedBackdropKey = key;
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        bgView.setImageDrawable(new BitmapDrawable(getResources(), blurred));
                    }
                });
            }
        });
    }

    private static Bitmap buildSettingsBackdropStatic(android.content.Context ctx, String sceneId) {
        try {
            Scenes.Spec spec = Scenes.byId(sceneId);
            Bitmap src = Scenes.CUSTOM_ID.equals(sceneId)
                    ? CustomMedia.getCustomSceneWallpaper(ctx, 480, 270)
                    : Scenes.loadAssetBitmap(ctx, spec.bgAsset, 480, 270);
            if (src == null) return null;
            int w = 240, h = 135;
            Bitmap base = Bitmap.createScaledBitmap(src, w, h, true);
            if (base != src) src.recycle();
            Canvas c = new Canvas(base);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            // Subtle dock-like color dots along the bottom before blurring, as seen in home-settings.jpg
            int[] dockCols = {0xFFE50914, 0xFF00C2B8, 0xFF0066E5, 0xFF8E3CF7, 0xFF00A8E1, 0xFFF5C518};
            for (int i = 0; i < dockCols.length; i++) {
                p.setColor(dockCols[i]);
                p.setAlpha(110);
                float cx = w * (0.20f + i * 0.11f);
                c.drawCircle(cx, h * 0.88f, w * 0.035f, p);
            }
            Bitmap blurred = Ui.blur(base, w, h, 8, 3);
            if (blurred != base) base.recycle();
            if (blurred != null) {
                new Canvas(blurred).drawColor(0xCC0A080C);
            }
            return blurred;
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap buildSettingsBackdrop(String sceneId) {
        return buildSettingsBackdropStatic(this, sceneId);
    }

    /** Pre-warms the Settings backdrop and scene/background thumbnails in the background so Settings opens in <16ms. */
    public static void prewarmCache(final android.content.Context ctx) {
        if (ctx == null) return;
        THUMB_EXEC.execute(() -> {
            try {
                List<String> enabled = Scenes.getEnabledSceneIds(ctx);
                String sceneId = enabled.isEmpty() ? "night" : enabled.get(0);
                String key = sceneId + ":" + (Scenes.isCustomId(sceneId) ? "custom" : "asset");
                if (sCachedBackdropBmp == null || sCachedBackdropBmp.isRecycled() || !key.equals(sCachedBackdropKey)) {
                    Bitmap blurred = buildSettingsBackdropStatic(ctx, sceneId);
                    if (blurred != null) {
                        sCachedBackdropBmp = blurred;
                        sCachedBackdropKey = key;
                    }
                }
                int reqW = Ui.px(ctx, 46) * 2;
                int reqH = Ui.px(ctx, 26) * 2;
                for (Scenes.Spec spec : Scenes.ALL) {
                    if (spec.bgAsset == null || spec.bgAsset.isEmpty()) continue;
                    String tk = spec.bgAsset + ":" + reqW + "x" + reqH;
                    if (SCENE_THUMB_CACHE.get(tk) == null) {
                        Bitmap raw = Scenes.loadAssetBitmap(ctx, spec.bgAsset, reqW, reqH);
                        if (raw != null) {
                            Bitmap scaled = Bitmap.createScaledBitmap(raw, reqW, reqH, true);
                            if (scaled != raw) raw.recycle();
                            SCENE_THUMB_CACHE.put(tk, scaled);
                        }
                    }
                }
                for (int i = 0; i < Scenes.CLASSIC_PHOTOS.length; i++) {
                    String assetPath = "backgrounds/" + Scenes.CLASSIC_PHOTOS[i][0];
                    String tk = assetPath + ":" + reqW + "x" + reqH;
                    if (SCENE_THUMB_CACHE.get(tk) == null) {
                        Bitmap raw = Scenes.loadAssetBitmap(ctx, assetPath, reqW, reqH);
                        if (raw != null) {
                            Bitmap scaled = Bitmap.createScaledBitmap(raw, reqW, reqH, true);
                            if (scaled != raw) raw.recycle();
                            SCENE_THUMB_CACHE.put(tk, scaled);
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        });
    }

    private void buildSidebarRows() {
        sideList.removeAllViews();
        sideRowViews.clear();
        sideIcons.clear();
        sideIconBgs.clear();
        sideLabels.clear();
        sidePillBgs.clear();
        sideRowStates.clear();

        int iconBoxSz = u(27);
        final float iconRad = iconBoxSz * 0.26f; // Exact squircle corner ratio used by dock app icons

        for (int i = 0; i < SIDEBAR_SECTIONS.length; i++) {
            final String secKey = SIDEBAR_SECTIONS[i][0];
            final String glyphName = SIDEBAR_SECTIONS[i][1];
            if (SIDEBAR_SECTIONS[i].length > 2 && !SIDEBAR_SECTIONS[i][2].isEmpty()) {
                TextView group = new TextView(this);
                group.setText(SIDEBAR_SECTIONS[i][2]);
                group.setTextColor(0x80FFFFFF);
                group.setTypeface(Ui.font(this, "semibold"));
                group.setLetterSpacing(0.08f);
                Ui.textSize(group, 10.5f);
                LinearLayout.LayoutParams groupLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                groupLp.leftMargin = u(13);
                groupLp.topMargin = u(i == 0 ? 0 : 13);
                groupLp.bottomMargin = u(5);
                sideList.addView(group, groupLp);
            }
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setFocusable(true);
            row.setClickable(true);
            row.setClipChildren(false);
            row.setClipToPadding(false);
            row.setMinimumHeight(u(35));
            row.setPadding(u(7), u(4), u(14), u(4));

            GradientDrawable pillBg = new GradientDrawable();
            pillBg.setCornerRadius(u(12));
            pillBg.setColor(Color.TRANSPARENT);
            row.setBackground(pillBg);

            // Squircle icon container matching the dock app icon shape (.ssec i)
            FrameLayout iconBox = new FrameLayout(this);
            GradientDrawable iconBg = new GradientDrawable();
            iconBg.setCornerRadius(iconRad);
            iconBg.setColor(0x1AFFFFFF);
            iconBox.setBackground(iconBg);

            int iconGlyphSz = u(15);
            ImageView iconIv = new ImageView(this);
            iconIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iconIv.setImageBitmap(Glyphs.get(glyphName, iconGlyphSz, Color.WHITE));
            iconBox.addView(iconIv, new FrameLayout.LayoutParams(iconGlyphSz, iconGlyphSz, Gravity.CENTER));

            LinearLayout.LayoutParams ibLp = new LinearLayout.LayoutParams(iconBoxSz, iconBoxSz);
            ibLp.rightMargin = u(11);
            row.addView(iconBox, ibLp);

            TextView label = new TextView(this);
            label.setText(sectionTitle(secKey));
            label.setTypeface(Ui.font(this, "semibold"));
            Ui.textSize(label, 14.5f);
            label.setTextColor(0xC2FFFFFF);
            label.setSingleLine(true);
            row.addView(label, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            row.setOnFocusChangeListener((v, has) -> {
                if (rebuilding) return;
                if (has) {
                    inRows = false;
                    liftedSceneId = null;
                    boolean sectionChanged = !secKey.equals(currentSection) || showingHiddenApps || activeSubMenu != null;
                    if (sectionChanged) {
                        showingHiddenApps = false;
                        activeSubMenu = null;
                        subMenuStack.clear();
                        subMenuParentFocusIdx.clear();
                        currentSection = secKey;
                        mainHeadView.setText(sectionTitle(currentSection));
                        scheduleSidebarRebuild();
                    }
                    updateSidebarStyles();
                    updateHintText(false);
                } else {
                    updateSidebarStyles();
                }
                v.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                        .setDuration(160).start();
            });

            row.setOnClickListener(v -> focusFirstRow());

            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.bottomMargin = u(2);
            sideList.addView(row, rlp);

            sideRowViews.put(secKey, row);
            sideIcons.put(secKey, iconIv);
            sideIconBgs.put(secKey, iconBg);
            sideLabels.put(secKey, label);
            sidePillBgs.put(secKey, pillBg);
        }
    }

    private void scheduleSidebarRebuild() {
        if (sideList == null) {
            rebuildMainRows();
            return;
        }
        sideList.removeCallbacks(pendingSidebarRebuild);
        pendingSidebarRebuildScheduled = true;
        sideList.postDelayed(pendingSidebarRebuild, 32);
    }

    private void flushPendingSidebarRebuild() {
        if (pendingSidebarRebuildScheduled) {
            if (sideList != null) sideList.removeCallbacks(pendingSidebarRebuild);
            pendingSidebarRebuildScheduled = false;
            rebuildMainRows();
        }
    }

    private void updateSidebarStyles() {
        int iconGlyphSz = u(15);
        for (String[] pair : SIDEBAR_SECTIONS) {
            String key = pair[0];
            String glyph = pair[1];
            View row = sideRowViews.get(key);
            if (row == null) continue;
            boolean focused = row.isFocused();
            boolean active = key.equals(currentSection);
            int state = focused ? 2 : (active ? 1 : 0);
            Integer prev = sideRowStates.get(key);
            if (prev != null && prev == state) continue;
            sideRowStates.put(key, state);

            GradientDrawable pillBg = sidePillBgs.get(key);
            GradientDrawable iconBg = sideIconBgs.get(key);
            ImageView iconIv = sideIcons.get(key);
            TextView label = sideLabels.get(key);

            if (focused) {
                pillBg.setColor(Color.WHITE);
                iconBg.setColor(0x14000000);
                iconIv.setImageBitmap(Glyphs.get(glyph, iconGlyphSz, 0xFF111114));
                label.setTextColor(0xFF111114);
            } else if (active) {
                pillBg.setColor(0x22FFFFFF); // .ssec.on: rgba(255,255,255,0.13)
                iconBg.setColor(0x1AFFFFFF);
                iconIv.setImageBitmap(Glyphs.get(glyph, iconGlyphSz, Color.WHITE));
                label.setTextColor(Color.WHITE);
            } else {
                pillBg.setColor(Color.TRANSPARENT);
                iconBg.setColor(0x1AFFFFFF);
                iconIv.setImageBitmap(Glyphs.get(glyph, iconGlyphSz, 0xD9FFFFFF));
                label.setTextColor(0xC2FFFFFF);
            }
        }
    }

    private void updateSidebarTranslations() {
        sideTitleView.setText(tr("Home settings", "Ana ekran ayarları"));
        for (String[] pair : SIDEBAR_SECTIONS) {
            String key = pair[0];
            TextView label = sideLabels.get(key);
            if (label != null) label.setText(sectionTitle(key));
        }
    }

    private void selectSection(String sec, boolean focusRightPane, int rowIdx) {
        if (sideList != null) sideList.removeCallbacks(pendingSidebarRebuild);
        pendingSidebarRebuildScheduled = false;
        currentSection = sec;
        inRows = focusRightPane;
        rebuildMainRows();
        updateSidebarStyles();
        if (focusRightPane) {
            focusRowAtIndex(rowIdx);
        } else {
            View sideRow = sideRowViews.get(sec);
            if (sideRow != null) sideRow.requestFocus();
        }
    }

    private void focusFirstRow() {
        focusRowAtIndex(0);
    }

    private void focusRowAtIndex(int targetIdx) {
        flushPendingSidebarRebuild();
        View target = null;
        int count = 0;
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            if (child.isFocusable()) {
                if (target == null || count == targetIdx) {
                    target = child;
                }
                count++;
            }
        }
        if (target != null) {
            inRows = true;
            target.requestFocus();
            updateSidebarStyles();
        }
    }

    private void updateHintText(boolean movableRow) {
        if (hintView == null) return;
        if (liftedSceneId != null) {
            hintView.setText(tr("Up and down to move, OK when done",
                    "Taşımak için Yukarı ve Aşağı, bitince OK"));
        } else if (!inRows) {
            hintView.setText(tr("Up and down to browse, OK to change, Back to close",
                    "Gezinmek için Yukarı ve Aşağı, değiştirmek için OK, kapatmak için Geri"));
        } else if (HOME.equals(currentSection) && showingHiddenApps) {
            hintView.setText(tr("OK to open  ·  Hold OK to restore  ·  Back to go back",
                    "Açmak için OK  ·  Geri yüklemek için OK'e basılı tutun  ·  Dönmek için Geri"));
        } else if (movableRow) {
            hintView.setText(tr("OK to change, hold OK to move, Back to go back",
                    "Değiştirmek için OK, taşımak için OK'e basılı tutun, dönmek için Geri"));
        } else {
            hintView.setText(tr("OK to change, Back to go back",
                    "Değiştirmek için OK, dönmek için Geri"));
        }
    }

    private void showStatusMsg(String text) {
        if (msgView == null) return;
        if (text == null || text.isEmpty()) {
            msgView.setVisibility(View.GONE);
        } else {
            msgView.setText(text);
            msgView.setVisibility(View.VISIBLE);
        }
    }

    private int getFocusedRowIndex() {
        int count = 0;
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            if (child.isFocusable()) {
                if (child.hasFocus() || child.isFocused()) return count;
                count++;
            }
        }
        return 0;
    }

    public void openSubMenu(String id) {
        if (id == null || id.isEmpty()) return;
        subMenuStack.add(activeSubMenu != null ? activeSubMenu : "");
        subMenuParentFocusIdx.add(getFocusedRowIndex());
        activeSubMenu = id;
        inRows = true;
        rebuildMainRows();
        focusFirstRow();
    }

    public boolean closeSubMenu() {
        if (showingHiddenApps) {
            showingHiddenApps = false;
            rebuildMainRows();
            focusFirstRow();
            return true;
        }
        if (activeSubMenu != null) {
            String prev = "";
            int parentIdx = 0;
            if (!subMenuStack.isEmpty()) {
                prev = subMenuStack.remove(subMenuStack.size() - 1);
            }
            if (!subMenuParentFocusIdx.isEmpty()) {
                parentIdx = subMenuParentFocusIdx.remove(subMenuParentFocusIdx.size() - 1);
            }
            activeSubMenu = (prev == null || prev.isEmpty()) ? null : prev;
            inRows = true;
            rebuildMainRows();
            focusRowAtIndex(parentIdx);
            return true;
        }
        return false;
    }

    public void openDefaultHomeSubMenu() {
        openSubMenu("default_home");
    }

    public void openSystemLaunchersSubMenu() {
        openSubMenu("cyt_launchers");
    }

    public void openButtonKeySubMenu(int keyCode) {
        openSubMenu("bm_key:" + keyCode);
    }

    private void rebuildMainRows() {
        boolean wasInRows = inRows;
        int prevFocusIdx = getFocusedRowIndex();
        rebuilding = true;
        if (sideList != null) {
            sideList.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        }
        list.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        list.removeAllViews();
        refreshers.clear();
        showStatusMsg(null);
        mainHeadView.setText(sectionTitle(currentSection));

        try {
            if (activeSubMenu != null) {
                renderSubMenu(activeSubMenu);
            } else {
                switch (currentSection) {
                    case HOME:
                    case QUICK:
                        pageMainScreen();
                        break;
                    case LOCATION:
                    case WEATHER:
                        pageWeather();
                        break;
                    case LOOK_SCENES:
                        pageLookAndScenes();
                        break;
                    case ADVANCED:
                        pageAdvanced();
                        break;
                    case UPDATES:
                        pageUpdates();
                        break;
                    case ABOUT:
                    default:
                        pageAbout();
                        break;
                }
            }
        } catch (Throwable t) {
            android.util.Log.w("tvOSLauncher", "Settings section build error (" + currentSection + "): " + t);
        }
        list.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        if (sideList != null) {
            sideList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        }
        rebuilding = false;
        if (wasInRows) {
            focusRowAtIndex(prevFocusIdx);
        } else {
            scroller.scrollTo(0, 0);
        }
        updateHintText(activeSubMenu == null && LOOK_SCENES.equals(currentSection));
    }

    // ================================================================ Section Pages



    void pageWeather() {
        section("ПОГОДА");
        row("Показывать погоду на главном экране",
                () -> Weather.enabled(this) ? "Включена" : "Выключена", () -> {
                    Weather.prefs(this).edit().putBoolean(Weather.K_ENABLED, !Weather.enabled(this)).apply();
                    Weather.changed(this);
                    refreshAll();
                });
        row("Кнопка погоды на главном экране",
                () -> SceneHome.isWeatherShortcutEnabled(this) ? "Включена" : "Выключена", () -> {
                    boolean next = !SceneHome.isWeatherShortcutEnabled(this);
                    SceneHome.setWeatherShortcutEnabled(this, next);
                    refreshAll();
                });
        row("Единицы температуры", () -> Weather.prefs(this).getString(Weather.K_UNITS, "c").equals("f")
                ? "°F (Фаренгейт)" : "°C (Цельсий)", () -> {
            String cur = Weather.prefs(this).getString(Weather.K_UNITS, "c");
            Weather.prefs(this).edit().putString(Weather.K_UNITS, "f".equals(cur) ? "c" : "f").apply();
            Weather.changed(this);
            refreshAll();
        });
        row("Обновлять каждые", () -> Weather.refreshMinutes(this) + " мин.", () -> {
            int cur = Weather.refreshMinutes(this);
            Weather.prefs(this).edit().putInt(Weather.K_REFRESH, cur == 15 ? 30 : cur == 30 ? 60 : 15).apply();
        });
        row("Источник погоды", () -> Weather.sourceName(Weather.source(this)), () -> openSubMenu("weather_source"));
        infoRow("Поставщик прогноза", "Open-Meteo", "Бесплатный сервис, аккаунт не нужен");
        section("МЕСТОПОЛОЖЕНИЕ");
        String city = Weather.place(this);
        boolean isManual = Weather.manual(this);
        infoRow("Текущий город", city.isEmpty() ? "Автоматически" : city,
                isManual ? "Указан вручную" : "Определён автоматически");
        row("Найти город…", () -> isManual ? city : "›", this::searchCity);
        if (isManual) row("Использовать автоматическое определение", () -> "", () -> {
            Weather.setPlace(this, null);
            Weather.refresh(this, true, this::rebuildMainRows);
            rebuildMainRows();
            focusRowAtIndex(4);
        });
    }

    /**
     * Exact Couch-TV "Look and scenes" layout (matching docs/screens/home-settings.jpg):
     * - DOCK: Dock resize option (-50% to +50%, default 100%)
     * - LOOK: Night scenes vs Classic
     * - SCENES: Night, Golden hour, Waterline, Fog, Rainforest dawn (with 16:9 thumbnails & green circular checkmarks)
     * - Each scene stays: 20 seconds
     * - Clock: 12 hour / 24 hour
     * - CLASSIC BACKGROUND: Couch-TV photos & ambient themes
     */
    void pageLookAndScenes() {
        boolean scenesLook = Scenes.isScenesLook(this);
        if (scenesLook) {
            section("ПАНЕЛЬ ПРИЛОЖЕНИЙ");
            row("Размер панели приложений", () -> Scenes.dockScaleLabel(this), () -> openSubMenu("dock_size"));
            row("Показывать названия приложений",
                    () -> Scenes.isDockNamesShown(this) ? "Включены" : "Выключены", () -> {
                Scenes.setDockNamesShown(this, !Scenes.isDockNamesShown(this));
                refreshAll();
            });
        }
        section("РЕЖИМ ОТОБРАЖЕНИЯ");
        choiceRow("Ночные сцены", "Природные сцены, стеклянные часы и нижняя панель", scenesLook, () -> {
            Scenes.setLook(this, Scenes.LOOK_SCENES);
            selectSection(LOOK_SCENES, true, 0);
        });
        choiceRow("Классический", "Обои и сетка приложений", !scenesLook, () -> {
            Scenes.setLook(this, Scenes.LOOK_CLASSIC);
            selectSection(LOOK_SCENES, true, 0);
        });
        section("СЦЕНЫ");
        List<String> order = Scenes.getSceneOrder(this);
        for (int i = 0; i < order.size(); i++) {
            String id = order.get(i);
            if (!Scenes.isCustomId(id)) sceneCheckRow(Scenes.byId(id), i, order);
        }
        for (int slot = 1; slot <= Scenes.MAX_CUSTOM_SCENES; slot++) {
            customSceneRow(slot);
            if (!Scenes.isSceneEnabled(this, Scenes.customId(slot))) break;
        }
        folderShuffleSceneRow();
        if (Scenes.isAnyCustomSceneEnabled(this)) row("Положение часов",
                () -> Scenes.customClockPosLabel(this), () -> {
                    Scenes.cycleCustomClockPos(this);
                    refreshAll();
                });
        row("Длительность сцены", () -> Scenes.getStaySeconds(this) + " сек.", () -> {
            Scenes.cycleStaySeconds(this);
            refreshAll();
        });
        row("Формат часов", () -> Scenes.isClock24h(this) ? "24 часа" : "12 часов", () -> {
            Scenes.setClock24h(this, !Scenes.isClock24h(this));
            refreshAll();
        });
        row("Шрифт часов", () -> Scenes.clockStyleLabel(this), () -> {
            Scenes.cycleClockStyle(this);
            refreshAll();
        });
        section("КЛАССИЧЕСКИЙ ФОН");
        String curWall = Profiles.homePrefs(this).getString("wallpaper", "photo:tessellated-pavement.jpg");
        for (int i = 0; i < Scenes.CLASSIC_PHOTOS.length; i++) {
            final String photoFile = Scenes.CLASSIC_PHOTOS[i][0];
            final String wallKey = "photo:" + photoFile;
            final String label = I18n.s(this, Scenes.CLASSIC_PHOTOS[i][1], Scenes.CLASSIC_PHOTOS[i][2]);
            classicPhotoRow(label, "backgrounds/" + photoFile, wallKey.equals(curWall), () -> {
                Profiles.homePrefs(this).edit().putString("wallpaper", wallKey).apply();
                rebuildMainRows();
            });
        }
        row("Цветовые темы…", () -> {
            String k = Profiles.homePrefs(this).getString("wallpaper", "photo:tessellated-pavement.jpg");
            int idx = Arrays.asList(MainActivity.WALL_KEYS).indexOf(k);
            return MainActivity.wallName(this, Math.max(0, idx));
        }, () -> openSubMenu("ambient_themes"));
        customClassicPhotoRow("custom".equals(curWall));
        folderShuffleClassicRow(MainActivity.FOLDER_SHUFFLE_WALL_KEY.equals(curWall));
    }

    void pageApps() {
        if (showingHiddenApps) {
            pageHiddenApps();
            return;
        }
        section("ПРИЛОЖЕНИЯ");
        row("Скрытые приложения", () -> {
            Set<String> hidden = Profiles.homePrefs(this).getStringSet(MainActivity.PREF_HIDDEN, Collections.emptySet());
            int count = hidden != null ? hidden.size() : 0;
            return (count > 0 ? count + "  " : "") + "›";
        }, () -> {
            showingHiddenApps = true;
            rebuildMainRows();
            focusFirstRow();
        });
        row("Сбросить порядок приложений", () -> "›", () ->
                dialog("Сбросить порядок приложений?")
                        .setMessage("Порядок приложений будет восстановлен, а скрытые приложения снова появятся на главном экране.")
                        .setPositiveButton("Сбросить", (d, w) -> {
                            Profiles.homePrefs(this).edit().remove(MainActivity.PREF_ORDER)
                                    .remove(MainActivity.PREF_OTHER).remove(MainActivity.PREF_HIDDEN).apply();
                            showStatusMsg("Порядок приложений сброшен.");
                            rebuildMainRows();
                        })
                        .setNegativeButton("Отмена", null).show());
        row("Управление приложениями Android TV", () -> "›",
                () -> Suggestions.start(this, new Intent(Settings.ACTION_APPLICATION_SETTINGS)));
        infoNote("Чтобы переместить или скрыть приложение, зажмите OK на его значке на главном экране.");
    }

    static final class HiddenAppItem {
        String key;
        String pkg;
        String label;
        android.graphics.drawable.Drawable icon;
        Intent launch;
    }

    private List<HiddenAppItem> listHiddenAppItems() {
        List<HiddenAppItem> out = new ArrayList<>();
        try {
            Set<String> hidden = Profiles.homePrefs(this).getStringSet(MainActivity.PREF_HIDDEN, Collections.emptySet());
            if (hidden == null || hidden.isEmpty()) return out;
            PackageManager pm = getPackageManager();
            Set<String> seenPkgs = new HashSet<>();
            collectHiddenResolved(pm, Intent.CATEGORY_LEANBACK_LAUNCHER, hidden, seenPkgs, true, out);
            collectHiddenResolved(pm, Intent.CATEGORY_LAUNCHER, hidden, seenPkgs, false, out);
            if (hidden.contains(MainActivity.INPUTS_KEY)) {
                HiddenAppItem it = new HiddenAppItem();
                it.key = MainActivity.INPUTS_KEY;
                it.pkg = getPackageName();
                it.label = MainActivity.getCustomAppName(this, it.key, null, tr("Inputs", "Girişler"));
                it.icon = new BitmapDrawable(getResources(), Glyphs.get("input", u(22), Color.WHITE));
                out.add(it);
            }
            Collections.sort(out, (a, b) -> a.label.compareToIgnoreCase(b.label));
        } catch (Throwable ignored) {
        }
        return out;
    }

    private void collectHiddenResolved(PackageManager pm, String category, Set<String> hidden,
                                       Set<String> seenPkgs, boolean leanback, List<HiddenAppItem> out) {
        Intent q = new Intent(Intent.ACTION_MAIN).addCategory(category);
        for (ResolveInfo ri : pm.queryIntentActivities(q, 0)) {
            if (ri == null || ri.activityInfo == null) continue;
            ActivityInfo ai = ri.activityInfo;
            if (ai.packageName.equals(getPackageName()) || seenPkgs.contains(ai.packageName)) continue;
            seenPkgs.add(ai.packageName);
            android.content.ComponentName cn = new android.content.ComponentName(ai.packageName, ai.name);
            String key = cn.flattenToShortString();
            if (!hidden.contains(key) && !hidden.contains(ai.packageName)) continue;
            HiddenAppItem it = new HiddenAppItem();
            it.key = key;
            it.pkg = ai.packageName;
            String defLabel = ri.loadLabel(pm).toString();
            it.label = MainActivity.getCustomAppName(this, key, ai.packageName, defLabel);
            it.icon = ri.loadIcon(pm);
            Intent launch = new Intent(Intent.ACTION_MAIN)
                    .setComponent(cn)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            if (leanback) launch.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
            it.launch = launch;
            out.add(it);
        }
    }

    private void pageHiddenApps() {
        mainHeadView.setText(tr("Hidden apps", "Gizlenen uygulamalar"));
        List<HiddenAppItem> hiddenList = listHiddenAppItems();
        if (hiddenList.isEmpty()) {
            row("←  " + tr("Back to Apps", "Uygulamalara dön"), () -> "", () -> {
                showingHiddenApps = false;
                rebuildMainRows();
                focusFirstRow();
            });
            infoNote(tr("No hidden apps. Hold OK on any app on the home screen dock and choose Hide.",
                    "Gizlenen uygulama yok. Ana ekran dock'undaki herhangi bir uygulamada OK tuşuna basılı tutup Gizle'yi seçin."));
            return;
        }
        for (int i = 0; i < hiddenList.size(); i++) {
            final HiddenAppItem item = hiddenList.get(i);
            hiddenAppRow(item);
        }
        infoNote(tr("OK to open  ·  Hold OK to restore",
                "Açmak için OK  ·  Geri yüklemek için OK'e basılı tutun"));
    }

    private void hiddenAppRow(HiddenAppItem item) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setLongClickable(true);
        r.setPadding(u(14), u(6), u(16), u(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        int icSz = u(28);
        ImageView iconIv = new ImageView(this);
        iconIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (item.icon != null) iconIv.setImageDrawable(item.icon);
        final float icRad = u(6.5f);
        iconIv.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), icRad);
            }
        });
        iconIv.setClipToOutline(true);
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(icSz, icSz);
        icLp.rightMargin = u(12);
        r.addView(iconIv, icLp);

        TextView l = new TextView(this);
        l.setText(item.label);
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        l.setEllipsize(TextUtils.TruncateAt.END);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView v = new TextView(this);
        v.setText(tr("Open", "Aç"));
        v.setTextColor(0x9EFFFFFF);
        v.setTypeface(Ui.font(this, "medium"));
        Ui.textSize(v, 13.5f);
        v.setSingleLine(true);
        r.addView(v);

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            v.setTextColor(has ? 0x99000000 : 0x9EFFFFFF);
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });

        r.setOnClickListener(view -> {
            if (MainActivity.INPUTS_KEY.equals(item.key)) {
                ControlPanelOverlay.openInputs(this, null);
                return;
            }
            if (item.launch != null && Suggestions.start(this, item.launch)) {
                overridePendingTransition(R.anim.app_open_enter, R.anim.app_open_exit);
                return;
            }
            if (item.pkg != null) {
                Intent launch = getPackageManager().getLeanbackLaunchIntentForPackage(item.pkg);
                if (launch == null) launch = getPackageManager().getLaunchIntentForPackage(item.pkg);
                if (launch != null && Suggestions.start(this, launch)) {
                    overridePendingTransition(R.anim.app_open_enter, R.anim.app_open_exit);
                }
            }
        });

        r.setOnLongClickListener(view -> {
            Set<String> hidden = new HashSet<>(Profiles.homePrefs(this).getStringSet(MainActivity.PREF_HIDDEN, Collections.emptySet()));
            Set<String> other = new HashSet<>(Profiles.homePrefs(this).getStringSet(MainActivity.PREF_OTHER, Collections.emptySet()));
            if (item.key != null) {
                hidden.remove(item.key);
                other.remove(item.key);
            }
            if (item.pkg != null) {
                hidden.remove(item.pkg);
                other.remove(item.pkg);
            }
            Profiles.homePrefs(this).edit()
                    .putStringSet(MainActivity.PREF_HIDDEN, hidden)
                    .putStringSet(MainActivity.PREF_OTHER, other)
                    .apply();
            rebuildMainRows();
            showStatusMsg(tr(item.label + " restored to Home Screen.", item.label + " Ana Ekran'a geri yüklendi."));
            return true;
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(42));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }





    private void promptAddPerson() {
        android.widget.EditText in = new android.widget.EditText(this);
        in.setSingleLine(true);
        in.setHint(tr("Name", "Ad"));
        int pad = u(14);
        FrameLayout box = new FrameLayout(this);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(in);
        AlertDialog dlg = dialog(tr("Add a person", "Kişi ekle"))
                .setView(box)
                .setPositiveButton(tr("Add", "Ekle"), (d, w) -> {
                    String n = in.getText().toString().trim();
                    if (!n.isEmpty()) {
                        Profiles.add(this, n);
                        selectSection(HOME, true, 6 + Profiles.list(this).size() - 1);
                    }
                })
                .setNegativeButton(tr("Cancel", "İptal"), null)
                .show();
        Ui.setupDialogEditText(in, dlg);
        in.requestFocus();
    }

    void pageButtonMapper() {
        section(tr("ACCESSIBILITY SERVICE", "СЛУЖБА СПЕЦИАЛЬНЫХ ВОЗМОЖНОСТЕЙ"));
        row(tr("Button Mapper accessibility service", "Служба переназначения кнопок"),
                () -> ButtonMapper.isServiceEnabled(this)
                        ? tr("Active (works in all apps)", "Активна (работает во всех приложениях)")
                        : tr("Enable in Accessibility…", "Включить в специальных возможностях…"),
                () -> {
                    Intent acc = Perms.accessibilityScreen(this);
                    if (acc != null && Suggestions.start(this, acc)) {
                        return;
                    }
                    dialog(tr("Accessibility Service", "Служба специальных возможностей"))
                            .setMessage(tr(
                                    "This TV does not have a built-in Accessibility Settings screen. You can enable the Button Mapper & Control Panel service automatically via Local ADB.",
                                    "На этом телевизоре нет встроенного экрана специальных возможностей. Службу переназначения кнопок и панели управления можно включить через локальный ADB."))
                            .setPositiveButton(tr("Enable via ADB", "Включить через ADB"), (d, w) -> pairOrRepairAdb())
                            .setNeutralButton(tr("TV Settings", "Настройки телевизора"), (d, w) -> Suggestions.start(this, new Intent(Settings.ACTION_SETTINGS)))
                            .setNegativeButton(tr("Cancel", "Отмена"), null)
                            .show();
                });
        row(tr("Fix Home button (Launch testOS)", "Короткое нажатие кнопки Домой открывает testOS"),
                () -> ButtonMapper.isHomeFixEnabled(this) ? tr("On", "Включено") : tr("Off", "Выключено"),
                () -> {
                    boolean next = !ButtonMapper.isHomeFixEnabled(this);
                    ButtonMapper.setHomeFixEnabled(this, next);
                    refreshAll();
                    showStatusMsg(next
                            ? tr("Short press Home opens testOS; long press opens TV Settings.",
                                 "Короткое нажатие Домой открывает testOS, долгое — настройки телевизора.")
                            : tr("Home button is handled by the TV system.",
                                 "Кнопка Домой обрабатывается самой системой телевизора."));
                });
        infoNote(tr("When the Home fix is enabled, a short press opens testOS and a long press opens the TV's own Settings. With the fix disabled, Home is handled by the TV system.",
                "Если исправление кнопки Домой включено, короткое нажатие открывает testOS, а долгое — настройки телевизора. Если выключено, кнопкой управляет сама система ТВ."));

        row(tr("Long-press volume button fix", "Исправление долгого нажатия кнопки громкости"),
                () -> ButtonMapper.isVolumeHoldFixEnabled(this) ? tr("On", "Включено") : tr("Off", "Выключено"),
                () -> {
                    boolean next = !ButtonMapper.isVolumeHoldFixEnabled(this);
                    ButtonMapper.setVolumeHoldFixEnabled(this, next);
                    refreshAll();
                    showStatusMsg(next
                            ? tr("Long-press volume fix enabled (for TVs where holding volume stops repeating).",
                                 "Исправление долгого нажатия громкости включено.")
                            : tr("Long-press volume fix disabled (using native TV volume).",
                                 "Исправление выключено, используется штатная регулировка громкости."));
                });
        row(tr("Volume button fix mode", "Режим исправления кнопок громкости"),
                () -> ButtonMapper.getVolumeHoldFixModeLabel(this),
                this::showVolumeFixModeSelectorDialog);

        section(tr("BUTTON REMAPPING", "ПЕРЕНАЗНАЧЕНИЕ КНОПОК"));
        row(tr("Add new button to remap", "Добавить кнопку для переназначения"),
                () -> "＋",
                () -> ButtonMapper.showCaptureAndConfigureDialog(this, this::rebuildMainRows));

        List<Integer> mappedKeys = ButtonMapper.getMappedKeys(this);
        if (!mappedKeys.isEmpty()) {
            section(tr("REMAPPED BUTTONS (1X / 2X / 3X TAP)", "НАЗНАЧЕННЫЕ КНОПКИ (1 / 2 / 3 НАЖАТИЯ)"));
            for (final int keyCode : mappedKeys) {
                twoLineRow(ButtonMapper.keyLabel(this, keyCode),
                        () -> ButtonMapper.keySummaryLabel(this, keyCode),
                        () -> openButtonKeySubMenu(keyCode));
            }
            row(tr("Clear all remapped buttons", "Сбросить все переназначения"),
                    () -> "",
                    () -> {
                        ButtonMapper.clearAll(this);
                        rebuildMainRows();
                        showStatusMsg(tr("All button mappings cleared.", "Все переназначения кнопок сброшены."));
                    });
        }

        infoNote(tr("Assign Single Tap (1x), Double Tap (2x) and Triple Tap (3x) actions for each remote button — such as opening the Control Panel overlay, the Recent Apps Switcher overlay, or any installed app.",
                "Для каждой кнопки пульта можно назначить действия на одно, два или три нажатия: открыть панель управления, список недавних приложений или любое установленное приложение."));
    }

    void pageMainScreen() {
        section("ОБЩИЕ");
        row("Звуки навигации",
                () -> Clicks.enabled(this) ? "Включены" : "Выключены",
                () -> {
                    Clicks.setEnabled(this, !Clicks.enabled(this));
                    refreshAll();
                });
        row("Повторить первоначальную настройку", () -> "›", () -> {
            getSharedPreferences("setup", MODE_PRIVATE).edit().putInt("cur_step", 0).apply();
            startActivity(new Intent(this, SetupActivity.class).putExtra("step", 0));
        });
        row("Системные настройки Android TV", () -> "›",
                () -> Suggestions.start(this, new Intent(Settings.ACTION_SETTINGS)));
        section("СБРОС");
        row("Сбросить настройки testOS", () -> "›",
                () -> dialog("Сбросить настройки?")
                        .setMessage("Настройки внешнего вида, часов, погоды и приложений будут возвращены к значениям по умолчанию. После сброса откроется первоначальная настройка.")
                        .setPositiveButton("Сбросить", (d, w) -> resetAllAppDataAndReturnToSetup())
                        .setNegativeButton("Отмена", null).show());
        pageApps();
    }



    void pageAdvanced() {
        section("ГЛАВНЫЙ ЭКРАН И ПУЛЬТ");
        row("Выбор лаунчера по умолчанию",
                () -> Perms.isDefaultHome(this) ? "testOS активен" : "Выбрать лаунчер…",
                () -> Perms.requestDefaultHome(this));
        pageButtonMapper();

        section("ADB");
        twoLineRow("Режим работы ADB",
                () -> LocalAdb.getAdbModeLabel(this),
                this::showAdbModeSelectorDialog);
        row("ADB", () -> (LocalAdb.isAdbSettingEnabled(this) || LocalAdb.portReachable)
                        ? "Включён" : "Выключен",
                this::toggleAdbPort);
        row("Состояние подключения",
                () -> LocalAdb.getStatusLabel(this),
                this::pairOrRepairAdb);

        section("РАЗРЕШЕНИЯ");
        row("Просмотреть выданные разрешения",
                this::permissionsSummaryCount,
                () -> openSubMenu("check_permissions"));
        row("Отозвать выданные разрешения",
                () -> "Отозвать ›",
                this::confirmRevokeAllPermissions);

        section("РАБОТА СЛУЖБЫ ПУЛЬТА");
        row("Автовосстановление службы пульта",
                () -> LocalAdb.isKeepAliveEnabled(this) ? "Включено" : "Выключено",
                this::toggleKeepAliveWithWarning);
        infoNote("Поддерживает работу переназначения кнопок, если Android отключает службу специальных возможностей. В безопасном режиме ADB не остаётся постоянно открытым.");

        section("ОЧИСТКА ТВ");
        twoLineRow("Модель телевизора",
                () -> CleanYourTv.getSelectedModelSummary(this),
                () -> {
                    cytAutoContinueToDebloat = false;
                    openSubMenu("cyt_models");
                });
        row("Состояние очистки",
                () -> advDebloatSummary != null ? advDebloatSummary : "Загрузка…",
                () -> openSubMenu("cyt_saved"));
        row("Очистить телевизор",
                () -> "Начать ›",
                () -> {
                    if (CleanYourTv.getSelectedModelSlug(this).isEmpty()) {
                        cytAutoContinueToDebloat = true;
                        openSubMenu("cyt_models");
                    } else {
                        cytChecklistItems = null;
                        openSubMenu("cyt_debloat");
                    }
                });
        row("Системные лаунчеры",
                () -> advLaunchersSummary != null ? advLaunchersSummary : "Загрузка…",
                () -> openSubMenu("cyt_launchers"));
        row("Ручная очистка системных приложений",
                () -> "Дополнительно ›",
                this::promptManualDebloatWarning);
        row("Восстановить отключённые приложения",
                () -> "Восстановить ›",
                () -> openSubMenu("cyt_restore"));
        infoNote("Можно выбрать модель телевизора или найти её по системным приложениям. В состоянии очистки отображается список отключённых пакетов. Очистка отключает выбранные приложения, а не удаляет их.");

        section("БЛОКИРОВКА РЕКЛАМЫ");
        row("AdGuard DNS",
                () -> CleanYourTv.isAdguardDnsActive(this) ? "Включена" : "Выключена",
                () -> CleanYourTv.toggleAdguardDns(this, this::refreshAll));

        loadAdvancedSummariesAsync();
        if (LocalAdb.getAdbMode(this) == LocalAdb.ADB_MODE_PERSISTENT) {
            LocalAdb.probePortAsync(this, () -> runOnUiThread(() -> {
                if (!isFinishing()) refreshAll();
            }));
        } else {
            LocalAdb.portReachable = false;
        }
    }

    private volatile String advLaunchersSummary, advDebloatSummary;
    private boolean advSummariesLoading;

    private void loadAdvancedSummariesAsync() {
        if (advSummariesLoading) return;
        advSummariesLoading = true;
        final android.content.Context app = this;
        new Thread(() -> {
            String launchers, debloat;
            try {
                launchers = CleanYourTv.getSystemLaunchersSummary(app);
                debloat = CleanYourTv.getDebloatStatusSummary(app);
            } catch (Throwable t) {
                launchers = debloat = "";
            }
            final String l = launchers, d = debloat;
            runOnUiThread(() -> {
                advSummariesLoading = false;
                boolean changed = !l.equals(advLaunchersSummary) || !d.equals(advDebloatSummary);
                advLaunchersSummary = l;
                advDebloatSummary = d;
                if (changed && !isFinishing()) refreshAll();
            });
        }, "adv-summaries").start();
    }

    private boolean checkingLatestRelease = false;

    void pageUpdates() {
        section(tr("UPDATES", "GÜNCELLEMELER"));
        row(tr("Automatic updates", "Otomatik güncellemeler"),
                () -> AppUpdater.isAutoUpdateEnabled(this) ? tr("On", "Açık") : tr("Off", "Kapalı"),
                () -> {
                    boolean next = !AppUpdater.isAutoUpdateEnabled(this);
                    AppUpdater.setAutoUpdateEnabled(this, next);
                    refreshAll();
                    showStatusMsg(next
                            ? tr("Automatic updates enabled.", "Otomatik güncellemeler etkinleştirildi.")
                            : tr("Automatic updates disabled.", "Otomatik güncellemeler devre dışı bırakıldı."));
                    if (next) {
                        AppUpdater.checkAutoUpdateIfNeeded(this);
                    }
                });
        row(tr("Check for updates", "Güncellemeleri kontrol et"),
                () -> tr("Check now ›", "Şimdi kontrol et ›"),
                () -> AppUpdater.manualCheckAndPrompt(this, this::refreshAll));

        addUpdateDownloadProgress();
        section(tr("VERSION INFO", "ИНФОРМАЦИЯ О ВЕРСИИ"));
        infoRow(tr("Current version", "Mevcut sürüm"), "v" + AppUpdater.getCurrentVersion(this), null);
        infoRowDynamic(tr("GitHub release latest version", "GitHub son sürüm"),
                () -> {
                    String latest = AppUpdater.getCachedLatestVersion(this);
                    if (latest != null && !latest.isEmpty()) {
                        return "v" + latest;
                    }
                    return checkingLatestRelease
                            ? tr("Checking…", "Kontrol ediliyor…")
                            : tr("Tap Check for updates", "Güncellemeleri kontrol et'e basın");
                },
                null);

        infoNote("Проверяет GitHub Releases репозитория gonscharovd/tvOS и устанавливает обновления прямо на телевизор.");

        if (!checkingLatestRelease) {
            checkingLatestRelease = true;
            refreshAll();
            AppUpdater.checkForUpdates(this, (success, latestVersion, downloadUrl, updateAvailable, errorMsg) -> {
                checkingLatestRelease = false;
                if (!isFinishing()) {
                    refreshAll();
                    if (success && updateAvailable) {
                        AppUpdater.showUpdateAvailableDialog(this, latestVersion, downloadUrl,
                                AppUpdater.getCachedChangelog(this), this::refreshAll);
                    }
                }
            });
        }
    }

    private void addUpdateDownloadProgress() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(u(14), u(8), u(14), u(8));

        updateDownloadStatus = new TextView(this);
        updateDownloadStatus.setTextColor(0xBFFFFFFF);
        updateDownloadStatus.setTypeface(Ui.font(this, "medium"));
        Ui.textSize(updateDownloadStatus, 12.5f);
        box.addView(updateDownloadStatus);

        updateDownloadProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        updateDownloadProgress.setMax(100);
        LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(7));
        progressLp.topMargin = u(8);
        box.addView(updateDownloadProgress, progressLp);

        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boxLp.bottomMargin = u(5);
        list.addView(box, boxLp);

        refreshUpdateDownloadProgress();
        updateProgressHandler.removeCallbacks(updateProgressTick);
        if (AppUpdater.isDownloading()) {
            updateProgressHandler.post(updateProgressTick);
        }
    }

    private void refreshUpdateDownloadProgress() {
        if (updateDownloadProgress == null || updateDownloadStatus == null) return;
        boolean active = AppUpdater.isDownloading();
        long downloaded = AppUpdater.getDownloadedBytes();
        long total = AppUpdater.getDownloadTotalBytes();
        int percent = AppUpdater.getDownloadProgressPercent();

        if (!active && downloaded <= 0L && !"Download failed".equals(AppUpdater.getDownloadStatus())) {
            updateDownloadStatus.setVisibility(View.GONE);
            updateDownloadProgress.setVisibility(View.GONE);
            return;
        }
        updateDownloadStatus.setVisibility(View.VISIBLE);
        updateDownloadProgress.setVisibility(View.VISIBLE);
        if (percent >= 0) {
            updateDownloadProgress.setIndeterminate(false);
            updateDownloadProgress.setProgress(percent);
            updateDownloadStatus.setText(tr("Downloading update", "Загрузка обновления")
                    + "  " + percent + "%  ·  "
                    + android.text.format.Formatter.formatShortFileSize(this, downloaded)
                    + " / " + android.text.format.Formatter.formatShortFileSize(this, total));
        } else if (active) {
            updateDownloadProgress.setIndeterminate(true);
            updateDownloadStatus.setText(tr("Downloading update", "Загрузка обновления")
                    + "  ·  " + android.text.format.Formatter.formatShortFileSize(this, downloaded));
        } else if ("Download failed".equals(AppUpdater.getDownloadStatus())) {
            updateDownloadProgress.setIndeterminate(false);
            updateDownloadProgress.setProgress(0);
            updateDownloadStatus.setText(tr("Update download failed", "Не удалось скачать обновление"));
        } else {
            updateDownloadProgress.setIndeterminate(false);
            updateDownloadProgress.setProgress(100);
            updateDownloadStatus.setText(tr("Download complete — opening installer", "Загрузка завершена — открываем установщик"));
        }
    }

    private void showVolumeFixModeSelectorDialog() {
        int curMode = ButtonMapper.getVolumeHoldFixMode(this);
        String[] opts = new String[]{
            (curMode == ButtonMapper.VOLUME_FIX_MODE_1 ? "✓ " : "   ")
                    + tr("Mode 1 — Standard (Default, for most Android TVs & IR remotes)",
                         "Mod 1 — Standart (Varsayılan, çoğu Android TV ve IR kumanda için)"),
            (curMode == ButtonMapper.VOLUME_FIX_MODE_2 ? "✓ " : "   ")
                    + tr("Mode 2 — HY320 / Projector (AudioService & IR pulse-burst mode)",
                         "Mod 2 — HY320 / Projeksiyon (AudioService ve IR darbe modu)")
        };
        dialog(tr("Volume Button Fix Mode", "Ses Tuşu Düzeltme Modu"))
                .setItems(opts, (d, which) -> {
                    int nextMode = (which == 1) ? ButtonMapper.VOLUME_FIX_MODE_2 : ButtonMapper.VOLUME_FIX_MODE_1;
                    ButtonMapper.setVolumeHoldFixMode(this, nextMode);
                    refreshAll();
                    if (nextMode == ButtonMapper.VOLUME_FIX_MODE_1) {
                        showStatusMsg(tr("Volume Fix Mode 1 (Standard) selected.",
                                "Ses Tuşu Düzeltme Modu 1 (Standart) seçildi."));
                    } else {
                        showStatusMsg(tr("Volume Fix Mode 2 (HY320 / Projector) selected.",
                                "Ses Tuşu Düzeltme Modu 2 (HY320 / Projeksiyon) seçildi."));
                    }
                })
                .setNegativeButton(tr("Cancel", "İptal"), null)
                .show();
    }

    void pageAbout() {
        section("ПРИЛОЖЕНИЕ");
        infoRow("Название", "testOS", null);
        infoRow("Разработчик", "gonscharovd", null);
        infoRow("Хранение настроек", "Память телевизора", null);
        pageUpdates();
        section("РЕЗЕРВНАЯ КОПИЯ");
        row("Сохранить настройки в память ТВ",
                () -> "Download/" + CustomMedia.SETTINGS_BACKUP_FILENAME,
                () -> CustomMedia.backupSettingsToDownload(this, this::refreshAll));
        row("Восстановить настройки", () -> "Выбрать файл ›",
                () -> CustomMedia.showRestoreSettingsPicker(this, this::refreshAll));
    }



    private void resetAllAppDataAndReturnToSetup() {
        try {
            java.io.File spDir = new java.io.File(getApplicationInfo().dataDir, "shared_prefs");
            if (spDir.exists() && spDir.isDirectory()) {
                java.io.File[] files = spDir.listFiles();
                if (files != null) {
                    for (java.io.File f : files) {
                        String name = f.getName();
                        if (name.endsWith(".xml")) {
                            String prefName = name.substring(0, name.length() - 4);
                            getSharedPreferences(prefName, MODE_PRIVATE).edit().clear().commit();
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        String[] knownPrefs = {
            "settings", "setup", "profiles", "home", "home_default",
            "app_custom_names", "button_mapper", "control_panel",
            "weather", "inputs", "clicks",
            "cleanyourtv_prefs", "local_adb", "present",
            "custom_media", "app_updater"
        };
        for (String p : knownPrefs) {
            try {
                getSharedPreferences(p, MODE_PRIVATE).edit().clear().commit();
            } catch (Throwable ignored) {
            }
        }
        I18n.resetCache();
        Intent setup = new Intent(this, SetupActivity.class)
                .putExtra("step", 0)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setup);
        finish();
    }

    private String permissionsSummaryCount() {
        int granted = 0;
        int total = 3;
        if (Perms.isDefaultHome(this)) granted++;
        if (ButtonMapper.isServiceEnabled(this)) granted++;
        if (Perms.hasLocation(this)) granted++;
        return granted + " / " + total + " " + tr("active", "etkin");
    }

    private void showAdbModeSelectorDialog() {
        int curMode = LocalAdb.getAdbMode(this);
        String[] opts = {
            (curMode == LocalAdb.ADB_MODE_SECURE_CLOSE ? "✓ " : "   ")
                    + tr("Secure Mode — Grant WRITE_SECURE_SETTINGS & Close ADB (Recommended)",
                         "Güvenli Mod — WRITE_SECURE_SETTINGS Ver ve ADB'yi Kapat (Önerilen)"),
            (curMode == LocalAdb.ADB_MODE_PERSISTENT ? "✓ " : "   ")
                    + tr("Persistent ADB Mode — Keep Network ADB Open (Optional)",
                         "Kalıcı ADB Modu — Ağ ADB'sini Açık Tut (İsteğe Bağlı)")
        };
        dialog(tr("ADB Operating Mode", "ADB Çalışma Modu"))
                .setItems(opts, (d, which) -> {
                    int nextMode = (which == 1) ? LocalAdb.ADB_MODE_PERSISTENT : LocalAdb.ADB_MODE_SECURE_CLOSE;
                    if (nextMode == LocalAdb.ADB_MODE_PERSISTENT
                            && !LocalAdb.isAdbSettingEnabled(this) && !LocalAdb.portReachable) {
                        dialog(tr("Enable ADB for Persistent Mode?", "Kalıcı Mod İçin ADB Açılsın mı?"))
                                .setMessage(tr(
                                        "ADB debugging is currently turned off. Persistent ADB Mode requires ADB to be enabled.\n\nWould you like to enable ADB and switch to Persistent ADB Mode?",
                                        "ADB hata ayıklama şu anda kapalı. Kalıcı ADB Modu için ADB'nin açık olması gerekir.\n\nADB'yi açıp Kalıcı ADB Moduna geçmek ister misiniz?"))
                                .setPositiveButton(tr("Enable ADB & Switch", "ADB'yi Aç ve Geç"), (d2, w2) -> {
                                    AlertDialog progress = dialog(tr("Turning On ADB…", "ADB Açılıyor…"))
                                            .setMessage(tr("Enabling ADB debugging and switching to Persistent ADB Mode…",
                                                    "ADB hata ayıklama açılıyor ve Kalıcı ADB Moduna geçiliyor…"))
                                            .setCancelable(false)
                                            .create();
                                    progress.show();
                                    LocalAdb.enableAdbPort(this, (success, msg) -> runOnUiThread(() -> {
                                        if (isFinishing()) return;
                                        try {
                                            progress.dismiss();
                                        } catch (Throwable ignored) {
                                        }
                                        LocalAdb.setAdbMode(this, LocalAdb.ADB_MODE_PERSISTENT);
                                        refreshAll();
                                        if (list != null) {
                                            list.postDelayed(this::refreshAll, 500);
                                        }
                                        if (success) {
                                            showStatusMsg(tr("ADB enabled and switched to Persistent ADB Mode.",
                                                    "ADB açıldı ve Kalıcı ADB Moduna geçildi."));
                                        } else {
                                            dialog(tr("Turn On ADB Debugging", "ADB Hata Ayıklamayı Açın"))
                                                    .setMessage(tr(
                                                            "Open Developer Options and turn on USB Debugging / Network Debugging to complete switching to Persistent ADB Mode.",
                                                            "Kalıcı ADB Moduna geçişi tamamlamak için Geliştirici Seçenekleri'nden USB / Ağ Hata Ayıklamayı açın."))
                                                    .setPositiveButton(tr("Open Developer Options…", "Geliştirici Seçeneklerini Aç…"), (d3, w3) -> {
                                                        Intent dev = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                                                        if (!Suggestions.start(this, dev)) {
                                                            Intent about = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
                                                            if (!Suggestions.start(this, about)) {
                                                                Suggestions.start(this, new Intent(Settings.ACTION_SETTINGS));
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton(tr("Cancel", "İptal"), null)
                                                    .show();
                                        }
                                    }));
                                })
                                .setNegativeButton(tr("Cancel", "İptal"), null)
                                .show();
                        return;
                    }
                    LocalAdb.setAdbMode(this, nextMode);
                    refreshAll();
                    if (nextMode == LocalAdb.ADB_MODE_SECURE_CLOSE) {
                        showStatusMsg(tr("Secure Mode selected — ADB will grant WRITE_SECURE_SETTINGS and close port 5555 when paired.",
                                "Güvenli Mod seçildi — Eşleştirildiğinde WRITE_SECURE_SETTINGS verilir ve port 5555 kapatılır."));
                    } else {
                        showStatusMsg(tr("Persistent ADB Mode selected — Port 5555 stays open for CleanYourTV & ADB tools.",
                                "Kalıcı ADB Modu seçildi — Port 5555 CleanYourTV ve ADB araçları için açık kalır."));
                    }
                })
                .setNegativeButton(tr("Cancel", "İptal"), null)
                .show();
    }

    private void toggleAdbPort() {
        boolean adbOn = LocalAdb.isAdbSettingEnabled(this) || LocalAdb.portReachable;
        if (adbOn) {
            confirmCloseAdbPort();
        } else {
            AlertDialog progress = dialog(tr("Turning On ADB…", "ADB Açılıyor…"))
                    .setMessage(tr("Enabling ADB debugging…", "ADB hata ayıklama etkinleştiriliyor…"))
                    .setCancelable(false)
                    .create();
            progress.show();
            LocalAdb.enableAdbPort(this, (success, msg) -> runOnUiThread(() -> {
                if (isFinishing()) return;
                try {
                    progress.dismiss();
                } catch (Throwable ignored) {
                }
                refreshAll();
                if (list != null) {
                    list.postDelayed(this::refreshAll, 500);
                }
                if (success) {
                    showStatusMsg(tr("ADB debugging enabled.", "ADB hata ayıklama açıldı."));
                } else {
                    dialog(tr("Turn On ADB Debugging", "ADB Hata Ayıklamayı Açın"))
                            .setMessage(tr(
                                    "Open Developer Options and turn on USB Debugging / Network Debugging.",
                                    "Geliştirici Seçenekleri'ni açıp USB / Ağ Hata Ayıklamayı etkinleştirin."))
                            .setPositiveButton(tr("Open Developer Options…", "Geliştirici Seçeneklerini Aç…"), (d, w) -> {
                                Intent dev = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                                if (!Suggestions.start(this, dev)) {
                                    Intent about = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
                                    if (!Suggestions.start(this, about)) {
                                        Suggestions.start(this, new Intent(Settings.ACTION_SETTINGS));
                                    }
                                }
                            })
                            .setNegativeButton(tr("Cancel", "İptal"), null)
                            .show();
                }
            }));
        }
    }

    private void confirmCloseAdbPort() {
        dialog(tr("Close ADB (Disable Port 5555)", "ADB'yi Kapat (Port 5555'i Kapat)"))
                .setMessage(tr(
                        "This runs 'settings put global adb_enabled 0' over Local ADB and verifies that port 5555 is closed on your home network.\n\n"
                        + "If WRITE_SECURE_SETTINGS is already granted, testOS will continue repairing Accessibility in-process without needing ADB!\n\n"
                        + "Tip: For maximum security, you can also open Developer Options and click 'Revoke USB debugging authorizations'.",
                        "Bu işlem Yerel ADB üzerinden 'settings put global adb_enabled 0' komutunu çalıştırır ve ev ağınızda port 5555'in kapandığını doğrular.\n\n"
                        + "WRITE_SECURE_SETTINGS izni zaten verilmişse testOS, ADB'ye ihtiyaç duymadan Erişilebilirliği doğrudan uygulama içinden onarmaya devam eder!\n\n"
                        + "İpucu: Maksimum güvenlik için Geliştirici Seçenekleri'nden 'USB hata ayıklama yetkilerini geri al' seçeneğine de tıklayabilirsiniz."))
                .setPositiveButton(tr("Close ADB Now", "ADB'yi Şimdi Kapat"), (d, w) -> {
                    if (LocalAdb.getAdbMode(this) != LocalAdb.ADB_MODE_SECURE_CLOSE) {
                        LocalAdb.setAdbMode(this, LocalAdb.ADB_MODE_SECURE_CLOSE);
                    }
                    AlertDialog progress = dialog(tr("Closing ADB…", "ADB Kapatılıyor…"))
                            .setMessage(tr("Running settings put global adb_enabled 0 and verifying port 5555…",
                                    "settings put global adb_enabled 0 çalıştırılıyor ve port 5555 kontrol ediliyor…"))
                            .setCancelable(false)
                            .create();
                    progress.show();
                    LocalAdb.closeAdbPort(this, (success, msg) -> runOnUiThread(() -> {
                        if (isFinishing()) return;
                        try {
                            progress.dismiss();
                        } catch (Throwable ignored) {
                        }
                        refreshAll();
                        showStatusMsg(msg);
                    }));
                })
                .setNeutralButton(tr("Developer Options…", "Geliştirici Seçenekleri…"), (d, w) -> {
                    Intent dev = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                    if (!Suggestions.start(this, dev)) {
                        Suggestions.start(this, new Intent(Settings.ACTION_SETTINGS));
                    }
                })
                .setNegativeButton(tr("Cancel", "İptal"), null)
                .show();
    }

    private void toggleKeepAliveWithWarning() {
        boolean currentlyOn = LocalAdb.isKeepAliveEnabled(this);
        if (currentlyOn) {
            LocalAdb.setKeepAliveEnabled(this, false);
            refreshAll();
            showStatusMsg(tr("Keep Alive disabled.", "Keep Alive devre dışı bırakıldı."));
            return;
        }
        boolean hasSecure = LocalAdb.hasWriteSecureSettings(this);
        dialog(tr("Enable Keep Alive?", "Keep Alive Etkinleştirilsin mi?"))
                .setMessage(hasSecure
                        ? tr("Keep Alive monitors the Accessibility Service and automatically restores it if your TV turns it off.\n\n"
                             + "✓ Because WRITE_SECURE_SETTINGS is granted on this TV, Keep Alive repairs Accessibility directly in-process without needing Network ADB (port 5555) to stay open.",
                             "Keep Alive, Erişilebilirlik Servisini izler ve TV'niz kapatırsa otomatik olarak yeniden açar.\n\n"
                             + "✓ Bu TV'de WRITE_SECURE_SETTINGS izni verildiği için Keep Alive, Ağ ADB'sinin (port 5555) açık kalmasına gerek duymadan Erişilebilirliği doğrudan uygulama içinden onarır.")
                        : tr("⚠️ Security Warning:\n\n"
                             + "Keep Alive monitors and restores permissions if your TV turns them off in the background.\n\n"
                             + "Without WRITE_SECURE_SETTINGS, Keep Alive requires Network Debugging (ADB on port 5555) to stay enabled on your home network. For better security, run Pair / Repair ADB in Secure Mode first so WRITE_SECURE_SETTINGS is granted and port 5555 can be closed.\n\n"
                             + "Enable Keep Alive anyway?",
                             "⚠️ Güvenlik Uyarısı:\n\n"
                             + "Keep Alive, TV'niz arka planda izinleri kapatırsa otomatik olarak yeniden açar.\n\n"
                             + "WRITE_SECURE_SETTINGS izni olmadan Keep Alive'ın çalışması için Ağ Hata Ayıklamanın (port 5555) açık kalması gerekir. Daha yüksek güvenlik için önce Güvenli Mod'da ADB Eşleştir / Onar çalıştırarak WRITE_SECURE_SETTINGS iznini verin.\n\n"
                             + "Keep Alive yine de etkinleştirilsin mi?"))
                .setPositiveButton(tr("Enable Keep Alive", "Keep Alive'ı Etkinleştir"), (d, w) -> {
                    LocalAdb.setKeepAliveEnabled(this, true);
                    LocalAdb.checkAndRepairPermissionsIfNeeded(this, true);
                    refreshAll();
                    showStatusMsg(tr("Keep Alive enabled.", "Keep Alive etkinleştirildi."));
                })
                .setNegativeButton(tr("Cancel", "İptal"), null)
                .show();
    }

    private void pairOrRepairAdb() {
        AlertDialog progress = dialog(tr("Pair / Repair ADB", "ADB Eşleştir / Onar"))
                .setMessage(tr(
                        "Connecting to local ADB on port 5555 and granting permissions…\n\nIf an \u201cAllow USB debugging?\u201d prompt appears, check \u201cAlways allow\u201d and press Allow.",
                        "Port 5555 üzerinden yerel ADB'ye bağlanılıyor ve izinler veriliyor…\n\nEkranda \u201cUSB hata ayıklamaya izin verilsin mi?\u201d penceresi çıkarsa \u201cHer zaman izin ver\u201di işaretleyip İzin Ver'e basın."))
                .setCancelable(false)
                .create();
        progress.show();

        LocalAdb.grantAllPermissions(this, (success, msg) -> runOnUiThread(() -> {
            if (isFinishing()) return;
            try {
                progress.dismiss();
            } catch (Exception ignored) {
            }
            refreshAll();
            if (list != null) {
                list.postDelayed(this::refreshAll, 350);
                list.postDelayed(this::refreshAll, 900);
            }
            if (success) {
                showStatusMsg(tr("ADB paired and all permissions granted!", "ADB eşleşti ve tüm izinler verildi!"));
            } else {
                dialog(tr("ADB Debugging Needed", "ADB Hata Ayıklama Gerekli"))
                        .setMessage(tr(
                                "Could not connect to local ADB on port 5555.\n\n1. Open Settings \u2192 System \u2192 About and click Build 7 times.\n2. Open Developer Options and turn on USB Debugging / Network Debugging.\n3. Return here and tap Pair / Repair again.",
                                "Port 5555 üzerinden yerel ADB'ye bağlanılamadı.\n\n1. Ayarlar \u2192 Sistem \u2192 Hakkında menüsünden Yapı Numarası'na 7 kez basın.\n2. Geliştirici Seçenekleri'nden USB / Ağ Hata Ayıklamayı açın.\n3. Buraya dönüp tekrar Eşleştir / Onar'a basın."))
                        .setPositiveButton(tr("Open Developer Options…", "Geliştirici Seçeneklerini Aç…"), (d, w) -> {
                            Intent dev = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                            if (!Suggestions.start(this, dev)) {
                                Intent about = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
                                if (!Suggestions.start(this, about)) {
                                    Suggestions.start(this, new Intent(Settings.ACTION_SETTINGS));
                                }
                            }
                        })
                        .setNegativeButton(tr("Cancel", "İptal"), null)
                        .show();
            }
        }));
    }

    private void confirmRevokeAllPermissions() {
        dialog(tr("Remove / Revoke All Permissions", "Tüm İzinleri Kaldır / Geri Al"))
                .setMessage(tr(
                        "This will disable Keep Alive and remove Accessibility Service, Notification Access, Default Home Role, and launcher permissions (both locally and via ADB).",
                        "Bu işlem Keep Alive'ı kapatır ve Erişilebilirlik Servisi, Bildirim Erişimi, Varsayılan Ana Ekran ve tüm başlatıcı izinlerini (hem yerel olarak hem ADB üzerinden) kaldırır."))
                .setPositiveButton(tr("Revoke All Permissions", "Tüm İzinleri Kaldır"), (d, w) -> {
                    AlertDialog progress = dialog(tr("Revoking Permissions…", "İzinler Kaldırılıyor…"))
                            .setMessage(tr("Revoking all permissions…",
                                    "Tüm izinler kaldırılıyor…"))
                            .setCancelable(false)
                            .create();
                    progress.show();
                    LocalAdb.revokeAllPermissions(this, (success, msg) -> runOnUiThread(() -> {
                        if (isFinishing()) return;
                        try {
                            progress.dismiss();
                        } catch (Throwable ignored) {
                        }
                        refreshAll();
                        if (list != null) {
                            list.postDelayed(this::refreshAll, 350);
                            list.postDelayed(this::refreshAll, 900);
                        }
                        showStatusMsg(msg);
                    }));
                })
                .setNegativeButton(tr("Cancel", "İptal"), null)
                .show();
    }

    private void promptManualDebloatWarning() {
        dialog(tr("⚠️ Manual Debloat Warning", "⚠️ Manuel Debloat Uyarısı"))
                .setMessage(tr(
                        "WARNING: Manual Debloat lists ALL system apps installed on your TV.\n\n"
                        + "• Only disable packages if you know exactly what you are doing.\n"
                        + "• Disabling core Android system services, TV input/tuner services, or Settings packages can break HDMI inputs, Wi-Fi, remote control, or cause a bootloop.\n\n"
                        + "Proceed at your own risk?",
                        "UYARI: Manuel Debloat, TV'nizde yüklü olan TÜM sistem uygulamalarını listeler.\n\n"
                        + "• Yalnızca ne yaptığınızı tam olarak biliyorsanız paketleri devre dışı bırakın.\n"
                        + "• Temel Android sistem servislerini, TV giriş/tuner servislerini veya Ayarlar paketlerini devre dışı bırakmak HDMI girişlerini, Wi-Fi'yi, kumandayı bozabilir veya cihazın açılmamasına neden olabilir.\n\n"
                        + "Sorumluluk size ait olmak üzere devam edilsin mi?"))
                .setPositiveButton(tr("I Understand — Open Manual Debloat", "Anladım — Manuel Debloat'u Aç"), (d, w) -> {
                    cytManualItems = null;
                    cytManualFilterQuery = "";
                    openSubMenu("cyt_manual");
                })
                .setNegativeButton(tr("Cancel", "İptal"), null)
                .show();
    }

    // ================================================================ Native Right-Side Sub-Menus

    private void backSubMenuRow() {
        row("←  " + tr("Back", "Geri"), () -> "", this::closeSubMenu);
    }

    private void renderSubMenu(String subId) {
        if (subId == null) return;
        if ("dock_size".equals(subId)) {
            subMenuDockSize();
        } else if ("ambient_themes".equals(subId)) {
            subMenuAmbientThemes();
        } else if ("lock_screen_settings".equals(subId)) {
            subMenuLockScreenSettings();
        } else if ("weather_source".equals(subId)) {
            subMenuWeatherSource();
        } else if (subId.startsWith("bm_key:")) {
            try {
                int code = Integer.parseInt(subId.substring("bm_key:".length()));
                subMenuButtonKey(code);
            } catch (NumberFormatException ignored) {
                closeSubMenu();
            }
        } else if (subId.startsWith("bm_tap:")) {
            String rest = subId.substring("bm_tap:".length());
            int colon = rest.indexOf(':');
            if (colon > 0) {
                try {
                    int code = Integer.parseInt(rest.substring(0, colon));
                    int tap = Integer.parseInt(rest.substring(colon + 1));
                    subMenuButtonTapTarget(code, tap);
                } catch (NumberFormatException ignored) {
                    closeSubMenu();
                }
            }
        } else if ("default_home".equals(subId)) {
            subMenuDefaultHome();
        } else if ("check_permissions".equals(subId)) {
            subMenuCheckPermissions();
        } else if ("cyt_models".equals(subId)) {
            subMenuCytModels();
        } else if ("cyt_models_all".equals(subId)) {
            subMenuCytAllModels();
        } else if ("cyt_models_scan".equals(subId)) {
            subMenuCytScannedModels();
        } else if ("cyt_debloat".equals(subId)) {
            subMenuCytDebloat();
        } else if ("cyt_launchers".equals(subId)) {
            subMenuCytSystemLaunchers();
        } else if ("cyt_manual".equals(subId)) {
            subMenuCytManualDebloat();
        } else if ("cyt_saved".equals(subId)) {
            subMenuCytSavedDisabled();
        } else if ("cyt_restore".equals(subId)) {
            subMenuCytRestore();
        }
    }

    private void subMenuDockSize() {
        mainHeadView.setText(tr("Dock size", "Dock boyutu"));
        backSubMenuRow();
        section(tr("DOCK SCALE (-50% TO +50%)", "DOCK BOYUTU (-%50 İLE +%50 ARASI)"));
        int curPct = Scenes.getDockScalePercent(this);
        int[] steps = {50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150};
        for (int i = 0; i < steps.length; i++) {
            final int pct = steps[i];
            int diff = pct - 100;
            String label;
            if (diff == 0) {
                label = "100% (" + tr("Default", "Varsayılan") + ")";
            } else if (diff > 0) {
                label = pct + "% (+" + diff + "%)";
            } else {
                label = pct + "% (" + diff + "%)";
            }
            String sub = diff > 0
                    ? tr("Larger dock — extra apps automatically move to Other apps if needed",
                         "Daha büyük dock — ekrana sığmayan fazla uygulamalar otomatik olarak Diğer uygulamalar'a taşınır")
                    : (diff < 0 ? tr("Smaller, more compact dock", "Daha küçük, kompakt dock")
                                : tr("Standard Apple tvOS dock size", "Standart Apple tvOS dock boyutu"));
            choiceRow(label, sub, curPct == pct, () -> {
                Scenes.setDockScalePercent(this, pct);
                rebuildMainRows();
                showStatusMsg(tr("Dock size set to " + pct + "%.", "Dock boyutu %" + pct + " olarak ayarlandı."));
            });
        }
    }

    private void subMenuAmbientThemes() {
        mainHeadView.setText(tr("Ambient colour themes", "Ortam renk temaları"));
        backSubMenuRow();
        section(tr("AMBIENT THEMES", "ORTAM TEMALARI"));
        String cur = Profiles.homePrefs(this).getString("wallpaper", "photo:tessellated-pavement.jpg");
        for (int i = 0; i < MainActivity.WALL_KEYS.length; i++) {
            final String key = MainActivity.WALL_KEYS[i];
            final String name = MainActivity.wallName(this, i);
            choiceRow(name, null, key.equals(cur), () -> {
                Profiles.homePrefs(this).edit().putString("wallpaper", key).apply();
                rebuildMainRows();
            });
        }
    }

    private void subMenuWeatherSource() {
        mainHeadView.setText(tr("Weather source", "Hava durumu kaynağı"));
        backSubMenuRow();
        section(tr("FORECAST PROVIDER", "HAVA TAHMİNİ SAĞLAYICISI"));
        String cur = Weather.source(this);
        String[] keys = Weather.SOURCES;
        String[] names = {"Open-Meteo", "MET Norway (Yr)", "wttr.in  (3 days)"};
        for (int i = 0; i < keys.length; i++) {
            final String k = keys[i];
            final String n = names[i];
            choiceRow(n, null, k.equals(cur), () -> {
                Weather.prefs(this).edit().putString(Weather.K_SOURCE, k).apply();
                Weather.changed(this);
                rebuildMainRows();
            });
        }
    }











    private void subMenuButtonKey(int keyCode) {
        String keyName = ButtonMapper.keyLabel(this, keyCode);
        mainHeadView.setText(keyName);
        backSubMenuRow();
        section(keyName + " — " + tr("TAP ACTIONS", "BASIŞ EYLEMLERİ"));

        int[] taps = {ButtonMapper.TAP_SINGLE, ButtonMapper.TAP_DOUBLE, ButtonMapper.TAP_TRIPLE};
        for (int tap : taps) {
            final int t = tap;
            row(ButtonMapper.tapTypeLabel(this, t),
                    () -> ButtonMapper.targetLabel(this, ButtonMapper.getTarget(this, keyCode, t)),
                    () -> openSubMenu("bm_tap:" + keyCode + ":" + t));
        }

        if (ButtonMapper.hasAnyMapping(this, keyCode)) {
            section(tr("REMOVE", "KALDIR"));
            row("🗑  " + tr("Remove button remap", "Düğme eşlemesini kaldır"),
                    () -> "",
                    () -> {
                        ButtonMapper.removeMapping(this, keyCode);
                        closeSubMenu();
                        showStatusMsg(tr("Removed remap for " + keyName + ".", keyName + " eşlemesi kaldırıldı."));
                    });
        }
    }

    private void subMenuButtonTapTarget(int keyCode, int tapCount) {
        String keyName = ButtonMapper.keyLabel(this, keyCode);
        String tapName = ButtonMapper.tapTypeLabel(this, tapCount);
        String current = ButtonMapper.getTarget(this, keyCode, tapCount);
        mainHeadView.setText(keyName + " · " + tapName);
        backSubMenuRow();

        section(tr("BUILT-IN ACTIONS", "YERLEŞİK EYLEMLER"));
        choiceRow("🚫  " + tr("None (No action)", "Yok (Eylem yok)"),
                null,
                current.isEmpty(),
                () -> {
                    ButtonMapper.setTarget(this, keyCode, tapCount, "");
                    closeSubMenu();
                });
        choiceRow("🎛  " + tr("Control Panel (Overlay)", "Denetim Paneli (Ekran Üstü)"),
                null,
                ButtonMapper.TARGET_CONTROL_PANEL.equals(current),
                () -> {
                    ButtonMapper.setTarget(this, keyCode, tapCount, ButtonMapper.TARGET_CONTROL_PANEL);
                    closeSubMenu();
                });
        choiceRow("🗂  " + tr("Recent Apps Switcher (Overlay)", "Son Uygulamalar Değiştiricisi (Ekran Üstü)"),
                null,
                ButtonMapper.TARGET_RECENT_APPS.equals(current),
                () -> {
                    ButtonMapper.setTarget(this, keyCode, tapCount, ButtonMapper.TARGET_RECENT_APPS);
                    closeSubMenu();
                });
        choiceRow("🌙  " + tr("Ambient Mode / StandBy", "Ortam Modu / StandBy"),
                null,
                ButtonMapper.TARGET_AMBIENT.equals(current),
                () -> {
                    ButtonMapper.setTarget(this, keyCode, tapCount, ButtonMapper.TARGET_AMBIENT);
                    closeSubMenu();
                });
        choiceRow("🔒  " + tr("Lock Screen", "Kilit Ekranı"),
                null,
                ButtonMapper.TARGET_LOCK_SCREEN.equals(current),
                () -> {
                    ButtonMapper.setTarget(this, keyCode, tapCount, ButtonMapper.TARGET_LOCK_SCREEN);
                    closeSubMenu();
                });
        choiceRow("⏻  " + tr("Power Off (Screen Off)", "Kapat (Ekranı Kapat)"),
                null,
                ButtonMapper.TARGET_POWER_OFF.equals(current),
                () -> {
                    ButtonMapper.setTarget(this, keyCode, tapCount, ButtonMapper.TARGET_POWER_OFF);
                    closeSubMenu();
                });
        choiceRow("⌨  " + tr("Send Key Code (Remote / Media / System)…", "Tuş Kodu Gönder (Kumanda / Medya / Sistem)…"),
                current.startsWith(ButtonMapper.TARGET_KEYCODE_PREFIX) ? ButtonMapper.targetLabel(this, current) : tr("Send any Android KeyEvent keycode without 3rd-party apps", "Üçüncü parti uygulamaya gerek kalmadan istediğiniz tuş kodunu gönderin"),
                current.startsWith(ButtonMapper.TARGET_KEYCODE_PREFIX),
                () -> ButtonMapper.showSendKeyCodePickerDialog(this, keyCode, tapCount, this::closeSubMenu));

        section(tr("INSTALLED APPS", "YÜKLÜ UYGULAMALAR"));
        PackageManager pm = getPackageManager();
        Set<String> seenPkgs = new HashSet<>();
        List<String[]> appPairs = new ArrayList<>();
        String[] cats = {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER};
        for (String cat : cats) {
            Intent q = new Intent(Intent.ACTION_MAIN).addCategory(cat);
            for (ResolveInfo ri : pm.queryIntentActivities(q, 0)) {
                if (ri.activityInfo == null) continue;
                String pkg = ri.activityInfo.packageName;
                if (pkg.equals(getPackageName()) || seenPkgs.contains(pkg)) continue;
                seenPkgs.add(pkg);
                String appLabel = ri.loadLabel(pm).toString();
                appPairs.add(new String[]{appLabel, ButtonMapper.TARGET_APP_PREFIX + pkg, pkg});
            }
        }
        Collections.sort(appPairs, (x, y) -> x[0].compareToIgnoreCase(y[0]));
        for (String[] pair : appPairs) {
            final String appLabel = pair[0];
            final String targetVal = pair[1];
            final String pkg = pair[2];
            choiceRow(appLabel, pkg, targetVal.equals(current), () -> {
                ButtonMapper.setTarget(this, keyCode, tapCount, targetVal);
                closeSubMenu();
            });
        }
    }

    private void subMenuDefaultHome() {
        mainHeadView.setText(tr("Default home screen", "Varsayılan ana ekran"));
        backSubMenuRow();
        section(tr("DEFAULT LAUNCHER SETUP (TCL / PHILIPS / GOOGLE TV)", "VARSAYILAN BAŞLATICI KURULUMU (TCL / PHILIPS / GOOGLE TV)"));

        twoLineRow(tr("1. Set testOS as Default via Local ADB", "1. Yerel ADB ile testOS'ı Varsayılan Yap"),
                () -> Perms.isDefaultHome(this) ? tr("Active", "Etkin") : tr("Run ROLE_HOME & set-home-activity ›", "ROLE_HOME ve set-home-activity çalıştır ›"),
                () -> {
                    AlertDialog progress = dialog(tr("Setting Default Launcher…", "Varsayılan Başlatıcı Ayarlanıyor…"))
                            .setMessage(tr("Setting ROLE_HOME and default home activity over Local ADB…",
                                    "Yerel ADB üzerinden ROLE_HOME ve varsayılan ana ekran etkinliği ayarlanıyor…"))
                            .setCancelable(false)
                            .create();
                    progress.show();
                    LocalAdb.setDefaultLauncherViaAdb(this, (success, msg) -> runOnUiThread(() -> {
                        if (isFinishing()) return;
                        try {
                            progress.dismiss();
                        } catch (Throwable ignored) {
                        }
                        refreshAll();
                        showStatusMsg(msg);
                    }));
                });

        twoLineRow(tr("2. Disable System Launchers (Google TV / Android TV / OEM)", "2. Sistem Başlatıcılarını Devre Dışı Bırak (Google TV / Android TV / OEM)"),
                () -> CleanYourTv.getSystemLaunchersSummary(this),
                () -> openSubMenu("cyt_launchers"));

        twoLineRow(tr("3. Fix Home Button via Accessibility (Button Mapper)", "3. Erişilebilirlik ile Ana Ekran Tuşu Düzeltmesi"),
                () -> ButtonMapper.isHomeFixEnabled(this)
                        ? tr("On — Pressing Home launches testOS", "Açık — Ana Ekran tuşu testOS'ı açar")
                        : tr("Off — Tap to enable Home button remap", "Kapalı — Ana Ekran tuşu yönlendirmesini açmak için dokunun"),
                () -> {
                    boolean next = !ButtonMapper.isHomeFixEnabled(this);
                    ButtonMapper.setHomeFixEnabled(this, next);
                    rebuildMainRows();
                    showStatusMsg(next
                            ? tr("Home button fix enabled.", "Ana Ekran tuşu düzeltmesi etkinleştirildi.")
                            : tr("Home button fix disabled.", "Ana Ekran tuşu düzeltmesi kapatıldı."));
                });

        twoLineRow(tr("4. Open Android System Home Settings", "4. Android Sistem Ana Ekran Ayarlarını Aç"),
                () -> tr("Open system selector (if supported by TV) ›", "Sistem seçicisini aç (TV destekliyorsa) ›"),
                () -> Perms.openSystemHomeSettingsUi(this));

        infoNote(tr("On TCL and Philips Android 11/12 TVs, the OEM firmware often hides the Android Default Home UI and forces the stock launcher when pressing Home. Disabling the stock launcher (Option 2) or enabling the Home Button Fix (Option 3) guarantees pressing Home always opens testOS.",
                "TCL ve Philips Android 11/12 TV'lerde üretici yazılımı genellikle Android Varsayılan Ana Ekran seçicisini gizler ve Home tuşuna basıldığında stok başlatıcıyı zorlar. Stok başlatıcıyı devre dışı bırakmak (2. Seçenek) veya Ana Ekran Tuşu Düzeltmesini açmak (3. Seçenek) Home tuşunun her zaman testOS'ı açmasını sağlar."));
    }

    private void subMenuCheckPermissions() {
        mainHeadView.setText(tr("Check permissions", "İzinleri kontrol et"));
        backSubMenuRow();
        section(tr("PERMISSION STATUS", "İZİN DURUMU"));

        row(tr("Grant All Permissions via Local ADB", "Tüm İzinleri Yerel ADB ile Ver"),
                () -> tr("Grant All ›", "Tümünü Ver ›"),
                this::pairOrRepairAdb);

        row(tr("Default Home Screen", "Varsayılan Ana Ekran"),
                () -> Perms.isDefaultHome(this) ? "✓  " + tr("Active", "Etkin") : "✗  " + tr("Not set", "Ayarlanmadı"),
                () -> openSubMenu("default_home"));

        row(tr("Button Mapper & Control Panel (Accessibility)", "Düğme Eşleyici ve Denetim Paneli (Erişilebilirlik)"),
                () -> ButtonMapper.isServiceEnabled(this) ? "✓  " + tr("Active", "Etkin") : "✗  " + tr("Disabled", "Kapalı"),
                () -> {
                    Intent acc = Perms.accessibilityScreen(this);
                    if (acc == null || !Suggestions.start(this, acc)) {
                        pairOrRepairAdb();
                    }
                });

        row(tr("Wi-Fi Name (Location)", "Wi-Fi Adı (Konum)"),
                () -> Perms.hasLocation(this) ? "✓  " + tr("Allowed", "İzin verildi") : "✗  " + tr("Not allowed", "İzin verilmedi"),
                () -> Perms.requestLocation(this));
    }

    // ---------------- CleanYourTV Native Right-Side Sub-Menus ----------------

    private void subMenuCytModels() {
        CleanYourTv.HardwareMatchResult hw = CleanYourTv.findHardwareMatches(this);
        mainHeadView.setText(tr("Choose TV model", "TV modelini seçin"));
        backSubMenuRow();

        section(tr("ACTIONS", "İŞLEMLER"));
        twoLineRow(tr("🔍 Scan Installed System Apps via ADB", "🔍 ADB ile Yüklü Sistem Uygulamalarını Tara"),
                () -> tr("Pull full app list from ADB and match models by OEM/system packages",
                         "ADB'den tam uygulama listesini çek ve sistem/üretici paketlerine göre eşleştir"),
                () -> {
                    cytScannedMatches = null;
                    openSubMenu("cyt_models_scan");
                });
        twoLineRow(tr("📋 Browse / Search All 341 TV Models", "📋 Tüm 341 TV Modeline Göz At / Ara"),
                () -> tr("View or filter the complete list of supported TV models",
                         "Desteklenen tüm TV modellerinin tam listesini görüntüle veya filtrele"),
                () -> {
                    cytModelFilterQuery = "";
                    openSubMenu("cyt_models_all");
                });

        String currentSlug = CleanYourTv.getSelectedModelSlug(this);
        section(tr("MATCHING MODELS FOR ", "EŞLEŞEN MODELLER: ") + hw.deviceDescription);
        if (hw.candidates.isEmpty()) {
            infoNote(tr("No direct model name match for " + hw.deviceDescription + ". Use Scan Installed System Apps via ADB above to find the closest TV profile.",
                    hw.deviceDescription + " için doğrudan model adı eşleşmesi bulunamadı. En yakın TV profilini bulmak için yukarıdaki ADB ile Yüklü Sistem Uygulamalarını Tara seçeneğini kullanın."));
        } else {
            for (CleanYourTv.ModelMatch m : hw.candidates) {
                final CleanYourTv.ModelEntry entry = m.entry;
                boolean isCurrent = entry.slug.equalsIgnoreCase(currentSlug);
                choiceRow(entry.title,
                        "[" + entry.slug + "]  •  " + m.reason,
                        isCurrent,
                        () -> selectCytModelAndProceed(entry));
            }
        }
    }

    private void selectCytModelAndProceed(CleanYourTv.ModelEntry entry) {
        CleanYourTv.setSelectedModel(this, entry);
        cytChecklistItems = null;
        showStatusMsg(tr("Selected model: " + entry.title, "Seçilen model: " + entry.title));
        if (cytAutoContinueToDebloat) {
            cytAutoContinueToDebloat = false;
            activeSubMenu = "cyt_debloat";
            rebuildMainRows();
            focusFirstRow();
        } else {
            rebuildMainRows();
        }
    }

    private void subMenuCytScannedModels() {
        mainHeadView.setText(tr("Models by System App Similarity", "Sistem Uygulaması Benzerliğine Göre Modeller"));
        backSubMenuRow();

        if (cytScannedMatches == null) {
            if (!cytScanningApps) {
                cytScanningApps = true;
                new Thread(() -> {
                    Set<String> installed = CleanYourTv.fetchInstalledPackages(this, null, null);
                    List<CleanYourTv.ModelMatch> matches = CleanYourTv.findPackageSimilarityMatches(this, installed);
                    runOnUiThread(() -> {
                        cytScanningApps = false;
                        cytScannedCount = installed.size();
                        cytScannedMatches = matches;
                        if (!isFinishing() && "cyt_models_scan".equals(activeSubMenu)) {
                            rebuildMainRows();
                        }
                    });
                }, "cyt-submenu-scan").start();
            }
            infoNote(tr("Scanning installed system packages over ADB and comparing across 341 TV models…",
                    "ADB üzerinden yüklü sistem paketleri taranıyor ve 341 TV modeliyle karşılaştırılıyor…"));
            return;
        }

        section(tr("CLOSEST MATCHES (" + cytScannedCount + " APPS SCANNED)",
                "EN YAKIN EŞLEŞMELER (" + cytScannedCount + " UYGULAMA TARANDI)"));
        String currentSlug = CleanYourTv.getSelectedModelSlug(this);
        for (CleanYourTv.ModelMatch m : cytScannedMatches) {
            final CleanYourTv.ModelEntry entry = m.entry;
            boolean isCurrent = entry.slug.equalsIgnoreCase(currentSlug);
            choiceRow(entry.title,
                    "[" + entry.slug + "]  •  " + m.reason,
                    isCurrent,
                    () -> selectCytModelAndProceed(entry));
        }
    }

    private void subMenuCytAllModels() {
        mainHeadView.setText(tr("All 341 TV Models", "Tüm 341 TV Modeli"));
        backSubMenuRow();

        row(tr("🔍 Search / Filter by brand or model…", "🔍 Marka veya modele göre ara / filtrele…"),
                () -> cytModelFilterQuery.isEmpty() ? tr("All (341)", "Tümü (341)") : "\"" + cytModelFilterQuery + "\"",
                this::promptCytModelFilter);
        if (!cytModelFilterQuery.isEmpty()) {
            row(tr("Clear filter (Show all)", "Filtreyi temizle (Tümünü göster)"),
                    () -> "",
                    () -> {
                        cytModelFilterQuery = "";
                        cytAllModelsPage = 0;
                        rebuildMainRows();
                    });
        } else {
            section(tr("QUICK BRAND FILTERS", "HIZLI MARKA FİLTRELERİ"));
            String[][] brands = {
                {"TCL / FFALCON / Thomson", "tcl"},
                {"Philips / TPVision", "philips"},
                {"Sony Bravia", "sony"},
                {"Xiaomi / Mi TV / Redmi", "xiaomi"},
                {"Hisense / Toshiba", "hisense"},
                {"Skyworth / Coocaa / Strong", "skyworth"}
            };
            for (String[] b : brands) {
                final String bLabel = b[0];
                final String bQuery = b[1];
                row("🏷  " + bLabel, () -> tr("Filter ›", "Filtrele ›"), () -> {
                    cytModelFilterQuery = bQuery;
                    cytAllModelsPage = 0;
                    rebuildMainRows();
                    focusFirstRow();
                });
            }
        }

        List<CleanYourTv.ModelEntry> all = CleanYourTv.loadIndex(this);
        String q = cytModelFilterQuery.trim().toLowerCase(java.util.Locale.US);
        List<CleanYourTv.ModelEntry> filtered = new ArrayList<>();
        for (CleanYourTv.ModelEntry m : all) {
            if (q.isEmpty()
                    || m.slug.toLowerCase(java.util.Locale.US).contains(q)
                    || m.title.toLowerCase(java.util.Locale.US).contains(q)
                    || m.headerMeta.toLowerCase(java.util.Locale.US).contains(q)) {
                filtered.add(m);
            }
        }

        int total = filtered.size();
        int totalPages = Math.max(1, (total + CYT_PAGE_SIZE - 1) / CYT_PAGE_SIZE);
        if (cytAllModelsPage >= totalPages) cytAllModelsPage = totalPages - 1;
        if (cytAllModelsPage < 0) cytAllModelsPage = 0;
        int start = cytAllModelsPage * CYT_PAGE_SIZE;
        int end = Math.min(total, start + CYT_PAGE_SIZE);

        if (totalPages > 1) {
            section(tr("PAGE " + (cytAllModelsPage + 1) + " OF " + totalPages + " (" + (start + 1) + "–" + end + " OF " + total + ")",
                    "SAYFA " + (cytAllModelsPage + 1) + " / " + totalPages + " (" + (start + 1) + "–" + end + " / " + total + ")"));
            if (cytAllModelsPage > 0) {
                row("⬅  " + tr("Previous Page", "Önceki Sayfa"),
                        () -> (cytAllModelsPage) + " / " + totalPages,
                        () -> {
                            cytAllModelsPage--;
                            rebuildMainRows();
                            focusFirstRow();
                        });
            }
            if (cytAllModelsPage + 1 < totalPages) {
                row(tr("Next Page", "Sonraki Sayfa") + "  ➡",
                        () -> (cytAllModelsPage + 2) + " / " + totalPages,
                        () -> {
                            cytAllModelsPage++;
                            rebuildMainRows();
                            focusFirstRow();
                        });
            }
        }

        String currentSlug = CleanYourTv.getSelectedModelSlug(this);
        section(tr("MODELS (" + total + ")", "MODELLER (" + total + ")"));
        for (int i = start; i < end; i++) {
            final CleanYourTv.ModelEntry entry = filtered.get(i);
            boolean isCurrent = entry.slug.equalsIgnoreCase(currentSlug);
            choiceRow(entry.title,
                    "[" + entry.slug + "]  •  " + entry.packages.size() + " " + tr("packages", "paket"),
                    isCurrent,
                    () -> selectCytModelAndProceed(entry));
        }

        if (totalPages > 1) {
            section(tr("PAGE NAVIGATION", "SAYFA GEÇİŞİ"));
            if (cytAllModelsPage + 1 < totalPages) {
                row(tr("Next Page", "Sonraki Sayfa") + "  ➡",
                        () -> tr("Page " + (cytAllModelsPage + 2) + " of " + totalPages,
                                "Sayfa " + (cytAllModelsPage + 2) + " / " + totalPages),
                        () -> {
                            cytAllModelsPage++;
                            rebuildMainRows();
                            focusFirstRow();
                        });
            }
            if (cytAllModelsPage > 0) {
                row("⬅  " + tr("Previous Page", "Önceki Sayfa"),
                        () -> tr("Page " + cytAllModelsPage + " of " + totalPages,
                                "Sayfa " + cytAllModelsPage + " / " + totalPages),
                        () -> {
                            cytAllModelsPage--;
                            rebuildMainRows();
                            focusFirstRow();
                        });
            }
        }
    }

    private void promptCytModelFilter() {
        android.widget.EditText in = new android.widget.EditText(this);
        in.setSingleLine(true);
        in.setHint(tr("e.g. tcl, philips, sony, hisense, xiaomi…", "örn. tcl, philips, sony, hisense, xiaomi…"));
        in.setText(cytModelFilterQuery);
        in.setSelection(in.getText().length());
        int pad = u(14);
        FrameLayout box = new FrameLayout(this);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(in);
        AlertDialog dlg = dialog(tr("Filter TV Models", "TV Modellerini Filtrele"))
                .setView(box)
                .setPositiveButton(tr("Filter", "Filtrele"), (d, w) -> {
                    cytModelFilterQuery = in.getText().toString().trim();
                    cytAllModelsPage = 0;
                    rebuildMainRows();
                })
                .setNegativeButton(tr("Cancel", "İptal"), null)
                .show();
        Ui.setupDialogEditText(in, dlg);
        in.requestFocus();
    }

    private void subMenuCytDebloat() {
        String slug = CleanYourTv.getSelectedModelSlug(this);
        final CleanYourTv.ModelEntry model = CleanYourTv.findModelBySlug(this, slug);
        if (model == null) {
            cytAutoContinueToDebloat = true;
            activeSubMenu = "cyt_models";
            subMenuCytModels();
            return;
        }

        mainHeadView.setText(tr("Debloat TV — ", "TV Debloat — ") + model.title);
        backSubMenuRow();

        if (cytChecklistItems == null || !slug.equals(cytChecklistSlug)) {
            if (!cytChecklistLoading) {
                cytChecklistLoading = true;
                new Thread(() -> {
                    Set<String> enabled = new HashSet<>();
                    Set<String> disabled = new HashSet<>();
                    Set<String> installed = CleanYourTv.fetchInstalledPackages(this, enabled, disabled);
                    boolean hasSetupwraith = installed.contains("com.google.android.tungsten.setupwraith");
                    boolean isGoogleTv = hasSetupwraith || installed.contains("com.google.android.apps.tv.launcherx");
                    boolean wifiOwned = false;
                    if (isGoogleTv) {
                        try {
                            String wifiDump = LocalAdb.execShellOutput(this, "dumpsys wifi | grep -o 'cname=[^ ]*'");
                            if (wifiDump != null && wifiDump.contains("com.google.android.tungsten.setupwraith")) {
                                wifiOwned = true;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                    List<CleanYourTv.DebloatItem> built = CleanYourTv.buildChecklistForModel(this, slug, installed, false);
                    final boolean fGoogle = isGoogleTv;
                    final boolean fWifi = wifiOwned;
                    runOnUiThread(() -> {
                        cytChecklistLoading = false;
                        cytChecklistSlug = slug;
                        cytCachedEnabledPkgs = enabled;
                        cytChecklistIsGoogleTv = fGoogle;
                        cytChecklistWifiOwnedBySetupwraith = fWifi;
                        cytChecklistItems = built;
                        if (!isFinishing() && "cyt_debloat".equals(activeSubMenu)) {
                            rebuildMainRows();
                        }
                    });
                }, "cyt-submenu-debloat").start();
            }
            infoNote(tr("Checking installed packages over ADB for " + model.title + "…",
                    model.title + " için ADB üzerinden yüklü paketler kontrol ediliyor…"));
            return;
        }

        if (cytChecklistItems.isEmpty()) {
            infoNote(tr("None of the packages listed in " + model.title + " are installed on this TV.",
                    model.title + " profilindeki paketlerin hiçbiri bu TV'de yüklü değil."));
            row(tr("🔍 Scan Installed System Apps to Find Matching Model", "🔍 Eşleşen Modeli Bulmak İçin Yüklü Uygulamaları Tara"),
                    () -> "›",
                    () -> {
                        cytScannedMatches = null;
                        openSubMenu("cyt_models_scan");
                    });
            return;
        }

        if (cytChecklistIsGoogleTv) {
            section(tr("GOOGLE TV WI-FI NOTICE", "GOOGLE TV WI-FI UYARISI"));
            infoNote(cytChecklistWifiOwnedBySetupwraith
                    ? tr("⚠️ Your Wi-Fi network is currently owned by setupwraith. Before disabling setupwraith, open Wi-Fi Settings, Forget your Wi-Fi network, and re-connect so Wi-Fi stays saved.",
                         "⚠️ Mevcut Wi-Fi ağınız setupwraith tarafından kaydedilmiş. setupwraith'i devre dışı bırakmadan önce Wi-Fi Ayarları'ndan ağınızı unutup yeniden bağlanın.")
                    : tr("On Google TV, if you disable setupwraith, make sure your Wi-Fi network was re-added in Wi-Fi Settings.",
                         "Google TV'de setupwraith'i devre dışı bırakacaksanız Wi-Fi ağınızı Wi-Fi Ayarları'ndan yeniden eklediğinizden emin olun."));
            row(tr("📶 Open Wi-Fi Settings…", "📶 Wi-Fi Ayarlarını Aç…"),
                    () -> "›",
                    () -> {
                        Intent wifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        if (!Suggestions.start(this, wifi)) {
                            Suggestions.start(this, new Intent(Settings.ACTION_SETTINGS));
                        }
                    });
        }

        int selectedCount = 0;
        for (CleanYourTv.DebloatItem it : cytChecklistItems) {
            if (it.checked) selectedCount++;
        }
        final int finalSelectedCount = selectedCount;

        section(tr("ACTIONS", "İŞLEMLER"));
        row(tr("✅ Disable Selected Packages (" + finalSelectedCount + ")", "✅ Seçilen Paketleri Devre Dışı Bırak (" + finalSelectedCount + ")"),
                () -> tr("Run via ADB ›", "ADB ile Çalıştır ›"),
                () -> {
                    List<String> toDisable = new ArrayList<>();
                    for (CleanYourTv.DebloatItem it : cytChecklistItems) {
                        if (it.checked) toDisable.add(it.pkg);
                    }
                    if (toDisable.isEmpty()) {
                        showStatusMsg(tr("Select at least one package first.", "Önce en az bir paket seçin."));
                        return;
                    }
                    CleanYourTv.executeDebloatPackages(this, model.title, toDisable, () -> {
                        cytChecklistItems = null;
                        rebuildMainRows();
                    });
                });
        row(tr("Select All / Select None", "Tümünü Seç / Seçimi Kaldır"),
                () -> finalSelectedCount + " / " + cytChecklistItems.size(),
                () -> {
                    boolean anyUnchecked = false;
                    for (CleanYourTv.DebloatItem it : cytChecklistItems) {
                        if (!it.checked) {
                            anyUnchecked = true;
                            break;
                        }
                    }
                    for (CleanYourTv.DebloatItem it : cytChecklistItems) {
                        it.checked = anyUnchecked;
                    }
                    rebuildMainRows();
                });

        section(tr("PACKAGES ON THIS TV (" + cytChecklistItems.size() + ")", "BU TV'DEKİ PAKETLER (" + cytChecklistItems.size() + ")"));
        for (CleanYourTv.DebloatItem it : cytChecklistItems) {
            final CleanYourTv.DebloatItem item = it;
            boolean alreadyDisabled = !cytCachedEnabledPkgs.isEmpty() && !cytCachedEnabledPkgs.contains(item.pkg);
            String tag = alreadyDisabled
                    ? tr(" [Already disabled]", " [Zaten devre dışı]")
                    : (item.optionalChoice ? tr(" [Optional]", " [İsteğe bağlı]") : "");
            String sub = item.pkg + (item.description.isEmpty() ? "" : "  •  " + item.description);
            choiceRow(item.appLabel + tag, sub, item.checked, () -> {
                item.checked = !item.checked;
                rebuildMainRows();
            });
        }
    }

    private void subMenuCytSystemLaunchers() {
        mainHeadView.setText(tr("Disable system launchers", "Sistem başlatıcılarını devre dışı bırak"));
        backSubMenuRow();

        if (!cytLaunchersSyncedAdb) {
            cytLaunchersSyncedAdb = true;
            new Thread(() -> {
                Set<String> enabled = new HashSet<>();
                Set<String> disabled = new HashSet<>();
                Set<String> installed = CleanYourTv.fetchInstalledPackages(this, enabled, disabled);
                runOnUiThread(() -> {
                    cytCachedInstalledPkgs = installed;
                    cytCachedEnabledPkgs = enabled;
                    if (!isFinishing() && "cyt_launchers".equals(activeSubMenu)) {
                        rebuildMainRows();
                    }
                });
            }, "cyt-sync-launchers").start();
        }

        List<CleanYourTv.DebloatItem> launchers = CleanYourTv.getInstalledSystemLaunchers(
                this,
                cytCachedInstalledPkgs.isEmpty() ? null : cytCachedInstalledPkgs,
                cytCachedEnabledPkgs.isEmpty() ? null : cytCachedEnabledPkgs);
        if (launchers.isEmpty()) {
            infoNote(tr("No stock Android TV / Google TV / OEM launcher packages were detected on this TV.",
                    "Bu TV'de stok Android TV / Google TV / üretici başlatıcı paketi tespit edilmedi."));
            return;
        }

        section(tr("ONE-TAP ACTIONS", "TEK DOKUNUŞ İŞLEMLERİ"));
        twoLineRow(tr("Disable All Stock Launchers & Set testOS as Home", "Tüm Stok Başlatıcıları Devre Dışı Bırak ve tvOS'u Ana Ekran Yap"),
                () -> tr("Disable all ›", "Tümünü kapat ›"),
                () -> {
                    List<String> toDisable = new ArrayList<>();
                    for (CleanYourTv.DebloatItem it : launchers) {
                        toDisable.add(it.pkg);
                    }
                    CleanYourTv.executeDebloatPackages(this, tr("System Launchers", "Sistem Başlatıcıları"), toDisable, () -> {
                        cytLaunchersSyncedAdb = false;
                        rebuildMainRows();
                    });
                });
        twoLineRow(tr("Re-enable All Stock Launchers", "Tüm Stok Başlatıcıları Yeniden Etkinleştir"),
                () -> tr("Enable all ›", "Tümünü aç ›"),
                () -> {
                    List<String> toEnable = new ArrayList<>();
                    for (CleanYourTv.DebloatItem it : launchers) {
                        toEnable.add(it.pkg);
                    }
                    CleanYourTv.enablePackagesList(this, toEnable, false, () -> {
                        cytLaunchersSyncedAdb = false;
                        rebuildMainRows();
                    });
                });

        section(tr("INSTALLED SYSTEM LAUNCHERS ON THIS TV (" + launchers.size() + ")",
                "BU TV'DE YÜKLÜ SİSTEM BAŞLATICILARI (" + launchers.size() + ")"));
        for (CleanYourTv.DebloatItem it : launchers) {
            final CleanYourTv.DebloatItem item = it;
            boolean isEnabled = item.checked;
            String badge = isEnabled
                    ? tr(" [Enabled — Tap to Disable]", " [Etkin — Devre Dışı Bırakmak İçin OK]")
                    : tr(" [Disabled — Tap to Enable]", " [Devre Dışı — Etkinleştirmek İçin OK]");
            choiceRow(item.appLabel + badge,
                    item.pkg + "  •  " + item.description,
                    !isEnabled,
                    () -> CleanYourTv.toggleSinglePackage(this, item.pkg, isEnabled, () -> {
                        cytLaunchersSyncedAdb = false;
                        rebuildMainRows();
                    }));
        }

        infoNote(tr("Checked items are currently DISABLED. Disabling the stock system launcher (e.g. Google TV Home / LauncherX + setupwraith, Android TV Home, or TCL/Philips OEM Home) forces Android to use testOS when pressing the Home key.",
                "İşaretli öğeler şu anda DEVRE DIŞI bırakılmıştır. Stok sistem başlatıcısını devre dışı bırakmak, Home tuşuna basıldığında Android'in her zaman testOS'ı kullanmasını sağlar."));
    }

    private void subMenuCytManualDebloat() {
        mainHeadView.setText(tr("Manual debloat (All system apps)", "Manuel debloat (Tüm sistem uygulamaları)"));
        backSubMenuRow();

        // Populate immediately from PackageManager (instant with QUERY_ALL_PACKAGES + cached labels)
        if (cytManualItems == null) {
            cytManualItems = CleanYourTv.getAllSystemAppsForManualDebloat(
                    this,
                    cytCachedInstalledPkgs.isEmpty() ? null : cytCachedInstalledPkgs,
                    cytCachedEnabledPkgs.isEmpty() ? null : cytCachedEnabledPkgs);
            if (!cytManualLoading) {
                cytManualLoading = true;
                new Thread(() -> {
                    Set<String> enabled = new HashSet<>();
                    Set<String> disabled = new HashSet<>();
                    Set<String> installed = CleanYourTv.fetchInstalledPackages(this, enabled, disabled);
                    List<CleanYourTv.DebloatItem> allSys = CleanYourTv.getAllSystemAppsForManualDebloat(this, installed, enabled);
                    runOnUiThread(() -> {
                        cytManualLoading = false;
                        cytCachedInstalledPkgs = installed;
                        cytCachedEnabledPkgs = enabled;
                        cytManualItems = allSys;
                        if (!isFinishing() && "cyt_manual".equals(activeSubMenu)) {
                            rebuildMainRows();
                        }
                    });
                }, "cyt-submenu-manual").start();
            }
        }

        row(tr("🔍 Filter system apps…", "🔍 Sistem uygulamalarını filtrele…"),
                () -> cytManualFilterQuery.isEmpty() ? tr("All (" + cytManualItems.size() + ")", "Tümü (" + cytManualItems.size() + ")") : "\"" + cytManualFilterQuery + "\"",
                this::promptCytManualFilter);
        if (!cytManualFilterQuery.isEmpty()) {
            row(tr("Clear filter", "Filtreyi temizle"),
                    () -> "",
                    () -> {
                        cytManualFilterQuery = "";
                        cytManualPage = 0;
                        rebuildMainRows();
                    });
        }

        row(tr("↻ Refresh system app list", "↻ Sistem uygulama listesini yenile"),
                () -> "",
                () -> {
                    cytManualItems = null;
                    cytManualPage = 0;
                    rebuildMainRows();
                });

        String q = cytManualFilterQuery.trim().toLowerCase(java.util.Locale.US);
        List<CleanYourTv.DebloatItem> filtered = new ArrayList<>();
        for (CleanYourTv.DebloatItem it : cytManualItems) {
            if (q.isEmpty()
                    || it.appLabel.toLowerCase(java.util.Locale.US).contains(q)
                    || it.pkg.toLowerCase(java.util.Locale.US).contains(q)) {
                filtered.add(it);
            }
        }

        int total = filtered.size();
        int totalPages = Math.max(1, (total + CYT_PAGE_SIZE - 1) / CYT_PAGE_SIZE);
        if (cytManualPage >= totalPages) cytManualPage = totalPages - 1;
        if (cytManualPage < 0) cytManualPage = 0;
        int start = cytManualPage * CYT_PAGE_SIZE;
        int end = Math.min(total, start + CYT_PAGE_SIZE);

        if (totalPages > 1) {
            section(tr("PAGE " + (cytManualPage + 1) + " OF " + totalPages + " (" + (start + 1) + "–" + end + " OF " + total + ")",
                    "SAYFA " + (cytManualPage + 1) + " / " + totalPages + " (" + (start + 1) + "–" + end + " / " + total + ")"));
            if (cytManualPage > 0) {
                row("⬅  " + tr("Previous Page", "Önceki Sayfa"),
                        () -> (cytManualPage) + " / " + totalPages,
                        () -> {
                            cytManualPage--;
                            rebuildMainRows();
                            focusFirstRow();
                        });
            }
            if (cytManualPage + 1 < totalPages) {
                row(tr("Next Page", "Sonraki Sayfa") + "  ➡",
                        () -> (cytManualPage + 2) + " / " + totalPages,
                        () -> {
                            cytManualPage++;
                            rebuildMainRows();
                            focusFirstRow();
                        });
            }
        }

        section(tr("ALL SYSTEM APPS (" + total + " — TAP TO DISABLE / ENABLE)", "TÜM SİSTEM UYGULAMALARI (" + total + " — DEVRE DIŞI BIRAK / ETKİNLEŞTİR)"));
        for (int i = start; i < end; i++) {
            final CleanYourTv.DebloatItem item = filtered.get(i);
            final boolean isCurrentlyEnabled = item.checked;
            final boolean isCritical = CleanYourTv.isCriticalProtectedService(item.pkg, item.appLabel);
            String statusTag = isCritical && isCurrentlyEnabled
                    ? tr(" [Critical System Service — Protected]", " [Kritik Sistem Servisi — Korumalı]")
                    : (isCurrentlyEnabled
                            ? tr(" [Enabled]", " [Etkin]")
                            : tr(" [Disabled]", " [Devre Dışı]"));
            String sub = item.pkg + "  •  " + item.description;
            choiceRow(item.appLabel + statusTag, sub, !isCurrentlyEnabled, () -> {
                if (isCurrentlyEnabled && isCritical) {
                    dialog(tr("🛑 Critical System Service", "🛑 Kritik Sistem Servisi"))
                            .setMessage(tr(
                                    item.appLabel + " (" + item.pkg + ") is a critical system service required for your TV remote and Bluetooth connectivity.\n\nIt cannot be disabled under any circumstances.",
                                    item.appLabel + " (" + item.pkg + ") TV kumandanız ve Bluetooth bağlantısı için gerekli kritik bir sistem servisidir.\n\nHiçbir koşulda devre dışı bırakılamaz."))
                            .setPositiveButton(tr("OK", "Tamam"), null)
                            .show();
                } else if (isCurrentlyEnabled
                        && (isCritical || !CleanYourTv.isSafeToDisable(this, item.pkg))) {
                    dialog(tr("🛑 Protected System Package", "🛑 Korunan Sistem Paketi"))
                            .setMessage(tr(
                                    item.appLabel + " (" + item.pkg + ") is protected by testOS because it may be required for Android, TV input, Device Owner, or launcher recovery. It cannot be disabled from Manual Debloat.",
                                    item.appLabel + " (" + item.pkg + ") Android, TV girişi, Cihaz Sahibi veya başlatıcı kurtarma için gerekli olabileceğinden testOS tarafından korunuyor. Manuel Debloat ile devre dışı bırakılamaz."))
                            .setPositiveButton(tr("OK", "Tamam"), null)
                            .show();
                } else if (isCurrentlyEnabled) {
                    dialog(tr("Disable System App?", "Sistem Uygulaması Devre Dışı Bırakılsın mı?"))
                            .setMessage(tr(
                                    "You are about to disable " + item.appLabel + " (" + item.pkg + ").\n\nThis is a manual operation. It may affect TV features. Continue only if you know what this package does.",
                                    item.appLabel + " (" + item.pkg + ") paketini devre dışı bırakmak üzeresiniz.\n\nBu manuel bir işlemdir ve TV özelliklerini etkileyebilir. Yalnızca bu paketin ne işe yaradığını biliyorsanız devam edin."))
                            .setPositiveButton(tr("Disable", "Devre Dışı Bırak"), (d, w) -> {
                                CleanYourTv.toggleSinglePackage(this, item.pkg, true, () -> {
                                    item.checked = false;
                                    cytCachedEnabledPkgs.remove(item.pkg);
                                    rebuildMainRows();
                                });
                            })
                            .setNegativeButton(tr("Cancel", "İptal"), null)
                            .show();
                } else {
                    CleanYourTv.toggleSinglePackage(this, item.pkg, false, () -> {
                        item.checked = true;
                        cytCachedEnabledPkgs.add(item.pkg);
                        rebuildMainRows();
                    });
                }
            });
        }

        if (totalPages > 1) {
            section(tr("PAGE NAVIGATION", "SAYFA GEÇİŞİ"));
            if (cytManualPage + 1 < totalPages) {
                row(tr("Next Page", "Sonraki Sayfa") + "  ➡",
                        () -> tr("Page " + (cytManualPage + 2) + " of " + totalPages,
                                "Sayfa " + (cytManualPage + 2) + " / " + totalPages),
                        () -> {
                            cytManualPage++;
                            rebuildMainRows();
                            focusFirstRow();
                        });
            }
            if (cytManualPage > 0) {
                row("⬅  " + tr("Previous Page", "Önceki Sayfa"),
                        () -> tr("Page " + cytManualPage + " of " + totalPages,
                                "Sayfa " + cytManualPage + " / " + totalPages),
                        () -> {
                            cytManualPage--;
                            rebuildMainRows();
                            focusFirstRow();
                        });
            }
        }
    }

    private void promptCytManualFilter() {
        android.widget.EditText in = new android.widget.EditText(this);
        in.setSingleLine(true);
        in.setHint(tr("App name or package name…", "Uygulama adı veya paket adı…"));
        in.setText(cytManualFilterQuery);
        in.setSelection(in.getText().length());
        int pad = u(14);
        FrameLayout box = new FrameLayout(this);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(in);
        AlertDialog dlg = dialog(tr("Filter System Apps", "Sistem Uygulamalarını Filtrele"))
                .setView(box)
                .setPositiveButton(tr("Filter", "Filtrele"), (d, w) -> {
                    cytManualFilterQuery = in.getText().toString().trim();
                    cytManualPage = 0;
                    rebuildMainRows();
                })
                .setNegativeButton(tr("Cancel", "İptal"), null)
                .show();
        Ui.setupDialogEditText(in, dlg);
        in.requestFocus();
    }

    private void subMenuCytSavedDisabled() {
        mainHeadView.setText(tr("Disabled apps backup", "Devre dışı bırakılan uygulamalar kaydı"));
        backSubMenuRow();

        List<String> saved = CleanYourTv.getSavedDisabledPackages(this);
        if (saved.isEmpty()) {
            infoNote(tr("No disabled packages have been recorded by CleanYourTV yet.",
                    "CleanYourTV tarafından henüz kaydedilmiş devre dışı paket yok."));
            return;
        }

        section(tr("ACTIONS", "İŞLEMLER"));
        row(tr("Restore All Saved Packages (" + saved.size() + ")", "Kaydedilen Tüm Paketleri Geri Yükle (" + saved.size() + ")"),
                () -> tr("Restore all ›", "Tümünü yükle ›"),
                () -> CleanYourTv.enablePackagesList(this, saved, true, this::rebuildMainRows));

        section(tr("SAVED DISABLED PACKAGES (TAP TO RE-ENABLE)", "KAYITLI DEVRE DIŞI PAKETLER (ETKİNLEŞTİRMEK İÇİN OK)"));
        PackageManager pm = getPackageManager();
        for (String pkg : saved) {
            final String p = pkg;
            String label = CleanYourTv.resolvePackageLabel(this, p);
            boolean enabledNow = false;
            try {
                ApplicationInfo ai = pm.getApplicationInfo(p, 0);
                enabledNow = (ai != null && ai.enabled);
            } catch (Throwable ignored) {
            }
            String st = enabledNow ? tr(" [Enabled]", " [Etkin]") : tr(" [Disabled — Tap to Enable]", " [Devre Dışı — Etkinleştir]");
            choiceRow(label + st, p, !enabledNow, () -> {
                CleanYourTv.enablePackagesList(this, Collections.singletonList(p), false, this::rebuildMainRows);
            });
        }
    }

    private void subMenuCytRestore() {
        mainHeadView.setText(tr("Restore debloated apps", "Debloat edilen uygulamaları geri yükle"));
        backSubMenuRow();

        List<String> saved = CleanYourTv.getSavedDisabledPackages(this);
        String slug = CleanYourTv.getSelectedModelSlug(this);
        CleanYourTv.ModelEntry model = CleanYourTv.findModelBySlug(this, slug);

        section(tr("RESTORE OPTIONS", "GERİ YÜKLEME SEÇENEKLERİ"));
        if (!saved.isEmpty()) {
            twoLineRow(tr("Restore Saved Disabled Apps (" + saved.size() + " packages)",
                          "Kaydedilen Devre Dışı Uygulamaları Geri Yükle (" + saved.size() + " paket)"),
                    () -> tr("Re-enable all packages disabled by CleanYourTV over ADB",
                             "CleanYourTV ile devre dışı bırakılan tüm paketleri ADB üzerinden yeniden etkinleştir"),
                    () -> CleanYourTv.enablePackagesList(this, saved, true, this::rebuildMainRows));
        }
        if (model != null && !model.packages.isEmpty()) {
            twoLineRow(tr("Restore Selected Model Profile (" + model.title + " — " + model.packages.size() + " packages)",
                          "Seçili Model Profilini Geri Yükle (" + model.title + " — " + model.packages.size() + " paket)"),
                    () -> tr("Re-enable all packages in " + model.slug,
                             model.slug + " profilindeki tüm paketleri yeniden etkinleştir"),
                    () -> CleanYourTv.enablePackagesList(this, model.packages, true, this::rebuildMainRows));
        }
        if (saved.isEmpty() && model == null) {
            infoNote(tr("No saved disabled-app backup was found, and no TV model is selected yet. Choose a TV model first if you want to restore by model profile.",
                    "Kaydedilmiş devre dışı uygulama yedeği bulunamadı ve henüz bir TV modeli seçilmedi."));
            row(tr("Choose TV model…", "TV modelini seçin…"),
                    () -> "›",
                    () -> {
                        cytAutoContinueToDebloat = false;
                        openSubMenu("cyt_models");
                    });
        }
    }

    // ================================================================ Row Builders (.shead, .srow)

    private Bitmap getSceneThumb(String assetPath, int reqW, int reqH) {
        String key = assetPath + ":" + reqW + "x" + reqH;
        Bitmap cached = SCENE_THUMB_CACHE.get(key);
        if (cached != null && !cached.isRecycled()) return cached;
        Bitmap raw = Scenes.loadAssetBitmap(this, assetPath, reqW, reqH);
        if (raw != null) {
            Bitmap scaled = Bitmap.createScaledBitmap(raw, reqW, reqH, true);
            if (scaled != raw) raw.recycle();
            SCENE_THUMB_CACHE.put(key, scaled);
            return scaled;
        }
        return null;
    }

    private void bindAssetThumbAsync(final ImageView thumb, final String assetPath, final int reqW, final int reqH) {
        if (assetPath == null || assetPath.isEmpty()) return;
        final String key = assetPath + ":" + reqW + "x" + reqH;
        Bitmap cached = SCENE_THUMB_CACHE.get(key);
        if (cached != null && !cached.isRecycled()) {
            thumb.setImageBitmap(cached);
            return;
        }
        thumb.setImageBitmap(buildPlaceholderThumb(reqW, reqH));
        THUMB_EXEC.execute(() -> {
            Bitmap loaded = getSceneThumb(assetPath, reqW, reqH);
            if (loaded != null) {
                runOnUiThread(() -> {
                    if (!isFinishing()) thumb.setImageBitmap(loaded);
                });
            }
        });
    }

    private void bindCustomThumbAsync(final ImageView thumb, final String cacheKey, final int reqW, final int reqH,
                                      final java.util.concurrent.Callable<Bitmap> loader) {
        Bitmap cached = SCENE_THUMB_CACHE.get(cacheKey);
        if (cached != null && !cached.isRecycled()) {
            thumb.setImageBitmap(cached);
            return;
        }
        thumb.setImageBitmap(buildPlaceholderThumb(reqW, reqH));
        THUMB_EXEC.execute(() -> {
            try {
                Bitmap loaded = loader.call();
                if (loaded != null) {
                    SCENE_THUMB_CACHE.put(cacheKey, loaded);
                    runOnUiThread(() -> {
                        if (!isFinishing()) thumb.setImageBitmap(loaded);
                    });
                }
            } catch (Throwable ignored) {
            }
        });
    }

    /** Sub-heading (.shead in couch-tv). */
    void section(String name) {
        TextView t = new TextView(this);
        t.setText(name.toUpperCase(I18n.locale(this)));
        t.setTextColor(0x80FFFFFF);
        t.setTypeface(Ui.font(this, "semibold"));
        t.setLetterSpacing(0.06f);
        Ui.textSize(t, 10.5f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = u(14);
        lp.topMargin = list.getChildCount() == 0 ? u(2) : u(12);
        lp.bottomMargin = u(4);
        list.addView(t, lp);
    }

    /** Choice row (.srow.k-choice in couch-tv): bold title, optional subtitle, and stroke checkmark on the right. */
    private void choiceRow(String titleText, String subText, boolean checked, Runnable onClick) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setPadding(u(16), u(9), u(16), u(9));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF); // rgba(255, 255, 255, 0.08)
        r.setBackground(bg);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(this);
        t.setText(titleText);
        t.setTextColor(0xEBF4F4F6);
        t.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(t, 14.5f);
        texts.addView(t);

        TextView s = null;
        if (subText != null && !subText.isEmpty()) {
            s = new TextView(this);
            s.setText(subText);
            s.setTextColor(0x99FFFFFF);
            s.setTypeface(Ui.font(this, "regular"));
            Ui.textSize(s, 11f);
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            slp.topMargin = u(1);
            texts.addView(s, slp);
        }
        r.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        int cs = u(17);
        if (cachedCheckWhite == null) {
            cachedCheckWhite = Glyphs.get("check", cs, Color.WHITE);
        }
        if (cachedCheckDark == null) {
            cachedCheckDark = Glyphs.get("check", cs, 0xFF111114);
        }
        ImageView mark = new ImageView(this);
        if (checked) {
            mark.setImageBitmap(cachedCheckWhite);
        }
        r.addView(mark, new LinearLayout.LayoutParams(cs, cs));

        final TextView sRef = s;
        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            t.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            if (sRef != null) sRef.setTextColor(has ? 0x99000000 : 0x99FFFFFF);
            if (checked) {
                mark.setImageBitmap(has ? cachedCheckDark : cachedCheckWhite);
            }
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });
        r.setOnClickListener(view -> onClick.run());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }

    /**
     * Scene row (.srow.k-check in couch-tv):
     * 16:9 rounded thumbnail (.sthumb) + scene name + green circular check badge (.sbox.on).
     * Long-pressing OK lifts the row in-place so Up/Down reorders scenes directly.
     */
    private void sceneCheckRow(Scenes.Spec spec, int sceneIdx, List<String> order) {
        final boolean isLifted = spec.id.equals(liftedSceneId);

        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setLongClickable(true);
        r.setTag("scene:" + spec.id);
        r.setPadding(u(14), u(6), u(16), u(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        // 16:9 Scene Thumbnail (.sthumb: 5.5rem x 3.125rem -> 46dp x 26dp)
        int tw = u(46), th = u(26);
        ImageView thumb = new ImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bindAssetThumbAsync(thumb, spec.bgAsset, tw * 2, th * 2);
        final float trad = u(5.5f);
        thumb.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), trad);
            }
        });
        thumb.setClipToOutline(true);
        LinearLayout.LayoutParams thlp = new LinearLayout.LayoutParams(tw, th);
        thlp.rightMargin = u(12);
        r.addView(thumb, thlp);

        TextView l = new TextView(this);
        l.setText(spec.title(this));
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        int cs = u(20);
        ImageView check = new ImageView(this);
        Runnable updateCheck = () -> {
            if (spec.id.equals(liftedSceneId)) {
                check.setImageBitmap(Glyphs.get("updown", cs, r.isFocused() ? 0xFF111114 : Color.WHITE));
            } else {
                boolean on = Scenes.isSceneEnabled(this, spec.id);
                if (on) {
                    check.setImageBitmap(Glyphs.get("check_on", cs, Color.WHITE));
                } else {
                    check.setImageBitmap(Glyphs.get(r.isFocused() ? "check_off_dark" : "check_off", cs, Color.WHITE));
                }
            }
        };
        updateCheck.run();
        refreshers.add(updateCheck);
        r.addView(check, new LinearLayout.LayoutParams(cs, cs));

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(true);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            updateCheck.run();
            float scale = has ? (isLifted ? 1.04f : 1.02f) : 1f;
            float tx = has && isLifted ? u(6) : 0f;
            view.animate().scaleX(scale).scaleY(scale).translationX(tx)
                    .setDuration(160).start();
        });

        r.setOnClickListener(view -> {
            if (liftedSceneId != null) {
                liftedSceneId = null;
                updateHintText(true);
                refreshAll();
                view.animate().scaleX(1.02f).scaleY(1.02f).translationX(0f).setDuration(150).start();
                return;
            }
            if (!Scenes.toggleScene(this, spec.id)) {
                showStatusMsg(tr("One scene has to stay on.", "En az bir sahne açık kalmalıdır."));
            } else {
                showStatusMsg(null);
            }
            refreshAll();
        });

        r.setOnLongClickListener(view -> {
            if (liftedSceneId != null) {
                liftedSceneId = null;
            } else {
                liftedSceneId = spec.id;
            }
            updateHintText(true);
            updateCheck.run();
            boolean nowLifted = spec.id.equals(liftedSceneId);
            view.animate().scaleX(nowLifted ? 1.04f : 1.02f)
                    .scaleY(nowLifted ? 1.04f : 1.02f)
                    .translationX(nowLifted ? u(6) : 0f)
                    .setDuration(160).start();
            return true;
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(40));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }

    private Bitmap buildPlaceholderThumb(int w, int h) {
        Bitmap cached = sPlaceholderThumb;
        if (cached != null && !cached.isRecycled() && cached.getWidth() == w && cached.getHeight() == h) {
            return cached;
        }
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setShader(new android.graphics.LinearGradient(
                0, 0, w, h,
                0xFF282B3A, 0xFF151722,
                android.graphics.Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        int gsz = Math.min(w, h) * 3 / 5;
        Bitmap glyph = Glyphs.get("image", gsz, 0xCCFFFFFF);
        if (glyph != null) {
            c.drawBitmap(glyph, (w - gsz) / 2f, (h - gsz) / 2f, null);
        }
        sPlaceholderThumb = bmp;
        return bmp;
    }

    private String customSceneTitle(int slot) {
        if (slot <= 1) return tr("Custom", "Özel");
        return tr("Custom " + slot, "Özel " + slot);
    }

    private void pickCustomSceneWallpaper(int slot) {
        CustomMedia.showImageFilePicker(
                this,
                tr("Choose custom scene image", "Özel sahne görseli seçin") + (slot > 1 ? " (" + slot + ")" : ""),
                false,
                1920,
                1080,
                null,
                null,
                bmp -> {
                    if (CustomMedia.setCustomSceneWallpaper(this, slot, bmp)) {
                        SCENE_THUMB_CACHE.remove("custom_scene:" + slot);
                        sCachedBackdropKey = null;
                        Scenes.enableCustomScene(this, slot, true);
                        rebuildMainRows();
                        showStatusMsg(tr("Custom scene wallpaper updated.", "Özel sahne duvar kağıdı güncellendi."));
                    } else {
                        Toast.makeText(
                                this,
                                tr("Could not load selected image.", "Seçilen görsel yüklenemedi."),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void customSceneRow(int slot) {
        final String cid = Scenes.customId(slot);
        final String labelText = customSceneTitle(slot);

        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setLongClickable(true);
        r.setTag("scene:" + cid);
        r.setPadding(u(14), u(6), u(16), u(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        int tw = u(46), th = u(26);
        ImageView thumb = new ImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (CustomMedia.hasCustomSceneWallpaper(this, slot)) {
            bindCustomThumbAsync(thumb, "custom_scene:" + slot, tw * 2, th * 2,
                    () -> CustomMedia.getCustomSceneWallpaper(this, slot, tw * 2, th * 2));
        } else {
            thumb.setImageBitmap(buildPlaceholderThumb(tw * 2, th * 2));
        }
        final float trad = u(5.5f);
        thumb.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), trad);
            }
        });
        thumb.setClipToOutline(true);
        LinearLayout.LayoutParams thlp = new LinearLayout.LayoutParams(tw, th);
        thlp.rightMargin = u(12);
        r.addView(thumb, thlp);

        TextView l = new TextView(this);
        l.setText(labelText);
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        int cs = u(20);
        ImageView check = new ImageView(this);
        Runnable updateCheck = () -> {
            boolean on = Scenes.isSceneEnabled(this, cid);
            if (on) {
                check.setImageBitmap(Glyphs.get("check_on", cs, Color.WHITE));
            } else {
                check.setImageBitmap(Glyphs.get(r.isFocused() ? "check_off_dark" : "check_off", cs, Color.WHITE));
            }
        };
        updateCheck.run();
        refreshers.add(updateCheck);
        r.addView(check, new LinearLayout.LayoutParams(cs, cs));

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            updateCheck.run();
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });

        r.setOnClickListener(view -> {
            if (!CustomMedia.hasCustomSceneWallpaper(this, slot)) {
                pickCustomSceneWallpaper(slot);
                return;
            }
            boolean enabled = Scenes.isSceneEnabled(this, cid);
            if (!enabled) {
                String[] opts = {
                    tr("Turn on Custom scene", "Özel sahneyi aç"),
                    tr("Choose image…", "Görsel seç…")
                };
                dialog(labelText)
                        .setItems(opts, (d, which) -> {
                            if (which == 0) {
                                Scenes.enableCustomScene(this, slot, true);
                                rebuildMainRows();
                            } else {
                                pickCustomSceneWallpaper(slot);
                            }
                        })
                        .show();
            } else {
                String[] opts = {
                    tr("Choose image…", "Görsel seç…"),
                    tr("Turn off Custom scene", "Özel sahneyi kapat")
                };
                dialog(labelText)
                        .setItems(opts, (d, which) -> {
                            if (which == 0) {
                                pickCustomSceneWallpaper(slot);
                            } else {
                                if (!Scenes.toggleScene(this, cid)) {
                                    showStatusMsg(tr("One scene has to stay on.", "En az bir sahne açık kalmalıdır."));
                                } else {
                                    rebuildMainRows();
                                }
                            }
                        })
                        .show();
            }
        });

        r.setOnLongClickListener(view -> {
            pickCustomSceneWallpaper(slot);
            return true;
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(40));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }

    private void pickCustomClassicWallpaper() {
        CustomMedia.showImageFilePicker(
                this,
                tr("Choose custom background image", "Özel arka plan görseli seçin"),
                false,
                1920,
                1080,
                null,
                null,
                bmp -> {
                    if (CustomMedia.setCustomClassicWallpaper(this, bmp)) {
                        SCENE_THUMB_CACHE.remove("custom_classic");
                        Profiles.homePrefs(this).edit().putString("wallpaper", MainActivity.CUSTOM_WALL_KEY).apply();
                        rebuildMainRows();
                        showStatusMsg(tr("Custom background updated.", "Özel arka plan güncellendi."));
                    } else {
                        Toast.makeText(
                                this,
                                tr("Could not load selected image.", "Seçilen görsel yüklenemedi."),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void customClassicPhotoRow(boolean checked) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setLongClickable(true);
        r.setPadding(u(14), u(6), u(16), u(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        int tw = u(46), th = u(26);
        ImageView thumb = new ImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (CustomMedia.hasCustomClassicWallpaper(this)) {
            bindCustomThumbAsync(thumb, "custom_classic", tw * 2, th * 2, () -> {
                Bitmap full = CustomMedia.getCustomClassicWallpaper(this);
                if (full == null) return null;
                Bitmap scaled = Bitmap.createScaledBitmap(full, tw * 2, th * 2, true);
                if (scaled != full) full.recycle();
                return scaled;
            });
        } else {
            thumb.setImageBitmap(buildPlaceholderThumb(tw * 2, th * 2));
        }
        final float trad = u(5.5f);
        thumb.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), trad);
            }
        });
        thumb.setClipToOutline(true);
        LinearLayout.LayoutParams thlp = new LinearLayout.LayoutParams(tw, th);
        thlp.rightMargin = u(12);
        r.addView(thumb, thlp);

        TextView l = new TextView(this);
        l.setText(tr("Custom", "Özel"));
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        int cs = u(17);
        ImageView mark = new ImageView(this);
        if (checked) {
            mark.setImageBitmap(Glyphs.get("check", cs, Color.WHITE));
        }
        r.addView(mark, new LinearLayout.LayoutParams(cs, cs));

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            if (checked) {
                mark.setImageBitmap(Glyphs.get("check", cs, has ? 0xFF111114 : Color.WHITE));
            }
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });

        r.setOnClickListener(view -> {
            if (!CustomMedia.hasCustomClassicWallpaper(this)) {
                pickCustomClassicWallpaper();
                return;
            }
            if (!checked) {
                String[] opts = {
                    tr("Use custom background", "Özel arka planı kullan"),
                    tr("Choose image…", "Görsel seç…")
                };
                dialog(tr("Custom", "Özel"))
                        .setItems(opts, (d, which) -> {
                            if (which == 0) {
                                Profiles.homePrefs(this).edit().putString("wallpaper", MainActivity.CUSTOM_WALL_KEY).apply();
                                rebuildMainRows();
                            } else {
                                pickCustomClassicWallpaper();
                            }
                        })
                        .show();
            } else {
                pickCustomClassicWallpaper();
            }
        });

        r.setOnLongClickListener(view -> {
            pickCustomClassicWallpaper();
            return true;
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(40));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }

    private void pickFolderForSceneShuffle() {
        CustomMedia.showFolderPicker(
                this,
                CustomMedia.SCOPE_SCENE,
                tr("Choose wallpaper folder to shuffle", "Karıştırılacak duvar kağıdı klasörünü seçin"),
                dir -> {
                    SCENE_THUMB_CACHE.remove("folder_scene");
                    Scenes.enableFolderShuffleScene(this, true);
                    rebuildMainRows();
                    showStatusMsg(tr("Folder shuffle enabled: " + dir.getName(),
                            "Klasör karıştırma etkinleştirildi: " + dir.getName()));
                });
    }

    private void folderShuffleSceneRow() {
        final String fid = Scenes.FOLDER_SHUFFLE_ID;
        boolean hasFolder = CustomMedia.hasWallpaperShuffleFolder(this, CustomMedia.SCOPE_SCENE);
        final String baseTitle = tr("Folder shuffle", "Klasör karıştırma");

        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setLongClickable(true);
        r.setTag("scene:" + fid);
        r.setPadding(u(14), u(6), u(16), u(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        int tw = u(46), th = u(26);
        ImageView thumb = new ImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (hasFolder) {
            bindCustomThumbAsync(thumb, "folder_scene", tw * 2, th * 2,
                    () -> CustomMedia.getNextShuffledFolderBitmap(this, CustomMedia.SCOPE_SCENE, tw * 2, th * 2, false));
        } else {
            thumb.setImageBitmap(buildPlaceholderThumb(tw * 2, th * 2));
        }
        final float trad = u(5.5f);
        thumb.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), trad);
            }
        });
        thumb.setClipToOutline(true);
        LinearLayout.LayoutParams thlp = new LinearLayout.LayoutParams(tw, th);
        thlp.rightMargin = u(12);
        r.addView(thumb, thlp);

        TextView l = new TextView(this);
        l.setText(baseTitle);
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        l.setEllipsize(TextUtils.TruncateAt.END);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (hasFolder) {
            THUMB_EXEC.execute(() -> {
                String summary = CustomMedia.getWallpaperShuffleFolderSummary(this, CustomMedia.SCOPE_SCENE);
                if (!summary.isEmpty()) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) l.setText(baseTitle + "  ·  " + summary);
                    });
                }
            });
        }

        int cs = u(20);
        ImageView check = new ImageView(this);
        Runnable updateCheck = () -> {
            boolean on = Scenes.isSceneEnabled(this, fid);
            if (on) {
                check.setImageBitmap(Glyphs.get("check_on", cs, Color.WHITE));
            } else {
                check.setImageBitmap(Glyphs.get(r.isFocused() ? "check_off_dark" : "check_off", cs, Color.WHITE));
            }
        };
        updateCheck.run();
        refreshers.add(updateCheck);
        r.addView(check, new LinearLayout.LayoutParams(cs, cs));

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            updateCheck.run();
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });

        r.setOnClickListener(view -> {
            if (!CustomMedia.hasWallpaperShuffleFolder(this, CustomMedia.SCOPE_SCENE)) {
                pickFolderForSceneShuffle();
                return;
            }
            boolean enabled = Scenes.isSceneEnabled(this, fid);
            if (!enabled) {
                String[] opts = {
                    tr("Turn on Folder shuffle", "Klasör karıştırmayı aç"),
                    tr("Choose folder…", "Klasör seç…")
                };
                dialog(tr("Folder shuffle", "Klasör karıştırma"))
                        .setItems(opts, (d, which) -> {
                            if (which == 0) {
                                Scenes.enableFolderShuffleScene(this, true);
                                rebuildMainRows();
                            } else {
                                pickFolderForSceneShuffle();
                            }
                        })
                        .show();
            } else {
                String[] opts = {
                    tr("Choose folder…", "Klasör seç…"),
                    tr("Turn off Folder shuffle", "Klasör karıştırmayı kapat")
                };
                dialog(tr("Folder shuffle", "Klasör karıştırma"))
                        .setItems(opts, (d, which) -> {
                            if (which == 0) {
                                pickFolderForSceneShuffle();
                            } else {
                                if (!Scenes.toggleScene(this, fid)) {
                                    showStatusMsg(tr("One scene has to stay on.", "En az bir sahne açık kalmalıdır."));
                                } else {
                                    rebuildMainRows();
                                }
                            }
                        })
                        .show();
            }
        });

        r.setOnLongClickListener(view -> {
            pickFolderForSceneShuffle();
            return true;
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(40));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }

    private void pickFolderForClassicShuffle() {
        CustomMedia.showFolderPicker(
                this,
                CustomMedia.SCOPE_CLASSIC,
                tr("Choose background folder to shuffle", "Karıştırılacak arka plan klasörünü seçin"),
                dir -> {
                    SCENE_THUMB_CACHE.remove("folder_classic");
                    Profiles.homePrefs(this).edit().putString("wallpaper", MainActivity.FOLDER_SHUFFLE_WALL_KEY).apply();
                    rebuildMainRows();
                    showStatusMsg(tr("Folder shuffle enabled: " + dir.getName(),
                            "Klasör karıştırma etkinleştirildi: " + dir.getName()));
                });
    }

    private void folderShuffleClassicRow(boolean checked) {
        boolean hasFolder = CustomMedia.hasWallpaperShuffleFolder(this, CustomMedia.SCOPE_CLASSIC);
        final String baseTitle = tr("Folder shuffle", "Klasör karıştırma");

        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setLongClickable(true);
        r.setPadding(u(14), u(6), u(16), u(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        int tw = u(46), th = u(26);
        ImageView thumb = new ImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (hasFolder) {
            bindCustomThumbAsync(thumb, "folder_classic", tw * 2, th * 2,
                    () -> CustomMedia.getNextShuffledFolderBitmap(this, CustomMedia.SCOPE_CLASSIC, tw * 2, th * 2, false));
        } else {
            thumb.setImageBitmap(buildPlaceholderThumb(tw * 2, th * 2));
        }
        final float trad = u(5.5f);
        thumb.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), trad);
            }
        });
        thumb.setClipToOutline(true);
        LinearLayout.LayoutParams thlp = new LinearLayout.LayoutParams(tw, th);
        thlp.rightMargin = u(12);
        r.addView(thumb, thlp);

        TextView l = new TextView(this);
        l.setText(baseTitle);
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        l.setEllipsize(TextUtils.TruncateAt.END);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (hasFolder) {
            THUMB_EXEC.execute(() -> {
                String summary = CustomMedia.getWallpaperShuffleFolderSummary(this, CustomMedia.SCOPE_CLASSIC);
                if (!summary.isEmpty()) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) l.setText(baseTitle + "  ·  " + summary);
                    });
                }
            });
        }

        int cs = u(17);
        ImageView mark = new ImageView(this);
        if (checked) {
            mark.setImageBitmap(Glyphs.get("check", cs, Color.WHITE));
        }
        r.addView(mark, new LinearLayout.LayoutParams(cs, cs));

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            if (checked) {
                mark.setImageBitmap(Glyphs.get("check", cs, has ? 0xFF111114 : Color.WHITE));
            }
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });

        r.setOnClickListener(view -> {
            if (!CustomMedia.hasWallpaperShuffleFolder(this, CustomMedia.SCOPE_CLASSIC)) {
                pickFolderForClassicShuffle();
                return;
            }
            if (!checked) {
                String[] opts = {
                    tr("Use folder shuffle background", "Klasör karıştırma arka planını kullan"),
                    tr("Choose folder…", "Klasör seç…")
                };
                dialog(tr("Folder shuffle", "Klasör karıştırma"))
                        .setItems(opts, (d, which) -> {
                            if (which == 0) {
                                Profiles.homePrefs(this).edit().putString("wallpaper", MainActivity.FOLDER_SHUFFLE_WALL_KEY).apply();
                                rebuildMainRows();
                            } else {
                                pickFolderForClassicShuffle();
                            }
                        })
                        .show();
            } else {
                pickFolderForClassicShuffle();
            }
        });

        r.setOnLongClickListener(view -> {
            pickFolderForClassicShuffle();
            return true;
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(40));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }

    private void subMenuLockScreenSettings() {
        mainHeadView.setText(tr("Lock screen settings", "Kilit ekranı ayarları"));
        backSubMenuRow();

        section(tr("LOCK SCREEN OPTIONS", "KİLİT EKRANI SEÇENEKLERİ"));
        row(tr("Clock position", "Saat konumu"),
                () -> Scenes.lockClockPosLabel(this),
                () -> {
                    Scenes.cycleLockClockPos(this);
                    refreshAll();
                });
        row(tr("Auto-launch Lock Screen when idle", "Boşta kaldığında Kilit Ekranını otomatik başlat"),
                () -> Scenes.lockIdleLabel(this),
                () -> {
                    Scenes.cycleLockIdleMinutes(this);
                    refreshAll();
                });
        row(tr("Each wallpaper stays", "Her duvar kağıdı süresi"),
                () -> Scenes.getStaySeconds(this) + tr(" seconds", " saniye"),
                () -> {
                    Scenes.cycleStaySeconds(this);
                    refreshAll();
                });
        row(tr("Preview Lock Screen now", "Kilit Ekranını şimdi önizle"),
                () -> "›",
                () -> MainActivity.triggerLockScreen(this));

        section(tr("NIGHT SCENES", "GECE SAHNELERİ"));
        for (Scenes.Spec spec : Scenes.ALL) {
            lockSceneCheckRow(spec.id, spec.title(this), spec.bgAsset);
        }

        section(tr("CLASSIC WALLPAPERS", "KLASİK DUVAR KAĞITLARI"));
        for (int i = 0; i < Scenes.CLASSIC_PHOTOS.length; i++) {
            String photoFile = Scenes.CLASSIC_PHOTOS[i][0];
            String id = "photo:" + photoFile;
            String label = I18n.s(this, Scenes.CLASSIC_PHOTOS[i][1], Scenes.CLASSIC_PHOTOS[i][2]);
            lockSceneCheckRow(id, label, "backgrounds/" + photoFile);
        }
        String[] gradIds = {"aurora", "midnight", "sunset", "ocean", "graphite"};
        for (String gid : gradIds) {
            Scenes.Spec gspec = Scenes.byId(gid);
            lockSceneCheckRow(gid, gspec.title(this), null);
        }

        section(tr("CUSTOM & FOLDER SHUFFLE", "ÖZEL GÖRSEL VE KLASÖR KARIŞTIRMA"));
        lockCustomImageRow();
        lockFolderShuffleRow();

        infoNote(tr("On the Lock Screen, only Clock, Date, and Greeting are shown above your selected backgrounds. Pressing any remote button returns to where you were before.",
                "Kilit Ekranında yalnızca seçilen arka planların üzerinde Saat, Tarih ve Karşılama gösterilir. Kumandadaki herhangi bir tuşa basmak kaldığınız yere geri döner."));
    }

    private void lockSceneCheckRow(String id, String titleText, String assetPath) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setPadding(u(14), u(6), u(16), u(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        int tw = u(46), th = u(26);
        ImageView thumb = new ImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (assetPath != null && !assetPath.isEmpty()) {
            bindAssetThumbAsync(thumb, assetPath, tw * 2, th * 2);
        } else {
            Bitmap tbmp = MainActivity.makeWallpaperBase(id);
            if (tbmp != null) thumb.setImageBitmap(tbmp);
        }
        final float trad = u(5.5f);
        thumb.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), trad);
            }
        });
        thumb.setClipToOutline(true);
        LinearLayout.LayoutParams thlp = new LinearLayout.LayoutParams(tw, th);
        thlp.rightMargin = u(12);
        r.addView(thumb, thlp);

        TextView l = new TextView(this);
        l.setText(titleText);
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        int cs = u(20);
        ImageView check = new ImageView(this);
        Runnable updateCheck = () -> {
            boolean on = Scenes.isLockSceneEnabled(this, id);
            if (on) {
                check.setImageBitmap(Glyphs.get("check_on", cs, Color.WHITE));
            } else {
                check.setImageBitmap(Glyphs.get(r.isFocused() ? "check_off_dark" : "check_off", cs, Color.WHITE));
            }
        };
        updateCheck.run();
        refreshers.add(updateCheck);
        r.addView(check, new LinearLayout.LayoutParams(cs, cs));

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            updateCheck.run();
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });

        r.setOnClickListener(view -> {
            if (!Scenes.toggleLockScene(this, id)) {
                showStatusMsg(tr("One wallpaper has to stay on.", "En az bir duvar kağıdı açık kalmalıdır."));
            } else {
                showStatusMsg(null);
            }
            refreshAll();
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(40));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }

    private void pickCustomLockWallpaper() {
        CustomMedia.showImageFilePicker(
                this,
                tr("Choose Lock Screen image", "Kilit Ekranı görseli seçin"),
                false,
                1920,
                1080,
                null,
                null,
                bmp -> {
                    if (CustomMedia.setCustomLockWallpaper(this, bmp)) {
                        SCENE_THUMB_CACHE.remove("custom_lock");
                        Scenes.enableLockScene(this, Scenes.LOCK_CUSTOM_ID, true);
                        rebuildMainRows();
                        showStatusMsg(tr("Custom Lock Screen image updated.", "Özel Kilit Ekranı görseli güncellendi."));
                    }
                });
    }

    private void lockCustomImageRow() {
        final String cid = Scenes.LOCK_CUSTOM_ID;
        boolean hasCustom = CustomMedia.hasCustomLockWallpaper(this);
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setLongClickable(true);
        r.setPadding(u(14), u(6), u(16), u(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        int tw = u(46), th = u(26);
        ImageView thumb = new ImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (hasCustom) {
            bindCustomThumbAsync(thumb, "custom_lock", tw * 2, th * 2,
                    () -> CustomMedia.getCustomLockWallpaper(this, tw * 2, th * 2));
        } else {
            thumb.setImageBitmap(buildPlaceholderThumb(tw * 2, th * 2));
        }
        final float trad = u(5.5f);
        thumb.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), trad);
            }
        });
        thumb.setClipToOutline(true);
        LinearLayout.LayoutParams thlp = new LinearLayout.LayoutParams(tw, th);
        thlp.rightMargin = u(12);
        r.addView(thumb, thlp);

        TextView l = new TextView(this);
        l.setText(tr("Custom image", "Özel görsel"));
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        int cs = u(20);
        ImageView check = new ImageView(this);
        Runnable updateCheck = () -> {
            boolean on = Scenes.isLockSceneEnabled(this, cid);
            if (on) {
                check.setImageBitmap(Glyphs.get("check_on", cs, Color.WHITE));
            } else {
                check.setImageBitmap(Glyphs.get(r.isFocused() ? "check_off_dark" : "check_off", cs, Color.WHITE));
            }
        };
        updateCheck.run();
        refreshers.add(updateCheck);
        r.addView(check, new LinearLayout.LayoutParams(cs, cs));

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            updateCheck.run();
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });

        r.setOnClickListener(view -> {
            if (!CustomMedia.hasCustomLockWallpaper(this)) {
                pickCustomLockWallpaper();
                return;
            }
            boolean enabled = Scenes.isLockSceneEnabled(this, cid);
            String[] opts = enabled
                    ? new String[]{tr("Choose image…", "Görsel seç…"), tr("Turn off Custom image", "Özel görseli kapat")}
                    : new String[]{tr("Turn on Custom image", "Özel görseli aç"), tr("Choose image…", "Görsel seç…")};
            dialog(tr("Custom image", "Özel görsel"))
                    .setItems(opts, (d, which) -> {
                        if (enabled) {
                            if (which == 0) pickCustomLockWallpaper();
                            else {
                                Scenes.toggleLockScene(this, cid);
                                rebuildMainRows();
                            }
                        } else {
                            if (which == 0) {
                                Scenes.enableLockScene(this, cid, true);
                                rebuildMainRows();
                            } else {
                                pickCustomLockWallpaper();
                            }
                        }
                    })
                    .show();
        });
        r.setOnLongClickListener(view -> {
            pickCustomLockWallpaper();
            return true;
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(40));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }

    private void pickLockFolderShuffle() {
        CustomMedia.showFolderPicker(
                this,
                CustomMedia.SCOPE_LOCK,
                tr("Choose Lock Screen folder to shuffle", "Karıştırılacak Kilit Ekranı klasörünü seçin"),
                dir -> {
                    SCENE_THUMB_CACHE.remove("folder_lock");
                    Scenes.enableLockScene(this, Scenes.LOCK_FOLDER_SHUFFLE_ID, true);
                    rebuildMainRows();
                    showStatusMsg(tr("Lock Screen folder shuffle enabled: " + dir.getName(),
                            "Kilit Ekranı klasör karıştırma etkinleştirildi: " + dir.getName()));
                });
    }

    private void lockFolderShuffleRow() {
        final String fid = Scenes.LOCK_FOLDER_SHUFFLE_ID;
        boolean hasFolder = CustomMedia.hasWallpaperShuffleFolder(this, CustomMedia.SCOPE_LOCK);
        final String baseTitle = tr("Folder shuffle", "Klasör karıştırma");

        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setLongClickable(true);
        r.setPadding(u(14), u(6), u(16), u(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        int tw = u(46), th = u(26);
        ImageView thumb = new ImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (hasFolder) {
            bindCustomThumbAsync(thumb, "folder_lock", tw * 2, th * 2,
                    () -> CustomMedia.getNextShuffledFolderBitmap(this, CustomMedia.SCOPE_LOCK, tw * 2, th * 2, false));
        } else {
            thumb.setImageBitmap(buildPlaceholderThumb(tw * 2, th * 2));
        }
        final float trad = u(5.5f);
        thumb.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), trad);
            }
        });
        thumb.setClipToOutline(true);
        LinearLayout.LayoutParams thlp = new LinearLayout.LayoutParams(tw, th);
        thlp.rightMargin = u(12);
        r.addView(thumb, thlp);

        TextView l = new TextView(this);
        l.setText(baseTitle);
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        l.setEllipsize(TextUtils.TruncateAt.END);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (hasFolder) {
            THUMB_EXEC.execute(() -> {
                String summary = CustomMedia.getWallpaperShuffleFolderSummary(this, CustomMedia.SCOPE_LOCK);
                if (!summary.isEmpty()) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) l.setText(baseTitle + "  ·  " + summary);
                    });
                }
            });
        }

        int cs = u(20);
        ImageView check = new ImageView(this);
        Runnable updateCheck = () -> {
            boolean on = Scenes.isLockSceneEnabled(this, fid);
            if (on) {
                check.setImageBitmap(Glyphs.get("check_on", cs, Color.WHITE));
            } else {
                check.setImageBitmap(Glyphs.get(r.isFocused() ? "check_off_dark" : "check_off", cs, Color.WHITE));
            }
        };
        updateCheck.run();
        refreshers.add(updateCheck);
        r.addView(check, new LinearLayout.LayoutParams(cs, cs));

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            updateCheck.run();
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });

        r.setOnClickListener(view -> {
            if (!CustomMedia.hasWallpaperShuffleFolder(this, CustomMedia.SCOPE_LOCK)) {
                pickLockFolderShuffle();
                return;
            }
            boolean enabled = Scenes.isLockSceneEnabled(this, fid);
            String[] opts = enabled
                    ? new String[]{tr("Choose folder…", "Klasör seç…"), tr("Turn off Folder shuffle", "Klasör karıştırmayı kapat")}
                    : new String[]{tr("Turn on Folder shuffle", "Klasör karıştırmayı aç"), tr("Choose folder…", "Klasör seç…")};
            dialog(tr("Folder shuffle", "Klasör karıştırma"))
                    .setItems(opts, (d, which) -> {
                        if (enabled) {
                            if (which == 0) pickLockFolderShuffle();
                            else {
                                Scenes.toggleLockScene(this, fid);
                                rebuildMainRows();
                            }
                        } else {
                            if (which == 0) {
                                Scenes.enableLockScene(this, fid, true);
                                rebuildMainRows();
                            } else {
                                pickLockFolderShuffle();
                            }
                        }
                    })
                    .show();
        });
        r.setOnLongClickListener(view -> {
            pickLockFolderShuffle();
            return true;
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(40));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }

    private void classicPhotoRow(String label, String assetPath, boolean checked, Runnable onClick) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setPadding(u(14), u(6), u(16), u(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        int tw = u(46), th = u(26);
        ImageView thumb = new ImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bindAssetThumbAsync(thumb, assetPath, tw * 2, th * 2);
        final float trad = u(5.5f);
        thumb.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), trad);
            }
        });
        thumb.setClipToOutline(true);
        LinearLayout.LayoutParams thlp = new LinearLayout.LayoutParams(tw, th);
        thlp.rightMargin = u(12);
        r.addView(thumb, thlp);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        int cs = u(17);
        ImageView mark = new ImageView(this);
        if (checked) {
            mark.setImageBitmap(Glyphs.get("check", cs, Color.WHITE));
        }
        r.addView(mark, new LinearLayout.LayoutParams(cs, cs));

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            if (checked) {
                mark.setImageBitmap(Glyphs.get("check", cs, has ? 0xFF111114 : Color.WHITE));
            }
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });
        r.setOnClickListener(view -> onClick.run());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(40));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
    }

    interface Value {
        String get();
    }

    /** Standard value/cycle row (.srow in couch-tv): bold label on left, muted value on right. */
    View row(String label, Value value, Runnable action) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setPadding(u(16), 0, u(16), 0);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        l.setEllipsize(TextUtils.TruncateAt.END);
        r.addView(l, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView v = new TextView(this);
        v.setTextColor(0x9EFFFFFF);
        v.setTypeface(Ui.font(this, "medium"));
        Ui.textSize(v, 14f);
        v.setSingleLine(true);
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vlp.leftMargin = u(10);
        r.addView(v, vlp);

        Runnable refresh = () -> {
            try {
                v.setText(value.get());
            } catch (Throwable ignored) {
                v.setText("");
            }
        };
        refresh.run();
        refreshers.add(refresh);

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            v.setTextColor(has ? 0x99000000 : 0x9EFFFFFF);
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });
        r.setOnClickListener(view -> {
            action.run();
            for (Runnable x : refreshers) x.run();
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, u(39));
        lp.bottomMargin = u(5);
        list.addView(r, lp);
        return r;
    }

    /** Double-line row: 1st line is bold title (e.g. key name), 2nd line is muted subtitle (e.g. remapped functions). */
    View twoLineRow(String label, Value subValue, Runnable action) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setFocusable(true);
        r.setClickable(true);
        r.setPadding(u(16), u(9), u(16), u(9));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(u(13));
        bg.setColor(0x14FFFFFF);
        r.setBackground(bg);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(0xEBF4F4F6);
        l.setTypeface(Ui.font(this, "semibold"));
        Ui.textSize(l, 14.5f);
        l.setSingleLine(true);
        l.setEllipsize(TextUtils.TruncateAt.END);
        texts.addView(l);

        TextView v = new TextView(this);
        v.setTextColor(0x9EFFFFFF);
        v.setTypeface(Ui.font(this, "regular"));
        Ui.textSize(v, 11.5f);
        v.setSingleLine(true);
        v.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vlp.topMargin = u(2);
        texts.addView(v, vlp);

        r.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView chev = new TextView(this);
        chev.setText("›");
        chev.setTextColor(0x9EFFFFFF);
        chev.setTypeface(Ui.font(this, "medium"));
        Ui.textSize(chev, 15f);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.leftMargin = u(10);
        r.addView(chev, clp);

        Runnable refresh = () -> {
            try {
                v.setText(subValue.get());
            } catch (Throwable ignored) {
                v.setText("");
            }
        };
        refresh.run();
        refreshers.add(refresh);

        r.setOnFocusChangeListener((view, has) -> {
            if (has) {
                inRows = true;
                updateSidebarStyles();
                updateHintText(false);
            }
            bg.setColor(has ? Color.WHITE : 0x14FFFFFF);
            l.setTextColor(has ? 0xFF111114 : 0xEBF4F4F6);
            v.setTextColor(has ? 0x99000000 : 0x9EFFFFFF);
            chev.setTextColor(has ? 0x99000000 : 0x9EFFFFFF);
            view.animate().scaleX(has ? 1.02f : 1f).scaleY(has ? 1.02f : 1f)
                    .setDuration(160).start();
        });
        r.setOnClickListener(view -> {
            action.run();
            for (Runnable x : refreshers) x.run();
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = u(5);
        list.addView(r, lp);
        return r;
    }

    /** Non-focusable info row (.srow.k-info in couch-tv). */
    private void infoRow(String label, String val, String subText) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(u(14), u(5), u(14), u(5));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(0xB3FFFFFF);
        l.setTypeface(Ui.font(this, "medium"));
        Ui.textSize(l, 12.5f);
        texts.addView(l);

        if (subText != null && !subText.isEmpty()) {
            TextView s = new TextView(this);
            s.setText(subText);
            s.setTextColor(0x80FFFFFF);
            s.setTypeface(Ui.font(this, "regular"));
            Ui.textSize(s, 10.5f);
            texts.addView(s);
        }
        r.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        if (val != null && !val.isEmpty()) {
            TextView v = new TextView(this);
            v.setText(val);
            v.setTextColor(0x9EFFFFFF);
            v.setTypeface(Ui.font(this, "medium"));
            Ui.textSize(v, 12.5f);
            r.addView(v);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = u(3);
        list.addView(r, lp);
    }

    private void infoRowDynamic(String label, Value val, String subText) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(u(14), u(5), u(14), u(5));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(0xB3FFFFFF);
        l.setTypeface(Ui.font(this, "medium"));
        Ui.textSize(l, 12.5f);
        texts.addView(l);

        if (subText != null && !subText.isEmpty()) {
            TextView s = new TextView(this);
            s.setText(subText);
            s.setTextColor(0x80FFFFFF);
            s.setTypeface(Ui.font(this, "regular"));
            Ui.textSize(s, 10.5f);
            texts.addView(s);
        }
        r.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView v = new TextView(this);
        v.setTextColor(0x9EFFFFFF);
        v.setTypeface(Ui.font(this, "medium"));
        Ui.textSize(v, 12.5f);
        r.addView(v);

        Runnable refresh = () -> {
            try {
                v.setText(val.get());
            } catch (Throwable ignored) {
                v.setText("");
            }
        };
        refresh.run();
        refreshers.add(refresh);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = u(3);
        list.addView(r, lp);
    }

    private void infoNote(String note) {
        TextView t = new TextView(this);
        t.setText(note);
        t.setTextColor(0x88FFFFFF);
        t.setTypeface(Ui.font(this, "regular"));
        Ui.textSize(t, 11.5f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = u(14);
        lp.rightMargin = u(14);
        lp.topMargin = u(6);
        lp.bottomMargin = u(4);
        list.addView(t, lp);
    }

    // ================================================================ Key Navigation

    @Override
    public boolean dispatchKeyEvent(KeyEvent ev) {
        MainActivity.navSound(this, ev);
        int k = ev.getKeyCode();
        boolean down = ev.getAction() == KeyEvent.ACTION_DOWN;

        // If a scene row is picked up for moving, Up/Down moves it in the scene list
        if (liftedSceneId != null) {
            if (k == KeyEvent.KEYCODE_DPAD_UP || k == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (down) {
                    moveLiftedScene(k == KeyEvent.KEYCODE_DPAD_UP ? -1 : 1);
                }
                return true;
            }
            if (k == KeyEvent.KEYCODE_BACK || k == KeyEvent.KEYCODE_ESCAPE) {
                if (!down) {
                    liftedSceneId = null;
                    rebuildMainRows();
                    focusSceneById(currentSection);
                }
                return true;
            }
        }

        // Left / Back from the right pane returns focus to the active left sidebar item
        if (inRows) {
            if (k == KeyEvent.KEYCODE_DPAD_UP || k == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (down) {
                    int dir = (k == KeyEvent.KEYCODE_DPAD_UP) ? -1 : 1;
                    View cur = getCurrentFocus();
                    View next = findAdjacentFocusable(list, cur, dir);
                    if (next != null) {
                        next.requestFocus();
                    } else if (dir > 0 && scroller != null && list != null) {
                        scroller.smoothScrollTo(0, list.getBottom());
                    } else if (dir < 0 && scroller != null) {
                        scroller.smoothScrollTo(0, 0);
                    }
                }
                return true;
            }
            if (k == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (down) {
                    if (!closeSubMenu()) {
                        View sideRow = sideRowViews.get(currentSection);
                        if (sideRow != null) sideRow.requestFocus();
                    }
                }
                return true;
            }
            if (k == KeyEvent.KEYCODE_BACK || k == KeyEvent.KEYCODE_ESCAPE) {
                if (!down) {
                    if (!closeSubMenu()) {
                        View sideRow = sideRowViews.get(currentSection);
                        if (sideRow != null) sideRow.requestFocus();
                    }
                }
                return true;
            }
        } else {
            if (k == KeyEvent.KEYCODE_DPAD_UP || k == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (down) {
                    int dir = (k == KeyEvent.KEYCODE_DPAD_UP) ? -1 : 1;
                    View cur = getCurrentFocus();
                    View next = findAdjacentFocusable(sideList, cur, dir);
                    if (next != null) {
                        next.requestFocus();
                    }
                }
                return true;
            }
            // In left sidebar: Right moves focus to the first focusable row on the right
            if (k == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (down) focusFirstRow();
                return true;
            }
        }

        return super.dispatchKeyEvent(ev);
    }

    private View findAdjacentFocusable(ViewGroup container, View focused, int step) {
        if (container == null || focused == null) return null;
        int curIdx = -1;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child == focused || isDescendantOf(focused, child)) {
                curIdx = i;
                break;
            }
        }
        if (curIdx < 0) return null;
        for (int i = curIdx + step; i >= 0 && i < container.getChildCount(); i += step) {
            View v = container.getChildAt(i);
            if (v.getVisibility() == View.VISIBLE && v.isFocusable()) {
                return v;
            }
        }
        return null;
    }

    private boolean isDescendantOf(View target, View potentialParent) {
        if (!(potentialParent instanceof ViewGroup)) return false;
        android.view.ViewParent p = target.getParent();
        while (p != null) {
            if (p == potentialParent) return true;
            p = p.getParent();
        }
        return false;
    }

    private void moveLiftedScene(int delta) {
        if (liftedSceneId == null || Scenes.CUSTOM_ID.equals(liftedSceneId)) return;
        List<String> order = new ArrayList<>();
        for (String id : Scenes.getSceneOrder(this)) {
            if (!Scenes.CUSTOM_ID.equals(id)) order.add(id);
        }
        int idx = order.indexOf(liftedSceneId);
        int target = idx + delta;
        if (idx < 0 || target < 0 || target >= order.size()) return;
        String moved = order.remove(idx);
        order.add(target, moved);
        order.add(Scenes.CUSTOM_ID);
        Scenes.setSceneOrder(this, order);
        String keepLifted = liftedSceneId;
        rebuildMainRows();
        liftedSceneId = keepLifted;
        focusSceneById(keepLifted);
    }

    private void focusSceneById(String sceneId) {
        String tag = "scene:" + sceneId;
        for (int i = 0; i < list.getChildCount(); i++) {
            View v = list.getChildAt(i);
            if (tag.equals(v.getTag())) {
                inRows = true;
                v.requestFocus();
                updateSidebarStyles();
                return;
            }
        }
        focusFirstRow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUpdater.resumePendingInstallIfAllowed(this);
        if (BUTTON_MAPPER.equals(currentSection)) {
            rebuildMainRows();
        } else {
            refreshAll();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CustomMedia.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        updateProgressHandler.removeCallbacks(updateProgressTick);
        updateDownloadProgress = null;
        updateDownloadStatus = null;
        ButtonMapper.captureListener = null;
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        refreshAll();
    }

    void refreshAll() {
        for (Runnable x : refreshers) x.run();
        if (updateDownloadProgress != null && AppUpdater.isDownloading()) {
            updateProgressHandler.removeCallbacks(updateProgressTick);
            updateProgressHandler.post(updateProgressTick);
        }
    }

    AlertDialog.Builder dialog(String title) {
        return new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert).setTitle(title);
    }

    String version() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "2.1.2";
        }
    }

    // ================================================================ Dialog Helpers

    void searchCity() {
        android.widget.EditText in = new android.widget.EditText(this);
        in.setSingleLine(true);
        in.setHint(tr("City name, e.g. Istanbul", "Şehir adı, örn. İstanbul"));
        in.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        int pad = u(14);
        FrameLayout box = new FrameLayout(this);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(in);
        AlertDialog dlg = dialog(tr("Choose City", "Şehir Seçin"))
                .setView(box)
                .setPositiveButton(tr("Search", "Ara"), (d, w) -> {
                    String q = in.getText().toString().trim();
                    if (q.isEmpty()) return;
                    showStatusMsg(tr("Searching…", "Aranıyor…"));
                    new Thread(() -> {
                        List<Weather.Place> found;
                        try {
                            found = Weather.search(q);
                        } catch (Exception e) {
                            found = null;
                        }
                        List<Weather.Place> res = found;
                        runOnUiThread(() -> showCities(q, res));
                    }).start();
                })
                .setNegativeButton(tr("Cancel", "İptal"), null)
                .show();
        Ui.setupDialogEditText(in, dlg);
        in.requestFocus();
    }

    void showCities(String q, List<Weather.Place> res) {
        if (isFinishing()) return;
        showStatusMsg(null);
        if (res == null) {
            showStatusMsg(tr("Couldn't search. Check the internet connection.", "Arama yapılamadı. İnternet bağlantısını kontrol edin."));
            return;
        }
        if (res.isEmpty()) {
            showStatusMsg(tr("No place found for \u201c" + q + "\u201d.", "\u201c" + q + "\u201d için konum bulunamadı."));
            return;
        }
        String[] names = new String[res.size()];
        for (int i = 0; i < names.length; i++) {
            Weather.Place pl = res.get(i);
            names[i] = pl.detail.isEmpty() ? pl.name : pl.name + "  ·  " + pl.detail;
        }
        dialog(tr("Matching places", "Eşleşen konumlar")).setItems(names, (d, w) -> {
            Weather.Place picked = res.get(w);
            Weather.setPlace(this, picked);
            rebuildMainRows();
            focusFirstRow();
            showStatusMsg(tr("Location set to " + picked.name + ".", "Konum " + picked.name + " olarak ayarlandı."));
        }).setNegativeButton(tr("Cancel", "İptal"), null).show();
    }


}
