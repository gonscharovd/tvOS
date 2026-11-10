#!/usr/bin/env python3
"""Guard the standalone single-user migration test and both CI runner integrations."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
migration = ROOT / "src/com/techgenius/tvoslauncher/SingleUserMigration.java"
test = ROOT / "tests/SingleUserMigrationTest.java"
workflow = (ROOT / ".github/workflows/build-apk.yml").read_text(encoding="utf-8")
source = migration.read_text(encoding="utf-8")
prefs_source = (ROOT / "src/com/techgenius/tvoslauncher/SingleUserPrefs.java").read_text(encoding="utf-8")
test_source = test.read_text(encoding="utf-8")

assert "android." not in source, "Pure migration helper must remain JVM-testable without Android SDK"
assert "public static void main(String[] args)" in test_source
assert "SingleUserMigration.migrate(" in test_source
assert "legacyHome.containsKey" in test_source, "Legacy home values must remain covered"
assert "p1Selected" in test_source, "Canonical p1 selection must remain covered"
assert "missingSelected" in test_source, "Missing selected profile fallback must remain covered"
assert "secondPass" in test_source, "Repeated migration idempotency must remain covered"
assert "preserveLegacyHome" in test_source, "Legacy preference preservation must remain covered"
assert "p1.get(\"order\")" in test_source, "Source map immutability must remain covered"
assert "selectedProfileAvailable" in source
assert "new LinkedHashMap<>()" in source

# Preserve the legacy registry values so a previous APK can still select its old profile.
assert 'legacy_list_before_single_user' in prefs_source
assert 'legacy_current_before_single_user' in prefs_source
assert '.putString("list", single.toString())' not in prefs_source
assert '.putString("current", "p1")' not in prefs_source
assert '.putBoolean("ask_on_wake", false)' not in prefs_source
assert '.putString("single_user_name", selected.name)' in prefs_source

linux_job = workflow.split("  build:\n", 1)[1].split("\n  build-windows:\n", 1)[0]
windows_job = workflow.split("  build-windows:\n", 1)[1]
for name, job in (("Linux", linux_job), ("Windows", windows_job)):
    assert "Test single-user migration data" in job, f"{name} CI job does not run migration test"
    assert "SingleUserMigration.java" in job
    assert "SingleUserMigrationTest.java" in job
    assert "com.techgenius.tvoslauncher.SingleUserMigrationTest" in job
    assert "javac --release 11" in job

assert "Test single-user migration data" in workflow
print("Single-user migration CI contract: JVM test is wired on Linux and Windows; legacy-preservation checks remain present.")
