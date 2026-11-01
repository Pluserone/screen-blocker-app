package com.screentime.blocker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.screentime.blocker.ui.components.InlineNumberStepper
import com.screentime.blocker.ui.components.ScreenHeader
import com.screentime.blocker.ui.theme.*

@Composable
fun MotivationScreen(
    initialText: String,
    initialPhotoUri: String?,
    initialMinutes: Int,
    onBack: () -> Unit,
    onSave: (String, String?, Int) -> Unit
) {
    var motivationText by remember { mutableStateOf(initialText) }
    var selectedUri by remember { mutableStateOf<Uri?>(initialPhotoUri?.let { Uri.parse(it) }) }
    var minutes by remember { mutableStateOf(initialMinutes) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "Дыхание и мотивация", onBack = onBack)

        Spacer(modifier = Modifier.height(8.dp))

        // Настройка времени дыхания
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Дыхательная практика",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                InlineNumberStepper(
                    value = minutes,
                    onValueChange = { minutes = it },
                    step = 5,
                    min = 1,
                    suffix = "минут"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Поле для ввода мотивирующего текста
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "напишите текст который будет вас мотивировать",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = motivationText,
                onValueChange = { motivationText = it },
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                    Text(
                        text = "Введите текст здесь...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Кнопка добавления мотивирующего фото
        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GlassButtonBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (selectedUri != null) "фото добавлено (выбрать другое)" else "добавить мотивирующее фото",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка применить
        Button(
            onClick = {
                onSave(motivationText, selectedUri?.toString(), minutes)
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
