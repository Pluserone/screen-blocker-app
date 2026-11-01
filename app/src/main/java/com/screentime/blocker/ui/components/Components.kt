package com.screentime.blocker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.screentime.blocker.ui.theme.TextPrimary
import com.screentime.blocker.ui.theme.TextSecondary

@Composable
fun ScreenHeader(
    title: String? = null,
    onBack: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier.clickable { onBack() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "назад",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "назад",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
        if (title != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun InlineNumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    step: Int = 1,
    min: Int = 0,
    isInfinityAllowed: Boolean = false,
    suffix: String = ""
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "–",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier
                .clickable {
                    if (value == -1) onValueChange(60)
                    else onValueChange((value - step).coerceAtLeast(min))
                }
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )

        val displayText = when {
            value == -1 && isInfinityAllowed -> "∞"
            else -> "$value"
        }

        Text(
            text = if (suffix.isNotEmpty()) "$displayText $suffix" else displayText,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier
                .clickable { showDialog = true }
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Text(
            text = "+",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier
                .clickable {
                    if (value == -1) onValueChange(0)
                    else onValueChange(value + step)
                }
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }

    if (showDialog) {
        var input by remember { mutableStateOf(if (value == -1) "0" else value.toString()) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Введите значение", style = MaterialTheme.typography.bodyLarge) },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter { ch -> ch.isDigit() || ch == '-' } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    input.toIntOrNull()?.let { onValueChange(it) }
                    showDialog = false
                }) {
                    Text("применить", style = MaterialTheme.typography.bodyLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("отмена", style = MaterialTheme.typography.bodyLarge)
                }
            }
        )
    }
}
