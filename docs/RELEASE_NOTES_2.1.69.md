# testOS 2.1.69

## Changes

- Complete Russian localization across launcher screens, Settings, CleanYourTV, setup/onboarding, search, profiles, weather, playback, updates, backup/restore, and dynamic status messages.
- Add source-wide coverage checks ensuring every literal translation key has a Russian entry.
- Add JVM regression tests for dynamic Russian translations and plural forms.
- Include the localization checks in both development and release CI workflows.

## Compatibility and safety

- Package remains `com.gonscharovd.tvoslauncher`.
- This release is built from the merged `main` branch after PR #9.
- No CleanYourTV package profiles, protected-package rules, preference keys, ADB behavior, OTA signing configuration, or TV-side state were changed by the localization work.
- Release workflow verifies the persistent signing certificate before publishing.
