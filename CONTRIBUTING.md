# Contributing to Mono Dual-Space

Thank you for helping keep **Mono Dual-Space** open-source, free, and privacy-respecting.

## Core Principles

All contributions must preserve the project's core guarantees:
1. **No Ads, Paywalls, or Subscriptions:** Every feature must remain 100% free.
2. **No Telemetry or Trackers:** Do not add analytics SDKs, crash-reporting trackers, or unnecessary `INTERNET` permissions.
3. **Real Android APIs Only:** Never simulate cloning, duplicate APKs as fake clones, or bypass Android sandboxing, signature verification, or anti-cheat mechanisms.
4. **Honest Compatibility:** Clearly communicate unsupported devices or Android OS constraints to users.

## Development Setup

1. Clone the repository and open it in Android Studio (AGP 9+, JDK 11+, Android SDK 36).
2. Run unit and Robolectric tests before submitting changes:
   ```bash
   gradle :app:testDebugUnitTest
   ```
3. Ensure debug builds compile cleanly:
   ```bash
   gradle :app:assembleDebug
   ```

## Code Style

- Follow standard Kotlin formatting and Jetpack Compose Material 3 conventions.
- Keep UI colors in `ui/theme/Color.kt` and `Theme.kt` rather than hardcoding hex literals in Composables.
- Include `Modifier.testTag(...)` on primary interactive controls and `contentDescription` on icons/images.

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
