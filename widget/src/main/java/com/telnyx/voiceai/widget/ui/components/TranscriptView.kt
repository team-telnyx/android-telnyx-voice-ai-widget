package com.telnyx.voiceai.widget.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.telnyx.voiceai.widget.R
import com.telnyx.voiceai.widget.state.AgentStatus
import com.telnyx.voiceai.widget.state.TranscriptItem
import com.telnyx.voiceai.widget.ui.theme.LocalTranscriptColors
import com.telnyx.voiceai.widget.ui.theme.VoiceAIWidgetTheme
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

    // Get custom colors from theme
    val transcriptColors = LocalTranscriptColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(transcriptColors.topSectionColor)
    ) {
        // Close button in top right (only show in regular mode)
        if (!iconOnly) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.collapse_button_description),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Upper section - Audio controls (30% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
        ) {
            ExpandedAudioSection(
                isConnected = isConnected,
                isMuted = isMuted,
                agentStatus = agentStatus,
                settings = settings,
                audioLevels = audioLevels,
                onToggleMute = onToggleMute,
                onEndCall = onEndCall,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Lower section - Conversation area (70% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .background(
                    color = transcriptColors.bottomSectionColor,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp)
            ) {
                // Transcript messages
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    backgroundColor = transcriptColors.textBoxColor,
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
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isMuted) stringResource(R.string.unmute_button_description) else stringResource(R.string.mute_button_description),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }

            // End call button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = Color.Red,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = stringResource(R.string.end_call_button_description),
                    tint = Color.White,
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
                modifier = Modifier.fillMaxWidth()
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
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .weight(1f)
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
            enabled = enabled && value.trim().isNotEmpty(),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = stringResource(R.string.send_button_description),
                tint = Color.White
            )
        }
    }
}

@Preview
@Suppress("UnusedPrivateMember")
@Composable
private fun TranscriptViewPreview() {
    val sampleTranscript = listOf(
        TranscriptItem(id = "1", text = "Hello! How can I assist you today?", isUser = false),
        TranscriptItem(id = "2", text = "I need help with my account", isUser = true),
        TranscriptItem(id = "3", text = "I'd be happy to help you with your account. What specific issue are you experiencing?", isUser = false),
        TranscriptItem(id = "4", text = "I can't log in", isUser = true)
    )

    val sampleSettings = WidgetSettings(
        agentThinkingText = "AI is thinking...",
        speakToInterruptText = "Speak to interrupt"
    )

    VoiceAIWidgetTheme(darkTheme = false) {
        TranscriptDialogContent(
            settings = sampleSettings,
            transcriptItems = sampleTranscript,
            userInput = "",
            isConnected = true,
            isMuted = false,
            agentStatus = AgentStatus.Waiting,
            audioLevels = listOf(0.2f, 0.4f, 0.6f, 0.8f, 0.5f, 0.3f, 0.7f, 0.9f, 0.4f, 0.2f, 0.5f, 0.3f),
            onUserInputChange = {},
            onSendMessage = {},
            onToggleMute = {},
            onEndCall = {},
            onCollapse = {},
            iconOnly = false
        )
    }
}

@Preview
@Suppress("UnusedPrivateMember")
@Composable
private fun TranscriptViewDarkPreview() {
    val sampleTranscript = listOf(
        TranscriptItem(id = "1", text = "Hello! How can I assist you today?", isUser = false),
        TranscriptItem(id = "2", text = "I need help with my account", isUser = true),
        TranscriptItem(id = "3", text = "I'd be happy to help you with your account. What specific issue are you experiencing?", isUser = false),
        TranscriptItem(id = "4", text = "I can't log in", isUser = true)
    )

    val sampleSettings = WidgetSettings(
        agentThinkingText = "AI is thinking...",
        speakToInterruptText = "Speak to interrupt"
    )

    VoiceAIWidgetTheme(darkTheme = true) {
        TranscriptDialogContent(
            settings = sampleSettings,
            transcriptItems = sampleTranscript,
            userInput = "",
            isConnected = true,
            isMuted = false,
            agentStatus = AgentStatus.Waiting,
            audioLevels = listOf(0.2f, 0.4f, 0.6f, 0.8f, 0.5f, 0.3f, 0.7f, 0.9f, 0.4f, 0.2f, 0.5f, 0.3f),
            onUserInputChange = {},
            onSendMessage = {},
            onToggleMute = {},
            onEndCall = {},
            onCollapse = {},
            iconOnly = false
        )
    }
}
