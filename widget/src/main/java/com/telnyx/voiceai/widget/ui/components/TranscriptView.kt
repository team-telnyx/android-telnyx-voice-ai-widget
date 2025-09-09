package com.telnyx.voiceai.widget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.telnyx.voiceai.widget.R
import com.telnyx.voiceai.widget.state.AgentStatus
import com.telnyx.voiceai.widget.state.TranscriptItem
import com.telnyx.webrtc.sdk.model.WidgetSettings
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full transcript view component displayed as fullscreen dialog overlay
 */
@Composable
fun TranscriptView(
    settings: WidgetSettings,
    transcriptItems: List<TranscriptItem>,
    userInput: String,
    isConnected: Boolean,
    isMuted: Boolean,
    agentStatus: AgentStatus,
    audioLevels: List<Float>,
    onUserInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onToggleMute: () -> Unit,
    onEndCall: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onCollapse,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        TranscriptDialogContent(
            settings = settings,
            transcriptItems = transcriptItems,
            userInput = userInput,
            isConnected = isConnected,
            isMuted = isMuted,
            agentStatus = agentStatus,
            audioLevels = audioLevels,
            onUserInputChange = onUserInputChange,
            onSendMessage = onSendMessage,
            onToggleMute = onToggleMute,
            onEndCall = onEndCall,
            onCollapse = onCollapse
        )
    }
}

@Composable
private fun TranscriptDialogContent(
    settings: WidgetSettings,
    transcriptItems: List<TranscriptItem>,
    userInput: String,
    isConnected: Boolean,
    isMuted: Boolean,
    agentStatus: AgentStatus,
    audioLevels: List<Float>,
    onUserInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onToggleMute: () -> Unit,
    onEndCall: () -> Unit,
    onCollapse: () -> Unit
) {
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(transcriptItems.size) {
        if (transcriptItems.isNotEmpty()) {
            listState.animateScrollToItem(transcriptItems.size - 1)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header with controls
        TranscriptHeader(
            isConnected = isConnected,
            isMuted = isMuted,
            agentStatus = agentStatus,
            settings = settings,
            audioLevels = audioLevels,
            onToggleMute = onToggleMute,
            onEndCall = onEndCall,
            onCollapse = onCollapse
        )
        
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        
        // Transcript messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(transcriptItems) { item ->
                TranscriptMessage(
                    item = item,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        
        // Input field
        MessageInput(
            value = userInput,
            onValueChange = onUserInputChange,
            onSend = onSendMessage,
            enabled = isConnected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun TranscriptHeader(
    isConnected: Boolean,
    isMuted: Boolean,
    agentStatus: AgentStatus,
    settings: WidgetSettings,
    audioLevels: List<Float>,
    onToggleMute: () -> Unit,
    onEndCall: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Collapse button
        IconButton(onClick = onCollapse) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.collapse_button_description),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Status and visualizer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AudioVisualizer(
                audioLevels,
                modifier = Modifier
                    .width(80.dp)
                    .height(24.dp),
                isActive = isConnected && agentStatus == AgentStatus.Waiting,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = when (agentStatus) {
                    AgentStatus.Thinking -> settings.agentThinkingText ?: stringResource(R.string.default_agent_thinking_text)
                    AgentStatus.Waiting -> settings.speakToInterruptText ?: stringResource(R.string.default_speak_to_interrupt_text)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onToggleMute) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isMuted) stringResource(R.string.unmute_button_description) else stringResource(R.string.mute_button_description),
                    tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = onEndCall) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = stringResource(R.string.end_call_button_description),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TranscriptMessage(
    item: TranscriptItem,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = modifier,
        horizontalArrangement = if (item.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!item.isUser) {
            // Agent message
            Column(
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Card(
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = item.text,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = timeFormat.format(Date(item.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        } else {
            // User message
            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.End
            ) {
                Card(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = item.text,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Text(
                    text = timeFormat.format(Date(item.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = if (enabled) stringResource(R.string.message_input_placeholder) else stringResource(R.string.message_input_disconnected),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            shape = RoundedCornerShape(24.dp),
            maxLines = 3
        )
        
        IconButton(
            onClick = onSend,
            enabled = enabled && value.trim().isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = stringResource(R.string.send_button_description),
                tint = if (enabled && value.trim().isNotEmpty()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                }
            )
        }
    }
}