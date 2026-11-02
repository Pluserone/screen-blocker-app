package com.screentime.blocker.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.screentime.blocker.data.local.ModeSettingsEntity
import com.screentime.blocker.data.local.UsageTracker
import com.screentime.blocker.service.BlockerAccessibilityService
import com.screentime.blocker.ui.theme.*

@Composable
fun MainScreen(
    softSettings: ModeSettingsEntity,
    hardSettings: ModeSettingsEntity,
    onNavigateToSoft: () -> Unit,
    onNavigateToHard: () -> Unit,
    onNavigateToPasswordCheck: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isAccessibilityEnabled by remember { mutableStateOf(checkAccessibilityEnabled(context)) }
    var isUsageAccessEnabled by remember { mutableStateOf(UsageTracker.hasUsageStatsPermission(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityEnabled = checkAccessibilityEnabled(context)
                isUsageAccessEnabled = UsageTracker.hasUsageStatsPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Выберите режим",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Баннер предупреждения, если спец. возможности не включены
        if (!isAccessibilityEnabled) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = GlassSurface,
                border = BorderStroke(1.dp, HardRedAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ Внимание: требуется спец. возможность",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HardRedAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Для мгновенной блокировки включите службу «Screen Blocker» в спец. возможностях телефона.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HardRedAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Включить спец. возможности",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Баннер предупреждения, если доступ к данным об использовании не включен (для 24/7 работы)
        if (!isUsageAccessEnabled) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = GlassSurface,
                border = BorderStroke(1.dp, HardRedAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚙️ Требуется доступ к использованию (24/7)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HardRedAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Чтобы блокировка не отключалась при очистке памяти и работала 24/7, разрешите доступ к данным использования.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HardRedAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Разрешить доступ к использованию",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Карточка: Агрессивная блокировка
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (hardSettings.isPasswordEnabled && !hardSettings.passwordHash.isNullOrBlank()) {
                    onNavigateToPasswordCheck()
                } else {
                    onNavigateToHard()
                }
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ Агрессивная блокировка",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = if (hardSettings.isLockActive) "активна" else "выключена",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (hardSettings.isLockActive) HardRedAccent else TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Блокировка ставится на определенное время и ее нельзя снять\nэтот вид блокировки рекомендуется ставить на развлекательные приложения от которых есть сильная зависимость...",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Карточка: Мягкая блокировка
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToSoft
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛡️ Мягкая блокировка",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = if (softSettings.isLockActive) "активна" else "выключена",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (softSettings.isLockActive) SoftGreenAccent else TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Блокировку можно снять в любое время\nэтот вид блокировки рекомендуется ставить на мессенджеры и другие важные приложения от которых нет сильной зависимости...",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Блокировку можно снять в любое время\nэтот вид блокировки рекомендуется ставить на мессенджеры и другие важные приложения от которых нет сильной зависимости...",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка-ссылка t.me/pluseron
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/pluseron"))
                context.startActivity(intent)
            }
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "t.me/pluseron",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}

private fun checkAccessibilityEnabled(context: Context): Boolean {
    val expectedServiceName = "${context.packageName}/${BlockerAccessibilityService::class.java.canonicalName}"
    val settingValue = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(settingValue)
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedServiceName, ignoreCase = true)) {
            return true
        }
    }
    return false
}
