# testOS 2.1.66

## Control Panel
- Corrected remote navigation so moving down from VPN lands on RAM instead of skipping to Sleep Timer.
- Added deterministic up/down/left/right movement for the paired RAM / Recent Apps and Launcher / TV Settings rows.
- Applied the same focus map to the in-app panel and the system-wide accessibility overlay.

## Russian localization
- Filled missing Russian translations in the Control Panel, permissions, update flow, weather loading state and remaining Home screen labels.
- Added a regression check that scans literal I18n keys used by launcher Java screens.

## Clock fonts
- Kept iPhone Duo, iPhone Rounded and Tall Glass.
- Added Classic, Serif, Digital, Condensed, Thin and Bold clock font options using Android system typefaces.

## Validation
- Control Panel navigation, Russian localization and clock font regression test passed.
- Existing CleanYourTV, OTA version, OTA installer and build-contract tests passed.
- Development APK build and signature/package verification passed.
- TV runtime verification remains pending.
