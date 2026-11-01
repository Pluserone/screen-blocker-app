package com.screentime.blocker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Строго одинаковый размер шрифта для всех текстов, лимитов и кнопок
val StrictUniformTextStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 21.sp,
    color = TextPrimary
)

val UniformTypography = Typography(
    displayLarge = StrictUniformTextStyle,
    displayMedium = StrictUniformTextStyle,
    displaySmall = StrictUniformTextStyle,
    headlineLarge = StrictUniformTextStyle,
    headlineMedium = StrictUniformTextStyle,
    headlineSmall = StrictUniformTextStyle,
    titleLarge = StrictUniformTextStyle,
    titleMedium = StrictUniformTextStyle,
    titleSmall = StrictUniformTextStyle,
    bodyLarge = StrictUniformTextStyle,
    bodyMedium = StrictUniformTextStyle,
    bodySmall = StrictUniformTextStyle,
    labelLarge = StrictUniformTextStyle,
    labelMedium = StrictUniformTextStyle,
    labelSmall = StrictUniformTextStyle
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BlackBackground,
            surface = GlassSurface,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        ),
        typography = UniformTypography,
        content = content
    )
}
