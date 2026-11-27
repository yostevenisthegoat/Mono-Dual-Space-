package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.preferences.SecurityPreferencesState
import com.example.data.preferences.ThemeMode
import com.example.engine.EngineCapabilities
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoStatusPill
import com.example.ui.theme.MonoTheme

private enum class SettingsModalDoc(val title: String, val body: String) {
    LICENSE(
        title = "Apache License 2.0",
        body = "Mono Dual-Space is free and open-source software licensed under the Apache License, Version 2.0.\n\n" +
            "You may obtain a copy of the License at:\nhttps://www.apache.org/licenses/LICENSE-2.0\n\n" +
            "Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND.\n\n" +
            "Open-source dependencies used:\n" +
            "• Jetpack Compose & Material 3 (Apache 2.0)\n" +
            "• AndroidX Room, DataStore, WorkManager, Biometric, Navigation (Apache 2.0)\n" +
            "• Kotlin & Kotlinx Coroutines / Serialization (Apache 2.0)\n" +
            "• Space Grotesk, Inter, JetBrains Mono Fonts (SIL OFL 1.1)"
    ),
    PRIVACY_POLICY(
        title = "Privacy Policy (Offline-First)",
        body = "1. Zero Data Collection: Mono Dual-Space collects no personal data, usage analytics, crash telemetry, or advertising identifiers.\n\n" +
            "2. No Internet Permission: The app does not declare android.permission.INTERNET.\n\n" +
            "3. Local Storage Only: Configured clone labels, space metadata, and salted PBKDF2 PIN hashes reside strictly in local Room and DataStore storage.\n\n" +
            "4. Cloned App Isolation: Applications inside Android Managed Work Profiles are isolated by the Android OS kernel and SELinux. Mono cannot read credentials or private storage of cloned apps."
    ),
    CHANGELOG(
        title = "Changelog — v1.0.0",
        body = "v1.0.0 — Initial Public Release\n" +
            "• Primary Engine: Android Managed Work Profile provisioning & lifecycle via DevicePolicyManager, UserManager, LauncherApps, and CrossProfileApps.\n" +
            "• Secondary Engine: Extensible VirtualizationSandboxEngine interface contract.\n" +
            "• App Discovery: Launcher & package visibility discovery, search, alphabetical sort, user/system filters, and APK size inspection.\n" +
            "• Clone Management: Custom names, monochrome icon styles, space badges, group organization, home-screen shortcuts, and uninstall flows.\n" +
            "• Privacy & Security: PBKDF2 PIN lock, BiometricPrompt support, automatic background locking, and FLAG_SECURE recents hiding.\n" +
            "• Monochrome UI: Mono Dark (#101010) and Mono Light adaptive Compose interface."
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    preferences: SecurityPreferencesState,
    capabilities: EngineCapabilities,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onToggleCompactDensity: (Boolean) -> Unit,
    onShowOnboardingAgain: () -> Unit,
    onOpenGitHub: () -> Unit
) {
    val colors = MonoTheme.colors
    var activeDoc by remember { mutableStateOf<SettingsModalDoc?>(null) }

    activeDoc?.let { doc ->
        AlertDialog(
            onDismissRequest = { activeDoc = null },
            containerColor = colors.secondaryBackground,
            title = { Text(doc.title, color = colors.textPrimary) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = doc.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDoc = null }) {
                    Text("Close", color = colors.accent)
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "settings_header") {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Appearance, diagnostics, open-source licenses, and documentation",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            item(key = "appearance_card") {
                MonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = colors.accent
                            )
                            Text(
                                text = "Appearance",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = preferences.themeMode == mode,
                                    onClick = { onSelectThemeMode(mode) },
                                    label = { Text(mode.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.accent,
                                        selectedLabelColor = colors.onAccent
                                    ),
                                    modifier = Modifier.testTag("theme_${mode.name.lowercase()}")
                                )
                            }
                        }

                        HorizontalDivider(color = colors.border)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Compact Card Density",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Fit more cloned apps on screen at once",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Switch(
                                checked = preferences.compactDensity,
                                onCheckedChange = onToggleCompactDensity
                            )
                        }
                    }
                }
            }

            item(key = "diagnostics_card") {
                MonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = colors.accent
                            )
                            Text(
                                text = "Application & Environment Info",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                        }
                        DiagnosticRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                        DiagnosticRow("Package ID", BuildConfig.APPLICATION_ID)
                        DiagnosticRow("Android OS", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                        DiagnosticRow(
                            "Managed Users Feature",
                            if (capabilities.supportsManagedUsersFeature) "Available" else "Unavailable"
                        )
                        DiagnosticRow(
                            "Profile Owner Role",
                            if (capabilities.isProfileOwner) "Active inside Work Space" else "Personal Profile context"
                        )

                        OutlinedButton(
                            onClick = onShowOnboardingAgain,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Review Setup & Work Profile Guide")
                        }
                    }
                }
            }

            item(key = "opensource_card") {
                MonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Open Source & Legal",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            MonoStatusPill(text = "APACHE 2.0", toneColor = colors.accent)
                        }

                        OutlinedButton(
                            onClick = { activeDoc = SettingsModalDoc.LICENSE },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_license_button")
                        ) {
                            Icon(Icons.Outlined.Description, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Open-Source Licenses")
                        }

                        OutlinedButton(
                            onClick = { activeDoc = SettingsModalDoc.PRIVACY_POLICY },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_privacy_policy_button")
                        ) {
                            Icon(Icons.Outlined.Policy, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Privacy Policy")
                        }

                        OutlinedButton(
                            onClick = { activeDoc = SettingsModalDoc.CHANGELOG },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_changelog_button")
                        ) {
                            Icon(Icons.Outlined.Code, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Changelog (v${BuildConfig.VERSION_NAME})")
                        }

                        OutlinedButton(
                            onClick = onOpenGitHub,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.accent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_github_button")
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("GitHub Repository", color = colors.accent)
                        }
                    }
                }
            }

            item(key = "brand_footer") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "MONO DUAL-SPACE",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "OPEN SOURCE. FREE. NO ADS. NO SUBSCRIPTIONS.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    val colors = MonoTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textPrimary
        )
    }
}
