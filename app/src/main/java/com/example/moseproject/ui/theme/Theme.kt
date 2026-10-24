package com.example.moseproject.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
private fun cinemaColors() = darkColorScheme(
    primary = CinemaAccent,
    onPrimary = CinemaBackground,
    primaryContainer = CinemaSelection,
    onPrimaryContainer = CinemaAccent,
    secondary = CinemaAccent,
    onSecondary = CinemaBackground,
    secondaryContainer = CinemaSelection,
    onSecondaryContainer = CinemaAccent,
    tertiary = CinemaAccent,
    onTertiary = CinemaBackground,
    tertiaryContainer = CinemaSelection,
    onTertiaryContainer = CinemaAccent,
    background = CinemaBackground,
    onBackground = CinemaText,
    surface = CinemaSurface,
    onSurface = CinemaText,
    surfaceVariant = CinemaElevated,
    onSurfaceVariant = CinemaMuted,
    surfaceTint = CinemaSurface,
    inverseSurface = CinemaSurface,
    inverseOnSurface = CinemaText,
    inversePrimary = CinemaBackground,
    outline = CinemaOutline,
    outlineVariant = CinemaOutline,
    scrim = CinemaBackground
)

@Composable
fun MoSeProjectTheme(content: @Composable () -> Unit) {
    val colors = cinemaColors()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(colorScheme = colors, typography = Typography, content = content)
}
