package com.screentime.blocker.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.screentime.blocker.ui.components.ScreenHeader
import com.screentime.blocker.ui.theme.*
import kotlinx.coroutines.delay

enum class BreathPhase(val title: String, val durationSeconds: Int, val targetScale: Float) {
    INHALE("Вдох через нос...", 4, 1.30f),
    EXHALE("Плавный выдох...", 5, 0.85f),
    REST("🌿 Перерыв: обычное дыхание", 15, 1.0f)
}

@Composable
fun ActiveBreathingSessionScreen(
    durationMinutes: Int,
    motivationText: String,
    onBack: () -> Unit,
    onFinishPracticeAndUnlock: () -> Unit
) {
    var remainingSeconds by remember { mutableStateOf(durationMinutes * 60) }
    var isCompleted by remember { mutableStateOf(false) }

    // Логика дыхательных сетов: 4 вдоха-выдоха, затем 15 сек спокойного перерыва
    var currentPhase by remember { mutableStateOf(BreathPhase.INHALE) }
    var cycleCount by remember { mutableStateOf(1) } // 1..4
    var phaseSecondsLeft by remember { mutableStateOf(BreathPhase.INHALE.durationSeconds) }

    // Основной таймер сессии
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        isCompleted = true
    }

    // Автоматическая смена фаз с перерывами для безопасности
    LaunchedEffect(isCompleted) {
        while (!isCompleted) {
            delay(1000L)
            phaseSecondsLeft--
            if (phaseSecondsLeft <= 0) {
                when (currentPhase) {
                    BreathPhase.INHALE -> {
                        currentPhase = BreathPhase.EXHALE
                        phaseSecondsLeft = BreathPhase.EXHALE.durationSeconds
                    }
                    BreathPhase.EXHALE -> {
                        if (cycleCount < 4) {
                            cycleCount++
                            currentPhase = BreathPhase.INHALE
                            phaseSecondsLeft = BreathPhase.INHALE.durationSeconds
                        } else {
                            // 4 цикла завершены — перерыв на обычное спокойное дыхание
                            currentPhase = BreathPhase.REST
                            phaseSecondsLeft = BreathPhase.REST.durationSeconds
                        }
                    }
                    BreathPhase.REST -> {
                        // После перерыва начинаем новый сет
                        cycleCount = 1
                        currentPhase = BreathPhase.INHALE
                        phaseSecondsLeft = BreathPhase.INHALE.durationSeconds
                    }
                }
            }
        }
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.0f else currentPhase.targetScale,
        animationSpec = tween(
            durationMillis = (currentPhase.durationSeconds * 950).coerceAtMost(4500),
            easing = FastOutSlowInEasing
        ),
        label = "breath_scale"
    )

    val minutesPart = remainingSeconds / 60
    val secondsPart = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutesPart, secondsPart)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(
            title = "Дыхательная практика",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (motivationText.isNotBlank()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = motivationText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(0.4f))

        // Анимированный круг дыхания
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(GlassSurface)
                .border(
                    2.dp,
                    if (currentPhase == BreathPhase.REST) TextSecondary.copy(alpha = 0.4f) else SoftGreenAccent.copy(alpha = 0.7f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isCompleted) "Практика окончена" else currentPhase.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                if (!isCompleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (currentPhase == BreathPhase.REST) {
                            "отдых: $phaseSecondsLeft с"
                        } else {
                            "шаг $cycleCount из 4 ($phaseSecondsLeft с)"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.4f))

        // Информационный блок с оставшимся временем
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isCompleted) "Вы можете снять блокировку" else "Осталось до снятия блокировки:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SoftGreenAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка разблокировки
        Button(
            onClick = onFinishPracticeAndUnlock,
            enabled = isCompleted,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SoftGreen,
                disabledContainerColor = GlassButtonBg
            ),
            border = if (!isCompleted) androidx.compose.foundation.BorderStroke(1.dp, GlassBorder) else null,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (isCompleted) "снять блокировку" else "выполните дыхательную практику",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCompleted) TextPrimary else TextSecondary
            )
        }
    }
}
