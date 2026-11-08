# testOS Launcher — dependency map for core cleanup

**Audit date:** 2026-11-09  
**Repository:** `gonscharovd/tvOS`  
**Audited base:** `main` / `a765318b577e66fae66a8ffe28439dcd6233f38f`  
**Working branch:** `refactor/testos-core-cleanup-2026-11`  
**Status (2026-11-09):** Classic home UI removal implemented in PR #14; latest CI on head `c08bc91` passed (run #293). Documentation sync is in progress on the PR #15 branch. No OTA release or TV installation.

## Confirmed baseline

- Stable release: `v2.1.73`, versionCode `88`, package `com.gonscharovd.tvoslauncher`.
- Tag points to the audited commit. Release APK: 6,192,577 bytes; SHA-256 `769acad3561983ba5a0b8d20b0547f08a16180843c1c9e20441d8bb41b846754`.
- User confirmed installation on Sber SDX-50U4123B and basic launch / D-pad navigation. Full regression on TV is not yet established.
- Target compatibility remains Android 9 / API 28 / ARMv7.
- No open PRs existed when the branch was created. No OTA release is being published.

## 1. Retired Continue Watching / Hero shelf

The old shelf and recommendation carousel are removed from the PR #14 implementation.

- `SceneHome.java` no longer builds a Continue Watching row or owns watch-card caches, package polling, thumbnails, shelf focus or shelf navigation.
- `MainActivity.java` no longer owns Hero carousel state, item models, timers, transitions or TV Provider permission-result handling.
- `Suggestions.java` retains shared helpers for safe activity launch, URL intents, image loading and stream reading; it no longer contains the Hero recommendation model/API.
- `AndroidManifest.xml` contains no `READ_TV_LISTINGS` or `READ_EPG_DATA` permission.
- Search artwork loading and app launch helpers remain in use and must not be removed with the retired feed UI.

No app-published TV Provider row is part of the current Night Scenes home screen.

## 2. Classic home UI — completed implementation

**Implemented in PR #14 head `c08bc91e06b7fa66f6bd185e3ab5633033884c33`; latest CI run #293 passed.**

The current `MainActivity.buildChrome()` creates `SceneHome` and its `SceneView` as the only home experience. The old Classic interface is no longer constructed or toggled.

### Removed from MainActivity.java

- `applyLookMode()` and the old Classic look-mode switching path.
- Classic app grid: `buildGrid()`, `makeCell()`, old grid focus / scrolling / move state, and the synthetic `FOLDER_KEY` pseudo-folder.
- Classic wallpaper rendering: `setWallpaper()`, `makeWallpaper()`, wallpaper drift, prefetch, wallpaper blur and wallpaper selection state.
- Hero / Continue Watching carousel, its models, View tree, navigation, animations, timers and permission result handling.
- Old Classic status chrome: clock/tabs, network glyphs, refresh/search/Control Center buttons and the old weather card.
- Old carousel idle / full-screen transition logic and obsolete focus-sway / tile-grid handlers.

The cleanup reduced `MainActivity.java` from 4,522 to 2,438 lines (2,084 lines removed). The unused `LaunchCard` / `launchBackground` tile-to-full-screen animation and its helpers were removed because no active caller remained. Current app launch still uses `launchAnimated()` and the Android activity transition; SceneHome retains its own dock focus and move animations.

### Preserved Night Scenes and shared functionality

- `SceneHome.java` owns the dock, `MORE_KEY` / `moreExpanded`, D-pad focus/navigation, app menus, dock reordering and Other Apps.
- `MainActivity.java` retains `AppEntry`, `loadApps()`, order/hidden/Other Apps persistence, package receiver, app launch, Search / Weather / Control Center / Recent Apps overlays, LiquidGlass, `glassBg` / `glassSource`, generic app-artwork rendering and root container.
- `dispatchKeyEvent()` delegates Home navigation to `SceneHome.dispatchKey()`. Search and overlay key handling remain in MainActivity.
- Weather refresh, AirPlayGuard checks, Local ADB permission repair and connectivity recovery remain active.
- `MainActivity.makeWallpaperBase()` remains because Setup and Settings use it for scene/gradient previews.
- `CustomMedia.buildAppBannerFromCustomIcon()` is the neutral shared app-banner generator used by Search and Recent Apps.
- `Suggestions.image()` and `Suggestions.start()` remain. The obsolete Hero-only `Suggestions.Item`, `open()` and `logoFor()` API was removed.
- `Scenes.CLASSIC_PHOTOS`, `CLASSIC_GRADIENTS`, underlying assets and migration methods remain as legacy data/image sources, not as a Classic home UI.
- CustomMedia legacy Classic wallpaper/folder readers remain for idempotent migration; old files and keys are not deleted.
- Lock-screen photo/gradient choices remain available; their category is now PHOTOS & GRADIENTS.
- `SetupActivity.lookSubStep` and its obsolete Classic appearance back-navigation branch are removed.

