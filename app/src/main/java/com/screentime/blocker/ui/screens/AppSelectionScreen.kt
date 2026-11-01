package com.screentime.blocker.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.screentime.blocker.data.local.BlockMode
import com.screentime.blocker.data.local.BlockedAppEntity
import com.screentime.blocker.ui.components.InlineNumberStepper
import com.screentime.blocker.ui.components.ScreenHeader
import com.screentime.blocker.ui.theme.*

@Composable
fun AppSelectionScreen(
    mode: BlockMode,
    blockedApps: List<BlockedAppEntity>,
    onBack: () -> Unit,
    onToggleApp: (BlockedAppEntity) -> Unit,
    onUpdateLimit: (BlockedAppEntity, Int) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager

    // Считываем только реальные приложения, установленные на телефоне пользователя
    val installedApps = remember {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        resolveInfos.mapNotNull { ri ->
            val pkg = ri.activityInfo.packageName
            if (pkg == context.packageName) null
            else {
                val label = ri.loadLabel(pm).toString()
                pkg to label
            }
        }.distinctBy { it.first }
         .sortedBy { it.second.lowercase() }
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.second.contains(searchQuery, ignoreCase = true) ||
            it.first.contains(searchQuery, ignoreCase = true)
        }
    }

    val selectedMap = remember(blockedApps) {
        blockedApps.associateBy { it.packageName }
    }

    val selectedCount = blockedApps.count { it.isSelected }

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
            ScreenHeader(title = "Выберите приложения", onBack = onBack)

            // Поисковая строка для удобного поиска среди установленных приложений
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Поиск среди установленных приложений...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = TextSecondary
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.first }) { (pkg, name) ->
                    val appEntity = selectedMap[pkg] ?: BlockedAppEntity(
                        mode = mode,
                        packageName = pkg,
                        appName = name,
                        dailyLimitMinutes = 30,
                        isSelected = false
                    )

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onToggleApp(appEntity) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = appEntity.isSelected,
                                onCheckedChange = { onToggleApp(appEntity) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (mode == BlockMode.SOFT) SoftGreenAccent else HardRedAccent
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = appEntity.appName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            if (appEntity.isSelected) {
                                InlineNumberStepper(
                                    value = appEntity.dailyLimitMinutes,
                                    onValueChange = { onUpdateLimit(appEntity, it) },
                                    step = 10,
                                    isInfinityAllowed = true
                                )
                            }
                        }
                    }
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
                    text = "выбрано $selectedCount приложений -> применить",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}
