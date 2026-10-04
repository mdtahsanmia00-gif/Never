package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonViolet,
    onPrimary = DeepSpace,
    primaryContainer = CosmicSurfaceCard,
    onPrimaryContainer = NeonVioletLight,
    secondary = ElectricCyan,
    onSecondary = DeepSpace,
    secondaryContainer = CosmicSurfaceBorder,
    onSecondaryContainer = ElectricTeal,
    tertiary = HologramPink,
    background = DeepSpace,
    onBackground = TextPrimary,
    surface = DeepSpaceLighter,
    onSurface = TextPrimary,
    surfaceVariant = CosmicSurface,
    onSurfaceVariant = TextSecondary,
    outline = CosmicSurfaceBorder
)

@Composable
fun NeverForgetTheme(
    darkTheme: Boolean = true, // Default to gorgeous cosmic dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepSpace.toArgb()
            window.navigationBarColor = DeepSpace.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
