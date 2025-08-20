package com.telnyx.voiceai.widget.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.telnyx.voiceai.widget.data.WidgetSettings
import com.telnyx.voiceai.widget.state.AgentStatus

/**
 * Expanded widget component showing audio visualizer and controls
 */
@Composable
fun ExpandedWidget(
    settings: WidgetSettings,
    isConnected: Boolean,
    isMuted: Boolean,
    agentStatus: AgentStatus,
    onToggleMute: () -> Unit,
    onEndCall: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                isActive = isConnected && agentStatus == AgentStatus.Waiting,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Status text
            Text(
                text = when (agentStatus) {
                    AgentStatus.Thinking -> settings.agentThinkingText ?: "Agent is thinking..."
                    AgentStatus.Waiting -> settings.speakToInterruptText ?: "Speak to interrupt"
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
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                
                // End call button
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}