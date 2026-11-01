package com.screentime.blocker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockerDao {
    @Query("SELECT * FROM mode_settings WHERE mode = :mode")
    fun getSettingsFlow(mode: BlockMode): Flow<ModeSettingsEntity?>

    @Query("SELECT * FROM mode_settings WHERE mode = :mode")
    suspend fun getSettings(mode: BlockMode): ModeSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ModeSettingsEntity)

    @Query("SELECT * FROM blocked_apps WHERE mode = :mode")
    fun getBlockedApps(mode: BlockMode): Flow<List<BlockedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApp(app: BlockedAppEntity)

    @Delete
    suspend fun deleteApp(app: BlockedAppEntity)

    @Query("SELECT * FROM blocked_sites WHERE mode = :mode")
    fun getBlockedSites(mode: BlockMode): Flow<List<BlockedSiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: BlockedSiteEntity)

    @Delete
    suspend fun deleteSite(site: BlockedSiteEntity)
}
