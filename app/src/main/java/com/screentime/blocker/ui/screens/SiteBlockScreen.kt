package com.screentime.blocker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.screentime.blocker.data.local.BlockedSiteEntity
import com.screentime.blocker.ui.components.ScreenHeader
import com.screentime.blocker.ui.theme.*

@Composable
fun SiteBlockScreen(
    sites: List<BlockedSiteEntity>,
    onBack: () -> Unit,
    onAddSite: (String) -> Unit,
    onDeleteSite: (BlockedSiteEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var inputUrl by remember { mutableStateOf("https://") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 76.dp)
        ) {
            ScreenHeader(title = "Блокировка сайтов", onBack = onBack)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sites, key = { it.id }) { site ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = site.url,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDeleteSite(site) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "удалить",
                                    tint = HardRedAccent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassButtonBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "добавить",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassButtonBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "выбрано ${sites.size} сайта -> применить",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }

    val clipboardManager = LocalClipboardManager.current

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF1E1E22),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Добавить сайт", style = MaterialTheme.typography.bodyLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Вставьте или введите адрес сайта:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        trailingIcon = {
                            IconButton(onClick = {
                                clipboardManager.getText()?.text?.let { clipText ->
                                    inputUrl = clipText.trim()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Вставить ссылку",
                                    tint = TextSecondary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (inputUrl.isNotBlank() && inputUrl != "https://") {
                        onAddSite(inputUrl)
                        inputUrl = "https://"
                        showAddDialog = false
                    }
                }) {
                    Text("добавить", style = MaterialTheme.typography.bodyLarge, color = SoftGreenAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("отмена", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                }
            }
        )
    }
}
