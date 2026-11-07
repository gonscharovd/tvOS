package com.techgenius.tvoslauncher;

import android.content.Context;
import java.util.Collections;
import java.util.List;

/** Continue Watching is disabled in this device-specific lightweight build. */
final class MediaWatchService {
    static MediaWatchService instance = null;

    static boolean granted(Context context) { return false; }
    static void ensureSessionTracking(Context context) { }
    static void pollActiveSessionsImmediate(Context context) { }
    static void pollActiveSessionsAsync(Context context) { }

    void requestInterruptionFilter(int filter) { }

    static final class Store {
        static List<Suggestions.Item> list(Context context) { return Collections.emptyList(); }
        static List<Suggestions.Item> listForPackage(Context context, String pkg, int maxItems) { return Collections.emptyList(); }
        static void remove(Context context, String key) { }
        static void clear(Context context) { }
        static void openContinueItem(Context context, Suggestions.Item item, Runnable onComplete) {
            if (onComplete != null) onComplete.run();
        }
    }
}
