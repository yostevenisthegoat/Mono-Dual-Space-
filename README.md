# Mono Dual-Space

**One device. Multiple spaces.**  
OPEN SOURCE · 100% FREE · NO ADS · NO SUBSCRIPTIONS · OFFLINE-FIRST

Mono Dual-Space is a privacy-focused Android application cloning and multi-account management utility built with Kotlin, Jetpack Compose, Room, DataStore, and Android's native Managed Work Profile APIs.

---

## Architecture & Cloning Engine

### Primary Engine: Android Managed Work Profile
Mono Dual-Space uses Android's official `DevicePolicyManager`, `UserManager`, `LauncherApps`, and `CrossProfileApps` APIs to provision and manage an OS-isolated Work Profile:

- **True OS Sandboxing:** Applications in the Work Space run under a separate Android Linux user ID with independent `/data/user/<id>` storage.
- **No APK Tampering:** Original APK cryptographic signatures and Play integrity are preserved.
- **Explicit Consent:** Work Profile provisioning is initiated strictly through Android's system provisioning dialog (`ACTION_PROVISION_MANAGED_PROFILE`).
- **Lifecycle Controls:** Inspect spaces, launch isolated apps, toggle Quiet Mode (pause space), pin home-screen shortcuts, and remove Work Space installations.

### Secondary Engine: Extensible Interface
`CloningEngine` defines a clean abstraction (`VirtualizationSandboxEngine`) reserved for future audited sandboxing implementations. No unimplemented virtualization is faked or claimed active.

---

## Features

- **Application Discovery:** Fast package discovery respecting Android 11+ `<queries>` visibility, alphabetical sorting, User/System/In-Space filtering, search, and APK size inspection.
- **Clone Management:** Custom display names, monochrome icon treatments, space badges, dashboard groups, pinned entries, home-screen shortcuts, and uninstall controls.
- **Privacy & Security:** Local salted PBKDF2 PIN protection, `BiometricPrompt` unlock, automatic background locking via `ProcessLifecycleOwner`, and `FLAG_SECURE` recents protection.
- **Backup & Restore:** Export and restore non-sensitive clone labels, groups, and preferences as local JSON using Storage Access Framework (SAF).
- **Mono Ecosystem Design:** Minimalist `#101010` dark mode and clean light mode built on Material Design 3.

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 + Adaptive `WindowSizeClass`
- **Architecture:** MVVM + Repository Pattern + `CloningEngine` interface
- **Database:** Room (KSP)
- **Preferences:** Jetpack DataStore
- **Security:** `androidx.biometric:biometric` + PBKDF2WithHmacSHA256
- **Background Tasks:** WorkManager (on-demand space & storage audit)
- **Minimum SDK:** Android 10 (API 29)

---

## Building from Source

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

Or open the repository in Android Studio and build the `:app` configuration directly.

---

## License

Licensed under the **Apache License, Version 2.0**. See [LICENSE](LICENSE).
