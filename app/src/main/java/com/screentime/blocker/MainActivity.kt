package com.screentime.blocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
