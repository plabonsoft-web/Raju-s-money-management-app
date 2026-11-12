package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = SumiCardDark,
    onPrimaryContainer = DarkPrimary,
    secondary = DarkSecondary,
    background = SumiBackgroundDark,
    surface = SumiSurfaceDark,
    surfaceVariant = SumiCardDark,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    outline = SumiOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = CharcoalPrimary,
    onPrimary = CharcoalOnPrimary,
    primaryContainer = WashiCardLight,
    onPrimaryContainer = CharcoalPrimary,
    secondary = SandSecondary,
    background = WashiPaperLight,
    surface = WashiSurfaceLight,
    surfaceVariant = WashiCardLight,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
    outline = WashiOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
