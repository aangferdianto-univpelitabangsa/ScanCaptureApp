package com.example.scancaptureapp.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Design system spacing: 8 / 16 / 24 / 32 dp */
object AppSpacing {
    val small = 8.dp
    val standard = 16.dp
    val section = 24.dp
    val large = 32.dp

    // Legacy aliases
    val screen = standard
    val item = 12.dp
}

val AppShape = RoundedCornerShape(16.dp)
val AppShapeLarge = RoundedCornerShape(20.dp)
val AppButtonShape = RoundedCornerShape(50.dp)

@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "pressScale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun FadeInContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label = "fadeIn"
    )
    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(AppShape)
    ) {
        content()
    }
}
