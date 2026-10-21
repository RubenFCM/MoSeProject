package com.example.moseproject.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CinemaColors = darkColorScheme(
    primary = CinemaAccent,
    onPrimary = Color(0xFF062B2B),
    primaryContainer = Color(0xFF163C40),
    onPrimaryContainer = CinemaAccent,
    secondary = CinemaAccent,
    secondaryContainer = CinemaElevated,
    onSecondaryContainer = CinemaAccent,
    background = CinemaBackground,
    onBackground = CinemaText,
    surface = CinemaSurface,
    onSurface = CinemaText,
    surfaceVariant = CinemaElevated,
    onSurfaceVariant = CinemaMuted,
    outline = CinemaOutline
)

@Composable
fun MoSeProjectTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CinemaBackground.toArgb()
            window.navigationBarColor = CinemaBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(colorScheme = CinemaColors, typography = Typography, content = content)
}
