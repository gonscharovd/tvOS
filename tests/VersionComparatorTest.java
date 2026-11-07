package com.techgenius.tvoslauncher;

public final class VersionComparatorTest {
    private static void expect(boolean actual, boolean expected, String label) {
        if (actual != expected) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    public static void main(String[] args) {
        expect(VersionComparator.isNewer("2.1.2-custom1", "2.1.2-testOS1"), true,
                "different prerelease labels");
        expect(VersionComparator.isNewer("2.1.2-testOS1", "2.1.2-custom1"), false,
                "reverse prerelease ordering");
        expect(VersionComparator.isNewer("2.1.2-testOS1", "2.1.2"), true,
                "release newer than prerelease");
        expect(VersionComparator.isNewer("2.1.2", "2.1.2-testOS1"), false,
                "prerelease older than release");
        expect(VersionComparator.isNewer("2.1.2-testOS1", "2.1.2-testOS2"), true,
                "numeric suffix ordering");
        expect(VersionComparator.isNewer("2.1.61", "v2.1.61"), false,
                "leading v and equal versions");
        expect(VersionComparator.isNewer("2.1.9", "2.1.10"), true,
                "numeric component ordering");
        expect(VersionComparator.isNewer("2.1.10", "2.1.9"), false,
                "older numeric component");
        expect(VersionComparator.isNewer("2.1.2+build.1", "2.1.2+build.2"), false,
                "build metadata ignored");
        expect(VersionComparator.isNewer("", "v1.0.0"), true,
                "unknown current version");
        expect(VersionComparator.isNewer("v1.0.0", "not-a-version"), false,
                "invalid remote version");
        expect(VersionComparator.isNewer("1.0.0-alpha.9", "1.0.0-alpha.10"), true,
                "numeric prerelease identifiers");
        expect(VersionComparator.isNewer("1.0.0-alpha", "1.0.0-alpha.1"), true,
                "longer equal prerelease sequence is newer");

        System.out.println("VersionComparator: 13 regression cases passed.");
    }
}
