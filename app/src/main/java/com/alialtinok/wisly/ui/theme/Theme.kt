package com.alialtinok.wisly.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val WislyDarkScheme = darkColorScheme(
    primary = WislyColors.Primary,
    onPrimary = Color.White,
    secondary = WislyColors.AccentPurple,
    onSecondary = Color.White,
    tertiary = WislyColors.AccentAmber,
    onTertiary = Color.Black,
    background = WislyColors.Background,
    onBackground = Color.White,
    surface = WislyColors.Surface,
    onSurface = Color.White,
    surfaceVariant = WislyColors.Surface,
    onSurfaceVariant = WislyColors.OnSurfaceMuted,
    outline = WislyColors.SurfaceBorder,
    error = WislyColors.AccentRed,
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
