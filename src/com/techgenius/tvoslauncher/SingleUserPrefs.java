package com.techgenius.tvoslauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Canonical single-user preference access. Migration is one-time, idempotent and non-destructive:
 * inactive profile files and legacy metadata are deliberately retained for rollback.
 */
final class SingleUserPrefs {
    private static final String TAG = "SingleUserPrefs";
    private static final String STORE = "profiles";
    private static final String MIGRATED = "single_user_migration_v1";
    private static final Object LOCK = new Object();

    private SingleUserPrefs() { }

    static SharedPreferences homePrefs(Context context) {
        migrateIfNeeded(context);
        return context.getSharedPreferences("home", Context.MODE_PRIVATE);
    }

    static String userName(Context context) {
        migrateIfNeeded(context);
        return store(context).getString("single_user_name", "Пользователь");
    }

    private static SharedPreferences store(Context context) {
        return context.getSharedPreferences(STORE, Context.MODE_PRIVATE);
    }

    private static void migrateIfNeeded(Context context) {
        if (context == null) return;
        SharedPreferences registry = store(context);
        if (registry.getBoolean(MIGRATED, false)) return;
        synchronized (LOCK) {
            if (registry.getBoolean(MIGRATED, false)) return;
            try {
                String rawList = registry.getString("list", "[]");
                String rawCurrent = registry.getString("current", "p1");
                LegacyProfile selected = readSelected(rawList, rawCurrent);

                SharedPreferences home = context.getSharedPreferences("home", Context.MODE_PRIVATE);
                String selectedPrefsName = "p1".equals(selected.id) ? "home" : "home_" + selected.id;
                SharedPreferences selectedPrefs = context.getSharedPreferences(selectedPrefsName, Context.MODE_PRIVATE);
                boolean selectedFileExists = "p1".equals(selected.id)
                        || !selectedPrefs.getAll().isEmpty()
                        || new File(context.getApplicationInfo().dataDir,
                                "shared_prefs/" + selectedPrefsName + ".xml").exists();

                SingleUserMigration.Result result = SingleUserMigration.migrate(
                        home.getAll(), selectedPrefs.getAll(), selected.id, selectedFileExists);

                if (result.preserveLegacyHome && !home.getAll().isEmpty()) {
                    SharedPreferences legacy = context.getSharedPreferences("home_legacy_p1", Context.MODE_PRIVATE);
                    if (legacy.getAll().isEmpty() && !writePrefs(legacy, result.legacyHome, true)) {
                        Log.w(TAG, "Could not preserve the previous p1 preferences; migration deferred");
                        return;
                    }
                }
                if (result.copiedSelectedProfile && !writePrefs(home, result.home, true)) {
                    Log.w(TAG, "Could not copy the selected profile into home; migration deferred");
                    return;
                }

                SharedPreferences.Editor editor = registry.edit();
                if (!registry.contains("legacy_list_before_single_user")) {
                    editor.putString("legacy_list_before_single_user", rawList);
                }
                if (!registry.contains("legacy_current_before_single_user")) {
                    editor.putString("legacy_current_before_single_user", rawCurrent);
                }
                // Keep list/current and ask_on_wake untouched. The single-user UI no longer
                // consumes the profile registry, while an older APK can still read the exact
                // pre-migration selection if the user rolls back this update.
                editor.putString("single_user_name", selected.name)
                        .putBoolean(MIGRATED, true);
                if (!editor.commit()) Log.w(TAG, "Could not commit single-user profile metadata");
            } catch (Exception e) {
                Log.w(TAG, "Single-user migration will be retried on next launch", e);
            }
        }
    }

    private static boolean writePrefs(SharedPreferences target, Map<String, Object> values, boolean clear) {
        SharedPreferences.Editor editor = target.edit();
        if (clear) editor.clear();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) editor.putString(key, (String) value);
            else if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
            else if (value instanceof Integer) editor.putInt(key, (Integer) value);
            else if (value instanceof Long) editor.putLong(key, (Long) value);
            else if (value instanceof Float) editor.putFloat(key, (Float) value);
            else if (value instanceof Set<?>) {
                java.util.HashSet<String> copy = new java.util.HashSet<>();
                for (Object item : (Set<?>) value) if (item instanceof String) copy.add((String) item);
                editor.putStringSet(key, copy);
            }
        }
        return editor.commit();
    }

    private static LegacyProfile readSelected(String rawList, String currentId) {
        LegacyProfile first = null;
        LegacyProfile selected = null;
        try {
            JSONArray list = new JSONArray(rawList == null ? "[]" : rawList);
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.optJSONObject(i);
                if (item == null) continue;
                String id = item.optString("id", "");
                if (!id.matches("[A-Za-z0-9_-]{1,32}")) continue;
                LegacyProfile profile = new LegacyProfile();
                profile.id = id;
                profile.name = safeName(item.optString("name", "Пользователь"));
                profile.color = Math.max(0, Math.min(6, item.optInt("color", 0)));
                profile.emoji = item.optString("emoji", "");
                if (profile.emoji.isEmpty()) profile.emoji = "\uD83D\uDE00";
                if (first == null) first = profile;
                if (id.equals(currentId)) selected = profile;
            }
        } catch (Exception ignored) { }
        if (selected != null) return selected;
        if (first != null) return first;
        LegacyProfile fallback = new LegacyProfile();
        fallback.id = "p1";
        fallback.name = "Пользователь";
        fallback.color = 0;
        fallback.emoji = "\uD83D\uDE00";
        return fallback;
    }

    private static String safeName(String name) {
        if (name == null) return "Пользователь";
        String clean = name.trim();
        if (clean.isEmpty()) return "Пользователь";
        return clean.length() > 40 ? clean.substring(0, 40) : clean;
    }

    private static final class LegacyProfile {
        String id;
        String name;
        String emoji;
        int color;
    }
}
