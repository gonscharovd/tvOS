# testOS Launcher — dependency map for core cleanup

**Audit date:** 2026-11-09  
**Repository:** `gonscharovd/tvOS`  
**Audited base:** `main` / `a765318b577e66fae66a8ffe28439dcd6233f38f`  
**Working branch:** `refactor/testos-core-cleanup-2026-11`  
**Status:** Classic home UI removal implemented in PR #14; CI validation pending. No OTA release or TV installation.

## Confirmed baseline

- Stable release: `v2.1.73`, versionCode `88`, package `com.gonscharovd.tvoslauncher`.
- Tag points to the audited commit. Release APK: 6,192,577 bytes; SHA-256 `769acad3561983ba5a0b8d20b0547f08a16180843c1c9e20441d8bb41b846754`.
- User confirmed installation on Sber SDX-50U4123B and basic launch / D-pad navigation. Full regression on TV is not yet established.
- Target compatibility remains Android 9 / API 28 / ARMv7.
- No open PRs existed when the branch was created. No OTA release is being published.

## 1. “Continue watching” shelf

| File | Confirmed dependency | Planned change |
|---|---|---|
| `SceneHome.java` | Constructor creates the shelf. Fields / methods include shelf container, cards, `WatchCardData`, package and thumbnail caches, async loading, focus callbacks, animations and DPAD navigation. `loadWatchCardsForPackage()` always returns an empty list. | Remove shelf-only UI, caches, background work, card actions and navigation. Keep dock and shared logo cache. |
| `MainActivity.java` | `cwEnabled()` is hard false. Old hero carousel still uses `Suggestions.Item`, `loadHero()`, `setHeroItems()`, `heroKeys()`, `openHeroItem()` and timers. | Remove dead hero / continue-watching code. Preserve app launch, recent-app tracking, Search and Night Scenes ambient mode. |
| `Suggestions.java` | `Item`, `open()`, `logoFor()` belong to old hero/shelf. `image()` is still used by SearchPanel; launch / installed-app / stream helpers have other callers. | Remove only obsolete item / hero helpers. Keep shared helpers still used elsewhere. |
| `AndroidManifest.xml` | Declares `READ_TV_LISTINGS` and `READ_EPG_DATA`. | Remove both after source references are removed. |
| `MainActivity.java` | `ensureTvListingsPermission()` requests `READ_TV_LISTINGS`; request code 901 is handled in `onRequestPermissionsResult()`. | Remove request and result branch. |
| `LocalAdb.java` | TV listings permission is included in ADB grant command lists. | Remove only TV listings grants. Preserve `DUMP`, `WRITE_SECURE_SETTINGS`, `SET_VOLUME_KEY_LONG_PRESS_LISTENER`; invoke volume-key repair independently of the removed TV permission check. |
| `tests/test_memory_settings_cleanup.py` | Contains assumptions about old hero / shelf state. | Assert shelf symbols and TV permission references are absent; shared image caches remain. |

No image asset was confirmed as shelf-exclusive. Do not delete app logos, Search artwork or general image-loading code.

## 2. Classic home UI — implementation status

**Implemented in working branch; CI validation pending.**

The current MainActivity.buildChrome() creates SceneHome and its SceneView as the only home experience. The old Classic interface is no longer constructed or toggled.

### Removed from MainActivity.java

- applyLookMode() and the old Classic look-mode switching path.
- Classic app grid: buildGrid(), makeCell(), old grid focus / scrolling / move state, and the synthetic FOLDER_KEY pseudo-folder.
- Classic wallpaper rendering: setWallpaper(), makeWallpaper(), wallpaper drift, prefetch, wallpaper blur and wallpaper selection state.
- Hero / Continue Watching carousel, its models, View tree, navigation, animations, timers and permission result handling.
- Old Classic status chrome: clock/tabs, network glyphs, refresh/search/Control Center buttons and the old weather card.
- Old carousel idle / full-screen transition logic and obsolete focus-sway / tile-grid handlers.

