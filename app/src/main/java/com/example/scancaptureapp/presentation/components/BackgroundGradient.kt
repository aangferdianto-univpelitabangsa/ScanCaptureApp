package com.example.scancaptureapp.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.scancaptureapp.ui.theme.GradientCream
import com.example.scancaptureapp.ui.theme.GradientDeepPink
import com.example.scancaptureapp.ui.theme.GradientLightOrange
import com.example.scancaptureapp.ui.theme.GradientPink
import com.example.scancaptureapp.ui.theme.GradientSoftCoral
import com.example.scancaptureapp.ui.theme.GradientCreamDark
import com.example.scancaptureapp.ui.theme.GradientDeepPinkDark
import com.example.scancaptureapp.ui.theme.GradientLightOrangeDark
import com.example.scancaptureapp.ui.theme.GradientPinkDark
import com.example.scancaptureapp.ui.theme.GradientSoftCoralDark

/**
 * Soft polygon-style gradient background with layered radial blooms.
 * Place screen content in [content] so it draws above the gradient.
 */
@Composable
fun BackgroundGradient(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "polygonGradient")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    val motion = if (animated) drift else 0.5f

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val height = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        val baseColors = if (darkTheme) darkBaseGradient else lightBaseGradient
        val bloomPink = if (darkTheme) GradientPinkDark else GradientPink
        val bloomCoral = if (darkTheme) GradientSoftCoralDark else GradientSoftCoral
        val bloomOrange = if (darkTheme) GradientLightOrangeDark else GradientLightOrange
        val bloomDeep = if (darkTheme) GradientDeepPinkDark else GradientDeepPink
        val veil = if (darkTheme) GradientCreamDark.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.18f)

        val shiftX = (motion - 0.5f) * width * 0.12f
        val shiftY = (0.5f - motion) * height * 0.08f

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = baseColors,
                        start = Offset(shiftX, shiftY),
                        end = Offset(width + shiftX * 0.5f, height + shiftY * 0.5f)
                    )
                )
        )

        // Polygon-like facets — soft radial overlays
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            bloomCoral.copy(alpha = if (darkTheme) 0.35f else 0.42f),
                            Color.Transparent
                        ),
                        center = Offset(width * (0.78f + motion * 0.06f), height * (0.12f + motion * 0.04f)),
                        radius = width * 0.85f
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            bloomPink.copy(alpha = if (darkTheme) 0.28f else 0.38f),
                            Color.Transparent
                        ),
                        center = Offset(width * (0.18f - motion * 0.05f), height * (0.72f - motion * 0.03f)),
                        radius = width * 0.9f
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            bloomOrange.copy(alpha = if (darkTheme) 0.22f else 0.32f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.52f, height * (0.48f + motion * 0.06f)),
                        radius = width * 0.65f
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            bloomDeep.copy(alpha = if (darkTheme) 0.18f else 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(width * (0.92f - motion * 0.04f), height * (0.88f - motion * 0.05f)),
                        radius = width * 0.55f
                    )
                )
        )

        // Soft center veil — keeps foreground text readable
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(veil, Color.Transparent),
                        center = Offset(width * 0.5f, height * 0.42f),
                        radius = width * 1.1f
                    )
                )
        )

        content()
    }
}

private val lightBaseGradient = listOf(
    GradientCream,
    GradientLightOrange,
    GradientSoftCoral,
    GradientPink,
    GradientDeepPink.copy(alpha = 0.92f)
)

private val darkBaseGradient = listOf(
    GradientCreamDark,
    GradientLightOrangeDark,
    GradientSoftCoralDark,
    GradientPinkDark,
    GradientDeepPinkDark.copy(alpha = 0.95f)
)

/**
 * Full-screen wrapper: gradient behind, [content] on top.
 */
@Composable
fun GradientScreenBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    content: @Composable () -> Unit
) {
    BackgroundGradient(modifier = modifier, animated = animated) {
        content()
    }
}
