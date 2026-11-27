package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CloneEntryEntity::class,
        SpaceProfileEntity::class,
        CachedAppEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MonoDatabase : RoomDatabase() {
    abstract fun cloneDao(): CloneDao
    abstract fun spaceDao(): SpaceDao
    abstract fun cachedAppDao(): CachedAppDao

    companion object {
        @Volatile
        private var INSTANCE: MonoDatabase? = null

        fun getInstance(context: Context): MonoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MonoDatabase::class.java,
                    "mono_dual_space.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
