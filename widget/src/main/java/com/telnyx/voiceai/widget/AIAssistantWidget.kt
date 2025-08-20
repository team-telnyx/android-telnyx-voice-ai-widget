package com.telnyx.voiceai.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.telnyx.voiceai.widget.state.WidgetState
import com.telnyx.voiceai.widget.ui.components.*
import com.telnyx.voiceai.widget.ui.theme.VoiceAIWidgetTheme
import com.telnyx.voiceai.widget.viewmodel.WidgetViewModel

/**
 * Main AI Assistant Widget component
 * 
 * This is the entry point for integrating the AI Assistant Widget into your application.
 * 
 * @param assistantId The Assistant ID used to connect to the Telnyx AI service
 * @param modifier Modifier for styling the widget
 * @param darkTheme Whether to use dark theme. If null, follows system theme
 */
@Composable
fun AIAssistantWidget(
    assistantId: String,
    modifier: Modifier = Modifier,
    darkTheme: Boolean? = null,
    viewModel: WidgetViewModel = viewModel()
) {
    // Initialize the widget when assistantId changes
    LaunchedEffect(assistantId) {
        viewModel.initialize(assistantId)
    }
    
    val widgetState by viewModel.widgetState.collectAsState()
    val transcriptItems by viewModel.transcriptItems.collectAsState()
    val userInput by viewModel.userInput.collectAsState()
    
    VoiceAIWidgetTheme(
        darkTheme = darkTheme ?: androidx.compose.foundation.isSystemInDarkTheme()
    ) {
        when (val state = widgetState) {
            is WidgetState.Loading -> {
                LoadingWidget(
                    message = "Loading...",
                    modifier = modifier
                )
            }
            
            is WidgetState.Collapsed -> {
                WidgetButton(
                    settings = state.settings,
                    onClick = { viewModel.startCall() },
                    modifier = modifier
                )
            }
            
            is WidgetState.Connecting -> {
                LoadingWidget(
                    message = "Connecting...",
                    modifier = modifier
                )
            }
            
            is WidgetState.Expanded -> {
                ExpandedWidget(
                    settings = state.settings,
                    isConnected = state.isConnected,
                    isMuted = state.isMuted,
                    agentStatus = state.agentStatus,
                    onToggleMute = { viewModel.toggleMute() },
                    onEndCall = { viewModel.endCall() },
                    onTap = { viewModel.showTranscriptView() },
                    modifier = modifier
                )
            }
            
            is WidgetState.TranscriptView -> {
                TranscriptView(
                    settings = state.settings,
                    transcriptItems = transcriptItems,
                    userInput = userInput,
                    isConnected = state.isConnected,
                    isMuted = state.isMuted,
                    agentStatus = state.agentStatus,
                    onUserInputChange = { viewModel.updateUserInput(it) },
                    onSendMessage = { viewModel.sendMessage() },
                    onToggleMute = { viewModel.toggleMute() },
                    onEndCall = { viewModel.endCall() },
                    onCollapse = { viewModel.collapseFromTranscriptView() },
                    modifier = modifier
                )
            }
            
            is WidgetState.Error -> {
                ErrorWidget(
                    message = state.message,
                    onRetry = { viewModel.initialize(assistantId) },
                    modifier = modifier
                )
            }
        }
    }
}

/**
 * Error widget component
 */
@Composable
private fun ErrorWidget(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            androidx.compose.material3.Button(
                onClick = onRetry,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Retry")
            }
        }
    }
}