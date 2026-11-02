package com.screentime.blocker.data.local

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import java.text.SimpleDateFormat
import java.util.*

object UsageTracker {

    private fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        return sdf.format(Date())
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getTodayUsageMinutes(context: Context, packageName: String): Int {
        // 1. Если есть системное разрешение на чтение статистики использования, берем точные данные ОС
        if (hasUsageStatsPermission(context)) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            if (usm != null) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startTime = calendar.timeInMillis
                val endTime = System.currentTimeMillis()
                val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
                val appStat = stats?.find { it.packageName == packageName }
                if (appStat != null) {
                    val minutesFromOs = (appStat.totalTimeInForeground / 60000L).toInt()
                    return minutesFromOs
                }
            }
        }

        // 2. Дополнительно считываем наш локальный секундомер из SharedPreferences
        val prefs = context.getSharedPreferences("app_usage_timer", Context.MODE_PRIVATE)
        val key = "${getTodayDateKey()}_$packageName"
        val seconds = prefs.getLong(key, 0L)
        return (seconds / 60L).toInt()
    }

    fun addUsageSeconds(context: Context, packageName: String, secondsToAdd: Long) {
        val prefs = context.getSharedPreferences("app_usage_timer", Context.MODE_PRIVATE)
        val key = "${getTodayDateKey()}_$packageName"
        val current = prefs.getLong(key, 0L)
        prefs.edit().putLong(key, current + secondsToAdd).apply()
    }

    fun resetTodayUsage(context: Context) {
        val prefs = context.getSharedPreferences("app_usage_timer", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun isAppLimitExceeded(context: Context, packageName: String, limitMinutes: Int): Boolean {
        // -1 означает бесконечность (без лимита)
        if (limitMinutes < 0) return false
        // 0 означает мгновенную блокировку (0 минут разрешено в день)
        if (limitMinutes == 0) return true

        val usedMinutes = getTodayUsageMinutes(context, packageName)
        return usedMinutes >= limitMinutes
    }

    fun getRemainingMinutes(context: Context, packageName: String, limitMinutes: Int): Int {
        if (limitMinutes <= 0) return 0
        val usedMinutes = getTodayUsageMinutes(context, packageName)
        return (limitMinutes - usedMinutes).coerceAtLeast(0)
    }
}