The completed cleanup removed 2,084 lines from MainActivity.java (4,522 → 2,438 lines). The unused LaunchCard / launchBackground tile-to-full-screen animation and its helpers were removed because no active caller remained; current app launch still uses launchAnimated() and the Android activity transition, while SceneHome keeps its own dock focus and move animations.

### Preserved Night Scenes and shared functionality

- SceneHome.java remains the owner of the dock, MORE_KEY / moreExpanded, DPAD focus and navigation, app menus, dock reordering and Other Apps.
- MainActivity.java keeps AppEntry, loadApps(), order / hidden / Other Apps persistence, package receiver, app launch, Search / Weather / Control Center / Recent Apps overlays, LiquidGlass, glassBg / glassSource, the generic backdropSharp() / tvosCard() app-artwork renderer, and the root container.
- dispatchKeyEvent() continues to delegate home navigation to SceneHome.dispatchKey(). Search and overlay key handling remain in MainActivity.
- weatherTick still refreshes Weather, performs the 15-minute AirPlayGuard check, repairs Local ADB permissions and checks connectivity recovery. The removed network glyph was UI-only.
- MainActivity.makeWallpaperBase() remains because Setup and Settings use it for scene / gradient previews.
- CustomMedia.buildAppBannerFromCustomIcon() is the neutral name for the shared app-banner generator used by Search and Recent Apps.
- Suggestions.image() and Suggestions.start() remain. The obsolete Suggestions.Item, open() and logoFor() Hero-only API has been removed. The unused profile avatar helper and stale Classic / Hero comments were also removed from MainActivity.
- Scenes.CLASSIC_PHOTOS, CLASSIC_GRADIENTS, the underlying assets, and Scenes.migrateClassicBackgroundsToNightGallery() / migrateClassicCustomAssets() remain. They are legacy data and image sources, not a Classic home UI.
- CustomMedia legacy Classic wallpaper / folder readers remain for idempotent migration; old files and keys are not deleted.
- Lock-screen photo and gradient choices remain available in Settings; their category is now PHOTOS & GRADIENTS. The unused classicPhotoRow() helper is removed.
- SetupActivity.lookSubStep and its obsolete Classic appearance back-navigation branch are removed.

### “Folders” clarification

There is no user-created folder model in this flow. FOLDER_KEY represented only the old Classic-grid “Other Apps” tile. Active Night Scenes continues to use MORE_KEY / moreExpanded, backed by PREF_OTHER. App order (PREF_ORDER), hidden state (PREF_HIDDEN) and app menus are preserved. SearchPanel no longer filters the removed synthetic key.

### Migration and rollback safety

The existing migration remains unchanged and idempotent:
1. Copy a legacy Classic custom wallpaper into the Night Scenes custom-scene slot only if that slot is absent; preserve timestamp.
2. Copy the legacy Classic folder path into Scene folder-shuffle scope only if the Scene scope is empty.
3. Add migrated scenes to the enabled set. Apply an old preferred wallpaper only when no newer preference exists.
4. Leave old files and keys untouched for rollback.

### Verification status

- Static scan of the edited MainActivity.java: no remaining references to removed grid, Hero, Classic wallpaper or old status-row symbols.
- AndroidManifest.xml at the current PR base has no READ_TV_LISTINGS / READ_EPG_DATA declarations; no manifest change was needed for this stage.
- Regression tests updated: tests/test_memory_settings_cleanup.py and tests/test_settings_navigation.py. I18n.java now contains the Russian translation for the neutral lock-screen gallery label.
- **Pending:** run the PR CI suite and inspect the APK build. No runtime test on the TV has been performed. No merge, OTA release or APK installation is authorized by this task.

---
## 3. Profiles → one user

### Current behavior

- `Profiles.MAX = 1`; add / switch / delete are stubs.
- `Profiles.homePrefs()` maps `p1` to `home`, other IDs to `home_<id>`.
- `Profiles.list()` truncates the profile list and clears preferences / continue files for extra profiles. The new implementation must not call this destructive path.
- Remaining UI references: MainActivity profile picker / menu, SceneHome `whoBar`, unused SettingsActivity add-person helper and Scenes greeting.

