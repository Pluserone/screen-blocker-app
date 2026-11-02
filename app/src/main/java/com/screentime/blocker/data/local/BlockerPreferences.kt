package com.screentime.blocker.data.local

import android.content.Context
import android.content.SharedPreferences

data class CachedAppLimit(
    val appName: String,
    val limitMinutes: Int
)

object BlockerPreferences {
    private const val PREFS_NAME = "screen_blocker_fast_cache"
    private const val KEY_SOFT_ACTIVE = "key_soft_active"
    private const val KEY_HARD_ACTIVE = "key_hard_active"
    private const val KEY_BLOCKED_PACKAGES = "key_blocked_packages"
    private const val KEY_BLOCKED_SITES = "key_blocked_sites"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun updateCache(
        context: Context,
        isSoftActive: Boolean,
        isHardActive: Boolean,
        blockedPackagesMap: Map<String, CachedAppLimit>, // pkg -> (appName, limitMinutes)
        blockedSitesSet: Set<String>
    ) {
        val prefs = getPrefs(context)
        val pkgsSerialized = blockedPackagesMap.entries.joinToString("|||") {
            "${it.key}:::${it.value.appName}:::${it.value.limitMinutes}"
        }
        val sitesSerialized = blockedSitesSet.joinToString("|||")

        prefs.edit()
            .putBoolean(KEY_SOFT_ACTIVE, isSoftActive)
            .putBoolean(KEY_HARD_ACTIVE, isHardActive)
            .putString(KEY_BLOCKED_PACKAGES, pkgsSerialized)
            .putString(KEY_BLOCKED_SITES, sitesSerialized)
            .commit() // commit синхронно для надежности
    }

    fun isAnyLockActive(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_SOFT_ACTIVE, false) || prefs.getBoolean(KEY_HARD_ACTIVE, false)
    }

    fun isHardLockActive(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HARD_ACTIVE, false)
    }

    fun getBlockedPackages(context: Context): Map<String, CachedAppLimit> {
        val raw = getPrefs(context).getString(KEY_BLOCKED_PACKAGES, "") ?: ""
        if (raw.isBlank()) return emptyMap()
        return raw.split("|||").mapNotNull { entry ->
            val parts = entry.split(":::")
            if (parts.size >= 3) {
                val pkg = parts[0]
                val name = parts[1]
                val limit = parts[2].toIntOrNull() ?: 0
                pkg to CachedAppLimit(name, limit)
            } else if (parts.size == 2) {
                val pkg = parts[0]
                val name = parts[1]
                pkg to CachedAppLimit(name, 0)
            } else null
        }.toMap()
    }

    fun getBlockedSites(context: Context): Set<String> {
        val raw = getPrefs(context).getString(KEY_BLOCKED_SITES, "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split("|||").filter { it.isNotBlank() }.toSet()
    }
}
