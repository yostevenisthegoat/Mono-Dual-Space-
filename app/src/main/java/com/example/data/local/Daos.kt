package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CloneDao {
    @Query("SELECT * FROM clone_entries ORDER BY pinned DESC, lastLaunchedAt DESC, customName COLLATE NOCASE ASC")
    fun observeAllClones(): Flow<List<CloneEntryEntity>>

    @Query("SELECT * FROM clone_entries WHERE id = :id")
    suspend fun getCloneById(id: Long): CloneEntryEntity?

    @Query("SELECT * FROM clone_entries WHERE packageName = :packageName AND profileUserSerial = :profileSerial LIMIT 1")
    suspend fun getCloneByPackageAndProfile(packageName: String, profileSerial: Long): CloneEntryEntity?

    @Query("SELECT * FROM clone_entries")
    suspend fun getAllClonesSnapshot(): List<CloneEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClone(entry: CloneEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClones(entries: List<CloneEntryEntity>)

    @Update
    suspend fun updateClone(entry: CloneEntryEntity)

    @Delete
    suspend fun deleteClone(entry: CloneEntryEntity)

    @Query("DELETE FROM clone_entries WHERE id = :id")
    suspend fun deleteCloneById(id: Long)

    @Query("UPDATE clone_entries SET lastLaunchedAt = :timestamp, launchCount = launchCount + 1 WHERE id = :id")
    suspend fun recordLaunch(id: Long, timestamp: Long)
}

@Dao
interface SpaceDao {
    @Query("SELECT * FROM space_profiles ORDER BY createdAt ASC")
    fun observeSpaces(): Flow<List<SpaceProfileEntity>>

    @Query("SELECT * FROM space_profiles")
    suspend fun getSpacesSnapshot(): List<SpaceProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpace(space: SpaceProfileEntity)

    @Query("DELETE FROM space_profiles WHERE userSerialNumber = :serial")
    suspend fun deleteSpace(serial: Long)
}

@Dao
interface CachedAppDao {
    @Query("SELECT * FROM cached_apps ORDER BY appName COLLATE NOCASE ASC")
    fun observeCachedApps(): Flow<List<CachedAppEntity>>

    @Query("SELECT * FROM cached_apps")
    suspend fun getAllSnapshot(): List<CachedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApps(apps: List<CachedAppEntity>)

    @Query("DELETE FROM cached_apps")
    suspend fun clearAll()
}
