package com.screentime.blocker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ModeSettingsEntity::class,
        BlockedAppEntity::class,
        BlockedSiteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppBlockerDatabase : RoomDatabase() {
    abstract fun blockerDao(): BlockerDao
}
