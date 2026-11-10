package com.techgenius.tvoslauncher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Immutable, vendor-neutral description of the Android TV platform.
 *
 * Detection is advisory only: an unknown or conflicting identity never implies that an OEM
 * feature exists or that a package is safe to disable.
 */
public final class DeviceProfile {
    public enum Family {
        SBER, XIAOMI, TCL, PHILIPS, SONY, HISENSE, OTHER_KNOWN, UNKNOWN
    }

    public final Family family;
    public final String manufacturer;
    public final String brand;
    public final String model;
    public final String device;
    public final String product;
    public final Set<String> evidence;

    private DeviceProfile(Family family, String manufacturer, String brand, String model,
                          String device, String product, Set<String> evidence) {
        this.family = family;
        this.manufacturer = clean(manufacturer);
        this.brand = clean(brand);
        this.model = clean(model);
        this.device = clean(device);
        this.product = clean(product);
        this.evidence = Collections.unmodifiableSet(new HashSet<>(evidence));
    }

    public static DeviceProfile resolve(String manufacturer, String brand, String model,
                                        String device, String product, Set<String> installedPackages) {
        String identity = join(manufacturer, brand, model, device, product).toLowerCase(Locale.US);
        Set<String> packages = installedPackages == null
                ? Collections.<String>emptySet() : installedPackages;
        Set<String> evidence = new HashSet<>();

        Family family = familyFromIdentity(identity);
        if (family != Family.UNKNOWN) {
            evidence.add("build-properties");
        } else {
            family = familyFromPackages(packages, evidence);
        }
        if (family == Family.UNKNOWN && !identity.trim().isEmpty()) {
            family = Family.OTHER_KNOWN;
            evidence.add("unclassified-build-identity");
        }
        return new DeviceProfile(family, manufacturer, brand, model, device, product, evidence);
    }

    private static Family familyFromIdentity(String value) {
        if (contains(value, "sber", "salutetv", "salute tv", "staros")) return Family.SBER;
        if (contains(value, "xiaomi", "redmi", "mitv", "mi tv", "mibox")) return Family.XIAOMI;
        if (contains(value, "tcl", "ffalcon", "thomson")) return Family.TCL;
        if (contains(value, "philips", "tpvision", "tpv")) return Family.PHILIPS;
        if (contains(value, "sony", "bravia")) return Family.SONY;
        if (contains(value, "hisense", "jamdeo", "vidaa")) return Family.HISENSE;
        return Family.UNKNOWN;
    }

    private static Family familyFromPackages(Set<String> packages, Set<String> evidence) {
        Family found = Family.UNKNOWN;
        for (String pkg : packages) {
            Family candidate = packageFamily(pkg);
            if (candidate == Family.UNKNOWN) continue;
            if (found != Family.UNKNOWN && found != candidate) {
                evidence.add("conflicting-oem-package-evidence");
                return Family.UNKNOWN;
            }
            found = candidate;
        }
        if (found != Family.UNKNOWN) evidence.add("installed-package-names");
        return found;
    }

    private static Family packageFamily(String pkg) {
        if (pkg == null) return Family.UNKNOWN;
        String p = pkg.toLowerCase(Locale.US);
        if (p.startsWith("ru.sberdevices.")) return Family.SBER;
        if (p.startsWith("com.xiaomi.") || p.startsWith("com.mitv.") || p.startsWith("com.miui.")) return Family.XIAOMI;
        if (p.startsWith("com.tcl.") || p.startsWith("com.falcon.") || p.startsWith("com.ffalcon.")) return Family.TCL;
        if (p.startsWith("org.droidtv.") || p.startsWith("com.tpvision.") || p.startsWith("com.philips.")) return Family.PHILIPS;
        if (p.startsWith("com.sony.") || p.contains(".bravia.")) return Family.SONY;
        if (p.startsWith("com.hisense.") || p.startsWith("com.jamdeo.")) return Family.HISENSE;
        return Family.UNKNOWN;
    }

    private static boolean contains(String value, String... tokens) {
        for (String token : tokens) if (value.contains(token)) return true;
        return false;
    }

    private static String join(String... values) {
        StringBuilder out = new StringBuilder();
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) out.append(' ').append(value.trim());
        }
        return out.toString();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public boolean isKnownOem() {
        return family != Family.UNKNOWN && family != Family.OTHER_KNOWN;
    }
}
