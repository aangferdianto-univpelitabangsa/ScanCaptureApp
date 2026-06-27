package com.example.scancaptureapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppGradients {
    val primaryButton: Brush
        @Composable get() = Brush.horizontalGradient(
            colors = listOf(GradientDeepPink, GradientSoftCoral, GradientLightOrange)
        )

    val primaryButtonDisabled: Brush
        @Composable get() = Brush.horizontalGradient(
            colors = listOf(
                GradientDeepPink.copy(alpha = 0.45f),
                GradientSoftCoral.copy(alpha = 0.45f)
            )
        )

    val subtleCard: Brush
        @Composable get() = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.92f),
                Color.White.copy(alpha = 0.85f)
            )
        )
}
