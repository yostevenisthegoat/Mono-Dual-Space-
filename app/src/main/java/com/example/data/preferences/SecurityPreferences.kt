package com.example.data.preferences

enum class ThemeMode(val label: String) {
    DARK("Mono Dark"),
    LIGHT("Mono Light"),
    SYSTEM("System Default")
}

enum class AppSortOrder(val label: String) {
    NAME_ASC("A – Z"),
    NAME_DESC("Z – A"),
    RECENTLY_USED("Recently Used")
}

enum class AutoLockTimeout(val seconds: Int, val label: String) {
    IMMEDIATE(0, "Immediately"),
    SECONDS_30(30, "After 30 seconds"),
    MINUTE_1(60, "After 1 minute"),
    MINUTES_5(300, "After 5 minutes"),
    MANUAL(-1, "Manual only")
}

data class SecurityPreferencesState(
    val onboardingCompleted: Boolean = false,
    val isPinEnabled: Boolean = false,
    val pinHash: String = "",
    val pinSalt: String = "",
    val isBiometricEnabled: Boolean = false,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.IMMEDIATE,
    val lockOnLaunch: Boolean = true,
    val hideInRecents: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val compactDensity: Boolean = false,
    val defaultSortOrder: AppSortOrder = AppSortOrder.NAME_ASC,
    val showSystemAppsByDefault: Boolean = false
)
