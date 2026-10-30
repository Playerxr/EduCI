package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Vibrant Ivory Coast Orange & Deep Blue Palette
val EduOrange = Color(0xFFE65100)
val EduOrangeSecondary = Color(0xFFFF9800)
val EduBlueNavy = Color(0xFF0F2027)
val EduEmeraldGreen = Color(0xFF2E7D32)
val EduLightBg = Color(0xFFF8F9FA)
val EduCardBg = Color(0xFFFFFFFF)
val EduTextPrimary = Color(0xFF1E293B)
val EduTextSecondary = Color(0xFF64748B)

private val LightColorScheme = lightColorScheme(
    primary = EduOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF4A1500),
    secondary = EduBlueNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = EduEmeraldGreen,
    onTertiary = Color.White,
    background = EduLightBg,
    onBackground = EduTextPrimary,
    surface = EduCardBg,
    onSurface = EduTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = EduTextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF9800),
    onPrimary = Color(0xFF1E1000),
    primaryContainer = Color(0xFF3E1D00),
    onPrimaryContainer = Color(0xFFFFDCBE),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF001E2B),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFF4ADE80),
    onTertiary = Color(0xFF00210B),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

@Composable
fun EduCITheme(
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
