from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
adapter = (ROOT / "src/com/techgenius/tvoslauncher/AndroidHomeAppDetector.java").read_text(encoding="utf-8")
perms = (ROOT / "src/com/techgenius/tvoslauncher/Perms.java").read_text(encoding="utf-8")
clean = (ROOT / "src/com/techgenius/tvoslauncher/CleanYourTv.java").read_text(encoding="utf-8")
local_adb = (ROOT / "src/com/techgenius/tvoslauncher/LocalAdb.java").read_text(encoding="utf-8")
power = (ROOT / "src/com/techgenius/tvoslauncher/PowerService.java").read_text(encoding="utf-8")
manifest = (ROOT / "AndroidManifest.xml").read_text(encoding="utf-8")
workflow = (ROOT / ".github/workflows/build-apk.yml").read_text(encoding="utf-8")

assert "queryIntentActivities(homeIntent" in adapter
assert "getPreferredActivities(preferredFilters, preferredActivities, null)" in adapter
assert "resolveActivity(" not in adapter, "HOME selection must not use best-match resolver priority"
assert "isRoleHeld(RoleManager.ROLE_HOME)" in adapter
assert "getRoleHolders(" not in adapter, "hidden RoleManager APIs must not be used"
assert "STOCK_TV_HOME_PACKAGES" not in perms
assert "isOemLockedHomeTv" not in perms
assert "AndroidHomeAppDetector.detect(c).isCurrentHome" in perms
assert "AndroidHomeAppDetector.detect(c)" in clean
assert "homeDetection.protectionFor(pkg)" in clean
assert "getStockTvHomePackages" not in local_adb
assert "homeDetection.candidates" in local_adb
assert "getStockTvHomePackages" not in power
assert "systemHomePackages.contains(fgPkg)" in power
assert "android.intent.category.HOME" in manifest
assert "android.permission.QUERY_ALL_PACKAGES" in manifest
assert "Test HOME detection policy" in workflow
assert workflow.count("HomeAppDetectorTest.java") >= 2

print("HOME detection Android wiring and OEM-independence contracts passed.")
