package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.engine.EngineCapabilities
import com.example.engine.ManagedSpaceInfo
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoStatusPill
import com.example.ui.theme.MonoTheme

@Composable
fun SpacesScreen(
    primaryCapabilities: EngineCapabilities,
    secondaryCapabilities: EngineCapabilities,
    spaces: List<ManagedSpaceInfo>,
    configuredCloneCount: Int,
    onRefresh: () -> Unit,
    onProvisionWorkSpace: () -> Unit,
    onToggleQuietMode: (ManagedSpaceInfo) -> Unit,
    onOpenMonoInWorkSpace: () -> Unit,
    onRunSpaceAudit: () -> Unit,
    onWipeWorkSpace: () -> Unit,
    onExportConfig: () -> Unit,
    onImportConfig: () -> Unit
) {
    val colors = MonoTheme.colors
    var showConsentDialog by remember { mutableStateOf(false) }
    var showWipeDialog by remember { mutableStateOf(false) }
    val hasWorkSpace = spaces.any { it.isManagedProfile }

    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            containerColor = colors.secondaryBackground,
            title = { Text("Create Managed Work Space?", color = colors.textPrimary) },
            text = {
                Text(
                    "Android will open the system Work Profile setup wizard. Mono Dual-Space will act as Profile Owner exclusively inside the new isolated Work Space.\n\n" +
                        "Personal apps and data remain untouched.",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConsentDialog = false
                        onProvisionWorkSpace()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.onAccent
                    )
                ) {
                    Text("Continue to System Setup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConsentDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            containerColor = colors.secondaryBackground,
            title = { Text("Remove Work Space?", color = colors.danger) },
            text = {
                Text(
                    "This permanently deletes the Managed Work Space and all cloned app data inside it. Your Personal Space apps will not be affected.",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWipeDialog = false
                        onWipeWorkSpace()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.danger,
                        contentColor = colors.onAccent
                    )
                ) {
                    Text("Remove Work Space")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
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
            item(key = "spaces_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Spaces",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${spaces.size} Android user space(s) • $configuredCloneCount configured clone(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("spaces_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh spaces",
                            tint = colors.textPrimary
                        )
                    }
                }
            }

            // Primary Engine Card
            item(key = "primary_engine") {
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
                                text = "Primary Engine",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            MonoStatusPill(
                                text = if (primaryCapabilities.supportsManagedUsersFeature) "SUPPORTED" else "UNSUPPORTED",
                                toneColor = if (primaryCapabilities.supportsManagedUsersFeature) colors.success else colors.warning
                            )
                        }
                        Text(
                            text = primaryCapabilities.engineType.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.accent
                        )
                        Text(
                            text = primaryCapabilities.statusSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )

                        primaryCapabilities.limitationNotes.forEach { note ->
                            Text(
                                text = "• $note",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        if (!hasWorkSpace) {
                            Button(
                                onClick = { showConsentDialog = true },
                                enabled = primaryCapabilities.isProvisioningAllowed,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.onAccent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("provision_work_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddModerator,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    if (primaryCapabilities.isProvisioningAllowed) "Create Managed Work Space"
                                    else "Provisioning Unavailable on This Device"
                                )
                            }
                        } else if (!primaryCapabilities.isProfileOwner) {
                            OutlinedButton(
                                onClick = onOpenMonoInWorkSpace,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, colors.accent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_mono_in_work_space_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Open Mono in Work Space (Profile Owner)", color = colors.accent)
                            }
                        }
                    }
                }
            }

            // Active Android User Spaces
            items(
                items = spaces,
                key = { it.serialNumber }
            ) { space ->
                MonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = space.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "User Serial #${space.serialNumber} • ${space.installedAppCount} launchable apps",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.textSecondary
                                )
                            }
                            MonoStatusPill(
                                text = when {
                                    space.isPersonalProfile -> "PERSONAL"
                                    space.isQuietModeEnabled -> "PAUSED"
                                    space.isUserUnlocked -> "ACTIVE"
                                    else -> "LOCKED"
                                },
                                toneColor = when {
                                    space.isPersonalProfile -> colors.textSecondary
                                    space.isQuietModeEnabled -> colors.warning
                                    else -> colors.success
                                }
                            )
                        }

                        if (space.isManagedProfile) {
                            HorizontalDivider(color = colors.border)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onToggleQuietMode(space) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, colors.border),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("toggle_quiet_mode_${space.serialNumber}")
                                ) {
                                    Icon(
                                        imageVector = if (space.isQuietModeEnabled) {
                                            Icons.Outlined.PlayCircle
                                        } else {
                                            Icons.Outlined.PauseCircle
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(if (space.isQuietModeEnabled) "Resume Space" else "Pause Space")
                                }

                                if (space.isProfileOwnerInSpace) {
                                    OutlinedButton(
                                        onClick = { showWipeDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.5f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteForever,
                                            contentDescription = null,
                                            tint = colors.danger,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text("Remove", color = colors.danger)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Secondary Engine Extensibility Card
            item(key = "secondary_engine") {
                MonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Secondary Engine Interface",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            MonoStatusPill(text = "FUTURE INTERFACE", toneColor = colors.textSecondary)
                        }
                        Text(
                            text = secondaryCapabilities.engineType.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textSecondary
                        )
                        Text(
                            text = secondaryCapabilities.statusSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Storage Audit & Backup/Restore
            item(key = "backup_and_audit") {
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
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Configuration Backup & Storage Audit",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                        }
                        Text(
                            text = "Export or restore clone labels, groups, and space preferences as local JSON. Android sandboxing prevents extracting third-party app private data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onExportConfig,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.border),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_backup_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Export JSON")
                            }
                            OutlinedButton(
                                onClick = onImportConfig,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.border),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("import_backup_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Import JSON")
                            }
                        }
                        OutlinedButton(
                            onClick = onRunSpaceAudit,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_space_audit_button")
                        ) {
                            Text("Run Space & Storage Audit (WorkManager)")
                        }
                    }
                }
            }
        }
    }
}
