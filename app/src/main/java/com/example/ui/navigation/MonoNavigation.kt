package com.example.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.CloneEntryEntity
import com.example.data.preferences.AppSortOrder
import com.example.engine.CloneInstallStatus
import com.example.security.BiometricHelper
import com.example.ui.screens.ApplicationsScreen
import com.example.ui.screens.CloneConfigSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpacesScreen
import com.example.ui.theme.MonoTheme
import com.example.ui.viewmodel.SecurityViewModel
import com.example.ui.viewmodel.SpaceUiEvent
import com.example.ui.viewmodel.SpaceViewModel
import kotlinx.serialization.Serializable

sealed interface MonoRoute {
    @Serializable data object Dashboard : MonoRoute
    @Serializable data object Applications : MonoRoute
    @Serializable data object Spaces : MonoRoute
    @Serializable data object Privacy : MonoRoute
    @Serializable data object Settings : MonoRoute
}

private data class TopLevelDestination(
    val route: MonoRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = MonoRoute.Dashboard,
        label = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_dashboard"
    ),
    TopLevelDestination(
        route = MonoRoute.Applications,
        label = "Apps",
        selectedIcon = Icons.Filled.Apps,
        unselectedIcon = Icons.Outlined.Apps,
        testTag = "nav_applications"
    ),
    TopLevelDestination(
        route = MonoRoute.Spaces,
        label = "Spaces",
        selectedIcon = Icons.Filled.Layers,
        unselectedIcon = Icons.Outlined.Layers,
        testTag = "nav_spaces"
    ),
    TopLevelDestination(
        route = MonoRoute.Privacy,
        label = "Privacy",
        selectedIcon = Icons.Filled.Lock,
        unselectedIcon = Icons.Outlined.Lock,
        testTag = "nav_privacy"
    ),
    TopLevelDestination(
        route = MonoRoute.Settings,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    )
)

private data class CloneEditorTarget(
    val packageName: String,
    val originalAppName: String,
    val existingEntry: CloneEntryEntity?,
    val installStatus: CloneInstallStatus,
    val apkSizeBytes: Long,
    val storageTotalBytes: Long?
)

