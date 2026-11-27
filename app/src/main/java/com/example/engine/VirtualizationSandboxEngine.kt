package com.example.engine

import android.content.Intent

/**
 * Architectural extension interface for a future userspace virtualization sandbox.
 * Intentionally unimplemented in v1.0.0: Mono Dual-Space never fakes cloning or bypasses
 * Android's signature verification, sandboxing, or anti-cheat protections.
 */
class VirtualizationSandboxEngine : CloningEngine {
    override val engineType: EngineType = EngineType.VIRTUALIZATION_SANDBOX
    override val displayName: String = "Virtualization Sandbox (Planned Interface)"
    override val isImplemented: Boolean = false

    private val notImplementedReason =
        "Virtualization sandbox is an extensible architecture interface reserved for future audited releases. " +
            "Use Android Managed Work Profile for authentic OS-isolated app spaces."

    override fun inspectCapabilities(): EngineCapabilities = EngineCapabilities(
        engineType = engineType,
        isImplemented = false,
        supportsManagedUsersFeature = false,
        isProvisioningAllowed = false,
        isRunningInsideWorkProfile = false,
        isProfileOwner = false,
        supportsMultipleSpaces = false,
        statusSummary = "Not yet implemented — architectural interface only.",
        limitationNotes = listOf(
            "No virtual APK container or signature-bypass hook is shipped in this release.",
            "Future implementations must preserve Android sandboxing and app integrity."
        )
    )

    override fun inspectSpaces(): List<ManagedSpaceInfo> = emptyList()

    override fun getCloneInstallStatus(packageName: String, spaceSerial: Long?): CloneInstallStatus =
        CloneInstallStatus.UNSUPPORTED

    override fun buildProvisioningIntent(): Intent? = null

    override fun launchInSpace(packageName: String, spaceSerial: Long): CloneOperationResult =
        CloneOperationResult.Unsupported(notImplementedReason)

    override fun enableOrPrepareCloneInSpace(packageName: String, spaceSerial: Long): CloneOperationResult =
        CloneOperationResult.Unsupported(notImplementedReason)

    override fun requestUninstallFromSpace(packageName: String, spaceSerial: Long): CloneOperationResult =
        CloneOperationResult.Unsupported(notImplementedReason)

    override fun openSpaceAppDetails(packageName: String, spaceSerial: Long): CloneOperationResult =
        CloneOperationResult.Unsupported(notImplementedReason)

    override fun setQuietMode(spaceSerial: Long, enableQuietMode: Boolean): CloneOperationResult =
        CloneOperationResult.Unsupported(notImplementedReason)

    override fun openMonoInWorkSpace(): CloneOperationResult =
        CloneOperationResult.Unsupported(notImplementedReason)

    override fun wipeManagedProfile(confirmationToken: String): CloneOperationResult =
        CloneOperationResult.Unsupported(notImplementedReason)
}
