# testOS documentation

This documentation describes the current Russian-only, single-user Android TV build in this repository. Older screenshots and upstream notes may show removed features; the current implementation is the source of truth.

## Contents

- [Features](features.md) — home screen, Control Panel, search, weather, settings, CleanYourTV, OTA and backup.
- [Install and build](install.md) — SDK requirements, signing, installation, permissions and rollback.
- [Architecture](architecture.md) — code map, build pipeline and performance constraints.
- [Getting started](getting-started.md) — developer setup and useful ADB commands.
- [Presentation mode](presentation.md) — browser upload, pairing and full-screen playback.
- [Contributing](../CONTRIBUTING.md) — code and review rules.

## Current build at a glance

| Area | Current behavior |
|---|---|
| Language | Russian UI only; Russian and English keyboard layouts |
| Users | Single-user mode; no profile picker |
| Recommendations/news | Removed, including the associated feed engines |
| Streaming integrations | Nuvio/Stremio cloud catalogue and sign-in removed |
| Control Panel | Overlay retained; separate customization page removed |
| Continue Watching | Only optional app-published Android TV provider rows; no notification-listener media service |
| Updates | GitHub Releases OTA, signed with the persistent release key |
| Backup | Allowlisted local preferences, validated JSON, atomic file write and merge restore |
| CleanYourTV | On-device package management with protected-package checks |
