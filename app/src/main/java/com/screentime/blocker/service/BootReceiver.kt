package com.screentime.blocker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.screentime.blocker.data.local.BlockerPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            if (BlockerPreferences.isAnyLockActive(context)) {
                BlockerForegroundService.start(context)
            }
        }
    }
}
