# testOS — Android TV launcher

A lightweight, Russian-only Android TV home screen based on the tvOS-inspired launcher in this repository. This device-specific build is maintained for the Sber SDX-50U4123B (Android 9 / API 28), while retaining the broader Android TV compatibility of the codebase.

**Latest stable release:** testOS v2.1.73 · `versionCode 88` · package `com.gonscharovd.tvoslauncher`.

The current core-cleanup work is in Draft PR [#14](https://github.com/gonscharovd/tvOS/pull/14) and is not part of `main` or the published OTA release.

## What is included

- Night Scenes home screen with rotating photo and gradient backgrounds, custom scenes, a clock, weather and a glass app dock.
- Remote-first navigation with D-pad focus, app launch, dock reordering, More Apps, hidden apps, custom app names and custom icons.
- Universal Control Panel overlay, Recent Apps switcher and remote Button Mapper.
- Search for installed apps, settings and titles via JustWatch; Russian and English input layouts and TV voice input.
- Weather from Open-Meteo, MET Norway or wttr.in.
- CleanYourTV: on-device model detection, protected package lists, debloat status, restore and AdGuard DNS.
- Advanced controls: default HOME selection, ADB status/setup, permissions, Keep Alive and system settings.
- OTA updates from GitHub Releases, download progress and local settings backup/restore.
- Built-in Ambient / lock-screen scene display with clock, date, weather and greeting.

The Control Panel overlay remains available. Its former separate customization page has been removed.

## Simplified device-specific build

This build intentionally has one user and one interface language (Russian). It removes the language selector and non-Russian UI packs, multi-user switching, Classic home UI, the Hero / Continue Watching shelf, news and recommendation feeds, and Nuvio/Stremio cloud catalogue/sign-in integrations. The former notification-listener media-session service is removed.

Search still offers an English keyboard layout for entering English titles and queries. Legacy Classic wallpaper assets and preferences are retained only as migration inputs; they are not an active home interface. The app dock uses a separate More Apps area rather than user-created app folders.

## Settings layout

The sidebar has four labelled groups:

1. **Внешний вид** — appearance, clock, dock sizing and Night Scenes gallery.
2. **Основные настройки** — navigation sounds, hidden apps, setup restart, reset and Android TV settings.
3. **Информация** — testOS identity, OTA updates, backup and restore.
4. **Расширенные настройки** — HOME launcher, Button Mapper, ADB, permissions, Keep Alive, CleanYourTV and AdGuard DNS.

Weather has its own settings entry under the appearance section.

## Build

Requirements: JDK 17+, Android SDK platform 34 and build-tools 34.0.0.

Linux/macOS:

```sh
./build.sh
```

Windows PowerShell:

```powershell
.\build.ps1
```

Both scripts produce `build/tvos-home.apk` and accept `VERSION_CODE` / `VERSION_NAME` overrides. **Their current defaults differ:** `build.sh` uses `15 / 2.1.2-testOS1`; `build.ps1` uses `13 / 2.1.2-custom1`. This is tracked as an open build-contract task; do not assume a local development APK has the stable release version. The scripts also support `DEBUG_KEYSTORE` or `RELEASE_KEYSTORE` signing variables. An in-place Android update requires the same signing certificate as the installed app.

## CI and OTA

- Pull requests targeting `main` run regression tests, build the APK, verify package/version/signature and upload a preview artifact.
- The PR build uses a runner-generated debug key, not the persistent release signing key. Its artifact is for CI validation and must not be installed over the release-signed TV app unless the certificate has been explicitly compared.
- Official releases are built from `release/2.1.PATCH` branches. Version code is `15 + PATCH`; v2.1.73 uses versionCode 88.
- The OTA updater reads the latest GitHub Release and installs through Android's PackageInstaller or the configured local ADB path.
- PR #14 is still Draft; its code has not been merged or published as OTA.

Do not install an APK signed with a different key over the existing launcher. Do not disable or remove the stock Sber launcher; keep it available as a fallback.

## Settings backup

The backup file is `Download/tvos-launcher-settings.json`. It contains an allowlisted set of user-facing preferences (home layout, appearance, weather, button mappings and related settings). It intentionally excludes ADB/security state, CleanYourTV's disabled-package backup, search history, update state and removed cloud features. Restore validates the format and merges saved keys without clearing newer settings.

A settings backup is not a firmware image and does not back up installed APKs or custom media files.

## Safety

- CleanYourTV checks protected package names and verifies actual package state after disable/restore operations.
- Keep the stock Sber launcher and critical system components enabled.
- No root access or system-partition modifications are required.

See [Features](docs/features.md), [Architecture](docs/architecture.md) and [Install](docs/install.md) for details.
