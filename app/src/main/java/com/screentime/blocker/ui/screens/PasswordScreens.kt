package com.screentime.blocker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.screentime.blocker.ui.components.ScreenHeader
import com.screentime.blocker.ui.theme.*

@Composable
fun PasswordInputScreen(
    expectedPasswordHash: String?,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        ScreenHeader(onBack = onBack)

        Text(
            text = "Введите пароль",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    hasError = false
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = hasError,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (password == expectedPasswordHash) {
                    onSuccess()
                } else {
                    hasError = true
                }
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
    }
}

@Composable
fun PasswordSetupScreen(
    onBack: () -> Unit,
    onPasswordSet: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        ScreenHeader(onBack = onBack)

        Text(
            text = "Придумайте пароль",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Предупреждение при создании или смене пароля
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = HardRedAccent.copy(alpha = 0.35f)
        ) {
            Text(
                text = "⚠️ главный экран не блокируется паролем, пароль только на раздел (жесткая блокировка)!!! чтобы можно было всегда снять мягкую блокировку",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onPasswordSet(password)
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
    }
}
