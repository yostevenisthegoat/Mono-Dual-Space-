package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CloneEntryEntity
import com.example.data.local.CloneRepository
import com.example.data.local.MonoDatabase
import com.example.data.preferences.PinCrypto
import com.example.engine.CloneInstallStatus
import com.example.engine.CloneOperationResult
import com.example.engine.ManagedProfileCloningEngine
import com.example.engine.VirtualizationSandboxEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MonoDualSpaceTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `pin crypto creates salted hash and verifies securely`() {
        val hashed1 = PinCrypto.createHash("2468")
        val hashed2 = PinCrypto.createHash("2468")

        assertNotEquals(hashed1.saltBase64, hashed2.saltBase64)
        assertNotEquals(hashed1.hashBase64, hashed2.hashBase64)
        assertTrue(PinCrypto.verifyPin("2468", hashed1.hashBase64, hashed1.saltBase64))
        assertFalse(PinCrypto.verifyPin("1357", hashed1.hashBase64, hashed1.saltBase64))
    }

    @Test
    fun `virtualization sandbox engine honestly reports unimplemented`() {
        val engine = VirtualizationSandboxEngine()
        assertFalse(engine.isImplemented)
        assertEquals(CloneInstallStatus.UNSUPPORTED, engine.getCloneInstallStatus("com.example.app", 0L))
        val launchRes = engine.launchInSpace("com.example.app", 0L)
        assertTrue(launchRes is CloneOperationResult.Unsupported)
    }

    @Test
    fun `managed profile engine inspects personal space cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = ManagedProfileCloningEngine(context)
        assertTrue(engine.isImplemented)
        val spaces = engine.inspectSpaces()
        assertTrue(spaces.isNotEmpty())
        assertTrue(spaces.any { it.isPersonalProfile })
    }

    @Test
    fun `room clone repository persists and updates clone entries`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, MonoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = CloneRepository(db.cloneDao(), db.spaceDao(), db.cachedAppDao())
            val id = repo.saveClone(
                CloneEntryEntity(
                    packageName = "org.example.chat",
                    originalAppName = "Chat",
                    customName = "Work Chat",
                    badgeLabel = "WORK",
                    profileUserSerial = 10L
                )
            )
            assertTrue(id > 0L)
            val all = repo.allClones.first()
            assertEquals(1, all.size)
            assertEquals("Work Chat", all.first().customName)

            repo.recordLaunch(id)
            val updated = repo.getCloneById(id)
            assertEquals(1, updated?.launchCount)

            repo.deleteCloneById(id)
            assertTrue(repo.allClones.first().isEmpty())
        } finally {
            db.close()
        }
    }

    @Test
    fun `first launch shows onboarding and transitions to dashboard`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("One device.\nMultiple spaces.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_skip_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dashboard_clone_count").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_spaces").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Primary Engine").assertIsDisplayed()
    }
}