### “Folders” clarification

There is no user-created app-folder model in the current Night Scenes flow. `FOLDER_KEY` represented only the old Classic-grid Other Apps tile. Active Night Scenes uses `MORE_KEY` / `moreExpanded`, backed by `PREF_OTHER`. App order (`PREF_ORDER`), hidden state (`PREF_HIDDEN`) and app menus are preserved. Wallpaper folder-shuffle preferences are a separate feature and remain supported.

## 3. Single-user migration — implemented

The multi-profile UI/model has been removed from the active flow. The PR #14 implementation uses `SingleUserPrefs` and `SingleUserMigration`.

- User-facing preferences resolve through the canonical `home` preference file.
- Migration reads legacy profile metadata and copies the selected profile's values into the canonical preference namespace.
- Existing legacy preference files and metadata are preserved for rollback; migration is designed to be idempotent.
- Scene migration separately preserves Classic wallpaper and wallpaper-folder shuffle data when the Night Scenes destination is empty.
- Do not reintroduce the old destructive `Profiles.list()` cleanup path or delete legacy user files during this refactor.
- Historical limitation: if v2.1.73 already ran old profile cleanup on a device with multiple profiles, files deleted by that older version cannot be reconstructed.

`tests/SingleUserMigrationTest.java` exists, but it is not currently compiled/run by `.github/workflows/build-apk.yml` (DEBT-008).

## 4. Build, versions and documentation status

| File | Confirmed current state | Follow-up |
|---|---|---|
| `build.sh` | Defaults 15 / `2.1.2-testOS1`; contains a macOS-specific JDK fallback. | TOS-002: move to a shared version source; keep environment overrides. |
| `build.ps1` | Defaults 13 / `2.1.2-custom1`; contains a hard-coded JDK path for another developer's Windows account. | TOS-002/TOS-004: unify version source and prefer `JAVA_HOME` / PATH. |
| `.github/workflows/build-apk.yml` | PR builds inherit build.sh defaults and use runner debug signing. | Add SingleUserMigrationTest to CI; preserve preview/release signing separation. |
| `.github/workflows/release.yml` | `versionCode = 15 + PATCH`; v2.1.73 maps to 88 and release certificate fingerprint is checked. | Keep formula and signing fingerprint; validate release branch version. |
| `README.md`, `docs/features.md`, `docs/architecture.md`, `docs/install.md`, `docs/getting-started.md`, `docs/README.md` | Updated in docs PR branch to remove Classic/Hero/Continue Watching claims, describe current stable release and signing model, and correct ADB component strings. | Review the docs PR diff; no further rewrite is pending for TOS-003. |
| `tests/test_build_contract.py` | Does not enforce a single source of truth for version defaults. | Extend after TOS-002. |

Stable release remains v2.1.73 / versionCode 88. The planned next development target 2.1.74 / 89 is not a published release. Keep package name, APK output path, release key/alias, certificate fingerprint verification, OTA comparator and PackageInstaller flow unchanged.

## 5. Manifest, resources and verification

- The current source manifest has no `READ_TV_LISTINGS` or `READ_EPG_DATA` declarations; no permission removal is pending for the retired shelf.
- The compiled PR #14 CI APK was inspected. Its manifest package is `com.gonscharovd.tvoslauncher`; the HOME activity class remains `com.techgenius.tvoslauncher.MainActivity`. The correct flattened component is therefore `com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity`, not `com.gonscharovd.tvoslauncher/.MainActivity`.
- No Classic-specific XML layout was identified; the old UI was built programmatically.
- Preserve `assets/backgrounds`, `assets/fonts`, `assets/app-logos`, CleanYourTV examples, presentation assets, app banner/launcher drawables, generic animations, device-admin XML and power-service XML unless a full reference scan proves an individual resource unused.
- Regression tests cover removed shelf/Classic/profile symbols and Night Scenes dependencies. CI run #293 passed on PR #14 head.
- Manual Sber TV tests remain necessary: Night Scenes, D-pad, dock/More Apps, app move/hide/reorder, Search, Control Panel, Weather, gallery, custom wallpaper/folder shuffle, lock screen, backup/restore and OTA from v2.1.73.
- Runtime testing on the physical TV has not been completed for this PR. Do not install the debug CI artifact over the release-signed launcher.

## 6. Explicit non-goals

Do not change firmware, `/system`, `/vendor`, bootloader, system partitions, package name, signing rules, CleanYourTV protection, OTA architecture, app library, Control Panel, Weather, Search, Recent Apps, Button Mapper, PowerService, LocalAdb keepalive, VPN, AirPlayGuard or unrelated subsystems. Do not publish OTA without separate user approval.
