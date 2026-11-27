package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.CloneRepository
import com.example.data.local.MonoDatabase
import com.example.engine.AppDiscoveryRepository
import com.example.engine.ManagedProfileCloningEngine

class SpaceAuditWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = MonoDatabase.getInstance(applicationContext)
            val repo = CloneRepository(db.cloneDao(), db.spaceDao(), db.cachedAppDao())
            val engine = ManagedProfileCloningEngine(applicationContext)
            val discovery = AppDiscoveryRepository(applicationContext, engine)

            val spaces = engine.inspectSpaces()
            val workSpaceSerial = spaces.firstOrNull { it.isManagedProfile }?.serialNumber
            val apps = discovery.discoverInstalledApps(workSpaceSerial)
            repo.upsertCachedApps(discovery.toCachedEntities(apps))

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "mono_space_audit_once"

        fun enqueueOneTimeAudit(context: Context) {
            val request = OneTimeWorkRequestBuilder<SpaceAuditWorker>().build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
