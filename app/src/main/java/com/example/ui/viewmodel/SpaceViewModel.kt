package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupResult
import com.example.data.local.CloneEntryEntity
import com.example.data.local.CloneRepository
import com.example.data.local.SpaceProfileEntity
import com.example.data.preferences.AppSortOrder
import com.example.engine.AppDiscoveryRepository
import com.example.engine.CloneInstallStatus
import com.example.engine.CloneOperationResult
import com.example.engine.CloningEngine
import com.example.engine.DiscoveredApp
import com.example.engine.EngineCapabilities
import com.example.engine.ManagedSpaceInfo
import com.example.worker.SpaceAuditWorker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppFilterMode(val label: String) {
    USER("User Apps"),
    ALL("All Apps"),
    SYSTEM("System"),
    IN_SPACE("In Work Space")
}

sealed interface SpaceUiEvent {
    data class Snackbar(val message: String) : SpaceUiEvent
    data class LaunchExternalIntent(val intent: Intent) : SpaceUiEvent
    data class ProvisioningRequested(val intent: Intent) : SpaceUiEvent
    data class PromptStoreInstall(
        val packageName: String,
        val appName: String,
        val message: String,
        val storeIntent: Intent?
    ) : SpaceUiEvent
    data class RequestProtectedCloneLaunch(val clone: CloneEntryEntity) : SpaceUiEvent
}

data class CloneDashboardItem(
    val entry: CloneEntryEntity,
    val installStatus: CloneInstallStatus,
    val apkSizeBytes: Long,
    val storageTotalBytes: Long?
)

