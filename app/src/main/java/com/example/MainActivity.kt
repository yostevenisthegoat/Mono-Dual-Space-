package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.MonoAppRoot
import com.example.ui.theme.MonoDualSpaceTheme
import com.example.ui.viewmodel.SecurityViewModel
import com.example.ui.viewmodel.SpaceViewModel

class MainActivity : FragmentActivity() {

    private val appContainer: AppContainer
        get() = (application as MonoDualSpaceApp).container

    private val securityViewModel: SecurityViewModel by viewModels {
        SecurityViewModel.Factory(
            context = this,
            preferencesRepository = appContainer.preferencesRepository,
            appLockManager = appContainer.appLockManager
        )
    }

    private val spaceViewModel: SpaceViewModel by viewModels {
        SpaceViewModel.Factory(
            context = this,
            repository = appContainer.cloneRepository,
            primaryEngine = appContainer.primaryEngine,
            secondaryEngine = appContainer.secondaryEngine,
            discoveryRepository = appContainer.appDiscoveryRepository,
            backupManager = appContainer.backupManager
        )
    }

    private val provisioningLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        spaceViewModel.onProvisioningCompleted(result.resultCode == Activity.RESULT_OK)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleShortcutIntent(intent)

        setContent {
            val prefs by securityViewModel.preferences.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(this)

            LaunchedEffect(prefs.hideInRecents) {
                if (prefs.hideInRecents) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            MonoDualSpaceTheme(themeMode = prefs.themeMode) {
                MonoAppRoot(
                    activity = this,
                    widthSizeClass = windowSizeClass.widthSizeClass,
                    securityViewModel = securityViewModel,
                    spaceViewModel = spaceViewModel,
                    onLaunchProvisioning = { provisionIntent ->
                        provisioningLauncher.launch(provisionIntent)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        securityViewModel.refreshBiometricStatus()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?) {
        if (intent?.action == ACTION_LAUNCH_CLONE) {
            val cloneId = intent.getLongExtra(EXTRA_CLONE_ID, -1L)
            val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return
            val spaceSerial = intent.getLongExtra(EXTRA_SPACE_SERIAL, -1L)
            spaceViewModel.launchByShortcutParams(cloneId, packageName, spaceSerial)
        }
    }

    companion object {
        const val ACTION_LAUNCH_CLONE = "com.example.action.LAUNCH_CLONE"
        const val EXTRA_CLONE_ID = "extra_clone_id"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_SPACE_SERIAL = "extra_space_serial"
    }
}
