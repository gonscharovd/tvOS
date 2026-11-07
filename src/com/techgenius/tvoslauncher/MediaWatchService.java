package com.techgenius.tvoslauncher;

import android.content.Context;

/** Continue Watching history is disabled in this device-specific lightweight build. */
final class MediaWatchService {
    static MediaWatchService instance = null;

    static boolean granted(Context context) { return false; }
    static void ensureSessionTracking(Context context) { }

    void requestInterruptionFilter(int filter) { }
}
