package com.telnyx.voiceai.widget.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.telnyx.voiceai.widget.R
import com.telnyx.voiceai.widget.state.AgentStatus
import com.telnyx.voiceai.widget.state.TranscriptItem
import com.telnyx.webrtc.sdk.model.WidgetSettings

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
    modifier: Modifier = Modifier,
    iconOnly: Boolean = false
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
            onCollapse = onCollapse,
            iconOnly = iconOnly
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
    onCollapse: () -> Unit,
    iconOnly: Boolean = false
) {
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(transcriptItems.size) {
        if (transcriptItems.isNotEmpty()) {
            listState.animateScrollToItem(transcriptItems.size - 1)
        }
    }
    
    // Define custom colors based on theme
    val backgroundColor = if (!isSystemInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF1C1B1F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Title bar with close button (only show in regular mode)
        if (!iconOnly) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.conversation_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.collapse_button_description),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Horizontal divider line below title
            Divider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        // Upper section - Audio controls (30% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .background(Color.Transparent)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                ExpandedAudioSection(
                    isConnected = isConnected,
                    isMuted = isMuted,
                    agentStatus = agentStatus,
                    settings = settings,
                    audioLevels = audioLevels,
                    onToggleMute = onToggleMute,
                    onEndCall = onEndCall,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }
        
        // Lower section - Conversation area (50% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // Horizontal divider line
                Divider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Transcript messages
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(transcriptItems) { item ->
                        TranscriptMessage(
                            item = item,
                            settings = settings,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
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
    }
}

@Composable
private fun ExpandedAudioSection(
    isConnected: Boolean,
    isMuted: Boolean,
    agentStatus: AgentStatus,
    settings: WidgetSettings,
    audioLevels: List<Float>,
    onToggleMute: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Audio visualizer - prominently in the middle
        AudioVisualizer(
            audioLevels = audioLevels,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            isActive = isConnected && agentStatus == AgentStatus.Waiting,
            audioBarColor = MaterialTheme.colorScheme.primary,
            settings = settings
        )

        // Agent status text
        Text(
            text = when (agentStatus) {
                AgentStatus.Idle -> stringResource(R.string.default_agent_idle_text)
                AgentStatus.Thinking -> settings.agentThinkingText ?: stringResource(R.string.default_agent_thinking_text)
                AgentStatus.Waiting -> settings.speakToInterruptText ?: stringResource(R.string.default_speak_to_interrupt_text)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Control buttons underneath
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute/Unmute button
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isMuted) stringResource(R.string.unmute_button_description) else stringResource(R.string.mute_button_description),
                    tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // End call button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = stringResource(R.string.end_call_button_description),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun TranscriptMessage(
    item: TranscriptItem,
    settings: WidgetSettings,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (item.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!item.isUser) {
            // Agent message with avatar on left
            // Avatar
            if (!settings.logoIconUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(settings.logoIconUrl)
                        .crossfade(false)
                        .fallback(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .build(),
                    contentDescription = "Agent avatar",
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.default_avatar),
                    contentDescription = "Agent avatar",
                    modifier = Modifier.size(32.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Message card
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                Text(
                    text = item.text,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // User message with avatar on right
            // Message card
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                Text(
                    text = item.text,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Avatar
            Image(
                painter = painterResource(R.drawable.person),
                contentDescription = "User avatar",
                modifier = Modifier.size(32.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@Composable
private fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
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
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor,
                disabledContainerColor = backgroundColor
            )
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
