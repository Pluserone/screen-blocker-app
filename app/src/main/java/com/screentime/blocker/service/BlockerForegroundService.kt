package com.screentime.blocker.service

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.screentime.blocker.MainActivity
import com.screentime.blocker.data.local.BlockerPreferences
import com.screentime.blocker.data.local.UsageTracker
import com.screentime.blocker.ui.BlockOverlayActivity
import kotlinx.coroutines.*

class BlockerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isLoopRunning = false
    private var lastBlockedLaunchTime = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isLockActive = BlockerPreferences.isAnyLockActive(this)
        val title = if (isLockActive) "🛡️ Screen Blocker: Защита активна" else "🛡️ Screen Blocker работает в фоне"
        val text = if (isLockActive) "Блокировка 24/7 приложений и сайтов включена" else "Служба готова к блокировке"

        val notification = buildNotification(title, text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (!isLoopRunning) {
            start24x7MonitoringLoop()
        }

        return START_STICKY
    }

    /**
     * Непрерывный цикл 24/7, работающий параллельно с AccessibilityService.
     * Даже если система временно усыпит спец. возможности при смахивании,
     * эта служба с наивысшим приоритетом процесса перехватывает приложения каждые 400мс.
     */
    private fun start24x7MonitoringLoop() {
        isLoopRunning = true
        serviceScope.launch {
            while (isActive) {
                try {
                    if (BlockerPreferences.isAnyLockActive(this@BlockerForegroundService)) {
                        checkTopAppAndEnforceBlock()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(400L)
            }
        }
    }

    private fun checkTopAppAndEnforceBlock() {
        val topPackage = getForegroundPackage(this) ?: return

        // Системные пакеты, лаунчер и само приложение не трогаем
        if (topPackage == packageName ||
            topPackage == "com.android.systemui" ||
            topPackage == "com.miui.home" ||
            topPackage == "com.google.android.apps.nexuslauncher"
        ) {
            return
        }

        val blockedMap = BlockerPreferences.getBlockedPackages(this)
        val limitInfo = blockedMap[topPackage] ?: return

        // 1. Лимит 0 минут — мгновенная блокировка 24/7
        if (limitInfo.limitMinutes == 0) {
            triggerBlock(limitInfo.appName, "Доступ к этому приложению ограничен")
            return
        }

        // 2. Лимит в минутах (> 0)
        if (limitInfo.limitMinutes > 0) {
            // Засчитываем секунду использования
            UsageTracker.addUsageSeconds(this, topPackage, 1L)

            if (UsageTracker.isAppLimitExceeded(this, topPackage, limitInfo.limitMinutes)) {
                triggerBlock(
                    limitInfo.appName,
                    "Дневной лимит (${limitInfo.limitMinutes} мин) исчерпан!"
                )
            }
        }
    }

    private fun triggerBlock(appName: String, subtitle: String) {
        val now = System.currentTimeMillis()
        // Предотвращаем спам Intent-ами чаще чем раз в 800мс
        if (now - lastBlockedLaunchTime < 800) return
        lastBlockedLaunchTime = now

        // 1. Немедленно сворачиваем запрещенное приложение на рабочий стол
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)

        // 2. Показываем экран блокировки
        val blockIntent = Intent(this, BlockOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockOverlayActivity.EXTRA_TITLE, appName)
            putExtra(BlockOverlayActivity.EXTRA_SUBTITLE, subtitle)
        }
        startActivity(blockIntent)
    }

    private fun getForegroundPackage(context: Context): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val time = System.currentTimeMillis()
        val usageEvents = usm.queryEvents(time - 8000, time) ?: return null
        val event = UsageEvents.Event()
        var lastResumedPackage: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastResumedPackage = event.packageName
            }
        }
        return lastResumedPackage
    }

    private fun buildNotification(title: String, text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Blocker Защита",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Постоянное уведомление для защиты 24/7 от закрытия блокировки"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val restartIntent = Intent(applicationContext, BlockerForegroundService::class.java)
        val pendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.set(
            AlarmManager.RTC,
            System.currentTimeMillis() + 300,
            pendingIntent
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        isLoopRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "screen_blocker_keep_alive_channel"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, BlockerForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BlockerForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
