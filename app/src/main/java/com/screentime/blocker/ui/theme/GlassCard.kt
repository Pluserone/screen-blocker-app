package com.screentime.blocker.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    backgroundColor: Color = GlassSurface,
    borderColor: Color = GlassBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Column(
        modifier = baseModifier
            .clip(shape)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, borderColor), shape)
            .padding(16.dp),
        content = content
    )
}
