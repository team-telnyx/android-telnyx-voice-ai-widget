package com.telnyx.voiceai.widget.ui.components

import androidx.compose.animation.core.*
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
import kotlin.math.sin
import kotlin.random.Random

/**
 * Audio visualizer component that shows animated bars representing audio levels
 */
@Composable
fun AudioVisualizer(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 5
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer")
    
    // Create animated values for each bar
    val animatedValues = remember(barCount) {
        (0 until barCount).map { index ->
            infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 800 + (index * 100),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
        }
    }
    
    Box(
        modifier = modifier.height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isActive) {
                drawAudioBars(
                    animatedValues = animatedValues.map { it.value },
                    color = color,
                    barCount = barCount
                )
            } else {
                // Draw static bars when not active
                drawAudioBars(
                    animatedValues = List(barCount) { 0.3f },
                    color = color.copy(alpha = 0.3f),
                    barCount = barCount
                )
            }
        }
    }
}

private fun DrawScope.drawAudioBars(
    animatedValues: List<Float>,
    color: Color,
    barCount: Int
) {
    val barWidth = size.width / (barCount * 2 - 1) // Account for spacing
    val maxBarHeight = size.height * 0.8f
    val minBarHeight = size.height * 0.2f
    
    animatedValues.forEachIndexed { index, animatedValue ->
        val barHeight = minBarHeight + (maxBarHeight - minBarHeight) * animatedValue
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