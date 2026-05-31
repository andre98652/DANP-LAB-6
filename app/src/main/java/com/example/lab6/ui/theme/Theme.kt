package com.example.lab6.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = SoftSage,
    onPrimary = DarkSlateBg,
    primaryContainer = DeepForest,
    onPrimaryContainer = Color.White,
    secondary = SoftOrange,
    onSecondary = DarkSlateBg,
    background = DarkSlateBg,
    surface = DarkCardSurface,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    error = Color(0xFFEF4444)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.White,
    primaryContainer = PaleMint,
    onPrimaryContainer = DeepForest,
    secondary = WarmAmber,
    onSecondary = Color.White,
    background = LightSlateBg,
    surface = LightCardSurface,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF1E293B),
    error = Color(0xFFEF4444)
)

@Composable
fun Lab6Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}