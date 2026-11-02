package com.screentime.blocker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.screentime.blocker.data.local.BlockerPreferences
import com.screentime.blocker.service.BlockerForegroundService
import com.screentime.blocker.ui.navigation.AppNavHost
import com.screentime.blocker.ui.theme.AppTheme
import com.screentime.blocker.ui.theme.BlackBackground
import com.screentime.blocker.ui.viewmodel.BlockerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BlockerViewModel by viewModels {
        BlockerViewModel.provideFactory((application as BlockerApp).database.blockerDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Запрос разрешения на показ уведомлений для надежного Foreground Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        if (BlockerPreferences.isAnyLockActive(this)) {
            BlockerForegroundService.start(this)
        }

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BlackBackground),
                    color = BlackBackground
                ) {
                    AppNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
