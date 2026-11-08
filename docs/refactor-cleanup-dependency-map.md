# testOS Launcher — dependency map for core cleanup

**Audit date:** 2026-11-08  
**Repository:** `gonscharovd/tvOS`  
**Audited base:** `main` / `a765318b577e66fae66a8ffe28439dcd6233f38f`  
**Working branch:** `refactor/testos-core-cleanup-2026-11`  
**Status:** dependency mapping only; no application source code changed at this stage.

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

## 2. Classic home UI

### Legacy UI to remove

`MainActivity.java` currently creates Classic UI in `buildChrome()`, then hides it through `applyLookMode()`. `buildGrid()` still creates the old app grid and synthetic “Other Apps” folder. The old UI includes:

- Wallpaper / drift: `setWallpaper()`, `makeWallpaper()`, `startWallpaperDrift()`, old wallpaper views, prefetch and blur state.
- Classic status chrome: old clock / tabs, weather card, refresh / search / Control Panel / avatar buttons, network glyphs.
- Hero carousel / top shelf and all hero timers, cards and navigation.
- Legacy grid: `buildGrid()`, `makeCell()`, grid focus / move / scroll, synthetic `FOLDER_KEY`, folder transitions and Classic reorder state.
- Profile UI: profile picker, profile cells / menu, avatar refresh and switching.
- Look-mode switches: `applyLookMode()`, `getLook()`, `isScenesLook()`, `setLook()` and all branches / guards.

Replace the old root with only the existing Night Scenes `SceneView` and `SceneHome` overlay. Keep `MainActivity.root`: Search, Weather, Control Panel, Recent Apps and other overlays attach to it.

### Night Scenes and shared components to preserve

- `SceneHome.java`: `SceneView`, top bar, dock, `rebuildDock()`, `toggleMoreApps()`, app menus and dock reordering. Remove only shelf and profile pill.
- `MainActivity.java`: `AppEntry`, `loadApps()`, `saveOrder()`, `setOtherApp()`, `setHidden()`, package receiver, app launch methods and `LiquidGlass`.
- Keep `launchAnimated()`, `finishLaunchReturn()`, `launchBackground()`, `noteLaunched()`, glass source / invalidation and generic layout helpers.
- `SearchPanel.java` and `RecentAppsOverlay.java` still use app banners / artwork. Preserve `AppEntry.banner` and the app-banner generator (rename the misleading Classic-named helper).
- `Suggestions.image()` is still used by SearchPanel. Keep.
- `MainActivity.makeWallpaperBase()` is used by Setup / Settings scene previews. Keep or move to a shared scene utility.
- `Scenes.CLASSIC_PHOTOS` and `CLASSIC_GRADIENTS` are still used by Night Scenes gallery and lock-screen scene list. Keep these definitions and their assets.
- Keep generic `CustomMedia` scene / lock wallpaper and folder APIs.

### “Folders” clarification

The audited source has no separate user-created folder model. `FOLDER_KEY` is a synthetic Classic-grid “Other Apps” tile. Active Night Scenes uses `MORE_KEY` / `moreExpanded`, backed by `PREF_OTHER`. Preserve More Apps, app order (`PREF_ORDER`), hidden-app state (`PREF_HIDDEN`) and all current app menus. Remove only the obsolete Classic pseudo-folder / grid navigation.

### Classic custom wallpaper migration

`CustomMedia.java` still stores `custom_classic_wallpaper.jpg` and Classic folder-shuffle scope `classic`. Settings helpers `pickCustomClassicWallpaper()`, `customClassicPhotoRow()`, `pickFolderForClassicShuffle()`, `folderShuffleClassicRow()` and `classicPhotoRow()` have no active callers.

Before removing those APIs:
1. Copy an existing Classic custom wallpaper to the Night Scenes custom-scene slot only if the new slot is absent; preserve timestamp.
2. Copy the legacy Classic folder path to Scene folder-shuffle scope only if Scene scope is empty.
3. Add migrated scenes to the enabled set. If the old wallpaper preference selected one, set the Night Scenes preferred scene only if no newer preference exists.
4. Make migration idempotent; leave old files / keys untouched for rollback.
5. Keep lock-screen photo / gradient choices; rename “CLASSIC WALLPAPERS” to a neutral photos-and-gradients label.

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
