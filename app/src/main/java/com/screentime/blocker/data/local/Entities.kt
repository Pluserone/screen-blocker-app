package com.screentime.blocker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mode_settings")
data class ModeSettingsEntity(
    @PrimaryKey val mode: BlockMode,
    val durationDays: Int = 30,
    val isBreathPracticeEnabled: Boolean = false,
    val breathDurationMinutes: Int = 20,
    val motivationText: String = "",
    val motivationPhotoUri: String? = null,
    val isPasswordEnabled: Boolean = false,
    val passwordHash: String? = null,
    val isAdminPermissionEnabled: Boolean = false,
    val isLockActive: Boolean = false
)

@Entity(
    tableName = "blocked_apps",
    primaryKeys = ["mode", "packageName"]
)
data class BlockedAppEntity(
    val mode: BlockMode,
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int = 0, // -1 для бесконечности (∞)
    val isSelected: Boolean = true
)

@Entity(tableName = "blocked_sites")
data class BlockedSiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: BlockMode,
    val url: String
)
