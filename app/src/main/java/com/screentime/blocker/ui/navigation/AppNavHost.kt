package com.screentime.blocker.ui.navigation

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.screentime.blocker.data.local.BlockMode
import com.screentime.blocker.service.BlockerAccessibilityService
import com.screentime.blocker.ui.screens.*
import com.screentime.blocker.ui.viewmodel.BlockerViewModel

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object SoftSettings : Screen("soft_settings")
    object HardSettings : Screen("hard_settings")
    object PasswordCheck : Screen("password_check/{action}") {
        fun createRoute(action: String) = "password_check/$action"
    }
    object PasswordSetup : Screen("password_setup")
    object AppSelection : Screen("apps/{mode}") {
        fun createRoute(mode: BlockMode) = "apps/${mode.name}"
    }
    object SiteBlock : Screen("sites/{mode}") {
        fun createRoute(mode: BlockMode) = "sites/${mode.name}"
    }
    object Motivation : Screen("motivation/{mode}") {
        fun createRoute(mode: BlockMode) = "motivation/${mode.name}"
    }
    object Breathing : Screen("breathing/{mode}") {
        fun createRoute(mode: BlockMode) = "breathing/${mode.name}"
    }
    object BreathingSession : Screen("breathing_session")
}

@Composable
fun AppNavHost(viewModel: BlockerViewModel) {
    val navController = rememberNavController()
    val softSettings by viewModel.softSettings.collectAsState()
    val hardSettings by viewModel.hardSettings.collectAsState()
    val context = LocalContext.current

    fun promptAccessibilityIfNeeded() {
        if (!BlockerAccessibilityService.isAccessibilityServiceEnabled(context)) {
            Toast.makeText(
                context,
                "Включите службу «Screen Blocker» в спец. возможностях для блокировки!",
                Toast.LENGTH_LONG
            ).show()
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Ignore if device doesn't support direct settings intent
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(
                softSettings = softSettings,
                hardSettings = hardSettings,
                onNavigateToSoft = { navController.navigate(Screen.SoftSettings.route) },
                onNavigateToHard = { navController.navigate(Screen.HardSettings.route) },
                onNavigateToPasswordCheck = {
                    navController.navigate(Screen.PasswordCheck.createRoute("enter"))
                }
            )
        }

        composable(
            route = Screen.PasswordCheck.route,
            arguments = listOf(navArgument("action") { type = NavType.StringType; defaultValue = "enter" })
        ) { backStackEntry ->
            val action = backStackEntry.arguments?.getString("action") ?: "enter"
            PasswordInputScreen(
                expectedPasswordHash = hardSettings.passwordHash,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    if (action == "stop") {
                        viewModel.setLockActive(BlockMode.HARD, false)
                        Toast.makeText(context, "Жесткая блокировка снята!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        navController.popBackStack()
                        navController.navigate(Screen.HardSettings.route)
                    }
                }
            )
        }

        composable(Screen.SoftSettings.route) {
            SoftLockSettingsScreen(
                settings = softSettings,
                onBack = { navController.popBackStack() },
                onDaysChange = { viewModel.updateDays(BlockMode.SOFT, it) },
                onSelectApps = { navController.navigate(Screen.AppSelection.createRoute(BlockMode.SOFT)) },
                onSelectSites = { navController.navigate(Screen.SiteBlock.createRoute(BlockMode.SOFT)) },
                onToggleBreathPractice = { viewModel.toggleBreathPractice(BlockMode.SOFT, it) },
                onOpenBreathPractice = { navController.navigate(Screen.Motivation.createRoute(BlockMode.SOFT)) },
                onToggleLock = {
                    if (softSettings.isLockActive) {
                        if (softSettings.isBreathPracticeEnabled) {
                            navController.navigate(Screen.BreathingSession.route)
                        } else {
                            viewModel.setLockActive(BlockMode.SOFT, false)
                            Toast.makeText(context, "Мягкая блокировка остановлена", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        promptAccessibilityIfNeeded()
                        viewModel.setLockActive(BlockMode.SOFT, true)
                        Toast.makeText(context, "Мягкая блокировка запущена", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        composable(Screen.HardSettings.route) {
            HardLockSettingsScreen(
                settings = hardSettings,
                onBack = { navController.popBackStack() },
                onDaysChange = { viewModel.updateDays(BlockMode.HARD, it) },
                onSelectApps = { navController.navigate(Screen.AppSelection.createRoute(BlockMode.HARD)) },
                onSelectSites = { navController.navigate(Screen.SiteBlock.createRoute(BlockMode.HARD)) },
                onOpenPasswordSetup = { navController.navigate(Screen.PasswordSetup.route) },
                onToggleAdmin = { viewModel.toggleAdmin(it) },
                onValidateAndStart = {
                    val res = viewModel.canStartHardLock(hardSettings)
                    if (res.first) {
                        promptAccessibilityIfNeeded()
                        viewModel.setLockActive(BlockMode.HARD, true)
                        Toast.makeText(context, "Жесткая блокировка запущена!", Toast.LENGTH_SHORT).show()
                    }
                    res
                },
                onStopLock = {
                    if (hardSettings.isPasswordEnabled && !hardSettings.passwordHash.isNullOrBlank()) {
                        navController.navigate(Screen.PasswordCheck.createRoute("stop"))
                    } else {
                        viewModel.setLockActive(BlockMode.HARD, false)
                        Toast.makeText(context, "Жесткая блокировка остановлена", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        composable(Screen.PasswordSetup.route) {
            PasswordSetupScreen(
                onBack = { navController.popBackStack() },
                onPasswordSet = { viewModel.setPassword(BlockMode.HARD, it) }
            )
        }

        composable(
            route = Screen.AppSelection.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val modeStr = backStackEntry.arguments?.getString("mode") ?: BlockMode.SOFT.name
            val mode = BlockMode.valueOf(modeStr)
            val apps by viewModel.getApps(mode).collectAsState(initial = emptyList())

            AppSelectionScreen(
                mode = mode,
                blockedApps = apps,
                onBack = { navController.popBackStack() },
                onToggleApp = { viewModel.toggleAppSelection(mode, it) },
                onUpdateLimit = { app, limit -> viewModel.updateAppLimit(mode, app, limit) }
            )
        }

        composable(
            route = Screen.SiteBlock.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val modeStr = backStackEntry.arguments?.getString("mode") ?: BlockMode.SOFT.name
            val mode = BlockMode.valueOf(modeStr)
            val sites by viewModel.getSites(mode).collectAsState(initial = emptyList())

            SiteBlockScreen(
                sites = sites,
                onBack = { navController.popBackStack() },
                onAddSite = { viewModel.addSite(mode, it) },
                onDeleteSite = { viewModel.removeSite(it) }
            )
        }

        composable(
            route = Screen.Motivation.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val modeStr = backStackEntry.arguments?.getString("mode") ?: BlockMode.SOFT.name
            val mode = BlockMode.valueOf(modeStr)
            val settings = if (mode == BlockMode.SOFT) softSettings else hardSettings

            MotivationScreen(
                initialText = settings.motivationText,
                initialPhotoUri = settings.motivationPhotoUri,
                initialMinutes = settings.breathDurationMinutes,
                onBack = { navController.popBackStack() },
                onSave = { text, uri, mins ->
                    viewModel.setMotivation(mode, text, uri)
                    viewModel.setBreathDuration(mode, mins)
                }
            )
        }

        composable(
            route = Screen.Breathing.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val modeStr = backStackEntry.arguments?.getString("mode") ?: BlockMode.SOFT.name
            val mode = BlockMode.valueOf(modeStr)
            val settings = if (mode == BlockMode.SOFT) softSettings else hardSettings

            BreathingPracticeScreen(
                initialMinutes = settings.breathDurationMinutes,
                onBack = { navController.popBackStack() },
                onSaveMinutes = { viewModel.setBreathDuration(mode, it) }
            )
        }

        composable(Screen.BreathingSession.route) {
            ActiveBreathingSessionScreen(
                durationMinutes = softSettings.breathDurationMinutes,
                motivationText = softSettings.motivationText,
                onBack = { navController.popBackStack() },
                onFinishPracticeAndUnlock = {
                    viewModel.setLockActive(BlockMode.SOFT, false)
                    Toast.makeText(context, "Мягкая блокировка успешно снята!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            )
        }
    }
}
