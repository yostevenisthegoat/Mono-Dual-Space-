package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CloneEntryEntity
import com.example.data.preferences.AppSortOrder
import com.example.engine.CloneInstallStatus
import com.example.engine.DiscoveredApp
import com.example.ui.components.AppIconAvatar
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoSearchField
import com.example.ui.components.MonoStatusPill
import com.example.ui.components.formatByteSize
import com.example.ui.theme.MonoTheme
import com.example.ui.viewmodel.AppFilterMode

@Composable
fun ApplicationsScreen(
    apps: List<DiscoveredApp>,
    configuredClones: List<CloneEntryEntity>,
    isScanning: Boolean,
    searchQuery: String,
    filterMode: AppFilterMode,
    sortOrder: AppSortOrder,
    compactDensity: Boolean,
    loadIcon: suspend (String) -> ImageBitmap?,
    onSearchQueryChange: (String) -> Unit,
    onFilterModeChange: (AppFilterMode) -> Unit,
    onToggleSortOrder: () -> Unit,
    onRefresh: () -> Unit,
    onSelectAppToClone: (DiscoveredApp, CloneEntryEntity?) -> Unit
) {
    val colors = MonoTheme.colors
    val cloneByPkg = configuredClones.associateBy { it.packageName }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (compactDensity) 10.dp else 12.dp)
        ) {
            item(key = "apps_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Applications",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${apps.size} discovered • Sorted ${sortOrder.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleSortOrder,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("apps_sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SwapVert,
                                contentDescription = "Toggle alphabetical sort",
                                tint = colors.textPrimary
                            )
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("apps_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh installed apps",
                                tint = colors.textPrimary
                            )
                        }
                    }
                }
            }

            item(key = "apps_controls") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MonoSearchField(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        placeholder = "Search installed apps or package IDs…",
                        testTag = "apps_search_input"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppFilterMode.entries.forEach { mode ->
                            FilterChip(
                                selected = filterMode == mode,
                                onClick = { onFilterModeChange(mode) },
                                label = { Text(mode.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.accent,
                                    selectedLabelColor = colors.onAccent
                                ),
                                modifier = Modifier.testTag("apps_filter_${mode.name.lowercase()}")
                            )
                        }
                    }

                    if (isScanning) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = colors.accent,
                            trackColor = colors.secondaryBackground
                        )
                    }
                }
            }

            if (apps.isEmpty() && !isScanning) {
                item(key = "apps_empty") {
                    MonoCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Apps,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No Applications Match",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Try switching filters (User Apps / System / All) or clearing your search query. Android limits visibility to launcher-declared packages.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            } else {
                items(
                    items = apps,
                    key = { it.packageName }
                ) { app ->
                    val existingClone = cloneByPkg[app.packageName]
                    DiscoveredAppRow(
                        app = app,
                        existingClone = existingClone,
                        compactDensity = compactDensity,
                        loadIcon = loadIcon,
                        onCloneClick = { onSelectAppToClone(app, existingClone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoveredAppRow(
    app: DiscoveredApp,
    existingClone: CloneEntryEntity?,
    compactDensity: Boolean,
    loadIcon: suspend (String) -> ImageBitmap?,
    onCloneClick: () -> Unit
) {
    val colors = MonoTheme.colors
    MonoCard(
        onClick = onCloneClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discovered_app_${app.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compactDensity) 12.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppIconAvatar(
                packageName = app.packageName,
                appName = app.appName,
                loadIcon = loadIcon,
                size = if (compactDensity) 42.dp else 46.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${app.packageName} • v${app.versionName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (existingClone != null) {
                        MonoStatusPill(text = "Configured", toneColor = colors.accent)
                    }
                    MonoStatusPill(
                        text = app.installStatus.label,
                        toneColor = when (app.installStatus) {
                            CloneInstallStatus.ACTIVE_IN_SPACE -> colors.success
                            CloneInstallStatus.NO_SPACE_PROVISIONED -> colors.warning
                            else -> colors.textSecondary
                        }
                    )
                    if (app.isSystemApp) {
                        MonoStatusPill(text = "System", toneColor = colors.textSecondary)
                    }
                    val size = formatByteSize(app.storageTotalBytes ?: app.apkSizeBytes)
                    if (size != "—") {
                        Text(
                            text = size,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Button(
                onClick = onCloneClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (existingClone != null) colors.secondaryBackground else colors.accent,
                    contentColor = if (existingClone != null) colors.textPrimary else colors.onAccent
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .height(40.dp)
                    .testTag("clone_action_${app.packageName}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddCircleOutline,
                    contentDescription = "Clone ${app.appName}",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = if (existingClone != null) "Edit" else "Clone",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
