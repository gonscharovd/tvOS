package com.techgenius.tvoslauncher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class HomeAppDetectorTest {
    private static final String TEST_OS = "com.gonscharovd.tvoslauncher";

    private static HomeAppDetector.Candidate candidate(String pkg, String activity,
                                                         Boolean system, Boolean enabled) {
        return new HomeAppDetector.Candidate(pkg, activity, system, enabled);
    }

    private static HomeAppDetector.Result evaluate(int api, boolean roleAvailable,
            boolean roleQuerySucceeded, List<String> holders, String pmPackage, String pmActivity,
            boolean candidatesKnown, List<HomeAppDetector.Candidate> candidates) {
        return HomeAppDetector.evaluate(api, roleAvailable, roleQuerySucceeded, holders,
                pmPackage, pmActivity, candidatesKnown, candidates, TEST_OS);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        HomeAppDetector.Candidate stock = candidate("vendor.tv.home", "vendor.tv.HomeActivity", true, true);
        HomeAppDetector.Candidate google = candidate("com.google.android.tvlauncher", "GoogleHome", true, true);
        HomeAppDetector.Candidate userHome = candidate("org.example.customhome", "CustomHome", false, true);
        HomeAppDetector.Candidate testOs = candidate(TEST_OS, "MainActivity", true, true);

        // One system HOME, explicitly selected by PackageManager.
        HomeAppDetector.Result one = evaluate(28, false, false, null,
                "vendor.tv.home", "vendor.tv.HomeActivity", true, Collections.singletonList(stock));
        check(one.currentHomeKnown && one.isCurrentHome("vendor.tv.home"), "single system HOME");
        check(one.protectionFor("vendor.tv.home") == HomeAppDetector.PackageProtection.PROTECTED_CURRENT_HOME,
                "selected system HOME must be protected");
        check(!one.hasEnabledSystemHomeCandidate(TEST_OS),
                "selected system HOME must not be counted as another launcher");

        // Multiple system HOME candidates: never assume the first candidate is selected.
        HomeAppDetector.Result multiple = evaluate(28, false, false, null,
                "com.google.android.tvlauncher", "GoogleHome", true, Arrays.asList(stock, google));
        check(multiple.isCurrentHome("com.google.android.tvlauncher"), "PM-selected HOME");
        check(multiple.protectionFor("vendor.tv.home") == HomeAppDetector.PackageProtection.UNKNOWN,
                "alternate HOME candidates must remain protected from automatic debloat");

        HomeAppDetector.Candidate disabledHome = candidate("vendor.tv.disabledhome",
                "vendor.tv.DisabledHomeActivity", true, false);
        HomeAppDetector.Result disabledAlternate = evaluate(28, false, false, null,
                "com.google.android.tvlauncher", "GoogleHome", true,
                Arrays.asList(stock, google, disabledHome));
        check(disabledAlternate.protectionFor("vendor.tv.disabledhome")
                        == HomeAppDetector.PackageProtection.UNKNOWN,
                "disabled alternate HOME remains a protected recovery candidate");

        // System HOME plus user-installed HOME.
        HomeAppDetector.Result mixed = evaluate(28, false, false, null,
                "org.example.customhome", "CustomHome", true, Arrays.asList(stock, userHome));
        check(mixed.isCurrentHome("org.example.customhome"), "user HOME selection");
        check(mixed.hasEnabledSystemHomeCandidate(TEST_OS), "other system HOME remains discoverable");

        // testOS can be a candidate without being the selected OEM HOME.
        HomeAppDetector.Result testOsCandidate = evaluate(28, false, false, null,
                "vendor.tv.home", "vendor.tv.HomeActivity", true, Arrays.asList(stock, testOs));
        check(!testOsCandidate.testOsIsCurrentHome, "testOS candidate must not be mistaken for current HOME");
        check(testOsCandidate.protectionFor(TEST_OS) == HomeAppDetector.PackageProtection.UNKNOWN,
                "testOS candidate is distinct from selected HOME and remains protected");

        // testOS may be selected, but remains explicitly identifiable as testOS.
        HomeAppDetector.Result testOsSelected = evaluate(28, false, false, null,
                TEST_OS, "MainActivity", true, Arrays.asList(stock, testOs));
        check(testOsSelected.testOsIsCurrentHome && testOsSelected.isCurrentHome(TEST_OS),
                "testOS selected HOME");

        // Unknown OEM identity is irrelevant when Android HOME evidence is available.
        HomeAppDetector.Result unknownOem = evaluate(28, false, false, null,
                "com.unlisted.vendor.home", "HomeActivity", true,
                Collections.singletonList(candidate("com.unlisted.vendor.home", "HomeActivity", true, true)));
        check(unknownOem.isCurrentHome("com.unlisted.vendor.home"), "unknown OEM generic detection");

        // Empty HOME result is UNKNOWN and fails safe.
        HomeAppDetector.Result empty = evaluate(24, false, false, null,
                null, null, true, Collections.<HomeAppDetector.Candidate>emptyList());
        check(!empty.currentHomeKnown && empty.unknownReason == HomeAppDetector.UnknownReason.NO_HOME_CANDIDATES,
                "empty HOME result must be UNKNOWN");
        check(empty.protectionFor("some.system.package") == HomeAppDetector.PackageProtection.UNKNOWN,
                "empty HOME result must fail safe");

        // PackageManager unavailable / query throws: no guessed default.
        HomeAppDetector.Result unavailable = evaluate(28, false, false, null,
                null, null, false, Collections.<HomeAppDetector.Candidate>emptyList());
        check(!unavailable.currentHomeKnown
                && unavailable.unknownReason == HomeAppDetector.UnknownReason.CANDIDATE_QUERY_FAILED,
                "unavailable PackageManager must be UNKNOWN");

        HomeAppDetector.Result queryFailureWithCandidates = evaluate(28, false, false, null,
                null, null, false, Collections.singletonList(stock));
        check(queryFailureWithCandidates.protectionFor("vendor.tv.home")
                == HomeAppDetector.PackageProtection.UNKNOWN, "candidate with unknown selection is protected");

        // Multiple candidates without a preferred component must not select index zero.
        HomeAppDetector.Result ambiguous = evaluate(28, false, false, null,
                null, null, true, Arrays.asList(stock, google));
        check(!ambiguous.currentHomeKnown && ambiguous.unknownReason == HomeAppDetector.UnknownReason.NO_DEFAULT_HOME,
                "multiple candidates without default must remain UNKNOWN");
        check(ambiguous.protectionFor("vendor.tv.home") == HomeAppDetector.PackageProtection.UNKNOWN,
                "ambiguous HOME candidates must be protected");

        // A single candidate is not enough evidence if PackageManager did not identify a preferred HOME.
        HomeAppDetector.Result singleWithoutDefault = evaluate(24, false, false, null,
                null, null, true, Collections.singletonList(stock));
        check(!singleWithoutDefault.currentHomeKnown
                && singleWithoutDefault.protectionFor("vendor.tv.home") == HomeAppDetector.PackageProtection.UNKNOWN,
                "candidate order/count must not be treated as selected HOME");

        // Android API 24 and API 28 use PackageManager preferred activity evidence.
        check(evaluate(24, false, false, null, "vendor.tv.home", null, true,
                Collections.singletonList(stock)).isCurrentHome("vendor.tv.home"), "API 24");
        check(evaluate(28, false, false, null, "vendor.tv.home", null, true,
                Collections.singletonList(stock)).isCurrentHome("vendor.tv.home"), "API 28");

        // API 29+ RoleManager is authoritative when it confirms that this app holds ROLE_HOME.
        HomeAppDetector.Result roleSelected = evaluate(34, true, true,
                Collections.singletonList("org.example.rolehome"), "vendor.tv.home", null,
                true, Arrays.asList(stock, candidate("org.example.rolehome", "RoleHome", false, true)));
        check(roleSelected.isCurrentHome("org.example.rolehome"), "RoleManager selection wins on API 29+");

        // If RoleManager cannot be queried, fall back to explicit PackageManager preferred activity.
        HomeAppDetector.Result roleUnavailable = evaluate(30, true, false, null,
                "vendor.tv.home", "HomeActivity", true, Collections.singletonList(stock));
        check(roleUnavailable.isCurrentHome("vendor.tv.home"), "RoleManager failure falls back to PackageManager");

        // Conflicting role holders are not resolved by taking the first.
        HomeAppDetector.Result roleConflict = evaluate(34, true, true,
                Arrays.asList("one.home", "two.home"), "vendor.tv.home", null,
                true, Arrays.asList(stock, google));
        check(!roleConflict.currentHomeKnown
                && roleConflict.unknownReason == HomeAppDetector.UnknownReason.AMBIGUOUS_ROLE_HOLDERS,
                "conflicting role holders must fail closed");

        // Limited package visibility: explicit selected HOME remains protected even without candidates.
        HomeAppDetector.Result limitedVisibility = evaluate(34, false, false, null,
                "vendor.tv.home", "HomeActivity", true, Collections.<HomeAppDetector.Candidate>emptyList());
        check(limitedVisibility.isCurrentHome("vendor.tv.home"),
                "selected HOME remains known under limited visibility");
        check(limitedVisibility.protectionFor("vendor.tv.home")
                == HomeAppDetector.PackageProtection.PROTECTED_CURRENT_HOME,
                "selected HOME protected under limited visibility");
        check(limitedVisibility.protectionFor("another.package")
                == HomeAppDetector.PackageProtection.UNKNOWN,
                "missing candidates do not prove another package is not HOME");

        HomeAppDetector.Result roleEmptyFallback = evaluate(34, true, true, Collections.<String>emptyList(),
                "vendor.tv.home", "HomeActivity", true, Collections.singletonList(stock));
        check(roleEmptyFallback.isCurrentHome("vendor.tv.home"), "empty RoleManager result falls back to PackageManager");

        System.out.println("Home app detection policy tests passed.");
    }
}
