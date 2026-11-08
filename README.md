# testOS — Android TV launcher

A lightweight, Russian-only Android TV home screen based on the tvOS-inspired launcher in this repository. This device-specific build is maintained for the Sber SDX-50U4123B (Android 9 / API 28), while retaining the broader Android TV compatibility of the codebase.

**Current preview:** testOS 2.1.72 · `versionCode 87` · package `com.gonscharovd.tvoslauncher`

## What is included

- Night Scenes home screen with rotating photo and gradient backgrounds, custom scenes, a clock, weather and a glass app dock.
- Remote-first navigation with D-pad focus, app launch, folders, hidden apps and custom app names.
- Universal Control Panel overlay, Recent Apps switcher and remote Button Mapper.
- Search for installed apps, settings and titles via JustWatch; Russian and English input layouts and TV voice input.
- Weather from Open-Meteo, MET Norway or wttr.in.
- CleanYourTV: on-device model detection, protected package lists, debloat status, restore and AdGuard DNS.
- Advanced controls: default HOME selection, ADB status/setup, permissions, Keep Alive and system settings.
- OTA updates from GitHub Releases, download progress and local settings backup/restore.

The Control Panel itself remains available. Its former separate customization page has been removed.

## Simplified device-specific build

This build intentionally has one user and one interface language (Russian). It removes the language selector and non-Russian UI packs, multi-user switching, news and recommendation feeds, and Nuvio/Stremio cloud catalogue/sign-in integrations. The former notification-listener media-session service is removed. A small native Continue Watching row may still display content that installed TV apps publish through Android TV's TV Provider; it does not use cloud catalogue integrations.

Search still offers an English keyboard layout for entering English titles and queries.

## Settings layout

1. **Внешний вид** — appearance, clock, dock/grid sizing, wallpapers, scenes and weather.
2. **Основные настройки** — navigation sounds, hidden apps, setup restart, reset and Android TV settings.
3. **Информация** — testOS identity, OTA updates, backup and restore.
4. **Расширенные настройки** — HOME launcher, Button Mapper, ADB, permissions, Keep Alive, CleanYourTV and AdGuard DNS.

## Build

Requirements: JDK 17, Android SDK platform 34 and build-tools 34.0.0.

Linux/macOS:

```sh
./build.sh
VERSION_CODE=85 VERSION_NAME=2.1.72 ./build.sh
```

Windows PowerShell:

```powershell
$env:VERSION_CODE = "85"
$env:VERSION_NAME = "2.1.72"
.\build.ps1
```

Both scripts produce `build/tvos-home.apk`. The scripts use the same version defaults and support `DEBUG_KEYSTORE` or `RELEASE_KEYSTORE` signing variables. An in-place Android update requires the same signing certificate as the installed app.

## CI and OTA

- Pull requests run regression tests, build the APK, verify its package/version/signature and upload a signed preview artifact.
- The preview workflow uses the persistent release signing key and checks its certificate fingerprint. It does not publish an OTA release.
- Official releases are built from `release/2.1.PATCH` branches. Version code is `15 + PATCH`; for example, 2.1.72 uses versionCode 87.
- The OTA updater reads the latest GitHub Release and installs through Android's PackageInstaller or the configured local ADB path.

Do not install an APK signed with a different key over the existing launcher. Do not disable or remove the stock Sber launcher; keep it available as a fallback.

## Settings backup

The backup file is `Download/tvos-launcher-settings.json`. It contains an allowlisted set of user-facing preferences (home layout, appearance, weather, button mappings and related settings). It intentionally excludes ADB/security state, CleanYourTV's disabled-package backup, search history, update state and removed cloud features. Restore validates the format and merges saved keys without clearing newer settings.

## Safety

- CleanYourTV checks protected package names and verifies actual package state after disable/restore operations.
- Keep the stock Sber launcher and critical system components enabled.
- No root access or system-partition modifications are required.
- A settings backup is not a firmware image and does not back up installed APKs or custom media files.

See [Features](docs/features.md), [Architecture](docs/architecture.md) and [Install](docs/install.md) for details.
