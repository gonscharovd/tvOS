# Contributing to tvOS Launcher

Thanks for helping. tvOS Launcher is a tvOS 26–style home screen for Android TV: plain Java, no
Gradle, no third-party runtime libraries, built to stay smooth on low-end TV boxes. This guide
covers how to work on it; to set up your machine first, see
**[docs/getting-started.md](docs/getting-started.md)**.

## Contents

1. [Project in one page](#1-project-in-one-page)
2. [Workflow](#2-workflow)
3. [Code style](#3-code-style)
4. [Design rules](#4-design-rules)
5. [Performance rules](#5-performance-rules)
6. [Permissions and privacy rules](#6-permissions-and-privacy-rules)
7. [Testing a change](#7-testing-a-change)
8. [Pull requests](#8-pull-requests)
9. [Good first areas](#9-good-first-areas)
10. [Licensing](#10-licensing)

## 1. Project in one page

```
AndroidManifest.xml      package com.techgenius.tvoslauncher, activities/services/receivers
build.sh                 the whole build (aapt2 → javac → d8 → zipalign → apksigner)
src/com/techgenius/tvoslauncher/   the launcher (≈9,400 lines of Java)
src/com/techgenius/tvoslauncher/present/   Presentation mode: HTTP server, player, storage
src/io/nayuki/qrcodegen/ vendored QR encoder (MIT)
res/                     manifest resources: icons, device-admin and accessibility XML, strings
assets/fonts/            Inter (OFL)
assets/present/          the browser admin app for Presentation (HTML/CSS/JS)
docs/                    user and developer documentation, screenshots
```

Key files (full map in **[docs/architecture.md](docs/architecture.md)**):

| File | What lives there |
|---|---|
| `MainActivity.java` | Home screen, dock/grid, remote focus, weather/status, Control Panel entry and idle behavior |
| `SceneHome.java`, `Scenes.java` | Night Scenes layout, clock, dock and app-published TV Continue Watching row |
| `SettingsActivity.java` | Four-group Settings navigation, D-pad focus, OTA progress and CleanYourTV menus |
| `SetupActivity.java` | First-run setup (single user, Russian UI) |
| `I18n.java` | Russian-only UI dictionary and dynamic labels |
| `Search.java`, `SearchPanel.java` | App/settings/title search, voice input and Russian/English keyboard layouts |
| `Suggestions.java` | Shared launch, URL, stream and artwork utilities; no recommendation/news feeds |
| `AppUpdater.java`, `VersionComparator.java` | GitHub Releases OTA discovery, version comparison and PackageInstaller flow |
| `CleanYourTv.java` | Model detection, protected package policy and verified disable/restore operations |
| `LocalAdb.java`, `Perms.java` | Local ADB, accessibility repair, DND policy access and HOME selection |
| `ButtonMapper.java`, `PowerService.java` | Remote-button mapping and accessibility service |
| `ControlPanelOverlay.java`, `RecentAppsOverlay.java` | Universal Control Panel and Recent Apps switcher |
| `CustomMedia.java` | Custom media and validated settings backup/restore |
| `Weather*.java`, `Inputs.java`, `SleepReceiver.java`, `Clicks.java` | Weather, TV inputs, sleep timer and navigation sounds |

Features are described in **[docs/features.md](docs/features.md)**.

## 2. Workflow

- `main` is protected: **never push to it**. Work on a branch and open a pull request; one approval
  is required to merge.
- Branch names: `feature/<short-name>`, `fix/<short-name>`, `docs/<short-name>`.
- Keep pull requests focused on one thing; several small ones beat one huge one.
- Commit messages: a short imperative summary line (≤ 72 chars), a blank line, then *what* and
  *why* in a few bullets. Mention what you tested and on which device.

```
Weather: retry the next source when one times out

- Open-Meteo timeouts left the pill empty until the next 15-minute tick.
- Tested on Mi Box (Android 12): pulled the network for 30 s, pill recovered.
```

- Bump the version only in release builds (`VERSION_CODE`/`VERSION_NAME` on the `build.sh` command
  line), not in source.

## 3. Code style

Match the code around you; consistency matters more than any rule here.

- **Java 11 language level**, Android API 24+ (`minSdk 24`, `targetSdk 34`). Guard newer APIs with
  `Build.VERSION.SDK_INT` checks.
- **No new runtime libraries.** The APK is ~1 MB and has no dependencies; keep it that way. Small,
  permissively licensed code may be vendored under `src/` with its licence (like Nayuki QR).
- **Views in code**, no XML layouts. Follow the existing patterns (`row(...)`, `ccTile(...)`,
  `option(...)`, `LiquidGlass`).
- **Sizes in design units**: always `dp(x)` / `Ui.px(ctx, x)`, never raw pixels, so 720p, 1080p and 4K
  keep their proportions. Text with `Ui.textSize`.
- **Fonts**: `Ui.font(ctx, "regular" | "medium" | "semibold" | "display")` (Inter).
- **ASCII-only source files.** Write non-ASCII characters as `\uXXXX` escapes (e.g. `"°"` for °).
- **Comments**: a short Javadoc on each class and non-obvious method saying *why*, matching the
  density of the surrounding code. No commented-out code.
- **Threads**: network, disk and bitmap decoding off the UI thread (`io` executor or a `Thread`);
  post results back with `handler`.
- **Strings**: user-facing text in sentence case for rows, Title Case for buttons/titles, as Apple
  does; keep it short and plain.

## 4. Design rules

tvOS Launcher's whole point is to look and feel like Apple tvOS 26. Before building UI:

- **Measure Apple's real UI first** (Apple's tvOS screenshots and the Human Interface Guidelines),
  then implement and state the measured numbers in the code or PR. Don't improvise the look.
- Liquid Glass is for **controls and panels** (dock, buttons, Control Center, pills). App cards stay
  opaque.
- Focus: lift/scale (cards 1.2×, controls ~1.06–1.12×), white focused state for buttons/tiles, never
  a coloured outline. Make sure focused items have room to grow (no clipping by parents).
- Text must stay legible over any artwork: use the existing scrims/shades, minimum text size 11.5
  design units (≈ Apple's 23 pt tvOS minimum).
- Everything must work with a 5-key remote (arrows, OK, Back, Home): every focusable item reachable,
  focus never lost, Home always returns to the first dock app.

## 5. Performance rules

The target is a Mi Box 4 (Mali-450 MP GPU, GLES 2.0, fill-rate bound). Home navigation should stay
near a 16 ms median frame time.

- **No perpetual animations.** Idle Home draws ~0 frames per second.
- **Nothing rendered in software on the UI thread during transitions.** Bake expensive things
  (blurs, gradients, glass backdrops) into bitmaps once.
- Avoid hidden full-screen layers and overdraw; prefer one baked image to several stacked ones.
- Cache network results and images on disk; never block startup on the network.

Measure before and after any visual change:

```sh
adb shell dumpsys gfxinfo com.techgenius.tvoslauncher reset
# …press Right/Left along the dock a few times…
adb shell dumpsys gfxinfo com.techgenius.tvoslauncher | grep -E "Janky|50th|90th"
```

Include the numbers in your PR. `--es perf <layer>` (see getting-started) helps find costly layers.

## 6. Permissions and privacy rules

- **ADB is the last resort.** Prefer Android runtime prompts, system settings pages and the Accessibility settings screen. Add new permission flows to `Perms.java`.
- Detect device support before offering a feature and degrade gracefully when the TV firmware does not expose a required setting.
- DND uses Android's Do Not Disturb policy access directly. Do not add a notification-listener service for DND.
- The accessibility service (`PowerService`) only handles the actions the user enabled (remote mapping and supported power controls). Do not read screen content.
- No analytics, tracking or user accounts. Network access is limited to the active feature: weather, JustWatch title search, IP-based location when requested and GitHub OTA checks/downloads. News and recommendation feeds and Nuvio/Stremio cloud integrations are not part of this build.
- Search history and settings backups stay local. Do not include ADB/security state, disabled-package backups or credentials in general settings backups.
- The microphone is used only during user-initiated voice search. The app does not record or store audio.
- Never commit signing keys, passwords or tokens. Use repository secrets for release signing.

## 7. Testing a change

CI runs Python regression guards, Java comparator/localization tests, APK build and signature/package verification. Relevant checks include:

- `tests/test_clean_your_tv_protection.py`
- `tests/test_memory_settings_cleanup.py`
- `tests/test_settings_navigation.py`
- `tests/test_settings_backup_safety.py`
- `tests/test_ota_install_flow.py`
- `tests/test_build_contract.py`
- `tests/test_control_panel_russian_clock.py`
- `tests/test_russian_localization_coverage.py`
- `VersionComparatorTest.java` and `I18nDynamicTranslationTest.java`

For every change:

1. Ensure the CI run for the latest branch commit succeeds.
2. Exercise the change on a TV with the remote, including Back and Home.
3. Capture screenshots for changed screens.
4. For home-screen changes, record `dumpsys gfxinfo` and memory use on target hardware.
5. For permissions, network or storage changes, test on Android 9 as well as a newer Android TV device when available.
6. Check `adb logcat -s TvHome AndroidRuntime` for errors and crashes.

UI checklist: readable over bright and dark artwork · nothing clipped when focused · every action reachable with arrows · Back returns to the expected pane · Home returns to the launcher.

## 8. Pull requests

Open the PR against `main` with:

- **What and why** (link the issue if there is one).
- **Screenshots** of every changed screen.
- **How you tested**: device(s), Android version, steps.
- **Frame-time numbers** for Home changes.
- Docs updated if behaviour changed (`docs/features.md`, and `docs/architecture.md` for new files).

Reviewers check: matches tvOS design, no performance regression, works with a remote, permissions
follow §6, code follows §3.

## 9. Good first areas

- Weather source reliability and weather icon coverage.
- D-pad navigation, focus visibility and TalkBack labels.
- Bounded artwork caches and startup/per-frame performance on low-memory TVs.
- Regression tests for CleanYourTV, backup/restore, OTA and settings navigation.
- Documentation and troubleshooting for supported Android TV firmware.

Do not reintroduce removed news/recommendation feeds, multi-user profiles, non-Russian UI packs or cloud catalogue integrations without an explicit product decision.

## 10. Licensing

tvOS Launcher is licensed under the **[Apache License 2.0](LICENSE)**. By submitting a pull request
you agree that your contribution is licensed under the same terms (Apache-2.0, section 5). Keep the
[NOTICE](NOTICE) file accurate: add an entry for any third-party code or asset you bring in, and only
bring in code under a licence compatible with Apache-2.0 (MIT, BSD, Apache, OFL for fonts — not GPL).

Third-party code and assets already here: **Inter** font (SIL Open Font License,
`assets/fonts/Inter-LICENSE.txt`) and **Nayuki QR Code generator** (MIT, `src/io/nayuki/qrcodegen`).

### Signing

Development builds are signed with a local `debug.keystore` that `build.sh` creates on first use; it
is ignored by git. **Release builds are signed by maintainers only**, with the private key kept in
a separate private repository (never in this one):

```sh
RELEASE_KEYSTORE=/path/to/tvos-release.jks RELEASE_KEYSTORE_PASS=... VERSION_CODE=.. VERSION_NAME=.. ./build.sh
```

A debug build can't update a release install (different key): uninstall first.
