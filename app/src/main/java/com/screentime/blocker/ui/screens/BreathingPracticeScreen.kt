package com.screentime.blocker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.screentime.blocker.ui.components.InlineNumberStepper
import com.screentime.blocker.ui.components.ScreenHeader
import com.screentime.blocker.ui.theme.*

@Composable
fun BreathingPracticeScreen(
    initialMinutes: Int,
    onBack: () -> Unit,
    onSaveMinutes: (Int) -> Unit
) {
    var minutes by remember { mutableStateOf(initialMinutes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        ScreenHeader(onBack = onBack)

        Text(
            text = "Дыхательная практика",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                InlineNumberStepper(
                    value = minutes,
                    onValueChange = { minutes = it },
                    step = 5,
                    min = 1,
                    suffix = "минут"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSaveMinutes(minutes)
                onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GlassButtonBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("применить", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "дыхательная практика поможет восстановиться, прийти в себя и обдумать свое решение. прежде чем снять блокировку",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
