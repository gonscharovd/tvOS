package com.techgenius.tvoslauncher;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public final class DeviceProfileTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        DeviceProfile unknown = DeviceProfile.resolve("", "", "", "", "", Collections.<String>emptySet());
        check(unknown.family == DeviceProfile.Family.UNKNOWN, "empty identity must remain unknown");
        check(!unknown.isKnownOem(), "unknown identity must not claim an OEM profile");

        DeviceProfile generic = DeviceProfile.resolve("Google", "Google", "Chromecast", "sabrina", "sabrina",
                Collections.<String>emptySet());
        check(generic.family == DeviceProfile.Family.OTHER_KNOWN, "unclassified build identity must be generic-known");
        check(!generic.isKnownOem(), "generic Android identity must not be treated as an OEM adapter");

        check(DeviceProfile.resolve("Xiaomi", "MiTV", "P1", "", "", null).family
                == DeviceProfile.Family.XIAOMI, "Xiaomi identity");
        check(DeviceProfile.resolve("TP Vision", "Philips", "OLED", "", "", null).family
                == DeviceProfile.Family.PHILIPS, "Philips identity");
        check(DeviceProfile.resolve("", "", "", "", "", new HashSet<>(Arrays.asList("ru.sberdevices.home")))
                .family == DeviceProfile.Family.SBER, "Sber package evidence");

        DeviceProfile conflict = DeviceProfile.resolve("", "", "", "", "",
                new HashSet<>(Arrays.asList("com.sony.tv", "com.tcl.tv")));
        check(conflict.family == DeviceProfile.Family.UNKNOWN, "conflicting package evidence must fail closed");
        check(conflict.evidence.contains("conflicting-oem-package-evidence"), "conflict must be observable");

        DeviceProfile buildPackageConflict = DeviceProfile.resolve("Sber", "SaluteTV", "m7332_eu",
                "m7332_eu", "SaluteTV",
                new HashSet<>(Arrays.asList("ru.sberdevices.home", "com.xiaomi.tv.home")), true);
        check(buildPackageConflict.family == DeviceProfile.Family.UNKNOWN,
                "Build OEM identity conflicting with installed package evidence must fail closed");
        check(buildPackageConflict.evidence.contains("conflicting-build-and-package-oem-evidence"),
                "Build/package conflict must be observable");

        DeviceProfile inventoryUnknown = DeviceProfile.resolve("Sber", "SaluteTV", "TV",
                "device", "product", null, false);
        check(inventoryUnknown.family == DeviceProfile.Family.SBER,
                "Build identity remains available for diagnostics when inventory is unknown");
        check(!inventoryUnknown.packageInventoryKnown,
                "unknown package inventory must remain explicit");

        DeviceProfile packageOnly = DeviceProfile.resolve("", "", "", "", "",
                new HashSet<>(Arrays.asList("ru.sberdevices.home")), true);
        check(packageOnly.isKnownOem() && !packageOnly.evidence.contains("build-properties"),
                "package-only OEM inference is distinguishable from Build identity");

        System.out.println("Device profile identity tests passed.");
    }
}