@Composable
fun MonoAppRoot(
    activity: FragmentActivity,
    widthSizeClass: WindowWidthSizeClass,
    securityViewModel: SecurityViewModel,
    spaceViewModel: SpaceViewModel,
    onLaunchProvisioning: (Intent) -> Unit
) {
    val context = LocalContext.current
    val colors = MonoTheme.colors
    val preferences by securityViewModel.preferences.collectAsStateWithLifecycle()
    val isLocked by securityViewModel.isLocked.collectAsStateWithLifecycle()
    val pinError by securityViewModel.pinError.collectAsStateWithLifecycle()
    val biometricAvailability by securityViewModel.biometricAvailability.collectAsStateWithLifecycle()

    val capabilities by spaceViewModel.capabilities.collectAsStateWithLifecycle()
    val spaces by spaceViewModel.spaces.collectAsStateWithLifecycle()
    val dashboardClones by spaceViewModel.dashboardClones.collectAsStateWithLifecycle()
    val allClones by spaceViewModel.allClones.collectAsStateWithLifecycle()
    val discoveredApps by spaceViewModel.filteredDiscoveredApps.collectAsStateWithLifecycle()
    val isScanningApps by spaceViewModel.isScanningApps.collectAsStateWithLifecycle()
    val appSearchQuery by spaceViewModel.appSearchQuery.collectAsStateWithLifecycle()
    val appFilterMode by spaceViewModel.appFilterMode.collectAsStateWithLifecycle()
    val appSortOrder by spaceViewModel.appSortOrder.collectAsStateWithLifecycle()
    val dashboardSearchQuery by spaceViewModel.dashboardSearchQuery.collectAsStateWithLifecycle()
    val selectedGroupTag by spaceViewModel.selectedGroupTag.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var forceShowOnboarding by remember { mutableStateOf(false) }
    var editorTarget by remember { mutableStateOf<CloneEditorTarget?>(null) }
    var storePromptEvent by remember { mutableStateOf<SpaceUiEvent.PromptStoreInstall?>(null) }
    var protectedCloneChallenge by remember { mutableStateOf<CloneEntryEntity?>(null) }
    var challengePinError by remember { mutableStateOf<String?>(null) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { spaceViewModel.exportBackup(it) }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { spaceViewModel.importBackup(it) }
    }

    LaunchedEffect(spaceViewModel) {
        spaceViewModel.events.collect { event ->
            when (event) {
                is SpaceUiEvent.Snackbar -> snackbarHostState.showSnackbar(event.message)
                is SpaceUiEvent.LaunchExternalIntent -> {
                    runCatching { context.startActivity(event.intent) }
                        .onFailure { snackbarHostState.showSnackbar("Unable to open system activity: ${it.message}") }
                }
                is SpaceUiEvent.ProvisioningRequested -> onLaunchProvisioning(event.intent)
                is SpaceUiEvent.PromptStoreInstall -> storePromptEvent = event
                is SpaceUiEvent.RequestProtectedCloneLaunch -> {
                    challengePinError = null
                    protectedCloneChallenge = event.clone
                }
            }
        }
    }

    // Lock screen gate
    if (isLocked && preferences.isPinEnabled) {
        LockScreen(
            errorMessage = pinError,
            biometricEnabled = preferences.isBiometricEnabled,
            biometricAvailable = biometricAvailability.canAuthenticate,
            onSubmitPin = { pin -> securityViewModel.verifyAndUnlock(pin) },
            onRequestBiometric = {
                BiometricHelper.authenticate(
                    activity = activity,
                    title = "Unlock Mono Dual-Space",
                    subtitle = "Verify identity to access isolated spaces",
                    negativeButtonText = "Use PIN",
                    onSuccess = { securityViewModel.unlockWithBiometrics() },
                    onError = { msg -> securityViewModel.clearPinError() }
                )
            },
            onClearError = { securityViewModel.clearPinError() }
        )
        return
    }

    // Per-clone lock gate
    protectedCloneChallenge?.let { targetClone ->
        LockScreen(
            title = "Unlock ${targetClone.customName}",
            subtitle = "Per-clone protection is enabled for this space entry",
            errorMessage = challengePinError,
            biometricEnabled = preferences.isBiometricEnabled,
            biometricAvailable = biometricAvailability.canAuthenticate,
            onSubmitPin = { pin ->
                val ok = securityViewModel.verifyPinOnly(pin)
                if (ok) {
                    protectedCloneChallenge = null
                    challengePinError = null
                    spaceViewModel.executeCloneLaunch(targetClone)
                } else {
                    challengePinError = "Incorrect PIN."
                }
                ok
            },
            onRequestBiometric = {
                BiometricHelper.authenticate(
                    activity = activity,
                    title = "Open ${targetClone.customName}",
                    subtitle = "Authenticate to launch cloned application",
                    negativeButtonText = "Use PIN",
                    onSuccess = {
                        protectedCloneChallenge = null
                        challengePinError = null
                        spaceViewModel.executeCloneLaunch(targetClone)
                    },
                    onError = { }
                )
            },
            onClearError = { challengePinError = null },
            onDismiss = {
                protectedCloneChallenge = null
                challengePinError = null
            }
        )
        return
    }

    // First-launch Onboarding flow
    if (!preferences.onboardingCompleted || forceShowOnboarding) {
        OnboardingScreen(
            capabilities = capabilities,
            spaces = spaces,
            onInitiateProvisioning = { spaceViewModel.requestProvisionManagedSpace() },
            onFinishOnboarding = {
                forceShowOnboarding = false
                securityViewModel.completeOnboarding()
            }
        )
        return
    }

    // Store/Installer guidance dialog when app isn't yet inside Work Space
    storePromptEvent?.let { prompt ->
        AlertDialog(
            onDismissRequest = { storePromptEvent = null },
            containerColor = colors.secondaryBackground,
            title = { Text("${prompt.appName} in Work Space", color = colors.textPrimary) },
            text = {
                Text(
                    text = prompt.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                if (prompt.storeIntent != null) {
                    Button(
                        onClick = {
                            val intent = prompt.storeIntent
                            storePromptEvent = null
                            runCatching { context.startActivity(intent) }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.onAccent
                        )
                    ) {
                        Text("Open App Store")
                    }
                } else {
                    Button(
                        onClick = {
                            storePromptEvent = null
                            spaceViewModel.openMonoInWorkSpace()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.onAccent
                        )
                    ) {
                        Text("Open Work Space")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { storePromptEvent = null }) {
                    Text("Close", color = colors.textSecondary)
                }
            }
        )
    }

    // Clone configuration bottom sheet
    editorTarget?.let { target ->
        CloneConfigSheet(
            packageName = target.packageName,
            originalAppName = target.originalAppName,
            existingEntry = target.existingEntry,
            installStatus = target.installStatus,
            apkSizeBytes = target.apkSizeBytes,
            storageTotalBytes = target.storageTotalBytes,
            loadIcon = { pkg -> spaceViewModel.loadIcon(pkg) },
            onDismiss = { editorTarget = null },
            onSave = { customName, iconStyleKey, badgeLabel, groupTag, notes, requireLock, separateNotif, pinned, activate ->
                spaceViewModel.createOrUpdateClone(
                    existingId = target.existingEntry?.id ?: 0L,
                    packageName = target.packageName,
                    originalAppName = target.originalAppName,
                    customName = customName,
                    iconStyleKey = iconStyleKey,
                    badgeLabel = badgeLabel,
                    groupTag = groupTag,
                    notes = notes,
                    requireAppLock = requireLock,
                    separateNotifications = separateNotif,
                    pinned = pinned,
                    attemptWorkSpaceActivation = activate
                )
                editorTarget = null
            },
            onPinShortcut = { entry ->
                spaceViewModel.pinHomeShortcut(entry)
                editorTarget = null
            },
            onOpenAppDetails = { entry ->
                spaceViewModel.openAppDetailsInSpace(entry)
            },
            onDeleteClone = { entry, alsoUninstall ->
                spaceViewModel.removeCloneEntry(entry, alsoUninstall)
                editorTarget = null
            }
        )
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val useRail = widthSizeClass != WindowWidthSizeClass.Compact

    fun navigateTopLevel(route: MonoRoute) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.primaryBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!useRail) {
                NavigationBar(
                    containerColor = colors.secondaryBackground,
                    tonalElevation = 0.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    topLevelDestinations.forEach { dest ->
                        val selected = currentDestination?.hasRoute(dest.route::class) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateTopLevel(dest.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.label
                                )
                            },
                            label = {
                                Text(
                                    text = dest.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.onAccent,
                                selectedTextColor = colors.textPrimary,
                                indicatorColor = colors.accent,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary
                            ),
                            modifier = Modifier.testTag(dest.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (useRail) {
                NavigationRail(
                    containerColor = colors.secondaryBackground
                ) {
                    topLevelDestinations.forEach { dest ->
                        val selected = currentDestination?.hasRoute(dest.route::class) == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTopLevel(dest.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.label
                                )
                            },
                            label = { Text(dest.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = colors.onAccent,
                                selectedTextColor = colors.textPrimary,
                                indicatorColor = colors.accent,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary
                            ),
                            modifier = Modifier.testTag(dest.testTag)
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = MonoRoute.Dashboard
                ) {
                    composable<MonoRoute.Dashboard> {
                        DashboardScreen(
                            clones = dashboardClones,
                            totalConfiguredCount = allClones.size,
                            spaces = spaces,
                            searchQuery = dashboardSearchQuery,
                            selectedGroupTag = selectedGroupTag,
                            compactDensity = preferences.compactDensity,
                            isPinEnabled = preferences.isPinEnabled,
                            loadIcon = { pkg -> spaceViewModel.loadIcon(pkg) },
                            onSearchQueryChange = spaceViewModel::updateDashboardSearchQuery,
                            onSelectGroupTag = spaceViewModel::updateSelectedGroupTag,
                            onAddApplicationClick = { navigateTopLevel(MonoRoute.Applications) },
                            onQuickPrivacyClick = { navigateTopLevel(MonoRoute.Privacy) },
                            onLaunchClone = { clone ->
                                spaceViewModel.requestLaunchClone(clone, preferences.isPinEnabled)
                            },
                            onConfigureClone = { item ->
                                editorTarget = CloneEditorTarget(
                                    packageName = item.entry.packageName,
                                    originalAppName = item.entry.originalAppName,
                                    existingEntry = item.entry,
                                    installStatus = item.installStatus,
                                    apkSizeBytes = item.apkSizeBytes,
                                    storageTotalBytes = item.storageTotalBytes
                                )
                            },
                            onPinShortcut = spaceViewModel::pinHomeShortcut,
                            onOpenAppDetails = spaceViewModel::openAppDetailsInSpace,
                            onRemoveClone = spaceViewModel::removeCloneEntry
                        )
                    }

                    composable<MonoRoute.Applications> {
                        ApplicationsScreen(
                            apps = discoveredApps,
                            configuredClones = allClones,
                            isScanning = isScanningApps,
                            searchQuery = appSearchQuery,
                            filterMode = appFilterMode,
                            sortOrder = appSortOrder,
                            compactDensity = preferences.compactDensity,
                            loadIcon = { pkg -> spaceViewModel.loadIcon(pkg) },
                            onSearchQueryChange = spaceViewModel::updateAppSearchQuery,
                            onFilterModeChange = spaceViewModel::updateAppFilterMode,
                            onToggleSortOrder = {
                                val next = if (appSortOrder == AppSortOrder.NAME_ASC) {
                                    AppSortOrder.NAME_DESC
                                } else {
                                    AppSortOrder.NAME_ASC
                                }
                                spaceViewModel.updateAppSortOrder(next)
                            },
                            onRefresh = { spaceViewModel.refreshSpacesAndApps(forceScan = true) },
                            onSelectAppToClone = { app, existing ->
                                editorTarget = CloneEditorTarget(
                                    packageName = app.packageName,
                                    originalAppName = app.appName,
                                    existingEntry = existing,
                                    installStatus = app.installStatus,
                                    apkSizeBytes = app.apkSizeBytes,
                                    storageTotalBytes = app.storageTotalBytes
                                )
                            }
                        )
                    }

                    composable<MonoRoute.Spaces> {
                        SpacesScreen(
                            primaryCapabilities = capabilities,
                            secondaryCapabilities = spaceViewModel.secondaryCapabilities,
                            spaces = spaces,
                            configuredCloneCount = allClones.size,
                            onRefresh = { spaceViewModel.refreshSpacesAndApps(forceScan = true) },
                            onProvisionWorkSpace = { spaceViewModel.requestProvisionManagedSpace() },
                            onToggleQuietMode = spaceViewModel::toggleQuietMode,
                            onOpenMonoInWorkSpace = spaceViewModel::openMonoInWorkSpace,
                            onRunSpaceAudit = spaceViewModel::runBackgroundAudit,
                            onWipeWorkSpace = spaceViewModel::wipeWorkSpace,
                            onExportConfig = {
                                exportBackupLauncher.launch("mono-dual-space-backup.json")
                            },
                            onImportConfig = {
                                importBackupLauncher.launch(arrayOf("application/json", "text/plain"))
                            }
                        )
                    }

                    composable<MonoRoute.Privacy> {
                        PrivacyScreen(
                            preferences = preferences,
                            biometricAvailability = biometricAvailability,
                            onSetupOrChangePin = securityViewModel::enableOrUpdatePin,
                            onDisablePin = securityViewModel::disablePin,
                            onToggleBiometric = securityViewModel::setBiometricEnabled,
                            onSelectAutoLockTimeout = securityViewModel::setAutoLockTimeout,
                            onToggleLockOnLaunch = securityViewModel::setLockOnLaunch,
                            onToggleHideInRecents = securityViewModel::setHideInRecents,
                            onLockNow = securityViewModel::lockNow
                        )
                    }

                    composable<MonoRoute.Settings> {
                        SettingsScreen(
                            preferences = preferences,
                            capabilities = capabilities,
                            onSelectThemeMode = securityViewModel::setThemeMode,
                            onToggleCompactDensity = securityViewModel::setCompactDensity,
                            onShowOnboardingAgain = { forceShowOnboarding = true },
                            onOpenGitHub = {
                                val githubIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/mono-ecosystem/mono-dual-space")
                                )
                                runCatching { context.startActivity(githubIntent) }
                            }
                        )
                    }
                }
            }
        }
    }
}
