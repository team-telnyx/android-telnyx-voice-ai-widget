package com.telnyx.voiceai.widget.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.telnyx.webrtc.sdk.model.WidgetSettings
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.max
import kotlin.random.Random

// Define gradient color schemes
private val audioGradients = mapOf(
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

// Configuration constants matching Flutter implementation
private const val BAR_COUNT = 12
private const val NOISE_FLOOR = 0.15f
private const val HISTORY_LENGTH = 5
private const val DECAY_RATE = 0.85f

/**
 * Amplify low audio levels for better visual response
 * Applies dynamic range compression and sensitivity boost
 */
private fun amplifyAudioLevel(level: Float): Float {
    // Apply dynamic range compression
    val boosted = level.pow(0.7f)
    // Boost sensitivity
    val amplified = boosted * 1.5f
    return min(1.0f, amplified)
}

/**
 * Process audio data to create frequency-like bar responses
 * Simulates different frequency bands responding differently to audio input
 */
private fun processAudioData(audioLevel: Float, barCount: Int = BAR_COUNT): List<Float> {
    val random = Random.Default
    val currentTime = System.currentTimeMillis()

    return List(barCount) { i ->
        // Create frequency-like response curves
        val frequency = i.toFloat() / (barCount - 1) // 0.0 to 1.0

        // Different frequency bands respond differently
        val response = when {
            frequency < 0.2f -> {
                // Bass: Strong response, slower decay
                audioLevel * (1.2f - frequency * 0.5f)
            }
            frequency < 0.6f -> {
                // Midrange: Moderate response with some randomness
                audioLevel * (0.8f + random.nextFloat() * 0.4f)
            }
            else -> {
                // Treble: Sharp response, quick changes
                val sineWave = sin((currentTime * 0.01 + i).toDouble()).toFloat()
                audioLevel * (0.6f + sineWave * 0.3f)
            }
        }

        // Add some random variation for organic feel
        val variedResponse = response * (0.8f + random.nextFloat() * 0.4f)

        // Ensure minimum activity and clamp
        max(NOISE_FLOOR, min(1.0f, variedResponse))
    }
}

/**
 * Audio visualizer component that shows bars representing audio levels
 * @param audioLevels List of 12 Float values (0.0-1.0) representing audio levels for each bar
 * @param settings Widget settings containing audio visualizer configuration
 */
@Composable
fun AudioVisualizer(
    audioLevels: List<Float>,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    audioBarColor: Color = MaterialTheme.colorScheme.primary,
    settings: WidgetSettings? = null
) {
    // Audio history for smoothing
    val audioHistory = remember { mutableStateListOf<Float>() }

    // Process audio data when it changes
    val processedAudioLevels = remember(audioLevels) {
        if (audioLevels.isEmpty()) {
            List(BAR_COUNT) { NOISE_FLOOR }
        } else {
            // Get the latest audio level
            val currentLevel = audioLevels.lastOrNull()?.coerceIn(0.0f, 1.0f) ?: 0.0f

            // Add to history
            audioHistory.add(currentLevel)

            // Keep history limited
            if (audioHistory.size > HISTORY_LENGTH) {
                audioHistory.removeAt(0)
            }

            // Calculate smoothed audio level
            val smoothedLevel = if (audioHistory.isNotEmpty()) {
                audioHistory.average().toFloat()
            } else {
                currentLevel
            }

            // Amplify low levels for better visual response
            val amplifiedLevel = amplifyAudioLevel(smoothedLevel)

            // Process into frequency bands
            processAudioData(amplifiedLevel, BAR_COUNT)
        }
    }

    // Get gradient colors from settings or use default
    val gradientColors = settings?.audioVisualizerConfig?.color?.let { colorName ->
        audioGradients[colorName]
    }

    Box(
        modifier = modifier.height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isActive) {
                if (gradientColors != null) {
                    drawAudioBarsWithGradient(
                        audioValues = processedAudioLevels,
                        gradientColors = gradientColors
                    )
                } else {
                    drawAudioBars(
                        audioValues = processedAudioLevels,
                        color = audioBarColor
                    )
                }
            } else {
                // Draw static bars when not active
                if (gradientColors != null) {
                    drawAudioBarsWithGradient(
                        audioValues = List(BAR_COUNT) { NOISE_FLOOR },
                        gradientColors = gradientColors.map { it.copy(alpha = 0.3f) }
                    )
                } else {
                    drawAudioBars(
                        audioValues = List(BAR_COUNT) { NOISE_FLOOR },
                        color = audioBarColor.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawAudioBars(
    audioValues: List<Float>,
    color: Color
) {
    val barCount = 12 // Fixed to 12 bars
    val spacingMultiplier = 0.3f // 70% reduction from original spacing (1.0 - 0.7 = 0.3)
    val barWidth = size.width / (barCount + (barCount - 1) * spacingMultiplier)
    val spacing = barWidth * spacingMultiplier
    val maxBarHeight = size.height * 0.8f
    val minBarHeight = size.height * 0.1f // 50% reduction from 0.2f

    audioValues.forEachIndexed { index, audioValue ->
        val barHeight = minBarHeight + (maxBarHeight - minBarHeight) * audioValue
        val x = index * (barWidth + spacing)
        val y = (size.height - barHeight) / 2

        drawRoundRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
        )
    }
}

private fun DrawScope.drawAudioBarsWithGradient(
    audioValues: List<Float>,
    gradientColors: List<Color>
) {
    val barCount = 12 // Fixed to 12 bars
    val spacingMultiplier = 0.3f // 70% reduction from original spacing (1.0 - 0.7 = 0.3)
    val barWidth = size.width / (barCount + (barCount - 1) * spacingMultiplier)
    val spacing = barWidth * spacingMultiplier
    val maxBarHeight = size.height * 0.8f
    val minBarHeight = size.height * 0.1f // 50% reduction from 0.2f

    audioValues.forEachIndexed { index, audioValue ->
        val barHeight = minBarHeight + (maxBarHeight - minBarHeight) * audioValue
        val x = index * (barWidth + spacing)
        val y = (size.height - barHeight) / 2

        // Create vertical gradient brush for each bar
        val gradientBrush = Brush.verticalGradient(
            colors = gradientColors,
            startY = y,
            endY = y + barHeight
        )

        drawRoundRect(
            brush = gradientBrush,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
        )
    }
}