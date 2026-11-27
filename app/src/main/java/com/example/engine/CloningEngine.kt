package com.example.engine

import android.content.Intent
import android.os.UserHandle

enum class EngineType(val displayName: String) {
    MANAGED_WORK_PROFILE("Android Managed Work Profile"),
    VIRTUALIZATION_SANDBOX("Virtualization Sandbox (Future Interface)")
}

data class EngineCapabilities(
    val engineType: EngineType,
    val isImplemented: Boolean,
    val supportsManagedUsersFeature: Boolean,
    val isProvisioningAllowed: Boolean,
    val isRunningInsideWorkProfile: Boolean,
    val isProfileOwner: Boolean,
    val supportsMultipleSpaces: Boolean,
    val statusSummary: String,
    val limitationNotes: List<String>
)

data class ManagedSpaceInfo(
    val userHandle: UserHandle?,
    val serialNumber: Long,
    val label: String,
    val isPersonalProfile: Boolean,
    val isManagedProfile: Boolean,
    val isQuietModeEnabled: Boolean,
    val isUserUnlocked: Boolean,
    val isProfileOwnerInSpace: Boolean,
    val installedAppCount: Int,
    val engineType: EngineType = EngineType.MANAGED_WORK_PROFILE
)

enum class CloneInstallStatus(val label: String) {
    ACTIVE_IN_SPACE("Active in Space"),
    PERSONAL_ONLY("Personal Only"),
    NO_SPACE_PROVISIONED("No Space Configured"),
    HIDDEN_IN_SPACE("Hidden in Space"),
    UNSUPPORTED("Unsupported")
}

sealed interface CloneOperationResult {
    data class Success(val message: String) : CloneOperationResult
    data class LaunchIntentReady(val intent: Intent, val message: String) : CloneOperationResult
    data class RequiresStoreOrInstallerInSpace(
        val packageName: String,
        val message: String,
        val fallbackIntent: Intent? = null
    ) : CloneOperationResult
    data class Unsupported(val reason: String) : CloneOperationResult
    data class Failure(val error: String) : CloneOperationResult
}

interface CloningEngine {
    val engineType: EngineType
    val displayName: String
    val isImplemented: Boolean

    fun inspectCapabilities(): EngineCapabilities
    fun inspectSpaces(): List<ManagedSpaceInfo>
    fun getCloneInstallStatus(packageName: String, spaceSerial: Long?): CloneInstallStatus
    fun buildProvisioningIntent(): Intent?
    fun launchInSpace(packageName: String, spaceSerial: Long): CloneOperationResult
    fun enableOrPrepareCloneInSpace(packageName: String, spaceSerial: Long): CloneOperationResult
    fun requestUninstallFromSpace(packageName: String, spaceSerial: Long): CloneOperationResult
    fun openSpaceAppDetails(packageName: String, spaceSerial: Long): CloneOperationResult
    fun setQuietMode(spaceSerial: Long, enableQuietMode: Boolean): CloneOperationResult
    fun openMonoInWorkSpace(): CloneOperationResult
    fun wipeManagedProfile(confirmationToken: String): CloneOperationResult
}
