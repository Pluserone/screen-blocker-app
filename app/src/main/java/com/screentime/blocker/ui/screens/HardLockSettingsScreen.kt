package com.screentime.blocker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.screentime.blocker.data.local.ModeSettingsEntity
import com.screentime.blocker.ui.components.InlineNumberStepper
import com.screentime.blocker.ui.components.ScreenHeader
import com.screentime.blocker.ui.theme.*

@Composable
fun HardLockSettingsScreen(
    settings: ModeSettingsEntity,
    onBack: () -> Unit,
    onDaysChange: (Int) -> Unit,
    onSelectApps: () -> Unit,
    onSelectSites: () -> Unit,
    onOpenPasswordSetup: () -> Unit,
    onToggleAdmin: (Boolean) -> Unit,
    onValidateAndStart: () -> Pair<Boolean, String?>,
    onStopLock: () -> Unit
) {
    val context = LocalContext.current

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
                .background(if (settings.isLockActive) HardRed.copy(alpha = 0.5f) else HardRed)
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "жесткая блокировка",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                if (settings.isLockActive) {
                    Text(
                        text = "активна",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HardRedAccent
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

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenPasswordSetup
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "поставить пароль (можно попросить придумать друга чтобы не сорваться)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = settings.isPasswordEnabled,
                    onCheckedChange = { onOpenPasswordSetup() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HardRedAccent,
                        checkedTrackColor = HardRed.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "дать права администратора приложению (вы не сможете удалить его пока не введете пароль или не пройдут временные ограничения)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = settings.isAdminPermissionEnabled,
                    onCheckedChange = onToggleAdmin,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HardRedAccent,
                        checkedTrackColor = HardRed.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (settings.isLockActive) {
                    onStopLock()
                } else {
                    val (isValid, errorMsg) = onValidateAndStart()
                    if (!isValid) {
                        Toast.makeText(context, errorMsg ?: "Ошибка", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (settings.isLockActive) GlassButtonBg else HardRed
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
