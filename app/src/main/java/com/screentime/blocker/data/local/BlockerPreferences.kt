package com.screentime.blocker.data.local

import android.content.Context
import android.content.SharedPreferences

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
        blockedPackagesMap: Map<String, String>, // pkg -> name
        blockedSitesSet: Set<String>
    ) {
        val prefs = getPrefs(context)
        val pkgsSerialized = blockedPackagesMap.entries.joinToString("|||") { "${it.key}:::${it.value}" }
        val sitesSerialized = blockedSitesSet.joinToString("|||")

        prefs.edit()
            .putBoolean(KEY_SOFT_ACTIVE, isSoftActive)
            .putBoolean(KEY_HARD_ACTIVE, isHardActive)
            .putString(KEY_BLOCKED_PACKAGES, pkgsSerialized)
            .putString(KEY_BLOCKED_SITES, sitesSerialized)
            .apply()
    }

    fun isAnyLockActive(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_SOFT_ACTIVE, false) || prefs.getBoolean(KEY_HARD_ACTIVE, false)
    }

    fun isHardLockActive(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HARD_ACTIVE, false)
    }

    fun getBlockedPackages(context: Context): Map<String, String> {
        val raw = getPrefs(context).getString(KEY_BLOCKED_PACKAGES, "") ?: ""
        if (raw.isBlank()) return emptyMap()
        return raw.split("|||").mapNotNull {
            val parts = it.split(":::")
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }

    fun getBlockedSites(context: Context): Set<String> {
        val raw = getPrefs(context).getString(KEY_BLOCKED_SITES, "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split("|||").filter { it.isNotBlank() }.toSet()
    }
}
