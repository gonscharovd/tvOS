package com.techgenius.tvoslauncher;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small SemVer-style comparator for launcher version names.
 * Supports numeric core versions, pre-release labels, and build metadata.
 */
public final class VersionComparator {
    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "^(\\d+(?:\\.\\d+)*)(?:-([0-9A-Za-z.-]+))?(?:\\+[0-9A-Za-z.-]+)?$"
    );

    private VersionComparator() { }

    public static String clean(String value) {
        if (value == null) return "";
        String result = value.trim();
        while (result.startsWith("v") || result.startsWith("V")) {
            result = result.substring(1).trim();
        }
        return result;
    }

    public static boolean isNewer(String currentVersion, String remoteVersion) {
        Version current = parse(currentVersion);
        Version remote = parse(remoteVersion);
        if (remote == null) return false;
        if (current == null) return true;
        return compare(remote, current) > 0;
    }

    private static Version parse(String value) {
        String cleaned = clean(value);
        if (cleaned.isEmpty()) return null;
        Matcher matcher = VERSION_PATTERN.matcher(cleaned);
        if (!matcher.matches()) return null;

        String[] numberParts = matcher.group(1).split("\\.");
        List<BigInteger> numbers = new ArrayList<>(numberParts.length);
        try {
            for (String part : numberParts) numbers.add(new BigInteger(part));
        } catch (NumberFormatException invalid) {
            return null;
        }
        return new Version(numbers, matcher.group(2));
    }

    private static int compare(Version left, Version right) {
        int coreLength = Math.max(left.core.size(), right.core.size());
        for (int i = 0; i < coreLength; i++) {
            BigInteger a = i < left.core.size() ? left.core.get(i) : BigInteger.ZERO;
            BigInteger b = i < right.core.size() ? right.core.get(i) : BigInteger.ZERO;
            int result = a.compareTo(b);
            if (result != 0) return result;
        }

        if (left.preRelease == null && right.preRelease == null) return 0;
        if (left.preRelease == null) return 1;  // A release is newer than its pre-release.
        if (right.preRelease == null) return -1;

        String[] a = left.preRelease.split("\\.", -1);
        String[] b = right.preRelease.split("\\.", -1);
        int length = Math.min(a.length, b.length);
        for (int i = 0; i < length; i++) {
            boolean aNumeric = a[i].matches("\\d+");
            boolean bNumeric = b[i].matches("\\d+");
            int result;
            if (aNumeric && bNumeric) {
                result = new BigInteger(a[i]).compareTo(new BigInteger(b[i]));
            } else if (aNumeric != bNumeric) {
                result = aNumeric ? -1 : 1;
            } else {
                result = a[i].compareTo(b[i]);
            }
            if (result != 0) return result;
        }
        return Integer.compare(a.length, b.length);
    }

    private static final class Version {
        final List<BigInteger> core;
        final String preRelease;

        Version(List<BigInteger> core, String preRelease) {
            this.core = core;
            this.preRelease = preRelease;
        }
    }
}
