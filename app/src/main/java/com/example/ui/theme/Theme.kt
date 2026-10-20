package com.example.ui.theme

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
    primary = KpGreenPrimary,
    onPrimary = KpSurface,
    primaryContainer = KpGreenUltralight,
    onPrimaryContainer = KpGreenDark,
    secondary = KpBlueData,
    onSecondary = KpSurface,
    secondaryContainer = KpBlueLight,
    onSecondaryContainer = KpBlueData,
    tertiary = KpYellowAccent,
    onTertiary = KpSurface,
    tertiaryContainer = KpYellowLight,
    onTertiaryContainer = KpYellowAccent,
    background = KpBackground,
    onBackground = KpTextPrimary,
    surface = KpSurface,
    onSurface = KpTextPrimary,
    surfaceVariant = KpSurfaceVariant,
    onSurfaceVariant = KpTextSecondary,
    outline = KpBorder,
    error = KpRedAlert,
    onError = KpSurface,
    errorContainer = KpRedLight,
    onErrorContainer = KpRedDark
)

private val DarkColorScheme = darkColorScheme(
    primary = KpGreenAccent,
    onPrimary = KpTextPrimary,
    primaryContainer = KpGreenDark,
    onPrimaryContainer = KpGreenLight,
    secondary = KpBlueAccent,
    onSecondary = KpTextPrimary,
    secondaryContainer = KpBlueData,
    onSecondaryContainer = KpBlueLight,
    tertiary = KpYellowWarning,
    onTertiary = KpTextPrimary,
    tertiaryContainer = KpYellowAccent,
    onTertiaryContainer = KpYellowLight,
    background = KpTextPrimary,
    onBackground = KpSurfaceVariant,
    surface = Color(0xFF1E293B),
    onSurface = KpSurfaceVariant,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    error = KpRedAlert,
    onError = KpSurface,
    errorContainer = KpRedDark,
    onErrorContainer = KpRedLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
