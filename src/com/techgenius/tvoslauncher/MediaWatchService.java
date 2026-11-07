package com.techgenius.tvoslauncher;

import android.content.Context;
import java.util.Collections;
import java.util.List;

/** Continue Watching is disabled in this device-specific lightweight build. */
final class MediaWatchService {
    static MediaWatchService instance = null;

    static boolean granted(Context context) { return false; }

    void requestInterruptionFilter(int filter) { }

    static final class Store {
        static List<Suggestions.Item> list(Context context) { return Collections.emptyList(); }
        static void remove(Context context, String key) { }
        static void openContinueItem(Context context, Suggestions.Item item, Runnable onComplete) {
            if (onComplete != null) onComplete.run();
        }
    }
}