### Safe migration plan

- Replace the multi-profile model with a small `SingleUserPrefs` adapter that always exposes canonical `home`.
- Read the selected profile ID using the existing `profiles` JSON format before rewriting metadata.
- If selected ID is not `p1`, copy `home_<id>` into `home`; first preserve non-empty existing `home` as `home_legacy_p1`.
- Preserve original profile list / current ID in legacy metadata keys; rewrite active list to one `p1` record with the selected user's name and set current ID to `p1`.
- Do not delete `home_<id>`, `home_legacy_p1`, continue files or legacy metadata. Keep selected display name for the Night Scenes greeting.
- Existing backup allowlist includes `home` and excludes profile registry. After migration, active user data is in `home`; old backups still restore to the same canonical preference file.
- Add a pure-Java migration helper and regression cases for p1 / non-p1, typed preference values, preservation of legacy p1 data and idempotence.

Historical limitation: if v2.1.73 already ran `Profiles.list()` on a device with multiple profiles, that older code may already have deleted inactive profile files. New code can preserve data still present, not reconstruct deleted files.

## 4. Build, versions and docs

| File | Current issue | Planned correction |
|---|---|---|
| `build.sh` | Defaults 15 / `2.1.2-testOS1`; includes a macOS-specific JDK fallback. | Read shared version file; keep environment overrides. |
| `build.ps1` | Defaults 13 / `2.1.2-custom1`; hard-coded JDK path for another developer's Windows account. | Remove user-specific path; use `JAVA_HOME` or JDK on `PATH`; read same version file. |
| `.github/workflows/build-apk.yml` | PR builds inherit stale build.sh defaults. | Use shared version defaults and run source / APK checks. |
| `.github/workflows/release.yml` | `versionCode = 15 + PATCH`; correct for current scheme (`v2.1.73` → 88). | Keep formula and signing fingerprint; validate release branch version against shared version file. |
| `README.md`, `docs/architecture.md`, `docs/install.md` | Refer to 2.1.72 and inconsistent example codes. | Document stable `v2.1.73 / 88`, next build target `2.1.74 / 89`, and one version source. |
| `tests/test_build_contract.py` | Does not enforce a single source of truth. | Validate both scripts, shared version file, release formula and absence of developer-specific paths. |

Keep package name, APK output path, release key / alias, certificate fingerprint verification, OTA comparator and PackageInstaller flow unchanged. 2.1.74 is a development build target, not an OTA release.

## 5. Manifest, resources and verification

- Remove only `READ_TV_LISTINGS` and `READ_EPG_DATA`; preserve all permissions used by CleanYourTV, OTA, ADB / volume-key repair, accessibility and services.
- No Classic-specific XML layout was identified; old UI is built programmatically.
- Preserve `assets/backgrounds`, `assets/fonts`, `assets/app-logos`, CleanYourTV examples, presentation assets, app banner / launcher drawables, generic animations, device-admin XML and power-service XML unless a full reference scan proves an individual resource unused.
- Add source checks for removed shelf / Classic / profile symbols and APK checks for removed permissions / DEX strings.
- Run existing tests plus migration tests; verify package, version, manifest, DEX and signature.
- Manual Sber TV tests remain necessary: Night Scenes, DPAD, dock / More Apps, app move / hide / reorder, Search, Control Panel, Weather, gallery, custom wallpaper / folder shuffle, lock screen, backup / restore and OTA update from v2.1.73.

## 6. Explicit non-goals

Do not change firmware, `/system`, `/vendor`, bootloader, system partitions, package name, signing rules, CleanYourTV protection, OTA architecture, app library, Control Panel, Weather, Search, Recent Apps, Button Mapper, PowerService, LocalAdb keepalive, VPN, AirPlayGuard or unrelated subsystems. Do not publish OTA without separate user approval.
