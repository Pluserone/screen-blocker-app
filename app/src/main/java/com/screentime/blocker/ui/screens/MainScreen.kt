package com.screentime.blocker.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.screentime.blocker.data.local.ModeSettingsEntity
import com.screentime.blocker.ui.theme.*

@Composable
fun MainScreen(
    hardSettings: ModeSettingsEntity,
    onNavigateToSoft: () -> Unit,
    onNavigateToHard: () -> Unit,
    onNavigateToPasswordCheck: () -> Unit
) {
    val context = LocalContext.current

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
            Text(
                text = "⚡ Агрессивная блокировка",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
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
            Text(
                text = "🛡️ Мягкая блокировка",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
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
