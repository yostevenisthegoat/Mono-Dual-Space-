package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Launch
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CloneEntryEntity
import com.example.engine.CloneInstallStatus
import com.example.engine.ManagedSpaceInfo
import com.example.ui.components.AppIconAvatar
import com.example.ui.components.MonoBrandMark
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoSearchField
import com.example.ui.components.MonoStatusPill
import com.example.ui.components.formatByteSize
import com.example.ui.theme.MonoTheme
import com.example.ui.viewmodel.CloneDashboardItem

@Composable
fun DashboardScreen(
    clones: List<CloneDashboardItem>,
    totalConfiguredCount: Int,
    spaces: List<ManagedSpaceInfo>,
    searchQuery: String,
    selectedGroupTag: String,
    compactDensity: Boolean,
    isPinEnabled: Boolean,
    loadIcon: suspend (String) -> ImageBitmap?,
    onSearchQueryChange: (String) -> Unit,
    onSelectGroupTag: (String) -> Unit,
    onAddApplicationClick: () -> Unit,
    onQuickPrivacyClick: () -> Unit,
    onLaunchClone: (CloneEntryEntity) -> Unit,
    onConfigureClone: (CloneDashboardItem) -> Unit,
    onPinShortcut: (CloneEntryEntity) -> Unit,
    onOpenAppDetails: (CloneEntryEntity) -> Unit,
    onRemoveClone: (CloneEntryEntity, Boolean) -> Unit
) {
    val colors = MonoTheme.colors
    val workSpace = spaces.firstOrNull { it.isManagedProfile }
    val groupTags = remember(clones) {
        listOf("All") + clones.map { it.entry.groupTag }.distinct().sorted()
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
            verticalArrangement = Arrangement.spacedBy(if (compactDensity) 10.dp else 14.dp)
        ) {
            item(key = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MonoBrandMark(size = 34.dp)
                        Column {
                            Text(
                                text = "Mono Dual-Space",
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "One device. Multiple spaces.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onQuickPrivacyClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("dashboard_privacy_button")
                    ) {
                        Icon(
                            imageVector = if (isPinEnabled) Icons.Outlined.Lock else Icons.Outlined.Shield,
                            contentDescription = "Quick privacy settings",
                            tint = colors.accent
                        )
                    }
                }
            }

            item(key = "metrics_strip") {
                MonoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "CONFIGURED CLONES",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "$totalConfiguredCount",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colors.textPrimary,
                                modifier = Modifier.testTag("dashboard_clone_count")
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            MonoStatusPill(
                                text = when {
                                    workSpace == null -> "No Work Space"
                                    workSpace.isQuietModeEnabled -> "Space Paused"
                                    else -> "Work Space Active"
                                },
                                toneColor = when {
                                    workSpace == null -> colors.warning
                                    workSpace.isQuietModeEnabled -> colors.warning
                                    else -> colors.success
                                }
                            )
                            Text(
                                text = if (workSpace != null) "${workSpace.installedAppCount} apps in space"
                                else "Open Spaces to provision",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            item(key = "search_and_add") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MonoSearchField(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChange,
                            placeholder = "Search configured clones…",
                            testTag = "dashboard_search_input",
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onAddApplicationClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.onAccent
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("dashboard_add_app_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add Application",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Add")
                        }
                    }

                    AnimatedVisibility(visible = groupTags.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            groupTags.forEach { tag ->
                                FilterChip(
                                    selected = selectedGroupTag == tag,
                                    onClick = { onSelectGroupTag(tag) },
                                    label = { Text(tag) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.accent,
                                        selectedLabelColor = colors.onAccent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (clones.isEmpty()) {
                item(key = "empty_state") {
                    MonoCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = if (totalConfiguredCount == 0) "No Clones Configured Yet"
                                else "No Matching Clones",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (totalConfiguredCount == 0) {
                                    "Discover installed apps in Applications and configure them inside your Android Managed Work Profile."
                                } else {
                                    "Clear your search or group filter to see all configured spaces."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            if (totalConfiguredCount == 0) {
                                OutlinedButton(
                                    onClick = onAddApplicationClick,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, colors.accent),
                                    modifier = Modifier.testTag("empty_add_application_button")
                                ) {
                                    Text("Browse Applications", color = colors.accent)
                                }
                            }
                        }
                    }
                }
            } else {
                items(
                    items = clones,
                    key = { it.entry.id }
                ) { item ->
                    CloneDashboardCard(
                        item = item,
                        compactDensity = compactDensity,
                        loadIcon = loadIcon,
                        onLaunch = { onLaunchClone(item.entry) },
                        onConfigure = { onConfigureClone(item) },
                        onPinShortcut = { onPinShortcut(item.entry) },
                        onOpenAppDetails = { onOpenAppDetails(item.entry) },
                        onRemove = { uninstall -> onRemoveClone(item.entry, uninstall) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CloneDashboardCard(
    item: CloneDashboardItem,
    compactDensity: Boolean,
    loadIcon: suspend (String) -> ImageBitmap?,
    onLaunch: () -> Unit,
    onConfigure: () -> Unit,
    onPinShortcut: () -> Unit,
    onOpenAppDetails: () -> Unit,
    onRemove: (Boolean) -> Unit
) {
    val colors = MonoTheme.colors
    val entry = item.entry
    var menuExpanded by remember { mutableStateOf(false) }

    MonoCard(
        onClick = onConfigure,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("clone_card_${entry.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compactDensity) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppIconAvatar(
                packageName = entry.packageName,
                appName = entry.customName,
                iconStyleKey = entry.iconStyleKey,
                badgeLabel = entry.badgeLabel,
                loadIcon = loadIcon,
                size = if (compactDensity) 44.dp else 50.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = entry.customName,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (entry.pinned) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = "Pinned",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (entry.requireAppLock) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Protected by app lock",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = if (entry.notes.isNotBlank()) "${entry.notes} • ${entry.groupTag}"
                    else "${entry.packageName} • ${entry.groupTag}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonoStatusPill(
                        text = item.installStatus.label,
                        toneColor = when (item.installStatus) {
                            CloneInstallStatus.ACTIVE_IN_SPACE -> colors.success
                            CloneInstallStatus.NO_SPACE_PROVISIONED -> colors.warning
                            else -> colors.textSecondary
                        }
                    )
                    val size = formatByteSize(item.storageTotalBytes ?: item.apkSizeBytes)
                    if (size != "—") {
                        Text(
                            text = size,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onLaunch,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.onAccent
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("launch_clone_${entry.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Launch,
                        contentDescription = "Launch ${entry.customName}",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Open",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More options for ${entry.customName}",
                            tint = colors.textSecondary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Configure & Rename") },
                            onClick = {
                                menuExpanded = false
                                onConfigure()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pin Home Shortcut") },
                            onClick = {
                                menuExpanded = false
                                onPinShortcut()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Space App Info") },
                            onClick = {
                                menuExpanded = false
                                onOpenAppDetails()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove from Dashboard") },
                            onClick = {
                                menuExpanded = false
                                onRemove(false)
                            }
                        )
                        if (item.installStatus == CloneInstallStatus.ACTIVE_IN_SPACE) {
                            DropdownMenuItem(
                                text = { Text("Uninstall from Work Space", color = colors.danger) },
                                onClick = {
                                    menuExpanded = false
                                    onRemove(true)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
