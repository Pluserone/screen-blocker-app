package com.screentime.blocker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val defaultAppCatalog = remember {
        listOf(
            "com.google.android.youtube" to "Ютуб",
            "com.zhiliaoapp.musically" to "тик Ток",
            "org.telegram.messenger" to "телеграмм",
            "com.example.smile" to "смайлик",
            "com.example.other" to "и так далее",
            "com.google.android.googlequicksearchbox" to "гугл",
            "com.shopping.app" to "покупки",
            "com.game.app" to "игры"
        )
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

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(defaultAppCatalog) { (pkg, name) ->
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
