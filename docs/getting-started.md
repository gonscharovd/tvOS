# Getting started: build and run testOS

This guide covers the current Russian-only, single-user Android TV build. The project uses Android SDK command-line tools; Android Studio and Gradle are not required.

## 1. Requirements

| Tool | Version |
|---|---|
| JDK | 17 or newer |
| Android SDK platform | android-34 |
| Android SDK build-tools | 34.0.0 |
| Android SDK platform-tools | latest |
| TV | Android TV 7.0+ (API 24+) |

The target device for this fork is the Sber SDX-50U4123B running Android 9 / API 28. Always test remote focus and performance on the actual TV.

## 2. Install SDK tools

Install JDK 17 and Android command-line tools, then install:

```sh
sdkmanager "platform-tools" "build-tools;34.0.0" "platforms;android-34"
```

Set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) and ensure `javac` is available on PATH.

## 3. Build

Linux/macOS:

```sh
./build.sh
./build.sh
```

Windows PowerShell:

```powershell
.\build.ps1
```

Both scripts create `build/tvos-home.apk` and accept explicit version overrides. Their defaults currently differ: build.sh uses `15 / 2.1.2-testOS1`, while build.ps1 uses `13 / 2.1.2-custom1`. This is tracked as an open build-contract task. Local builds normally use a debug key; configure a release keystore only when the matching signing materials are available. Do not install an APK with an unverified signing certificate over the existing launcher.

## 4. Install on a TV

1. Enable Developer options and network debugging on the TV.
2. Connect from a computer on the same network:

```sh
adb connect <tv-ip>:5555
adb install -r build/tvos-home.apk
adb shell pm set-home-activity com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity
```

3. Finish the on-screen setup and select testOS as the Home app.
4. Keep the stock Sber launcher installed and enabled as a fallback. Do not disable system launcher packages.

See [install.md](install.md) for signing, permissions, backup and rollback details.

## 5. Developer shortcuts

```sh
# Open the launcher
adb shell am start -n com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity

# Open the Control Panel overlay
adb shell am start -n com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity --es panel cc

# Open Search with a query
adb shell am start -n com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity --es panel search --es q youtube

# Open a Settings group
adb shell am start -n com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.SettingsActivity --es page advanced

# Open a setup step directly
adb shell am start -n com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.SetupActivity --ei step 3

# Mark setup as complete
adb shell am start -n com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.SetupActivity --ez skip true

# Performance probe (weather/status/dockglass/shadows; reset restores)
adb shell am start -n com.gonscharovd.tvoslauncher/com.techgenius.tvoslauncher.MainActivity --es perf weather

# Frame-time report
adb shell dumpsys gfxinfo com.gonscharovd.tvoslauncher
```

## 6. Troubleshooting

| Problem | What to check |
|---|---|
| Missing aapt2 / d8 | Install build-tools 34.0.0 and platform android-34 |
| javac not found | Install JDK 17+ and set JAVA_HOME |
| INSTALL_FAILED_UPDATE_INCOMPATIBLE | APK certificate differs from the installed app. Do not uninstall blindly; verify the release certificate and rebuild with the matching key |
| adb offline / unauthorized | Accept the TV's debugging prompt, then reconnect |
| Home still opens stock launcher | Select testOS in Android's Home app settings or use the HOME command in install.md |
| UI stutters | Capture gfxinfo and meminfo on the TV; do not infer TV performance from an emulator |

Check `adb logcat -s TvHome AndroidRuntime` for runtime errors.
