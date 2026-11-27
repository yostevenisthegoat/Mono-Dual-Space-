package com.example.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class MonoBackupSnapshot(
    val schemaVersion: Int = 1,
    val exportedAtEpochMs: Long,
    val appVersion: String = "1.0.0",
    val note: String = "Contains Mono Dual-Space configuration metadata only. PIN hashes and third-party app sandbox data are excluded by design.",
    val spaces: List<BackupSpaceRecord>,
    val clones: List<BackupCloneRecord>,
    val settings: BackupSettingsRecord
)

@Serializable
data class BackupSpaceRecord(
    val userSerialNumber: Long,
    val label: String,
    val engineType: String,
    val isProvisionedByMono: Boolean
)

@Serializable
data class BackupCloneRecord(
    val packageName: String,
    val originalAppName: String,
    val customName: String,
    val iconStyleKey: String,
    val badgeLabel: String,
    val profileUserSerial: Long,
    val spaceName: String,
    val groupTag: String,
    val notes: String,
    val requireAppLock: Boolean,
    val separateNotifications: Boolean,
    val pinned: Boolean
)

@Serializable
data class BackupSettingsRecord(
    val themeMode: String,
    val compactDensity: Boolean,
    val defaultSortOrder: String,
    val hideInRecents: Boolean
)
