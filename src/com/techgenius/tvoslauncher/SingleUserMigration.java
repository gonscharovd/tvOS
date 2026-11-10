package com.techgenius.tvoslauncher;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Pure data migration used when the launcher moves from profile-scoped prefs to one home file. */
final class SingleUserMigration {
    private SingleUserMigration() { }

    static final class Result {
        final Map<String, Object> home;
        final Map<String, Object> legacyHome;
        final boolean copiedSelectedProfile;
        final boolean preserveLegacyHome;

        Result(Map<String, Object> home, Map<String, Object> legacyHome,
               boolean copiedSelectedProfile, boolean preserveLegacyHome) {
            this.home = home;
            this.legacyHome = legacyHome;
            this.copiedSelectedProfile = copiedSelectedProfile;
            this.preserveLegacyHome = preserveLegacyHome;
        }
    }

    static Result migrate(Map<String, ?> currentHome, Map<String, ?> selectedProfile,
                          String selectedId, boolean selectedProfileAvailable) {
        boolean copySelected = selectedId != null && !"p1".equals(selectedId) && selectedProfileAvailable;
        Map<String, Object> active = copyValues(copySelected ? selectedProfile : currentHome);
        Map<String, Object> legacy = copySelected ? copyValues(currentHome) : new LinkedHashMap<>();
        return new Result(active, legacy, copySelected, copySelected && !legacy.isEmpty());
    }

    private static Map<String, Object> copyValues(Map<String, ?> source) {
        Map<String, Object> copy = new LinkedHashMap<>();
        if (source == null) return copy;
        for (Map.Entry<String, ?> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Set<?>) {
                Set<String> strings = new LinkedHashSet<>();
                for (Object item : (Set<?>) value) if (item instanceof String) strings.add((String) item);
                copy.put(entry.getKey(), strings);
            } else if (value instanceof String || value instanceof Boolean || value instanceof Integer
                    || value instanceof Long || value instanceof Float) {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }
}
