# Features

This document describes the Night Scenes-only, Russian-language testOS implementation in PR #14. The PR remains Draft and is not yet included in `main` or the stable OTA release.

## Home screen

- **Night Scenes** — rotating photo and gradient backgrounds, custom scenes, large clock, weather and frosted app dock. Legacy Classic background assets/preferences are retained as migration sources and surfaced through the Night Scenes gallery; the Classic home interface itself is removed.
- App tiles support launch, dock reordering, moving apps between the Home dock and More Apps, custom names/icons, hide/show and uninstall shortcuts where Android permits. There is no user-created app-folder model.
- D-pad focus, long-press actions and return-to-home behavior are designed for a TV remote.
- The Control Panel overlay and Recent Apps switcher can be opened without leaving the current app.

## Ambient and lock screen

Night Scenes can run as a full-screen ambient showcase with a large clock, date, weather and greeting. Lock-screen mode uses the scene renderer and can be configured separately. A key press restores the normal Home interface.

## Search

- Searches installed apps and launcher settings immediately; title lookup remains available through JustWatch.
- Supports Russian and English keyboard layouts, digits and the TV's available voice-input service.
- Search history is local to the device.
- No Nuvio, Stremio or Cinemeta catalogue lookup is performed, and search does not populate a recommendation feed.

## Weather

Weather can be enabled or hidden from the home screen. Supported sources are Open-Meteo, MET Norway and wttr.in, with units, location and refresh interval configured in Settings. The Night Scenes weather shortcut opens the five-day forecast overlay.

## Control Panel

The universal overlay remains available above the launcher and supported apps. It provides quick access to the supported system controls, including network, Bluetooth, sound, DND, inputs and ambient/sleep actions where the TV firmware exposes them. The separate page for assigning apps/actions to Control Panel tiles has been removed; the overlay itself remains.

## Remote and system controls

- **Button Mapper** — single/double/triple press actions for supported remote keys.
- **Keep Alive** — monitors and repairs the accessibility service used by remote mapping.
- **Default HOME** — select testOS or the stock launcher without removing the stock fallback.
- **ADB** — setup/status and local permission repair.
- **DND** — uses Android's Do Not Disturb policy access directly; no notification-listener service is installed.

## Settings

The sidebar has four labelled groups: Appearance, Basic settings, Information and Advanced settings. Weather is a separate settings entry under Appearance.

- **Appearance** — scene gallery, clock, dock sizing and visual options.
- **Basic settings** — navigation sounds, hidden apps, setup restart, reset and Android TV settings.
- **Information** — testOS version, OTA updates, backup and restore.
- **Advanced settings** — HOME selection, Button Mapper, ADB, permissions, Keep Alive, CleanYourTV and AdGuard DNS.

The UI is Russian-only. There is no language selector or multi-user profile switcher.

## CleanYourTV

CleanYourTV runs on the TV through Local ADB. It includes model detection, curated package profiles, manual package inspection, safe-disable checks, confirmation for system apps, verified disable/restore state and a restore path. Critical Android/Sber packages and the stock launcher remain protected.

CleanYourTV's disabled-package backup is separate from the general launcher settings backup. Restoring general settings never overwrites the package-disable backup.

## OTA updates

The updater checks the latest GitHub Release, compares versions, downloads the APK and shows progress. Installation uses Android's PackageInstaller confirmation flow or the configured local ADB route. The APK must have the same signing certificate as the installed package. The latest stable release is v2.1.73 / versionCode 88; PR #14 is not yet released.

## Settings backup and restore

The JSON backup is stored in `Download/tvos-launcher-settings.json`. It contains only allowlisted user-facing preferences. It excludes ADB/security state, CleanYourTV package backups, search history, update runtime state and removed integrations. Restore validates the file and merges saved values without clearing newer preference keys.

The backup does not contain the APK, installed apps, wallpaper files or other external media.

## Removed from this build

- Classic home UI, including its app grid and wallpaper-selection interface.
- Hero / Continue Watching shelf and its TV Provider / media-session paths.
- Non-Russian UI language packs and language selection.
- Multi-user profiles and profile switching.
- News feeds and recommendation/catalogue carousel engines.
- Nuvio and Stremio cloud catalogue/sign-in/history integrations.
- Notification-listener media-session tracking.
- The separate Control Panel customization page (the Control Panel overlay remains).
