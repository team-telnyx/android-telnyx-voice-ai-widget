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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * Audio visualizer component that shows bars representing audio levels
 * @param audioLevels List of 10 Float values (0.0-1.0) representing audio levels for each bar
 */
@Composable
fun AudioVisualizer(
    audioLevels: List<Float>,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary
) {
    // Ensure we always have exactly 10 values, padding with 0.0f if needed
    val normalizedAudioLevels = when {
        audioLevels.size >= 10 -> audioLevels.take(10)
        else -> audioLevels + List(10 - audioLevels.size) { 0.0f }
    }.map { it.coerceIn(0.0f, 1.0f) } // Ensure values are in 0.0-1.0 range
    
    Box(
        modifier = modifier.height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isActive) {
                drawAudioBars(
                    audioValues = normalizedAudioLevels,
                    color = color
                )
            } else {
                // Draw static bars when not active
                drawAudioBars(
                    audioValues = List(10) { 0.3f },
                    color = color.copy(alpha = 0.3f)
                )
            }
        }
    }
}

private fun DrawScope.drawAudioBars(
    audioValues: List<Float>,
    color: Color
) {
    val barCount = 10 // Fixed to 10 bars
    val barWidth = size.width / (barCount * 2 - 1) // Account for spacing
    val maxBarHeight = size.height * 0.8f
    val minBarHeight = size.height * 0.2f
    
    audioValues.forEachIndexed { index, audioValue ->
        val barHeight = minBarHeight + (maxBarHeight - minBarHeight) * audioValue
        val x = index * barWidth * 2
        val y = (size.height - barHeight) / 2
        
        drawRoundRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
        )
    }
}