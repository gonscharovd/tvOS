# Install and set up

## Requirements

- Android TV / Google TV, Android 7.0+ (min API 24); target API 34.
- Developer options and network debugging enabled.
- A computer with Android SDK platform-tools for initial ADB setup, or the TV's supported local ADB workflow.

## Build

Install JDK 17+, Android SDK platform 34 and build-tools 34.0.0.

Linux/macOS:

```sh
./build.sh
```

Windows PowerShell:

```powershell
.\build.ps1
```

Both scripts produce `build/tvos-home.apk` and accept `VERSION_CODE` / `VERSION_NAME` overrides. Current defaults differ: build.sh uses `15 / 2.1.2-testOS1`; build.ps1 uses `13 / 2.1.2-custom1`. This is an open build-contract issue, so specify the version explicitly when reproducibility matters. Do not assume either default is the latest stable release (v2.1.73 / versionCode 88).

## Signing and updates

Android allows an in-place update only when the APK certificate matches the installed app.

- Local development builds use the selected debug keystore unless `RELEASE_KEYSTORE` is configured.
- Pull-request CI preview builds use a runner-generated debug key. They are not signed with the persistent release key and must not be installed over the release-signed app unless the certificate has been compared.
- Official release builds use the persistent release key configured in GitHub Actions Secrets. The release workflow verifies the expected certificate fingerprint and package/version metadata.

The PR workflow uploads a preview artifact but does not publish a release. Official OTA releases are built from `release/2.1.PATCH` branches; versionCode is `15 + PATCH`. The latest stable release is testOS v2.1.73 / versionCode 88. PR #14 remains Draft and is not yet an OTA release.

Do not uninstall the current launcher to work around a signature mismatch. Do not install an artifact whose certificate has not been verified against the installed app.

## Install and select HOME

The source manifest package is `com.techgenius.tvoslauncher`; the build renames the APK application package to `com.gonscharovd.tvoslauncher`. The compiled PR #14 CI APK was inspected and confirms the HOME activity class remains fully qualified as `com.techgenius.tvoslauncher.MainActivity`.

Connect to the TV and install:

```sh
adb connect TV_IP:5555
adb install -r build/tvos-home.apk
adb shell pm set-home-activity com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity
```

If Android asks which Home app to use, select testOS. Keep the stock Sber launcher installed and enabled as a fallback; do not disable system launcher packages as part of normal setup.

## Permissions

Grant only the permissions required by the features you use:

| Feature | Permission / route |
|---|---|
| Remote Button Mapper | Enable testOS in Android Accessibility settings; follow the setup instructions for secure settings if requested |
| Keep Alive / local ADB repair | `WRITE_SECURE_SETTINGS` through the supported setup flow |
| DND toggle | Android Do Not Disturb policy access; if the TV has no settings page, an authorized ADB shell can run `adb shell cmd notification allow_dnd com.gonscharovd.tvoslauncher` |
| Weather location / Wi-Fi name | Location permission when needed; manual city selection is available |
| Voice search | Microphone permission or the TV's own voice prompt |
| Custom wallpapers/icons and backup | Storage access requested by Android when required |
| CleanYourTV | Local ADB connection; no root required |

No notification-listener access is required. The old media-session tracking service has been removed.

## Backup and restore

Open Settings → Information. Save the settings backup to `Download/tvos-launcher-settings.json`; restore from that file or another JSON backup. The backup contains allowlisted preferences only, not the APK, installed apps or custom media files. CleanYourTV's disabled-package backup is separate.

## Rollback

To return to the stock launcher, use Android's Home app selection in system settings and choose the stock Sber launcher. Keep it enabled. Once stock HOME is active, testOS can be uninstalled from Android's app settings if desired.
