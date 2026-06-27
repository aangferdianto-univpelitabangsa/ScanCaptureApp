package com.example.scancaptureapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRose,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4EC),
    onPrimaryContainer = Color(0xFF6B2438),
    secondary = SecondaryRose,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE8F0),
    onSecondaryContainer = Color(0xFF5C2D45),
    tertiary = TertiaryPeach,
    onTertiary = Color(0xFF3D2A32),
    tertiaryContainer = Color(0xFFFFEDE4),
    onTertiaryContainer = Color(0xFF4A2E32),
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRoseDark,
    onPrimary = Color(0xFF3D1524),
    primaryContainer = Color(0xFF5C2D45),
    onPrimaryContainer = Color(0xFFFFE4EC),
    secondary = SecondaryRoseDark,
    onSecondary = Color(0xFF3D1524),
    secondaryContainer = Color(0xFF4A2E3A),
    onSecondaryContainer = Color(0xFFFFE8F0),
    tertiary = TertiaryPeach,
    onTertiary = Color(0xFF2A1F26),
    tertiaryContainer = Color(0xFF4A2E32),
    onTertiaryContainer = Color(0xFFFFEDE4),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

@Composable
fun ScanCaptureAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val statusColor = if (darkTheme) GradientCreamDark else GradientCream
            window.statusBarColor = statusColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
