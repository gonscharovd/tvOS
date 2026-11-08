# testOS documentation

This documentation describes the current Russian-only, single-user Android TV build in this repository. Older screenshots and upstream notes may show removed features; the current implementation is the source of truth.

## Contents

- [Features](features.md) — home screen, Control Panel, search, weather, settings, CleanYourTV, OTA and backup.
- [Install and build](install.md) — SDK requirements, signing, installation, permissions and rollback.
- [Architecture](architecture.md) — code map, build pipeline and performance constraints.
- [Getting started](getting-started.md) — developer setup and useful ADB commands.
- [Presentation mode](presentation.md) — browser upload, pairing and full-screen playback.
- [Contributing](../CONTRIBUTING.md) — code and review rules.

## Current PR #14 implementation at a glance

The core cleanup is in Draft PR #14 and is not yet merged into `main`. Stable release: v2.1.73 / versionCode 88.



| Area | Current behavior |
|---|---|
| Language | Russian UI only; Russian and English keyboard layouts |
| Users | Single-user mode; no profile picker |
| Recommendations/news | Removed, including the associated feed engines |
| Streaming integrations | Nuvio/Stremio cloud catalogue and sign-in removed |
| Control Panel | Overlay retained; separate customization page removed |
| Home interface | Night Scenes only; Classic UI and Hero / Continue Watching shelf removed in PR #14 |
| Updates | GitHub Releases OTA; release workflow uses persistent signing key, PR preview uses runner debug key |
| Backup | Allowlisted local preferences, validated JSON, atomic file write and merge restore |
| CleanYourTV | On-device package management with protected-package checks |
