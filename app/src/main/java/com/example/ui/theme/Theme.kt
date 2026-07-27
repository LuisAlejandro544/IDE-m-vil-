package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DevStudioDarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = EditorPanelHeader,
    onPrimaryContainer = CodeTextPrimary,
    secondary = SoftGreen,
    onSecondary = Color.White,
    tertiary = SoftAmber,
    background = EditorBackground,
    onBackground = CodeTextPrimary,
    surface = EditorSurface,
    onSurface = CodeTextPrimary,
    surfaceVariant = EditorPanelHeader,
    onSurfaceVariant = CodeTextSecondary,
    outline = EditorBorder,
    outlineVariant = LineNumberColor
)

private val DevStudioLightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = SoftGreen,
    onSecondary = Color.White,
    tertiary = SoftAmber,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFF94A3B8)
)

@Composable
fun DevStudioTheme(
    darkTheme: Boolean = true, // Default to dark IDE theme for developer comfort
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DevStudioDarkColorScheme else DevStudioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
