package com.example

import com.example.data.backup.BackupCloneRecord
import com.example.data.backup.BackupSettingsRecord
import com.example.data.backup.BackupSpaceRecord
import com.example.data.backup.MonoBackupSnapshot
import com.example.data.preferences.AutoLockTimeout
import com.example.data.preferences.PinCrypto
import com.example.data.preferences.SecurityPreferencesState
import com.example.data.preferences.ThemeMode
import com.example.engine.CloneInstallStatus
import com.example.engine.CloneOperationResult
import com.example.engine.VirtualizationSandboxEngine
import com.example.ui.components.formatByteSize
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonoCoreUnitTest {

    @Test
    fun `pin crypto creates salted hash and verifies securely`() {
        val hashed1 = PinCrypto.createHash("2468")
        val hashed2 = PinCrypto.createHash("2468")

        assertNotEquals(hashed1.saltBase64, hashed2.saltBase64)
        assertNotEquals(hashed1.hashBase64, hashed2.hashBase64)
        assertTrue(PinCrypto.verifyPin("2468", hashed1.hashBase64, hashed1.saltBase64))
        assertFalse(PinCrypto.verifyPin("1357", hashed1.hashBase64, hashed1.saltBase64))
        assertFalse(PinCrypto.verifyPin("", hashed1.hashBase64, hashed1.saltBase64))
    }

    @Test
    fun `virtualization sandbox engine honestly reports unimplemented`() {
        val engine = VirtualizationSandboxEngine()
        assertFalse(engine.isImplemented)
        assertEquals(CloneInstallStatus.UNSUPPORTED, engine.getCloneInstallStatus("com.example.app", 0L))
        assertTrue(engine.launchInSpace("com.example.app", 0L) is CloneOperationResult.Unsupported)
        assertTrue(engine.wipeManagedProfile("WIPE_WORK_SPACE") is CloneOperationResult.Unsupported)
    }

    @Test
    fun `backup snapshot serializes and deserializes cleanly`() {
        val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
        val original = MonoBackupSnapshot(
            exportedAtEpochMs = 1_760_000_000_000L,
            spaces = listOf(
                BackupSpaceRecord(10L, "Managed Work Space", "MANAGED_WORK_PROFILE", true)
            ),
            clones = listOf(
                BackupCloneRecord(
                    packageName = "org.example.chat",
                    originalAppName = "Chat",
                    customName = "Work Chat",
                    iconStyleKey = "MONO_GREY",
                    badgeLabel = "WORK",
                    profileUserSerial = 10L,
                    spaceName = "Managed Work Space",
                    groupTag = "Messaging",
                    notes = "Secondary account",
                    requireAppLock = true,
                    separateNotifications = true,
                    pinned = true
                )
            ),
            settings = BackupSettingsRecord(
                themeMode = ThemeMode.DARK.name,
                compactDensity = false,
                defaultSortOrder = "NAME_ASC",
                hideInRecents = true
            )
        )

        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<MonoBackupSnapshot>(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `default preferences match mono ecosystem defaults`() {
        val defaults = SecurityPreferencesState()
        assertEquals(ThemeMode.DARK, defaults.themeMode)
        assertEquals(AutoLockTimeout.IMMEDIATE, defaults.autoLockTimeout)
        assertFalse(defaults.isPinEnabled)
        assertFalse(defaults.isBiometricEnabled)
        assertFalse(defaults.onboardingCompleted)
    }

    @Test
    fun `byte size formatter formats human readable units`() {
        assertEquals("—", formatByteSize(0L))
        assertEquals("512 KB", formatByteSize(512 * 1024L))
        assertEquals("2.5 MB", formatByteSize((2.5 * 1024 * 1024).toLong()))
    }
}
