package com.screentime.blocker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.screentime.blocker.MainActivity
import com.screentime.blocker.data.local.BlockerPreferences

class BlockerForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isLockActive = BlockerPreferences.isAnyLockActive(this)
        val title = if (isLockActive) "🛡️ Screen Blocker: Защита активна" else "🛡️ Screen Blocker работает в фоне"
        val text = if (isLockActive) "Блокировка приложений и сайтов включена" else "Служба готова к блокировке"

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

        return START_STICKY
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
                description = "Постоянное уведомление для защиты от закрытия блокировки"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Если пользователь смахнул приложение из недавних, перезапускаем службу
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
            System.currentTimeMillis() + 500,
            pendingIntent
        )
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
