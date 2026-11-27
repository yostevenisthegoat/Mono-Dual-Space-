package com.example.engine

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.CrossProfileApps
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.os.UserHandle
import android.os.UserManager

class ManagedProfileCloningEngine(private val context: Context) : CloningEngine {

    override val engineType: EngineType = EngineType.MANAGED_WORK_PROFILE
    override val displayName: String = "Android Managed Work Profile"
    override val isImplemented: Boolean = true

    private val dpm: DevicePolicyManager? =
        context.getSystemService(DevicePolicyManager::class.java)
    private val userManager: UserManager? =
        context.getSystemService(UserManager::class.java)
    private val launcherApps: LauncherApps? =
        context.getSystemService(LauncherApps::class.java)
    private val crossProfileApps: CrossProfileApps? =
        context.getSystemService(CrossProfileApps::class.java)
    private val packageManager: PackageManager = context.packageManager

    private val adminComponent: ComponentName
        get() = MonoDeviceAdminReceiver.getComponentName(context)

    override fun inspectCapabilities(): EngineCapabilities {
        val hasManagedUsers = packageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)
        val isProfileOwner = runCatching {
            dpm?.isProfileOwnerApp(context.packageName) ?: false
        }.getOrDefault(false)
        val provisioningAllowed = if (hasManagedUsers) {
            runCatching {
                dpm?.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE) ?: false
            }.getOrDefault(false)
        } else {
            false
        }

        val spaces = inspectSpaces()
        val hasExistingWorkSpace = spaces.any { it.isManagedProfile }
        val isInsideWorkProfile = isProfileOwner

        val summary = when {
            !hasManagedUsers -> "This device firmware does not advertise Android Managed Users support."
            isProfileOwner -> "Active Profile Owner inside the Managed Work Space."
            hasExistingWorkSpace -> "Managed Work Space detected on device. Ready to inspect and launch isolated apps."
            provisioningAllowed -> "Device supports Android Managed Work Profile provisioning."
            else -> "Managed profile provisioning is currently restricted (a work profile may already exist or OEM policy limits creation)."
        }

        val limitations = buildList {
            add("Android enforces a single active Managed Work Profile per personal user on standard consumer builds.")
            add("Applications are never duplicated as modified APKs; each space runs inside an OS-enforced Linux user sandbox.")
            if (!isProfileOwner && hasExistingWorkSpace) {
                add("System app toggling and profile wipe require opening Mono Dual-Space inside the Work Space (Profile Owner context).")
            }
            if (!hasManagedUsers) {
                add("android.software.managed_users feature is unavailable on this device or emulator build.")
            }
        }

        return EngineCapabilities(
            engineType = engineType,
            isImplemented = true,
            supportsManagedUsersFeature = hasManagedUsers,
            isProvisioningAllowed = provisioningAllowed,
            isRunningInsideWorkProfile = isInsideWorkProfile,
            isProfileOwner = isProfileOwner,
            supportsMultipleSpaces = false,
            statusSummary = summary,
            limitationNotes = limitations
        )
    }

    override fun inspectSpaces(): List<ManagedSpaceInfo> {
        val um = userManager ?: return emptyList()
        val la = launcherApps
        val myHandle = Process.myUserHandle()
        val profiles = la?.profiles ?: um.userProfiles ?: listOf(myHandle)
        val crossTargetProfiles = runCatching {
            crossProfileApps?.targetUserProfiles ?: emptyList()
        }.getOrDefault(emptyList())
        val isProfileOwnerHere = runCatching {
            dpm?.isProfileOwnerApp(context.packageName) ?: false
        }.getOrDefault(false)

        return profiles.distinct().map { handle ->
            val serial = um.getSerialNumberForUser(handle)
            val isCurrent = handle == myHandle
            val isManaged = (!isCurrent && crossTargetProfiles.contains(handle)) ||
                (!isCurrent && !isProfileOwnerHere) ||
                (isCurrent && isProfileOwnerHere)
            val isQuiet = runCatching { um.isQuietModeEnabled(handle) }.getOrDefault(false)
            val isUnlocked = runCatching { um.isUserUnlocked(handle) }.getOrDefault(true)
            val appCount = runCatching {
                la?.getActivityList(null, handle)?.map { it.applicationInfo.packageName }?.distinct()?.size ?: 0
            }.getOrDefault(0)

            val label = when {
                isCurrent && isProfileOwnerHere -> "Mono Work Space (Active DPC)"
                isManaged -> "Managed Work Space"
                else -> "Personal Space"
            }

            ManagedSpaceInfo(
                userHandle = handle,
                serialNumber = serial,
                label = label,
                isPersonalProfile = !isManaged,
                isManagedProfile = isManaged,
                isQuietModeEnabled = isQuiet,
                isUserUnlocked = isUnlocked,
                isProfileOwnerInSpace = isCurrent && isProfileOwnerHere,
                installedAppCount = appCount,
                engineType = engineType
            )
        }
    }

    private fun resolveUserHandle(spaceSerial: Long): UserHandle? {
        val um = userManager ?: return null
        val la = launcherApps
        val profiles = la?.profiles ?: um.userProfiles ?: emptyList()
        return profiles.firstOrNull { um.getSerialNumberForUser(it) == spaceSerial }
    }

    override fun getCloneInstallStatus(packageName: String, spaceSerial: Long?): CloneInstallStatus {
        val spaces = inspectSpaces()
        val targetSpace = if (spaceSerial != null && spaceSerial >= 0L) {
            spaces.firstOrNull { it.serialNumber == spaceSerial }
        } else {
            spaces.firstOrNull { it.isManagedProfile }
        }

        if (targetSpace == null || targetSpace.userHandle == null) {
            return CloneInstallStatus.NO_SPACE_PROVISIONED
        }

        val activities = runCatching {
            launcherApps?.getActivityList(packageName, targetSpace.userHandle) ?: emptyList()
        }.getOrDefault(emptyList())

        if (activities.isNotEmpty()) {
            return CloneInstallStatus.ACTIVE_IN_SPACE
        }

        if (targetSpace.isProfileOwnerInSpace) {
            val hidden = runCatching {
                dpm?.isApplicationHidden(adminComponent, packageName) ?: false
            }.getOrDefault(false)
            if (hidden) return CloneInstallStatus.HIDDEN_IN_SPACE
        }

        return CloneInstallStatus.PERSONAL_ONLY
    }

    override fun buildProvisioningIntent(): Intent? {
        val hasManagedUsers = packageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)
        val allowed = runCatching {
            dpm?.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE) ?: false
        }.getOrDefault(false)
        if (!hasManagedUsers || !allowed) return null

        return Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, adminComponent)
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, false)
        }
    }

    override fun launchInSpace(packageName: String, spaceSerial: Long): CloneOperationResult {
        val la = launcherApps ?: return CloneOperationResult.Failure("LauncherApps system service unavailable.")
        val targetHandle = resolveUserHandle(spaceSerial)
            ?: inspectSpaces().firstOrNull { it.isManagedProfile }?.userHandle
            ?: return CloneOperationResult.Unsupported("No active Managed Work Space found. Provision a space first.")

        if (userManager?.isQuietModeEnabled(targetHandle) == true) {
            return CloneOperationResult.Failure("Work Space is currently paused (Quiet Mode). Resume the space in Spaces first.")
        }

        val activities = runCatching {
            la.getActivityList(packageName, targetHandle)
        }.getOrElse { error ->
            return CloneOperationResult.Failure("Unable to inspect space activities: ${error.message}")
        }

        val entryActivity = activities.firstOrNull()
            ?: return CloneOperationResult.RequiresStoreOrInstallerInSpace(
                packageName = packageName,
                message = "This app is not yet installed inside the Managed Work Space."
            )

        return try {
            la.startMainActivity(entryActivity.componentName, targetHandle, null, null)
            CloneOperationResult.Success("Launched ${entryActivity.label} in Work Space")
        } catch (e: SecurityException) {
            CloneOperationResult.Failure("Launch blocked by profile security policy: ${e.message}")
        } catch (e: Exception) {
            CloneOperationResult.Failure("Failed to launch app in Work Space: ${e.message}")
        }
    }

    override fun enableOrPrepareCloneInSpace(packageName: String, spaceSerial: Long): CloneOperationResult {
        val targetHandle = resolveUserHandle(spaceSerial)
            ?: inspectSpaces().firstOrNull { it.isManagedProfile }?.userHandle
            ?: return CloneOperationResult.Unsupported("No Managed Work Space provisioned on this device.")

        val alreadyInSpace = runCatching {
            launcherApps?.getActivityList(packageName, targetHandle)?.isNotEmpty() ?: false
        }.getOrDefault(false)

        if (alreadyInSpace) {
            return CloneOperationResult.Success("Application is already active in the Managed Work Space.")
        }

        val isProfileOwner = runCatching { dpm?.isProfileOwnerApp(context.packageName) ?: false }.getOrDefault(false)
        if (isProfileOwner) {
            val hiddenCleared = runCatching {
                dpm?.setApplicationHidden(adminComponent, packageName, false) ?: false
            }.getOrDefault(false)
            if (hiddenCleared) {
                val nowPresent = runCatching {
                    launcherApps?.getActivityList(packageName, targetHandle)?.isNotEmpty() ?: false
                }.getOrDefault(false)
                if (nowPresent) {
                    return CloneOperationResult.Success("Unhid application inside Managed Work Space.")
                }
            }

            val enabledSystem = runCatching {
                dpm?.enableSystemApp(adminComponent, packageName)
                true
            }.getOrDefault(false)

            if (enabledSystem) {
                val nowPresent = runCatching {
                    launcherApps?.getActivityList(packageName, targetHandle)?.isNotEmpty() ?: false
                }.getOrDefault(false)
                if (nowPresent) {
                    return CloneOperationResult.Success("Enabled system application inside Managed Work Space.")
                }
            }
        }

        val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val canResolveStore = storeIntent.resolveActivity(packageManager) != null

        return CloneOperationResult.RequiresStoreOrInstallerInSpace(
            packageName = packageName,
            message = if (isProfileOwner) {
                "User-installed applications must be installed inside the Work Space via an app store or installer (Android does not allow silent cross-profile APK copying)."
            } else {
                "Registered in Mono dashboard. To install third-party apps into the isolated Work Space, install via your Work Profile store or open Mono inside the Work Space."
            },
            fallbackIntent = if (canResolveStore) storeIntent else null
        )
    }

    override fun requestUninstallFromSpace(packageName: String, spaceSerial: Long): CloneOperationResult {
        val targetHandle = resolveUserHandle(spaceSerial)
            ?: inspectSpaces().firstOrNull { it.isManagedProfile }?.userHandle
            ?: return CloneOperationResult.Unsupported("No Managed Work Space found.")

        val isProfileOwner = runCatching { dpm?.isProfileOwnerApp(context.packageName) ?: false }.getOrDefault(false)
        if (isProfileOwner && targetHandle == Process.myUserHandle()) {
            val deleteIntent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return CloneOperationResult.LaunchIntentReady(
                intent = deleteIntent,
                message = "Confirm removal in Android's system uninstall dialog."
            )
        }

        return openSpaceAppDetails(packageName, spaceSerial)
    }

    override fun openSpaceAppDetails(packageName: String, spaceSerial: Long): CloneOperationResult {
        val la = launcherApps ?: return CloneOperationResult.Failure("LauncherApps unavailable.")
        val targetHandle = resolveUserHandle(spaceSerial)
            ?: inspectSpaces().firstOrNull { it.isManagedProfile }?.userHandle
            ?: return CloneOperationResult.Unsupported("No Managed Work Space found.")

        val activityInfo = runCatching {
            la.getActivityList(packageName, targetHandle).firstOrNull()
        }.getOrNull() ?: return CloneOperationResult.Failure("App is not installed in the selected space.")

        return try {
            la.startAppDetailsActivity(activityInfo.componentName, targetHandle, null, null)
            CloneOperationResult.Success("Opened system App Info for Work Space instance.")
        } catch (e: Exception) {
            CloneOperationResult.Failure("Unable to open App Info: ${e.message}")
        }
    }

    override fun setQuietMode(spaceSerial: Long, enableQuietMode: Boolean): CloneOperationResult {
        val um = userManager ?: return CloneOperationResult.Failure("UserManager unavailable.")
        val targetHandle = resolveUserHandle(spaceSerial)
            ?: return CloneOperationResult.Unsupported("Target space handle not found.")
        return try {
            val result = um.requestQuietModeEnabled(enableQuietMode, targetHandle)
            if (result) {
                CloneOperationResult.Success(
                    if (enableQuietMode) "Work Space paused (Quiet Mode enabled)." else "Work Space resumed."
                )
            } else {
                CloneOperationResult.Failure(
                    "System launcher or Settings controls Quiet Mode on this Android version. Toggle Work Profile from Android Quick Settings."
                )
            }
        } catch (e: SecurityException) {
            CloneOperationResult.Failure(
                "Quiet Mode toggle requires default launcher or system permission on this device. Use Android Quick Settings > Work."
            )
        } catch (e: Exception) {
            CloneOperationResult.Failure("Could not update Quiet Mode: ${e.message}")
        }
    }

    override fun openMonoInWorkSpace(): CloneOperationResult {
        val cpa = crossProfileApps
        val la = launcherApps
        val targetProfiles = runCatching { cpa?.targetUserProfiles ?: emptyList() }.getOrDefault(emptyList())
        val workHandle = targetProfiles.firstOrNull()
            ?: inspectSpaces().firstOrNull { it.isManagedProfile }?.userHandle
            ?: return CloneOperationResult.Unsupported("No target Work Space found.")

        val mainComponent = ComponentName(context, "com.example.MainActivity")
        return try {
            if (cpa != null && targetProfiles.contains(workHandle)) {
                cpa.startMainActivity(mainComponent, workHandle)
                CloneOperationResult.Success("Switched to Mono Dual-Space inside Work Space.")
            } else if (la != null) {
                la.startMainActivity(mainComponent, workHandle, null, null)
                CloneOperationResult.Success("Opened Mono Dual-Space inside Work Space.")
            } else {
                CloneOperationResult.Failure("Cross-profile launch unavailable.")
            }
        } catch (e: Exception) {
            CloneOperationResult.Failure("Unable to launch Mono Dual-Space in Work Space: ${e.message}")
        }
    }

    override fun wipeManagedProfile(confirmationToken: String): CloneOperationResult {
        if (confirmationToken != "WIPE_WORK_SPACE") {
            return CloneOperationResult.Failure("Invalid wipe confirmation token.")
        }
        val isProfileOwner = runCatching { dpm?.isProfileOwnerApp(context.packageName) ?: false }.getOrDefault(false)
        if (!isProfileOwner) {
            return CloneOperationResult.Unsupported(
                "To remove the Work Profile, open Mono Dual-Space inside the Work Space (Profile Owner) or use Android Settings > Passwords & accounts > Remove work profile."
            )
        }
        return try {
            dpm?.wipeData(0)
            CloneOperationResult.Success("Managed Work Space removal initiated.")
        } catch (e: SecurityException) {
            CloneOperationResult.Failure("Work profile wipe denied by policy: ${e.message}")
        }
    }
}
