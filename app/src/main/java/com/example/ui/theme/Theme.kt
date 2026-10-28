package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyDark,
    onPrimaryContainer = Color.White,
    secondary = ExpressOrange,
    onSecondary = Color.White,
    secondaryContainer = ExpressOrangeContainer,
    onSecondaryContainer = ExpressOrange,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = TextDark,
    surface = CardBackground,
    onSurface = TextDark,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextMuted,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = NavyLight,
    onPrimary = NavyDark,
    primaryContainer = NavyPrimary,
    onPrimaryContainer = Color.White,
    secondary = ExpressOrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = ExpressOrange,
    onSecondaryContainer = Color.White,
    tertiary = SuccessGreen,
    onTertiary = Color.Black,
    background = NavyDark,
    onBackground = Color.White,
    surface = Color(0xFF1E293B),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569)
)

@Composable
fun DeliveryCheckerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
