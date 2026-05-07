package com.alialtinok.lexiup.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val WislyDarkScheme = darkColorScheme(
    primary = LexiColors.Primary,
    onPrimary = Color.White,
    secondary = LexiColors.AccentPurple,
    onSecondary = Color.White,
    tertiary = LexiColors.AccentAmber,
    onTertiary = Color.Black,
    background = LexiColors.Background,
    onBackground = Color.White,
    surface = LexiColors.Surface,
    onSurface = Color.White,
    surfaceVariant = LexiColors.Surface,
    onSurfaceVariant = LexiColors.OnSurfaceMuted,
    outline = LexiColors.SurfaceBorder,
    error = LexiColors.AccentRed,
)

@Composable
fun WislyTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = WislyDarkScheme,
        typography = Typography,
        content = content,
    )
}
