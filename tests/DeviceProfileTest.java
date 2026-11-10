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

        System.out.println("Device profile identity tests passed.");
    }
}
