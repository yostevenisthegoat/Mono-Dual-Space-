package com.example

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.data.backup.BackupManager
import com.example.data.local.CloneRepository
import com.example.data.local.MonoDatabase
import com.example.data.preferences.PreferencesRepository
import com.example.engine.AppDiscoveryRepository
import com.example.engine.CloningEngine
import com.example.engine.ManagedProfileCloningEngine
import com.example.engine.VirtualizationSandboxEngine
import com.example.security.AppLockManager

class AppContainer(application: Application) {
    val database: MonoDatabase = MonoDatabase.getInstance(application)
    val cloneRepository: CloneRepository = CloneRepository(
        cloneDao = database.cloneDao(),
        spaceDao = database.spaceDao(),
        cachedAppDao = database.cachedAppDao()
    )
    val preferencesRepository: PreferencesRepository = PreferencesRepository(application)
    val primaryEngine: CloningEngine = ManagedProfileCloningEngine(application)
    val secondaryEngine: CloningEngine = VirtualizationSandboxEngine()
    val appDiscoveryRepository: AppDiscoveryRepository = AppDiscoveryRepository(application, primaryEngine)
    val backupManager: BackupManager = BackupManager(application, cloneRepository, preferencesRepository)
    val appLockManager: AppLockManager = AppLockManager(preferencesRepository)
}

class MonoDualSpaceApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(container.appLockManager)
    }
}
