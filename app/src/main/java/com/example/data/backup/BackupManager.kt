package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.CloneEntryEntity
import com.example.data.local.CloneRepository
import com.example.data.local.SpaceProfileEntity
import com.example.data.preferences.AppSortOrder
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface BackupResult {
    data class ExportSuccess(val cloneCount: Int, val spaceCount: Int) : BackupResult
    data class ImportSuccess(val importedClones: Int, val importedSpaces: Int) : BackupResult
    data class Error(val message: String) : BackupResult
}

class BackupManager(
    private val context: Context,
    private val repository: CloneRepository,
    private val preferencesRepository: PreferencesRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportToUri(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val clones = repository.getAllClonesSnapshot()
            val spaces = repository.getSpacesSnapshot()
            val prefs = preferencesRepository.preferences.first()

            val snapshot = MonoBackupSnapshot(
                exportedAtEpochMs = System.currentTimeMillis(),
                spaces = spaces.map {
                    BackupSpaceRecord(
                        userSerialNumber = it.userSerialNumber,
                        label = it.label,
                        engineType = it.engineType,
                        isProvisionedByMono = it.isProvisionedByMono
                    )
                },
                clones = clones.map {
                    BackupCloneRecord(
                        packageName = it.packageName,
                        originalAppName = it.originalAppName,
                        customName = it.customName,
                        iconStyleKey = it.iconStyleKey,
                        badgeLabel = it.badgeLabel,
                        profileUserSerial = it.profileUserSerial,
                        spaceName = it.spaceName,
                        groupTag = it.groupTag,
                        notes = it.notes,
                        requireAppLock = it.requireAppLock,
                        separateNotifications = it.separateNotifications,
                        pinned = it.pinned
                    )
                },
                settings = BackupSettingsRecord(
                    themeMode = prefs.themeMode.name,
                    compactDensity = prefs.compactDensity,
                    defaultSortOrder = prefs.defaultSortOrder.name,
                    hideInRecents = prefs.hideInRecents
                )
            )

            val payload = json.encodeToString(snapshot)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(payload.toByteArray(Charsets.UTF_8))
                stream.flush()
            } ?: return@withContext BackupResult.Error("Unable to open destination document for writing.")

            BackupResult.ExportSuccess(
                cloneCount = clones.size,
                spaceCount = spaces.size
            )
        } catch (e: Exception) {
            BackupResult.Error("Export failed: ${e.message ?: "Unknown I/O error"}")
        }
    }

    suspend fun importFromUri(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext BackupResult.Error("Unable to read selected backup file.")

            val snapshot = json.decodeFromString<MonoBackupSnapshot>(raw)
            if (snapshot.schemaVersion > 1) {
                return@withContext BackupResult.Error("Backup schema v${snapshot.schemaVersion} requires a newer version of Mono Dual-Space.")
            }

            var importedSpaces = 0
            snapshot.spaces.forEach { space ->
                repository.upsertSpace(
                    SpaceProfileEntity(
                        userSerialNumber = space.userSerialNumber,
                        label = space.label,
                        engineType = space.engineType,
                        isProvisionedByMono = space.isProvisionedByMono
                    )
                )
                importedSpaces++
            }

            var importedClones = 0
            snapshot.clones.forEach { record ->
                val existing = repository.getCloneByPackageAndProfile(
                    record.packageName,
                    record.profileUserSerial
                )
                val entity = CloneEntryEntity(
                    id = existing?.id ?: 0L,
                    packageName = record.packageName,
                    originalAppName = record.originalAppName,
                    customName = record.customName,
                    iconStyleKey = record.iconStyleKey,
                    badgeLabel = record.badgeLabel.take(6),
                    profileUserSerial = record.profileUserSerial,
                    spaceName = record.spaceName,
                    groupTag = record.groupTag,
                    notes = record.notes,
                    requireAppLock = record.requireAppLock,
                    separateNotifications = record.separateNotifications,
                    pinned = record.pinned,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    lastLaunchedAt = existing?.lastLaunchedAt,
                    launchCount = existing?.launchCount ?: 0
                )
                repository.saveClone(entity)
                importedClones++
            }

            runCatching { ThemeMode.valueOf(snapshot.settings.themeMode) }.getOrNull()?.let {
                preferencesRepository.setThemeMode(it)
            }
            preferencesRepository.setCompactDensity(snapshot.settings.compactDensity)
            runCatching { AppSortOrder.valueOf(snapshot.settings.defaultSortOrder) }.getOrNull()?.let {
                preferencesRepository.setDefaultSortOrder(it)
            }
            preferencesRepository.setHideInRecents(snapshot.settings.hideInRecents)

            BackupResult.ImportSuccess(
                importedClones = importedClones,
                importedSpaces = importedSpaces
            )
        } catch (e: Exception) {
            BackupResult.Error("Import failed: ${e.message ?: "Invalid backup JSON"}")
        }
    }
}
