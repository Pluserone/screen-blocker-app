package com.screentime.blocker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.screentime.blocker.data.local.ModeSettingsEntity
import com.screentime.blocker.ui.components.InlineNumberStepper
import com.screentime.blocker.ui.components.ScreenHeader
import com.screentime.blocker.ui.theme.*

@Composable
fun SoftLockSettingsScreen(
    settings: ModeSettingsEntity,
    onBack: () -> Unit,
    onDaysChange: (Int) -> Unit,
    onSelectApps: () -> Unit,
    onSelectSites: () -> Unit,
    onToggleBreathPractice: (Boolean) -> Unit,
    onOpenBreathPractice: () -> Unit,
    onToggleLock: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        ScreenHeader(onBack = onBack)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (settings.isLockActive) SoftGreen.copy(alpha = 0.5f) else SoftGreen)
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "мягкая блокировка",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                if (settings.isLockActive) {
                    Text(
                        text = "активна",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SoftGreenAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSelectApps
        ) {
            Text(
                text = "выберите приложения и срок блокировки (по желанию)",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "на какой период блокируем приложения в днях",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                InlineNumberStepper(
                    value = settings.durationDays,
                    onValueChange = onDaysChange,
                    step = 5
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSelectSites
        ) {
            Text(
                text = "выбрать сайты для блокировки",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenBreathPractice
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "включить дыхательную практику и придумать мотивирующий текст",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    if (settings.motivationText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "«${settings.motivationText}»",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                }
                Switch(
                    checked = settings.isBreathPracticeEnabled,
                    onCheckedChange = onToggleBreathPractice,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SoftGreenAccent,
                        checkedTrackColor = SoftGreen.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onToggleLock,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (settings.isLockActive) GlassButtonBg else SoftGreen
            ),
            border = if (settings.isLockActive) androidx.compose.foundation.BorderStroke(1.dp, GlassBorder) else null,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (settings.isLockActive) "остановить блокировку" else "запустить блокировку",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
    }
}
