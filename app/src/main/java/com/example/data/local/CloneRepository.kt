package com.example.data.local

import kotlinx.coroutines.flow.Flow

class CloneRepository(
    private val cloneDao: CloneDao,
    private val spaceDao: SpaceDao,
    private val cachedAppDao: CachedAppDao
) {
    val allClones: Flow<List<CloneEntryEntity>> = cloneDao.observeAllClones()
    val allSpaces: Flow<List<SpaceProfileEntity>> = spaceDao.observeSpaces()
    val cachedApps: Flow<List<CachedAppEntity>> = cachedAppDao.observeCachedApps()

    suspend fun getCloneById(id: Long): CloneEntryEntity? = cloneDao.getCloneById(id)

    suspend fun getCloneByPackageAndProfile(packageName: String, profileSerial: Long): CloneEntryEntity? =
        cloneDao.getCloneByPackageAndProfile(packageName, profileSerial)

    suspend fun getAllClonesSnapshot(): List<CloneEntryEntity> = cloneDao.getAllClonesSnapshot()

    suspend fun saveClone(entry: CloneEntryEntity): Long = cloneDao.insertClone(entry)

    suspend fun updateClone(entry: CloneEntryEntity) = cloneDao.updateClone(entry)

    suspend fun deleteClone(entry: CloneEntryEntity) = cloneDao.deleteClone(entry)

    suspend fun deleteCloneById(id: Long) = cloneDao.deleteCloneById(id)

    suspend fun recordLaunch(id: Long) = cloneDao.recordLaunch(id, System.currentTimeMillis())

    suspend fun upsertSpace(space: SpaceProfileEntity) = spaceDao.upsertSpace(space)

    suspend fun deleteSpace(serial: Long) = spaceDao.deleteSpace(serial)

    suspend fun getSpacesSnapshot(): List<SpaceProfileEntity> = spaceDao.getSpacesSnapshot()

    suspend fun upsertCachedApps(apps: List<CachedAppEntity>) = cachedAppDao.upsertApps(apps)
}
