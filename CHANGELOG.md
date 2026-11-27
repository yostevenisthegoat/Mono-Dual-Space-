# Changelog

All notable changes to **Mono Dual-Space** will be documented in this file.

## [1.0.0] - 2026-11-26

### Added
- **Managed Work Profile Engine (`ManagedProfileCloningEngine`):** Real Android Work Profile inspection, consent-gated provisioning (`ACTION_PROVISION_MANAGED_PROFILE`), `MonoDeviceAdminReceiver`, cross-profile app launching via `LauncherApps`, Quiet Mode toggling, system app enablement in Profile Owner mode, and uninstall/app-info integration.
- **Extensible Architecture (`CloningEngine`):** Clean engine interface with `VirtualizationSandboxEngine` stub explicitly marked as an unimplemented future extension point.
- **Application Discovery:** Package discovery via `PackageManager` with `<queries>` visibility compliance, search, alphabetical sorting, User/System/In-Space filtering, and APK/storage size inspection.
- **Clone Dashboard & Customization:** Custom clone names, monochrome icon treatments, space badges, dashboard groups, pinned entries, home-screen shortcuts (`ShortcutManagerCompat`), and individual clone lock toggles.
- **Privacy & Security:** Local salted PBKDF2 PIN protection, `BiometricPrompt` authentication, automatic background locking via `ProcessLifecycleOwner`, and `FLAG_SECURE` recents protection.
- **Backup & Maintenance:** Local SAF JSON configuration export/import (`BackupManager`) and on-demand `WorkManager` space audit (`SpaceAuditWorker`).
- **Mono Ecosystem UI:** Minimalist `#101010` dark mode, light mode, bundled `Space Grotesk` / `Inter` / `JetBrains Mono` typography, and adaptive `NavigationBar` / `NavigationRail`.