class SpaceViewModel(
    private val context: Context,
    private val repository: CloneRepository,
    private val primaryEngine: CloningEngine,
    private val secondaryEngine: CloningEngine,
    private val discoveryRepository: AppDiscoveryRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _capabilities = MutableStateFlow(primaryEngine.inspectCapabilities())
    val capabilities: StateFlow<EngineCapabilities> = _capabilities.asStateFlow()

    val secondaryCapabilities: EngineCapabilities = secondaryEngine.inspectCapabilities()

    private val _spaces = MutableStateFlow(primaryEngine.inspectSpaces())
    val spaces: StateFlow<List<ManagedSpaceInfo>> = _spaces.asStateFlow()

    private val _discoveredApps = MutableStateFlow<List<DiscoveredApp>>(emptyList())
    private val _isScanningApps = MutableStateFlow(false)
    val isScanningApps: StateFlow<Boolean> = _isScanningApps.asStateFlow()

    private val _appSearchQuery = MutableStateFlow("")
    val appSearchQuery: StateFlow<String> = _appSearchQuery.asStateFlow()

    private val _appFilterMode = MutableStateFlow(AppFilterMode.USER)
    val appFilterMode: StateFlow<AppFilterMode> = _appFilterMode.asStateFlow()

    private val _appSortOrder = MutableStateFlow(AppSortOrder.NAME_ASC)
    val appSortOrder: StateFlow<AppSortOrder> = _appSortOrder.asStateFlow()

    private val _dashboardSearchQuery = MutableStateFlow("")
    val dashboardSearchQuery: StateFlow<String> = _dashboardSearchQuery.asStateFlow()

    private val _selectedGroupTag = MutableStateFlow("All")
    val selectedGroupTag: StateFlow<String> = _selectedGroupTag.asStateFlow()

    private val _events = MutableSharedFlow<SpaceUiEvent>()
    val events: SharedFlow<SpaceUiEvent> = _events.asSharedFlow()

    val allClones: StateFlow<List<CloneEntryEntity>> = repository.allClones
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val filteredDiscoveredApps: StateFlow<List<DiscoveredApp>> = combine(
        _discoveredApps,
        _appSearchQuery,
        _appFilterMode,
        _appSortOrder
    ) { apps, query, filter, sort ->
        val filtered = apps.filter { app ->
            val matchesQuery = query.isBlank() ||
                app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                AppFilterMode.ALL -> true
                AppFilterMode.USER -> !app.isSystemApp
                AppFilterMode.SYSTEM -> app.isSystemApp
                AppFilterMode.IN_SPACE -> app.installStatus == CloneInstallStatus.ACTIVE_IN_SPACE
            }
            matchesQuery && matchesFilter
        }
        when (sort) {
            AppSortOrder.NAME_ASC -> filtered.sortedBy { it.appName.lowercase() }
            AppSortOrder.NAME_DESC -> filtered.sortedByDescending { it.appName.lowercase() }
            AppSortOrder.RECENTLY_USED -> filtered.sortedBy { it.appName.lowercase() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val dashboardClones: StateFlow<List<CloneDashboardItem>> = combine(
        allClones,
        _discoveredApps,
        _dashboardSearchQuery,
        _selectedGroupTag,
        _spaces
    ) { clones, apps, query, groupTag, spaceList ->
        val appByPkg = apps.associateBy { it.packageName }
        val activeWorkSerial = spaceList.firstOrNull { it.isManagedProfile }?.serialNumber
        clones
            .filter { clone ->
                val matchesQuery = query.isBlank() ||
                    clone.customName.contains(query, ignoreCase = true) ||
                    clone.originalAppName.contains(query, ignoreCase = true) ||
                    clone.packageName.contains(query, ignoreCase = true)
                val matchesGroup = groupTag == "All" || clone.groupTag.equals(groupTag, ignoreCase = true)
                matchesQuery && matchesGroup
            }
            .map { clone ->
                val targetSerial = if (clone.profileUserSerial >= 0L) clone.profileUserSerial else activeWorkSerial
                val discovered = appByPkg[clone.packageName]
                val status = primaryEngine.getCloneInstallStatus(clone.packageName, targetSerial)
                CloneDashboardItem(
                    entry = clone,
                    installStatus = status,
                    apkSizeBytes = discovered?.apkSizeBytes ?: 0L,
                    storageTotalBytes = discovered?.storageTotalBytes
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        refreshSpacesAndApps(forceScan = true)
    }

    fun refreshSpacesAndApps(forceScan: Boolean = false) {
        viewModelScope.launch {
            _capabilities.value = primaryEngine.inspectCapabilities()
            val inspected = primaryEngine.inspectSpaces()
            _spaces.value = inspected

            inspected.filter { it.isManagedProfile }.forEach { space ->
                repository.upsertSpace(
                    SpaceProfileEntity(
                        userSerialNumber = space.serialNumber,
                        label = space.label,
                        engineType = space.engineType.name,
                        isProvisionedByMono = space.isProfileOwnerInSpace
                    )
                )
            }

            if (forceScan || _discoveredApps.value.isEmpty()) {
                _isScanningApps.value = true
                val targetSerial = inspected.firstOrNull { it.isManagedProfile }?.serialNumber
                val apps = discoveryRepository.discoverInstalledApps(targetSerial)
                _discoveredApps.value = apps
                repository.upsertCachedApps(discoveryRepository.toCachedEntities(apps))
                _isScanningApps.value = false
            }
        }
    }

    suspend fun loadIcon(packageName: String): ImageBitmap? =
        discoveryRepository.loadAppIcon(packageName)

    fun updateAppSearchQuery(query: String) {
        _appSearchQuery.value = query
    }

    fun updateAppFilterMode(mode: AppFilterMode) {
        _appFilterMode.value = mode
    }

    fun updateAppSortOrder(order: AppSortOrder) {
        _appSortOrder.value = order
    }

    fun updateDashboardSearchQuery(query: String) {
        _dashboardSearchQuery.value = query
    }

    fun updateSelectedGroupTag(tag: String) {
        _selectedGroupTag.value = tag
    }

    fun requestProvisionManagedSpace() {
        viewModelScope.launch {
            val intent = primaryEngine.buildProvisioningIntent()
            if (intent != null) {
                _events.emit(SpaceUiEvent.ProvisioningRequested(intent))
            } else {
                val cap = primaryEngine.inspectCapabilities()
                _events.emit(SpaceUiEvent.Snackbar(cap.statusSummary))
            }
        }
    }

    fun onProvisioningCompleted(success: Boolean) {
        refreshSpacesAndApps(forceScan = true)
        viewModelScope.launch {
            if (success) {
                _events.emit(SpaceUiEvent.Snackbar("Managed Work Space provisioned. Configure apps in Applications."))
            } else {
                _events.emit(SpaceUiEvent.Snackbar("Work Space provisioning was cancelled or declined."))
            }
        }
    }

    fun createOrUpdateClone(
        existingId: Long = 0L,
        packageName: String,
        originalAppName: String,
        customName: String,
        iconStyleKey: String,
        badgeLabel: String,
        groupTag: String,
        notes: String,
        requireAppLock: Boolean,
        separateNotifications: Boolean,
        pinned: Boolean,
        attemptWorkSpaceActivation: Boolean
    ) {
        viewModelScope.launch {
            val workSpace = _spaces.value.firstOrNull { it.isManagedProfile }
            val targetSerial = workSpace?.serialNumber ?: -1L
            val existingByPkg = if (existingId == 0L) {
                repository.getCloneByPackageAndProfile(packageName, targetSerial)
            } else null

            val resolvedId = if (existingId != 0L) existingId else (existingByPkg?.id ?: 0L)
            val entry = CloneEntryEntity(
                id = resolvedId,
                packageName = packageName,
                originalAppName = originalAppName,
                customName = customName.ifBlank { originalAppName },
                iconStyleKey = iconStyleKey,
                badgeLabel = badgeLabel.ifBlank { "SPACE" }.take(6).uppercase(),
                profileUserSerial = targetSerial,
                spaceName = workSpace?.label ?: "Work Space",
                groupTag = groupTag.ifBlank { "General" },
                notes = notes.trim(),
                requireAppLock = requireAppLock,
                separateNotifications = separateNotifications,
                pinned = pinned,
                createdAt = existingByPkg?.createdAt ?: System.currentTimeMillis(),
                lastLaunchedAt = existingByPkg?.lastLaunchedAt,
                launchCount = existingByPkg?.launchCount ?: 0
            )
            repository.saveClone(entry)

            if (attemptWorkSpaceActivation && workSpace != null) {
                when (val result = primaryEngine.enableOrPrepareCloneInSpace(packageName, workSpace.serialNumber)) {
                    is CloneOperationResult.Success -> _events.emit(SpaceUiEvent.Snackbar(result.message))
                    is CloneOperationResult.RequiresStoreOrInstallerInSpace -> {
                        _events.emit(
                            SpaceUiEvent.PromptStoreInstall(
                                packageName = packageName,
                                appName = customName.ifBlank { originalAppName },
                                message = result.message,
                                storeIntent = result.fallbackIntent
                            )
                        )
                    }
                    is CloneOperationResult.Unsupported -> _events.emit(SpaceUiEvent.Snackbar(result.reason))
                    is CloneOperationResult.Failure -> _events.emit(SpaceUiEvent.Snackbar(result.error))
                    is CloneOperationResult.LaunchIntentReady -> _events.emit(SpaceUiEvent.LaunchExternalIntent(result.intent))
                }
            } else if (workSpace == null) {
                _events.emit(
                    SpaceUiEvent.Snackbar("Saved to dashboard. Provision a Managed Work Space in Spaces to run isolated.")
                )
            } else {
                _events.emit(SpaceUiEvent.Snackbar("Clone configuration saved."))
            }
            refreshSpacesAndApps(forceScan = false)
        }
    }

    fun requestLaunchClone(clone: CloneEntryEntity, isPinProtectionActive: Boolean) {
        viewModelScope.launch {
            if (clone.requireAppLock && isPinProtectionActive) {
                _events.emit(SpaceUiEvent.RequestProtectedCloneLaunch(clone))
            } else {
                executeCloneLaunch(clone)
            }
        }
    }

    fun executeCloneLaunch(clone: CloneEntryEntity) {
        viewModelScope.launch {
            val activeWorkSerial = _spaces.value.firstOrNull { it.isManagedProfile }?.serialNumber
            val targetSerial = if (clone.profileUserSerial >= 0L) clone.profileUserSerial else (activeWorkSerial ?: -1L)

            when (val result = primaryEngine.launchInSpace(clone.packageName, targetSerial)) {
                is CloneOperationResult.Success -> {
                    repository.recordLaunch(clone.id)
                    _events.emit(SpaceUiEvent.Snackbar(result.message))
                }
                is CloneOperationResult.RequiresStoreOrInstallerInSpace -> {
                    val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${clone.packageName}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }.takeIf { it.resolveActivity(context.packageManager) != null }
                    _events.emit(
                        SpaceUiEvent.PromptStoreInstall(
                            packageName = clone.packageName,
                            appName = clone.customName,
                            message = result.message,
                            storeIntent = storeIntent
                        )
                    )
                }
                is CloneOperationResult.Unsupported -> _events.emit(SpaceUiEvent.Snackbar(result.reason))
                is CloneOperationResult.Failure -> _events.emit(SpaceUiEvent.Snackbar(result.error))
                is CloneOperationResult.LaunchIntentReady -> _events.emit(SpaceUiEvent.LaunchExternalIntent(result.intent))
            }
        }
    }

    fun launchByShortcutParams(cloneId: Long, packageName: String, spaceSerial: Long) {
        viewModelScope.launch {
            val clone = repository.getCloneById(cloneId)
            if (clone != null) {
                executeCloneLaunch(clone)
            } else {
                when (val result = primaryEngine.launchInSpace(packageName, spaceSerial)) {
                    is CloneOperationResult.Success -> _events.emit(SpaceUiEvent.Snackbar(result.message))
                    is CloneOperationResult.Failure -> _events.emit(SpaceUiEvent.Snackbar(result.error))
                    is CloneOperationResult.Unsupported -> _events.emit(SpaceUiEvent.Snackbar(result.reason))
                    else -> Unit
                }
            }
        }
    }

    fun removeCloneEntry(clone: CloneEntryEntity, alsoUninstallFromSpace: Boolean) {
        viewModelScope.launch {
            repository.deleteClone(clone)
            if (alsoUninstallFromSpace && clone.profileUserSerial >= 0L) {
                when (val result = primaryEngine.requestUninstallFromSpace(clone.packageName, clone.profileUserSerial)) {
                    is CloneOperationResult.LaunchIntentReady -> _events.emit(SpaceUiEvent.LaunchExternalIntent(result.intent))
                    is CloneOperationResult.Success -> _events.emit(SpaceUiEvent.Snackbar(result.message))
                    is CloneOperationResult.Failure -> _events.emit(SpaceUiEvent.Snackbar(result.error))
                    is CloneOperationResult.Unsupported -> _events.emit(SpaceUiEvent.Snackbar(result.reason))
                    else -> Unit
                }
            } else {
                _events.emit(SpaceUiEvent.Snackbar("Removed ${clone.customName} from dashboard."))
            }
        }
    }

    fun openAppDetailsInSpace(clone: CloneEntryEntity) {
        viewModelScope.launch {
            val serial = if (clone.profileUserSerial >= 0L) clone.profileUserSerial
            else _spaces.value.firstOrNull { it.isManagedProfile }?.serialNumber ?: -1L
            when (val result = primaryEngine.openSpaceAppDetails(clone.packageName, serial)) {
                is CloneOperationResult.Success -> _events.emit(SpaceUiEvent.Snackbar(result.message))
                is CloneOperationResult.Failure -> _events.emit(SpaceUiEvent.Snackbar(result.error))
                is CloneOperationResult.Unsupported -> _events.emit(SpaceUiEvent.Snackbar(result.reason))
                else -> Unit
            }
        }
    }

    fun pinHomeShortcut(clone: CloneEntryEntity) {
        viewModelScope.launch {
            val serial = if (clone.profileUserSerial >= 0L) clone.profileUserSerial
            else _spaces.value.firstOrNull { it.isManagedProfile }?.serialNumber ?: -1L
            val ok = discoveryRepository.pinCloneShortcut(
                cloneId = clone.id,
                packageName = clone.packageName,
                customName = clone.customName,
                spaceSerial = serial
            )
            _events.emit(
                SpaceUiEvent.Snackbar(
                    if (ok) "Home screen shortcut request sent for ${clone.customName}."
                    else "Launcher does not support pinned shortcuts on this device."
                )
            )
        }
    }

    fun toggleQuietMode(space: ManagedSpaceInfo) {
        viewModelScope.launch {
            when (val result = primaryEngine.setQuietMode(space.serialNumber, !space.isQuietModeEnabled)) {
                is CloneOperationResult.Success -> {
                    _events.emit(SpaceUiEvent.Snackbar(result.message))
                    refreshSpacesAndApps(forceScan = false)
                }
                is CloneOperationResult.Failure -> _events.emit(SpaceUiEvent.Snackbar(result.error))
                is CloneOperationResult.Unsupported -> _events.emit(SpaceUiEvent.Snackbar(result.reason))
                else -> Unit
            }
        }
    }

    fun openMonoInWorkSpace() {
        viewModelScope.launch {
            when (val result = primaryEngine.openMonoInWorkSpace()) {
                is CloneOperationResult.Success -> _events.emit(SpaceUiEvent.Snackbar(result.message))
                is CloneOperationResult.Failure -> _events.emit(SpaceUiEvent.Snackbar(result.error))
                is CloneOperationResult.Unsupported -> _events.emit(SpaceUiEvent.Snackbar(result.reason))
                else -> Unit
            }
        }
    }

    fun wipeWorkSpace() {
        viewModelScope.launch {
            when (val result = primaryEngine.wipeManagedProfile("WIPE_WORK_SPACE")) {
                is CloneOperationResult.Success -> _events.emit(SpaceUiEvent.Snackbar(result.message))
                is CloneOperationResult.Failure -> _events.emit(SpaceUiEvent.Snackbar(result.error))
                is CloneOperationResult.Unsupported -> _events.emit(SpaceUiEvent.Snackbar(result.reason))
                else -> Unit
            }
        }
    }

    fun runBackgroundAudit() {
        SpaceAuditWorker.enqueueOneTimeAudit(context)
        refreshSpacesAndApps(forceScan = true)
        viewModelScope.launch {
            _events.emit(SpaceUiEvent.Snackbar("Space & storage audit scheduled."))
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            val msg = when (val res = backupManager.exportToUri(uri)) {
                is BackupResult.ExportSuccess -> "Exported ${res.cloneCount} clone entries and ${res.spaceCount} spaces."
                is BackupResult.Error -> res.message
                else -> "Export finished."
            }
            _events.emit(SpaceUiEvent.Snackbar(msg))
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val msg = when (val res = backupManager.importFromUri(uri)) {
                is BackupResult.ImportSuccess -> "Restored ${res.importedClones} clone entries and ${res.importedSpaces} spaces."
                is BackupResult.Error -> res.message
                else -> "Import finished."
            }
            refreshSpacesAndApps(forceScan = false)
            _events.emit(SpaceUiEvent.Snackbar(msg))
        }
    }

    class Factory(
        private val context: Context,
        private val repository: CloneRepository,
        private val primaryEngine: CloningEngine,
        private val secondaryEngine: CloningEngine,
        private val discoveryRepository: AppDiscoveryRepository,
        private val backupManager: BackupManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SpaceViewModel(
                context = context.applicationContext,
                repository = repository,
                primaryEngine = primaryEngine,
                secondaryEngine = secondaryEngine,
                discoveryRepository = discoveryRepository,
                backupManager = backupManager
            ) as T
        }
    }
}
