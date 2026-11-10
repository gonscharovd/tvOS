# Architecture

This document describes the current Night Scenes-only implementation in PR #14. The PR is still Draft and has not been merged into `main`.

## Build pipeline

The project uses Android SDK command-line tools rather than Gradle:

1. `aapt2` compiles resources and links the manifest.
2. `javac --release 11` compiles Java against Android API 34.
3. `d8` creates dex bytecode for min API 24.
4. `zipalign` aligns the APK.
5. `apksigner` signs with either a development key or the persistent release key.

`build.sh` and `build.ps1` both output `build/tvos-home.apk`. Local builds can override versions with `VERSION_CODE` and `VERSION_NAME`. Their current defaults do not match: build.sh uses `15 / 2.1.2-testOS1`; build.ps1 uses `13 / 2.1.2-custom1`. This remains an open build-contract task. The latest stable release is testOS v2.1.73 / versionCode 88.

## Application identity

- Source manifest package: `com.techgenius.tvoslauncher`.
- Build links the APK with manifest package `com.gonscharovd.tvoslauncher`.
- The compiled PR #14 CI APK was inspected: its HOME activity is `com.techgenius.tvoslauncher.MainActivity`, while its application package is `com.gonscharovd.tvoslauncher`.
- Therefore explicit ADB component commands must use the application package before the slash and the fully qualified Java class after it; do not shorten the activity to `.MainActivity`.

## Code map

| Path | Responsibility |
|---|---|
| `MainActivity.java` | HOME Activity lifecycle, app discovery/launch, overlay dispatch, weather and ambient/lock-screen coordination, shared bitmap/backdrop helpers |
| `SceneHome.java`, `Scenes.java` | Only active Home UI: Night Scenes, clock, dock, More Apps, D-pad focus, app actions, scene gallery and legacy-background migration |
| `SettingsActivity.java` | Four-group settings navigation, D-pad focus, scene/weather settings, OTA, CleanYourTV and backup/restore entry points |
| `SetupActivity.java` | First-run setup for the single-user Russian UI and Night Scenes |
| `I18n.java` | Russian UI dictionary and dynamic labels |
| `Search.java`, `SearchPanel.java` | App/settings search, JustWatch title lookup, voice input and Russian/English keyboard layouts |
| `Suggestions.java` | Shared safe activity launch, URL intent, artwork loading/cache and stream-reading helpers; no recommendation engine |
| `AppUpdater.java`, `VersionComparator.java` | GitHub Release discovery, version comparison, download progress and PackageInstaller flow |
| `CleanYourTv.java` | TV model detection, protected package policy, package-state verification, backup and restore |
| `LocalAdb.java` | Local ADB setup/status and accessibility permission repair |
| `Perms.java` | Location, DND policy access, HOME selection and system-settings fallbacks |
| `ButtonMapper.java`, `PowerService.java` | Remote-button remapping and accessibility service |
| `ControlPanelOverlay.java` | Universal Control Panel overlay |
| `RecentAppsOverlay.java` | Recent-app switcher |
| `Weather.java`, `WeatherView.java`, `WeatherIcons.java` | Forecast sources, cache, overview card and forecast panel |
| `CustomMedia.java` | Custom app icons/artwork, legacy wallpaper readers and validated settings backup/restore |
| `AirPlayGuard.java` | Optional AirPlay receiver visibility/recovery |
| `Inputs.java`, `SleepReceiver.java`, `Clicks.java` | TV inputs, sleep timer and navigation sounds |
| `present/` | Optional browser-based presentation server and full-screen player |
| `assets/` | Fonts, wallpaper artwork, CleanYourTV model data and presentation web app |

The retired Classic home grid/wallpaper interface, Hero / Continue Watching shelf, news/recommendation engines, Nuvio/Stremio cloud clients and notification-listener media-session service are not active features in PR #14. Legacy Classic wallpaper/folder preference keys and assets remain only where required for migration and rollback.

## Settings navigation

The sidebar has four labelled groups:

1. **Appearance** — scene gallery, clock, dock sizing and visual options.
2. **Basic settings** — navigation sounds, hidden apps, setup restart, reset and Android TV settings.
3. **Information** — app identity, OTA update state and local settings backup/restore.
4. **Advanced settings** — default HOME, Button Mapper, ADB, permissions, Keep Alive, CleanYourTV and AdGuard DNS.

Weather is a separate sidebar entry under Appearance. The Control Panel overlay is independent of Settings and remains available; its former customization page is not part of navigation.

D-pad behavior is handled in `SettingsActivity.dispatchKeyEvent`: Up/Down move within the active pane, Right enters the content pane, Left/Back returns to the sidebar or closes a submenu, and scrolling preserves focus.

## Data and safety

- User-facing preferences are stored in SharedPreferences.
- `SingleUserPrefs` maps active user-facing settings to the canonical `home` preference file. `SingleUserMigration` copies selected legacy profile values while preserving old preferences/metadata for rollback.
- Scene migration copies legacy Classic wallpaper/folder-shuffle data only when the corresponding Night Scenes destination is absent or empty. Original legacy files and keys are not deleted.
- Backup exports only an explicit allowlist. It excludes ADB credentials/state, CleanYourTV package-disable backups, search history, OTA runtime state and obsolete integrations.
- Restore accepts supported backup versions, validates package/format/types/size before applying, and merges known keys instead of clearing whole preference files.
- Backup writes use Android `AtomicFile` so a failed write does not leave a truncated JSON file.
- CleanYourTV confirms package state after every operation and protects critical Sber/Android packages.
- The app does not require root and does not write to `/system` or `/vendor`.

## Performance constraints

The target device is a low-memory Android 9 TV with a Mali-class GPU. Keep the UI thread free of network, package scanning and large bitmap decoding. Use bounded bitmap caches, downsampled artwork, background executors and memory-trim eviction. Avoid perpetual animations and full-screen overdraw.

Useful device-side measurements:

```sh
adb shell dumpsys gfxinfo com.gonscharovd.tvoslauncher
adb shell top -m 10
adb shell dumpsys meminfo com.gonscharovd.tvoslauncher
```

## CI and release signing

- `.github/workflows/build-apk.yml` runs the PR regression suite and builds a preview APK.
- PR preview artifacts use a runner-generated debug key; they are not guaranteed to update the release-signed app in place.
- `.github/workflows/release.yml` uses the persistent release keystore from GitHub Secrets and verifies the expected certificate fingerprint before publishing.
- Release versionCode currently follows `15 + PATCH`; v2.1.73 is versionCode 88.
- `tests/SingleUserMigrationTest.java` exists, but the current CI workflow does not compile/run it yet. See DEBT-008 in project memory.

CI does not replace D-pad, lifecycle, permission, OTA and performance tests on the target Sber TV.
