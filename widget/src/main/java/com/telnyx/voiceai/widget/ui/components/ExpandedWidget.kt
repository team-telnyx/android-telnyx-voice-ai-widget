package com.telnyx.voiceai.widget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.telnyx.voiceai.widget.R
import com.telnyx.voiceai.widget.state.AgentStatus
import com.telnyx.webrtc.sdk.model.WidgetSettings

/**
 * Expanded widget component showing audio visualizer and controls
 */
@Composable
fun ExpandedWidget(
    settings: WidgetSettings,
    isConnected: Boolean,
    isMuted: Boolean,
    agentStatus: AgentStatus,
    audioLevels: List<Float>,
    onToggleMute: () -> Unit,
    onEndCall: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("expanded_widget")
            .clickable { onTap() }
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Audio visualizer
            AudioVisualizer(
                audioLevels = audioLevels,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                isActive = isConnected && agentStatus == AgentStatus.Waiting,
                audioBarColor = MaterialTheme.colorScheme.primary,
                settings = settings
            )
            
            // Status text
            Text(
                text = when (agentStatus) {
                    AgentStatus.Idle -> stringResource(R.string.default_agent_idle_text)
                    AgentStatus.Thinking -> if (settings.agentThinkingText?.isNullOrEmpty() == false) settings.agentThinkingText!! else stringResource(R.string.default_agent_thinking_text)
                    AgentStatus.Waiting -> if (settings.speakToInterruptText?.isNullOrEmpty() == false) settings.speakToInterruptText!! else stringResource(R.string.default_speak_to_interrupt_text)
                    AgentStatus.ProcessingImage -> stringResource(R.string.default_processing_image_text)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            
            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute/Unmute button
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isMuted) stringResource(R.string.unmute_button_description) else stringResource(R.string.mute_button_description),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // End call button
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .testTag("end_call_button")
                        .size(48.dp)
                        .background(
                            color = Color.Red,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = stringResource(R.string.end_call_button_description),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
