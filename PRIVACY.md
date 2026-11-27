# Privacy Policy — Mono Dual-Space

**Effective Date:** November 2026  
**License:** Apache License 2.0

Mono Dual-Space is designed from the ground up as an offline-first, zero-telemetry Android utility.

## 1. Data We Do Not Collect
- Mono Dual-Space does **not** collect, transmit, sell, or share any personal information, device identifiers, usage statistics, crash logs, or installed application lists.
- The application does **not** request `android.permission.INTERNET`.
- There are **no** advertising SDKs, analytics SDKs, or mandatory user accounts.

## 2. Local Data Storage
All data managed by Mono Dual-Space remains strictly on your device:
- **Room Database (`mono_dual_space.db`):** Stores your configured clone names, space badges, group tags, launch counts, and cached package metadata.
- **DataStore (`mono_dual_space_prefs`):** Stores UI theme, auto-lock preferences, and salted PBKDF2-HMAC-SHA256 PIN hashes (never plaintext PINs).

## 3. Cloned Application Isolation
Mono Dual-Space delegates application isolation to Android's native **Managed Work Profile** (`DevicePolicyManager`). Each cloned app runs inside its own Android user sandbox; Mono Dual-Space cannot access login credentials, tokens, or private files of applications installed in any profile.

## 4. Backups
If you explicitly use **Export JSON**, Mono Dual-Space writes only non-sensitive dashboard labels and preferences to the location you choose via Android's Storage Access Framework. PIN hashes and third-party app data are never exported.
