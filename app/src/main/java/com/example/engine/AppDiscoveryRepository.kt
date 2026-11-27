package com.example.engine

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.storage.StorageManager
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.CachedAppEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class DiscoveredApp(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val versionName: String,
    val apkSizeBytes: Long,
    val storageTotalBytes: Long?,
    val installStatus: CloneInstallStatus
)

class AppDiscoveryRepository(
    private val context: Context,
    private val engine: CloningEngine
) {
    private val packageManager: PackageManager = context.packageManager
    private val iconCache = object : LruCache<String, ImageBitmap>(120) {}

    suspend fun discoverInstalledApps(targetSpaceSerial: Long?): List<DiscoveredApp> =
        withContext(Dispatchers.IO) {
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolvedActivities = packageManager.queryIntentActivities(launcherIntent, 0)
            val launcherPackages = resolvedActivities
                .mapNotNull { it.activityInfo?.applicationInfo }
                .distinctBy { it.packageName }

            val installedPackages = runCatching {
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            }.getOrDefault(emptyList())

            val mergedByPkg = LinkedHashMap<String, ApplicationInfo>()
            launcherPackages.forEach { mergedByPkg[it.packageName] = it }
            installedPackages.forEach { info ->
                if (!mergedByPkg.containsKey(info.packageName)) {
                    mergedByPkg[info.packageName] = info
                }
            }

            // Exclude Mono Dual-Space itself from clone candidate list
            mergedByPkg.remove(context.packageName)

            val storageStatsManager = context.getSystemService(StorageStatsManager::class.java)
            val myHandle = Process.myUserHandle()

            mergedByPkg.values.map { appInfo ->
                val label = runCatching {
                    packageManager.getApplicationLabel(appInfo).toString()
                }.getOrDefault(appInfo.packageName)

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                val pkgInfo = runCatching {
                    packageManager.getPackageInfo(appInfo.packageName, 0)
                }.getOrNull()

                val versionName = pkgInfo?.versionName ?: "—"
                val apkSize = runCatching {
                    val source = appInfo.sourceDir
                    if (source != null) File(source).length() else 0L
                }.getOrDefault(0L)

                val totalBytes = runCatching {
                    val stats = storageStatsManager?.queryStatsForPackage(
                        StorageManager.UUID_DEFAULT,
                        appInfo.packageName,
                        myHandle
                    )
                    stats?.let { it.appBytes + it.dataBytes + it.cacheBytes }
                }.getOrNull()

                val status = engine.getCloneInstallStatus(appInfo.packageName, targetSpaceSerial)

                DiscoveredApp(
                    packageName = appInfo.packageName,
                    appName = label,
                    isSystemApp = isSystem,
                    versionName = versionName,
                    apkSizeBytes = apkSize,
                    storageTotalBytes = totalBytes,
                    installStatus = status
                )
            }.sortedBy { it.appName.lowercase() }
        }

    fun toCachedEntities(apps: List<DiscoveredApp>): List<CachedAppEntity> {
        val now = System.currentTimeMillis()
        return apps.map {
            CachedAppEntity(
                packageName = it.packageName,
                appName = it.appName,
                isSystemApp = it.isSystemApp,
                versionName = it.versionName,
                apkSizeBytes = it.apkSizeBytes,
                lastScannedAt = now
            )
        }
    }

    suspend fun loadAppIcon(packageName: String): ImageBitmap? = withContext(Dispatchers.IO) {
        iconCache.get(packageName)?.let { return@withContext it }
        val drawable = runCatching { packageManager.getApplicationIcon(packageName) }.getOrNull()
            ?: return@withContext null
        val bitmap = drawableToBitmap(drawable, 128)
        val imageBitmap = bitmap.asImageBitmap()
        iconCache.put(packageName, imageBitmap)
        imageBitmap
    }

    fun pinCloneShortcut(
        cloneId: Long,
        packageName: String,
        customName: String,
        spaceSerial: Long
    ): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }
        val iconCompat = runCatching {
            val drawable = packageManager.getApplicationIcon(packageName)
            IconCompat.createWithBitmap(drawableToBitmap(drawable, 192))
        }.getOrElse {
            IconCompat.createWithResource(context, R.mipmap.ic_launcher)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_LAUNCH_CLONE
            putExtra(MainActivity.EXTRA_CLONE_ID, cloneId)
            putExtra(MainActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(MainActivity.EXTRA_SPACE_SERIAL, spaceSerial)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val shortcut = ShortcutInfoCompat.Builder(context, "mono_clone_${cloneId}_$packageName")
            .setShortLabel(customName.take(25))
            .setLongLabel("$customName (Mono Space)")
            .setIcon(iconCompat)
            .setIntent(launchIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    private fun drawableToBitmap(drawable: Drawable, targetPx: Int): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return Bitmap.createScaledBitmap(drawable.bitmap, targetPx, targetPx, true)
        }
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: targetPx
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: targetPx
        val bitmap = Bitmap.createBitmap(targetPx, targetPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width.coerceAtLeast(1), canvas.height.coerceAtLeast(1))
        if (width != targetPx || height != targetPx) {
            drawable.setBounds(0, 0, targetPx, targetPx)
        }
        drawable.draw(canvas)
        return bitmap
    }
}
