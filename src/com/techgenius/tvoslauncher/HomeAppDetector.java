package com.techgenius.tvoslauncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure-Java policy model for identifying the selected Android HOME app.
 * A list of HOME-capable packages is not proof that any one of them is selected.
 */
final class HomeAppDetector {
    enum UnknownReason {
        NONE,
        PACKAGE_MANAGER_UNAVAILABLE,
        CANDIDATE_QUERY_FAILED,
        NO_HOME_CANDIDATES,
        NO_DEFAULT_HOME,
        AMBIGUOUS_ROLE_HOLDERS
    }

    enum PackageProtection {
        SAFE,
        PROTECTED_CURRENT_HOME,
        UNKNOWN
    }

    static final class Candidate {
        final String packageName;
        final String activityName;
        final Boolean systemApp;
        final Boolean enabled;

        Candidate(String packageName, String activityName, Boolean systemApp, Boolean enabled) {
            this.packageName = clean(packageName);
            this.activityName = clean(activityName);
            this.systemApp = systemApp;
            this.enabled = enabled;
        }
    }

    static final class Result {
        final String currentHomePackage;
        final String currentHomeActivity;
        final boolean currentHomeKnown;
        final boolean candidatesKnown;
        final boolean testOsIsCurrentHome;
        final UnknownReason unknownReason;
        final List<Candidate> candidates;

        private Result(String currentHomePackage, String currentHomeActivity,
                       boolean currentHomeKnown, boolean candidatesKnown,
                       String ownPackage, UnknownReason unknownReason,
                       List<Candidate> candidates) {
            this.currentHomePackage = clean(currentHomePackage);
            this.currentHomeActivity = clean(currentHomeActivity);
            this.currentHomeKnown = currentHomeKnown && !this.currentHomePackage.isEmpty();
            this.candidatesKnown = candidatesKnown;
            this.testOsIsCurrentHome = this.currentHomeKnown
                    && this.currentHomePackage.equals(clean(ownPackage));
            this.unknownReason = this.currentHomeKnown ? UnknownReason.NONE : unknownReason;
            this.candidates = Collections.unmodifiableList(new ArrayList<>(candidates));
        }

        boolean isCurrentHome(String packageName) {
            return currentHomeKnown && currentHomePackage.equals(clean(packageName));
        }

        PackageProtection protectionFor(String packageName) {
            String pkg = clean(packageName);
            if (pkg.isEmpty()) return PackageProtection.UNKNOWN;
            if (currentHomeKnown && currentHomePackage.equals(pkg)) {
                return PackageProtection.PROTECTED_CURRENT_HOME;
            }
            // If candidate discovery failed or returned no corroborating candidates, do not
            // infer that an arbitrary package is not HOME-capable.
            if (!candidatesKnown || candidates.isEmpty()) return PackageProtection.UNKNOWN;
            if (currentHomeKnown) return PackageProtection.SAFE;
            for (Candidate candidate : candidates) {
                if (candidate.packageName.equals(pkg)) return PackageProtection.UNKNOWN;
            }
            return PackageProtection.SAFE;
        }

        /**
         * Conservative UI signal: unknown candidate visibility is not interpreted as "no stock HOME".
         */
        Boolean isCurrentHomeSystemApp() {
            if (!currentHomeKnown) return null;
            for (Candidate candidate : candidates) {
                if (candidate.packageName.equals(currentHomePackage)) return candidate.systemApp;
            }
            return null;
        }

        boolean hasEnabledSystemHomeCandidate(String ownPackage) {
            if (!candidatesKnown) return true;
            if (candidates.isEmpty() && currentHomeKnown && !testOsIsCurrentHome) return true;
            String own = clean(ownPackage);
            for (Candidate candidate : candidates) {
                if (candidate.packageName.equals(own)) continue;
                if (!Boolean.FALSE.equals(candidate.enabled)
                        && !Boolean.FALSE.equals(candidate.systemApp)) return true;
            }
            return false;
        }
    }

    private HomeAppDetector() { }

    /**
     * Combines the platform's selected HOME signal with the independently discovered candidate list.
     * On API 29+, a single RoleManager holder is preferred. PackageManager's preferred HOME is the
     * fallback for older APIs, unavailable roles, or an empty role-holder response.
     */
    static Result evaluate(int apiLevel,
                           boolean roleAvailable,
                           boolean roleQuerySucceeded,
                           List<String> roleHolders,
                           String packageManagerHomePackage,
                           String packageManagerHomeActivity,
                           boolean candidatesQuerySucceeded,
                           List<Candidate> candidates,
                           String ownPackage) {
        List<Candidate> safeCandidates = candidates == null
                ? Collections.<Candidate>emptyList() : candidates;
        String selectedPackage = "";
        String selectedActivity = "";
        UnknownReason reason = UnknownReason.NO_DEFAULT_HOME;

        if (apiLevel >= 29 && roleAvailable && roleQuerySucceeded && roleHolders != null) {
            if (roleHolders.size() == 1 && !clean(roleHolders.get(0)).isEmpty()) {
                selectedPackage = clean(roleHolders.get(0));
                selectedActivity = activityForPackage(safeCandidates, selectedPackage);
            } else if (roleHolders.size() > 1) {
                reason = UnknownReason.AMBIGUOUS_ROLE_HOLDERS;
                return new Result("", "", false, candidatesQuerySucceeded,
                        ownPackage, reason, safeCandidates);
            }
        }

        if (selectedPackage.isEmpty() && !clean(packageManagerHomePackage).isEmpty()) {
            selectedPackage = clean(packageManagerHomePackage);
            selectedActivity = clean(packageManagerHomeActivity);
        }

        if (selectedPackage.isEmpty()) {
            if (!candidatesQuerySucceeded) reason = UnknownReason.CANDIDATE_QUERY_FAILED;
            else if (safeCandidates.isEmpty()) reason = UnknownReason.NO_HOME_CANDIDATES;
        }

        return new Result(selectedPackage, selectedActivity, !selectedPackage.isEmpty(),
                candidatesQuerySucceeded, ownPackage, reason, safeCandidates);
    }

    private static String activityForPackage(List<Candidate> candidates, String packageName) {
        for (Candidate candidate : candidates) {
            if (candidate.packageName.equals(packageName) && !candidate.activityName.isEmpty()) {
                return candidate.activityName;
            }
        }
        return "";
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
