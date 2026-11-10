package com.techgenius.tvoslauncher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SingleUserMigrationTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Map<String, Object> p1 = new HashMap<>();
        p1.put("order", "a,b,c");
        p1.put("hidden", new HashSet<>(Arrays.asList("pkg.hidden")));
        p1.put("idle_sec", 45);

        Map<String, Object> p2 = new HashMap<>();
        p2.put("order", "x,y");
        p2.put("other_apps", new HashSet<>(Arrays.asList("pkg.other")));
        p2.put("scenes", "night,photo");

        SingleUserMigration.Result selected = SingleUserMigration.migrate(p1, p2, "p2", true);
        check(selected.copiedSelectedProfile, "selected non-p1 profile must be copied");
        check(selected.preserveLegacyHome, "non-empty p1 preferences must be preserved");
        check("x,y".equals(selected.home.get("order")), "selected profile order must become canonical");
        check(selected.legacyHome.containsKey("idle_sec"), "legacy p1 settings must be retained");
        check(((Set<?>) selected.home.get("other_apps")).contains("pkg.other"), "string sets must be copied");

        SingleUserMigration.Result p1Selected = SingleUserMigration.migrate(p1, p2, "p1", true);
        check(!p1Selected.copiedSelectedProfile, "p1 is already the canonical home file");
        check("a,b,c".equals(p1Selected.home.get("order")), "p1 settings must remain unchanged");

        SingleUserMigration.Result missingSelected = SingleUserMigration.migrate(p1, new HashMap<>(), "p2", false);
        check(!missingSelected.copiedSelectedProfile, "missing selected-profile file must not erase home");
        check("a,b,c".equals(missingSelected.home.get("order")), "missing profile fallback must preserve home");

        SingleUserMigration.Result secondPass = SingleUserMigration.migrate(
                selected.home, selected.home, "p1", true);
        check("x,y".equals(secondPass.home.get("order")), "second pass must remain stable");
        check("a,b,c".equals(p1.get("order")), "migration must not mutate source maps");
        System.out.println("Single-user migration data tests passed.");
    }
}
