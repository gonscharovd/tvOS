# Architecture

## Build pipeline

The project uses Android SDK command-line tools rather than Gradle:

1. `aapt2` compiles resources and links the manifest.
2. `javac --release 11` compiles Java against Android API 34.
3. `d8` creates dex bytecode for min API 24.
4. `zipalign` aligns the APK.
5. `apksigner` signs with either a development key or the persistent release key.

`build.sh` and `build.ps1` both output `build/tvos-home.apk` and use the same default version (2.1.70 / versionCode 85). Override with `VERSION_NAME` and `VERSION_CODE`.

## Code map

| Path | Responsibility |
|---|---|
| `MainActivity.java` | Home screen, app grid/dock, focus, launch transitions, weather/status, Control Panel entry, Recent Apps and idle behavior |
| `SceneHome.java`, `Scenes.java` | Night Scenes layout, clock, dock and optional app-published TV Continue Watching row |
| `SettingsActivity.java` | Four-group settings navigation, D-pad focus, OTA progress, CleanYourTV menus and backup/restore entry points |
| `SetupActivity.java` | First-run setup; no language or profile step |
| `I18n.java` | Russian-only UI dictionary and dynamic Russian labels |
| `Search.java`, `SearchPanel.java` | App/settings search, voice input and Russian/English keyboard layouts |
| `Suggestions.java` | Small shared utilities for safe activity launch, URL intents, cached artwork and stream reading; no recommendation/news engine |
| `AppUpdater.java`, `VersionComparator.java` | GitHub Release discovery, version comparison, download progress and PackageInstaller flow |
| `CleanYourTv.java` | TV model detection, protected package policy, package-state verification, backup and restore |
| `LocalAdb.java` | Local ADB setup/status and accessibility permission repair |
| `Perms.java` | Location, DND policy access, HOME selection and system-settings fallbacks |
| `ButtonMapper.java`, `PowerService.java` | Remote-button remapping and accessibility service |
| `ControlPanelOverlay.java` | Universal Control Panel overlay |
| `RecentAppsOverlay.java` | Recent-app switcher |
| `Weather.java`, `WeatherView.java`, `WeatherIcons.java` | Forecast sources, cache, overview card and forecast panel |
| `CustomMedia.java` | Custom icons/wallpapers and validated settings backup/restore |
| `AirPlayGuard.java` | Optional AirPlay receiver visibility/recovery |
| `Inputs.java`, `SleepReceiver.java`, `Clicks.java` | TV inputs, sleep timer and navigation sounds |
| `present/` | Optional browser-based presentation server and full-screen player |
| `assets/` | Fonts, wallpaper artwork, CleanYourTV model data and presentation web app |

Removed source files include the news feed, Nuvio/Stremio cloud clients, cloud watch-history clients and notification-listener media service.

## Settings navigation

The sidebar contains four groups:

1. **Appearance** — appearance and clock, sizing, wallpapers, scenes and weather.
2. **Basic settings** — navigation sounds, hidden apps, setup restart, reset and Android TV settings.
3. **Information** — app identity, OTA update state and local settings backup/restore.
4. **Advanced settings** — default HOME, Button Mapper, ADB, permissions, Keep Alive, CleanYourTV and AdGuard DNS.

The Control Panel overlay is independent of Settings and remains available. Its former customization page is not part of the navigation.

D-pad behavior is handled in `SettingsActivity.dispatchKeyEvent`: Up/Down move within the active pane, Right enters the content pane, Left/Back returns to the sidebar or closes a submenu, and scrolling preserves focus.

## Data and safety

- User-facing preferences are stored in SharedPreferences.
- Backup exports only an explicit allowlist. It excludes ADB credentials/state, package-disable backups, search history, OTA runtime state and obsolete integrations.
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
