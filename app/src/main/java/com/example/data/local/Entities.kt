package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clone_entries",
    indices = [
        Index(value = ["packageName", "profileUserSerial"], unique = true)
    ]
)
data class CloneEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val originalAppName: String,
    val customName: String,
    val iconStyleKey: String = "DEFAULT",
    val badgeLabel: String = "SPACE",
    val profileUserSerial: Long = -1L,
    val spaceName: String = "Work Space",
    val groupTag: String = "General",
    val notes: String = "",
    val requireAppLock: Boolean = false,
    val separateNotifications: Boolean = true,
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLaunchedAt: Long? = null,
    val launchCount: Int = 0
)

@Entity(tableName = "space_profiles")
data class SpaceProfileEntity(
    @PrimaryKey val userSerialNumber: Long,
    val label: String,
    val engineType: String,
    val isProvisionedByMono: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_apps")
data class CachedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val versionName: String,
    val apkSizeBytes: Long,
    val lastScannedAt: Long
)
