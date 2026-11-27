package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.preferences.AutoLockTimeout
import com.example.data.preferences.SecurityPreferencesState
import com.example.security.BiometricAvailability
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoStatusPill
import com.example.ui.theme.MonoTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrivacyScreen(
    preferences: SecurityPreferencesState,
    biometricAvailability: BiometricAvailability,
    onSetupOrChangePin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onSelectAutoLockTimeout: (AutoLockTimeout) -> Unit,
    onToggleLockOnLaunch: (Boolean) -> Unit,
    onToggleHideInRecents: (Boolean) -> Unit,
    onLockNow: () -> Unit
) {
    val colors = MonoTheme.colors
    var showPinSetupSheet by remember { mutableStateOf(false) }

    if (showPinSetupSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPinSetupSheet = false },
            sheetState = sheetState,
            containerColor = colors.secondaryBackground
        ) {
            SetupPinDialogContent(
                onPinConfirmed = { pin ->
                    onSetupOrChangePin(pin)
                    showPinSetupSheet = false
                },
                onCancel = { showPinSetupSheet = false }
            )
        }
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
            item(key = "privacy_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Privacy & Security",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Local-only authentication and zero-telemetry controls",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    if (preferences.isPinEnabled) {
                        Button(
                            onClick = onLockNow,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.onAccent
                            ),
                            modifier = Modifier.testTag("privacy_lock_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Lock")
                        }
                    }
                }
            }

            item(key = "pin_protection_card") {
                MonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Password,
                                    contentDescription = null,
                                    tint = colors.accent
                                )
                                Column {
                                    Text(
                                        text = "PIN Protection",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (preferences.isPinEnabled) "Salted PBKDF2 hash stored locally"
                                        else "Protect dashboard & cloned spaces",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            MonoStatusPill(
                                text = if (preferences.isPinEnabled) "ACTIVE" else "OFF",
                                toneColor = if (preferences.isPinEnabled) colors.success else colors.textSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showPinSetupSheet = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.onAccent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("setup_pin_button")
                            ) {
                                Text(if (preferences.isPinEnabled) "Change PIN" else "Enable PIN")
                            }
                            if (preferences.isPinEnabled) {
                                OutlinedButton(
                                    onClick = onDisablePin,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("disable_pin_button")
                                ) {
                                    Text("Disable PIN", color = colors.danger)
                                }
                            }
                        }
                    }
                }
            }

            item(key = "biometric_and_autolock_card") {
                MonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Fingerprint,
                                    contentDescription = null,
                                    tint = colors.accent
                                )
                                Column {
                                    Text(
                                        text = "Biometric Authentication",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (!preferences.isPinEnabled) "Enable PIN first to use biometrics as fast unlock"
                                        else biometricAvailability.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = preferences.isBiometricEnabled,
                                onCheckedChange = onToggleBiometric,
                                enabled = preferences.isPinEnabled && biometricAvailability.canAuthenticate,
                                modifier = Modifier.testTag("biometric_switch")
                            )
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
                                    text = "Lock on App Launch",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Require authentication whenever Mono starts cold",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Switch(
                                checked = preferences.lockOnLaunch,
                                onCheckedChange = onToggleLockOnLaunch,
                                enabled = preferences.isPinEnabled
                            )
                        }

                        HorizontalDivider(color = colors.border)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Automatic Background Locking",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AutoLockTimeout.entries.forEach { option ->
                                    FilterChip(
                                        selected = preferences.autoLockTimeout == option,
                                        enabled = preferences.isPinEnabled,
                                        onClick = { onSelectAutoLockTimeout(option) },
                                        label = { Text(option.label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.accent,
                                            selectedLabelColor = colors.onAccent
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = colors.border)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VisibilityOff,
                                    contentDescription = null,
                                    tint = colors.accent
                                )
                                Column {
                                    Text(
                                        text = "Hide Screen in App Switcher",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Applies Android FLAG_SECURE to prevent recents thumbnails and screenshots",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = preferences.hideInRecents,
                                onCheckedChange = onToggleHideInRecents,
                                modifier = Modifier.testTag("hide_recents_switch")
                            )
                        }
                    }
                }
            }

            item(key = "local_privacy_audit") {
                MonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Local-Only Privacy Guarantee",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                        }
                        Text(
                            text = "• INTERNET permission is omitted from AndroidManifest.xml.\n" +
                                "• Zero telemetry, analytics, crash reporters, or ad SDKs.\n" +
                                "• Cloned application credentials remain inside Android's isolated Work Profile sandbox and are never readable by Mono Dual-Space.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
