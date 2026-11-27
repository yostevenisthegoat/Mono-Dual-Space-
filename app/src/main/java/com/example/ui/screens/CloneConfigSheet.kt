package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddToHomeScreen
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.CloneEntryEntity
import com.example.engine.CloneInstallStatus
import com.example.ui.components.AppIconAvatar
import com.example.ui.components.CloneIconStyle
import com.example.ui.components.MonoStatusPill
import com.example.ui.components.formatByteSize
import com.example.ui.theme.MonoTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CloneConfigSheet(
    packageName: String,
    originalAppName: String,
    existingEntry: CloneEntryEntity?,
    installStatus: CloneInstallStatus,
    apkSizeBytes: Long,
    storageTotalBytes: Long?,
    loadIcon: suspend (String) -> ImageBitmap?,
    onDismiss: () -> Unit,
    onSave: (
        customName: String,
        iconStyleKey: String,
        badgeLabel: String,
        groupTag: String,
        notes: String,
        requireAppLock: Boolean,
        separateNotifications: Boolean,
        pinned: Boolean,
        activateInWorkSpace: Boolean
    ) -> Unit,
    onPinShortcut: ((CloneEntryEntity) -> Unit)? = null,
    onOpenAppDetails: ((CloneEntryEntity) -> Unit)? = null,
    onDeleteClone: ((CloneEntryEntity, Boolean) -> Unit)? = null
) {
    val colors = MonoTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var customName by remember(existingEntry, originalAppName) {
        mutableStateOf(existingEntry?.customName ?: "$originalAppName (Space)")
    }
    var iconStyleKey by remember(existingEntry) {
        mutableStateOf(existingEntry?.iconStyleKey ?: CloneIconStyle.DEFAULT.key)
    }
    var badgeLabel by remember(existingEntry) {
        mutableStateOf(existingEntry?.badgeLabel ?: "WORK")
    }
    var groupTag by remember(existingEntry) {
        mutableStateOf(existingEntry?.groupTag ?: "General")
    }
    var notes by remember(existingEntry) {
        mutableStateOf(existingEntry?.notes ?: "")
    }
    var requireAppLock by remember(existingEntry) {
        mutableStateOf(existingEntry?.requireAppLock ?: false)
    }
    var separateNotifications by remember(existingEntry) {
        mutableStateOf(existingEntry?.separateNotifications ?: true)
    }
    var pinned by remember(existingEntry) {
        mutableStateOf(existingEntry?.pinned ?: false)
    }

    val groupChoices = listOf("General", "Messaging", "Social", "Work", "Finance", "Utilities")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.secondaryBackground,
        contentColor = colors.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AppIconAvatar(
                    packageName = packageName,
                    appName = customName.ifBlank { originalAppName },
                    iconStyleKey = iconStyleKey,
                    badgeLabel = badgeLabel,
                    loadIcon = loadIcon,
                    size = 56.dp
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (existingEntry == null) "Configure New Clone" else "Edit Clone Space Entry",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = packageName,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MonoStatusPill(
                            text = installStatus.label,
                            toneColor = when (installStatus) {
                                CloneInstallStatus.ACTIVE_IN_SPACE -> colors.success
                                CloneInstallStatus.NO_SPACE_PROVISIONED -> colors.warning
                                else -> colors.textSecondary
                            }
                        )
                        val sizeText = formatByteSize(storageTotalBytes ?: apkSizeBytes)
                        if (sizeText != "—") {
                            MonoStatusPill(text = sizeText, toneColor = colors.textSecondary)
                        }
                    }
                }
            }

            HorizontalDivider(color = colors.border)

            OutlinedTextField(
                value = customName,
                onValueChange = { customName = it },
                label = { Text("Custom Display Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clone_name_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = badgeLabel,
                    onValueChange = { badgeLabel = it.take(5).uppercase() },
                    label = { Text("Badge (max 5)") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("clone_badge_input")
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Account Label / Note") },
                    singleLine = true,
                    modifier = Modifier.weight(1.6f)
                )
            }

            // Icon Style selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Icon Appearance",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CloneIconStyle.entries.forEach { style ->
                        val selected = iconStyleKey == style.key
                        FilterChip(
                            selected = selected,
                            onClick = { iconStyleKey = style.key },
                            label = { Text(style.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accent,
                                selectedLabelColor = colors.onAccent
                            )
                        )
                    }
                }
            }

            // Organization Group selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dashboard Group",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupChoices.forEach { tag ->
                        FilterChip(
                            selected = groupTag == tag,
                            onClick = { groupTag = tag },
                            label = { Text(tag) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accent,
                                selectedLabelColor = colors.onAccent
                            )
                        )
                    }
                }
            }

            // Toggles
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingToggleRow(
                    title = "Require PIN / Biometrics on Launch",
                    subtitle = "Prompt authentication before opening this clone",
                    checked = requireAppLock,
                    onCheckedChange = { requireAppLock = it }
                )
                SettingToggleRow(
                    title = "Separate Space Notifications",
                    subtitle = "Handled natively by Android Work Profile badge",
                    checked = separateNotifications,
                    onCheckedChange = { separateNotifications = it }
                )
                SettingToggleRow(
                    title = "Pin to Top of Dashboard",
                    subtitle = "Keep this app at the start of your space list",
                    checked = pinned,
                    onCheckedChange = { pinned = it }
                )
            }

            // Extra actions for existing clones
            if (existingEntry != null) {
                HorizontalDivider(color = colors.border)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onPinShortcut != null) {
                        OutlinedButton(
                            onClick = { onPinShortcut(existingEntry) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddToHomeScreen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Shortcut")
                        }
                    }
                    if (onOpenAppDetails != null) {
                        OutlinedButton(
                            onClick = { onOpenAppDetails(existingEntry) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("App Info")
                        }
                    }
                }
                if (onDeleteClone != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { onDeleteClone(existingEntry, false) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                tint = colors.danger,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Remove Entry", color = colors.danger)
                        }
                        if (installStatus == CloneInstallStatus.ACTIVE_IN_SPACE) {
                            TextButton(
                                onClick = { onDeleteClone(existingEntry, true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Uninstall from Space", color = colors.danger)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSave(
                        customName,
                        iconStyleKey,
                        badgeLabel,
                        groupTag,
                        notes,
                        requireAppLock,
                        separateNotifications,
                        pinned,
                        existingEntry == null || installStatus != CloneInstallStatus.ACTIVE_IN_SPACE
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("save_clone_button")
            ) {
                Text(
                    if (existingEntry == null) "Save & Prepare in Work Space"
                    else "Save Changes"
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MonoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
