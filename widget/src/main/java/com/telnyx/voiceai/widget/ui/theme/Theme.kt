package com.telnyx.voiceai.widget.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightTranscriptColors = TranscriptColors(
    backgroundColor = Color(0xFFFFFFFF)
)

private val DarkTranscriptColors = TranscriptColors(
    backgroundColor = Color(0xFF1C1B1F)
)

val LocalTranscriptColors = staticCompositionLocalOf { LightTranscriptColors }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4F46E5),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF64748B),
    onSecondary = Color.White,
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFE2E8F0),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4F46E5),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF64748B),
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF1E293B),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569)
)

// Define gradient color schemes
val audioGradients = mapOf(
    "verdant" to listOf(
        Color(0xFFD3FFA6), // Light green
        Color(0xFF036B5B), // Dark teal
        Color(0xFFD3FFA6), // Light green
    ),
    "twilight" to listOf(
        Color(0xFF81B9FF), // Light blue
        Color(0xFF371A5E), // Dark purple
        Color(0xFF81B9FF), // Light blue
    ),
    "bloom" to listOf(
        Color(0xFFFFD4FE), // Light pink
        Color(0xFFFD05F9), // Bright magenta
        Color(0xFFFFD4FE), // Light pink
    ),
    "mystic" to listOf(
        Color(0xFF1F023A), // Dark purple
        Color(0xFFCA76FF), // Light purple
        Color(0xFF1F023A), // Dark purple
    ),
    "flare" to listOf(
        Color(0xFFFFFFFF), // White
        Color(0xFFFC5F00), // Orange
        Color(0xFFFFFFFF), // White
    ),
    "glacier" to listOf(
        Color(0xFF4CE5F2), // Light cyan
        Color(0xFF005A98), // Dark blue
        Color(0xFF4CE5F2), // Light cyan
    )
)

@Composable
fun VoiceAIWidgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val transcriptColors = when {
        darkTheme -> DarkTranscriptColors
        else -> LightTranscriptColors
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalTranscriptColors provides transcriptColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
