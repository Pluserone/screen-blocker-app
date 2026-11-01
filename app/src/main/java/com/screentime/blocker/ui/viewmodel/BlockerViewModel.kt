package com.screentime.blocker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.screentime.blocker.BlockerApp
import com.screentime.blocker.data.local.*
import com.screentime.blocker.service.BlockerForegroundService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BlockerViewModel(private val dao: BlockerDao) : ViewModel() {

    val softSettings = dao.getSettingsFlow(BlockMode.SOFT).map {
        it ?: ModeSettingsEntity(mode = BlockMode.SOFT, durationDays = 100)
    }.stateIn(viewModelScope, SharingStarted.Lazily, ModeSettingsEntity(mode = BlockMode.SOFT, durationDays = 100))

    val hardSettings = dao.getSettingsFlow(BlockMode.HARD).map {
        it ?: ModeSettingsEntity(mode = BlockMode.HARD, durationDays = 30)
    }.stateIn(viewModelScope, SharingStarted.Lazily, ModeSettingsEntity(mode = BlockMode.HARD, durationDays = 30))

    fun getApps(mode: BlockMode) = dao.getBlockedApps(mode)
    fun getSites(mode: BlockMode) = dao.getBlockedSites(mode)

    fun updateDays(mode: BlockMode, days: Int) {
        viewModelScope.launch {
            val current = dao.getSettings(mode) ?: ModeSettingsEntity(mode = mode)
            dao.saveSettings(current.copy(durationDays = days.coerceAtLeast(0)))
        }
    }

    fun toggleBreathPractice(mode: BlockMode, enabled: Boolean) {
        viewModelScope.launch {
            val current = dao.getSettings(mode) ?: ModeSettingsEntity(mode = mode)
            dao.saveSettings(current.copy(isBreathPracticeEnabled = enabled))
        }
    }

    fun setMotivation(mode: BlockMode, text: String, photoUri: String?) {
        viewModelScope.launch {
            val current = dao.getSettings(mode) ?: ModeSettingsEntity(mode = mode)
            dao.saveSettings(current.copy(motivationText = text, motivationPhotoUri = photoUri))
        }
    }

    fun setBreathDuration(mode: BlockMode, minutes: Int) {
        viewModelScope.launch {
            val current = dao.getSettings(mode) ?: ModeSettingsEntity(mode = mode)
            dao.saveSettings(current.copy(breathDurationMinutes = minutes.coerceAtLeast(1)))
        }
    }

    fun setPassword(mode: BlockMode, password: String) {
        viewModelScope.launch {
            val current = dao.getSettings(mode) ?: ModeSettingsEntity(mode = mode)
            dao.saveSettings(current.copy(isPasswordEnabled = password.isNotEmpty(), passwordHash = password))
        }
    }

    fun toggleAdmin(enabled: Boolean) {
        viewModelScope.launch {
            val current = dao.getSettings(BlockMode.HARD) ?: ModeSettingsEntity(mode = BlockMode.HARD)
            dao.saveSettings(current.copy(isAdminPermissionEnabled = enabled))
        }
    }

    fun toggleAppSelection(mode: BlockMode, app: BlockedAppEntity) {
        viewModelScope.launch {
            if (app.isSelected) {
                dao.deleteApp(app)
            } else {
                dao.upsertApp(app.copy(isSelected = true))
            }
        }
    }

    fun updateAppLimit(mode: BlockMode, app: BlockedAppEntity, newLimit: Int) {
        viewModelScope.launch {
            dao.upsertApp(app.copy(dailyLimitMinutes = newLimit))
        }
    }

    fun addSite(mode: BlockMode, url: String) {
        viewModelScope.launch {
            if (url.isNotBlank()) dao.insertSite(BlockedSiteEntity(mode = mode, url = url.trim()))
        }
    }

    fun removeSite(site: BlockedSiteEntity) {
        viewModelScope.launch { dao.deleteSite(site) }
    }

    fun setLockActive(mode: BlockMode, active: Boolean) {
        viewModelScope.launch {
            val current = dao.getSettings(mode) ?: ModeSettingsEntity(mode = mode)
            dao.saveSettings(current.copy(isLockActive = active))
            
            val app = BlockerApp.instance
            val soft = if (mode == BlockMode.SOFT) active else (dao.getSettings(BlockMode.SOFT)?.isLockActive == true)
            val hard = if (mode == BlockMode.HARD) active else (dao.getSettings(BlockMode.HARD)?.isLockActive == true)
            
            val pkgs = mutableMapOf<String, String>()
            val sites = mutableSetOf<String>()
            if (soft) {
                dao.getBlockedAppsList(BlockMode.SOFT).filter { it.isSelected }.forEach { pkgs[it.packageName] = it.appName }
                dao.getBlockedSitesList(BlockMode.SOFT).forEach { sites.add(it.url) }
            }
            if (hard) {
                dao.getBlockedAppsList(BlockMode.HARD).filter { it.isSelected }.forEach { pkgs[it.packageName] = it.appName }
                dao.getBlockedSitesList(BlockMode.HARD).forEach { sites.add(it.url) }
            }
            
            BlockerPreferences.updateCache(app, soft, hard, pkgs, sites)
            if (soft || hard) {
                BlockerForegroundService.start(app)
            } else {
                BlockerForegroundService.stop(app)
            }
        }
    }

    fun canStartHardLock(settings: ModeSettingsEntity): Pair<Boolean, String?> {
        val hasDays = settings.durationDays > 0
        val hasPassword = settings.isPasswordEnabled && !settings.passwordHash.isNullOrBlank()
        return if (!hasDays && !hasPassword) {
            false to "Нельзя отказываться от лимитов в днях и пароля одновременно, нужна хотя бы одна мера восстановления!"
        } else {
            true to null
        }
    }

    companion object {
        fun provideFactory(dao: BlockerDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BlockerViewModel(dao) as T
                }
            }
    }
}
