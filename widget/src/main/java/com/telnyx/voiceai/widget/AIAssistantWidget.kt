package com.telnyx.voiceai.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.telnyx.voiceai.widget.state.ErrorType
import com.telnyx.voiceai.widget.state.WidgetState
import com.telnyx.voiceai.widget.ui.components.*
import com.telnyx.voiceai.widget.ui.theme.VoiceAIWidgetTheme
import com.telnyx.voiceai.widget.viewmodel.WidgetViewModel

/**
 * Main AI Assistant Widget component
 * 
 * This is the entry point for integrating the AI Assistant Widget into your application.
 * The widget handles the complete lifecycle of AI Assistant interactions, including socket
 * connection, call management, and UI state transitions.
 * 
 * @param assistantId The Assistant ID from your Telnyx AI configuration used to establish
 *                   the connection to the AI service. This ID determines which AI assistant
 *                   configuration and capabilities are loaded.
 * @param modifier Modifier for styling and positioning the widget within your layout
 * @param shouldInitialize Controls when the widget establishes its socket connection to Telnyx.
 *                        When false, the widget remains in Idle state with no network activity.
 *                        When true, triggers socket connection initialization and loads widget settings.
 *                        This allows for conditional initialization (e.g., after user consent,
 *                        network availability checks, or deferred loading for performance).
 *                        Changing from false to true will trigger initialization.
 *                        Changing from true to false does NOT disconnect an active session.
 * @param iconOnly When true, displays the widget as a floating action button with only the icon.
 *                In this mode, tapping starts the call and opens directly into the full screen
 *                text view. When false, displays the regular widget button with text.
 * @param widgetButtonModifier Modifier applied to the widget button in collapsed state
 * @param expandedWidgetModifier Modifier applied to the expanded widget
 * @param buttonTextModifier Modifier applied to the text visible on the widget button
 * @param buttonImageModifier Modifier applied to the image/icon visible on the widget button
 */
@Composable
fun AIAssistantWidget(
    assistantId: String,
    modifier: Modifier = Modifier,
    shouldInitialize: Boolean = true,
    iconOnly: Boolean = false,
    widgetButtonModifier: Modifier = Modifier,
    expandedWidgetModifier: Modifier = Modifier,
    buttonTextModifier: Modifier = Modifier,
    buttonImageModifier: Modifier = Modifier
) {
    val viewModel: WidgetViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Initialize the widget when shouldInitialize becomes true and assistantId is available
    LaunchedEffect(shouldInitialize) {
        if (shouldInitialize) {
            viewModel.initialize(context, assistantId, iconOnly)
        }
    }

    val widgetState by viewModel.widgetState.collectAsState()
    val widgetSettings by viewModel.widgetSettings.collectAsState()
    val transcriptItems by viewModel.transcriptItems.collectAsState()
    val userInput by viewModel.userInput.collectAsState()
    val audioLevels by viewModel.audioLevels.collectAsState()
    var floatingButtonErrorState by remember { mutableStateOf(null as WidgetState.Error?) }

    val themeToUse = when (widgetSettings.theme?.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    // Don't show UI until we have proper state from external service
    // Only show content when we have a non-idle state
    VoiceAIWidgetTheme(darkTheme = themeToUse) {
        when (val state = widgetState) {
            is WidgetState.Idle -> {

            }
            is WidgetState.Loading -> {
                if (iconOnly) {
                    LoadingWidget(
                        isCircular = true // Make it circular for icon-only mode
                    )
                } else {
                    LoadingWidget(
                        modifier = modifier
                    )
                }
            }
            is WidgetState.Collapsed -> {
                if (iconOnly) {
                    FloatingButton(
                        settings = state.settings,
                        onClick = { 
                            viewModel.startCall()
                            // Note: In iconOnly mode, the ViewModel will automatically transition
                            // to transcript view when the call state reaches Expanded
                        },
                        buttonImageModifier = buttonImageModifier
                    )
                } else {
                    WidgetButton(
                        settings = state.settings,
                        onClick = { viewModel.startCall() },
                        modifier = modifier.then(widgetButtonModifier),
                        buttonTextModifier = buttonTextModifier,
                        buttonImageModifier = buttonImageModifier
                    )
                }
            }
            is WidgetState.Connecting -> {
                if (iconOnly) {
                    LoadingWidget(
                        isCircular = true // Make it circular for icon-only mode
                    )
                } else {
                    LoadingWidget(
                        modifier = modifier
                    )
                }
            }
            is WidgetState.Expanded -> {
                if (!iconOnly) {
                    // This should not happen in iconOnly mode due to ViewModel logic
                    ExpandedWidget(
                        settings = state.settings,
                        isConnected = state.isConnected,
                        isMuted = state.isMuted,
                        agentStatus = state.agentStatus,
                        audioLevels,
                        onToggleMute = { viewModel.toggleMute() },
                        onEndCall = { viewModel.endCall() },
                        onTap = { viewModel.showTranscriptView() },
                        modifier = modifier.then(expandedWidgetModifier)
                    )
                }
            }
            is WidgetState.TranscriptView -> {
                if (!iconOnly) {
                    // Keep the expanded widget visible behind the dialog (only in regular mode)
                    ExpandedWidget(
                        settings = state.settings,
                        isConnected = state.isConnected,
                        isMuted = state.isMuted,
                        agentStatus = state.agentStatus,
                        audioLevels,
                        onToggleMute = { viewModel.toggleMute() },
                        onEndCall = { viewModel.endCall() },
                        onTap = { /* Do nothing - already in transcript view */ },
                        modifier = modifier.then(expandedWidgetModifier)
                    )
                }
                
                // Show transcript view as overlay dialog
                TranscriptView(
                    settings = state.settings,
                    transcriptItems = transcriptItems,
                    userInput = userInput,
                    isConnected = state.isConnected,
                    isMuted = state.isMuted,
                    agentStatus = state.agentStatus,
                    audioLevels = audioLevels,
                    onUserInputChange = { viewModel.updateUserInput(it) },
                    onSendMessage = { viewModel.sendMessage() },
                    onToggleMute = { viewModel.toggleMute() },
                    onEndCall = { viewModel.endCall() },
                    onCollapse = { viewModel.collapseFromTranscriptView() },
                    iconOnly = iconOnly
                )
            }
            is WidgetState.Error -> {
                if (iconOnly) {
                    FloatingButton(
                        settings = widgetSettings,
                        onClick = { 
                            // In iconOnly mode, show error dialog when tapped
                            floatingButtonErrorState = state
                        },
                        isError = true,
                        buttonImageModifier = buttonImageModifier
                    )
                } else {
                    ErrorWidget(
                        message = state.message,
                        type = state.type,
                        assistantId = assistantId,
                        onRetry = { viewModel.initialize(context, assistantId, iconOnly) },
                        modifier = modifier
                    )
                }
            }
        }
    }

    if (floatingButtonErrorState != null) {
        VoiceAIWidgetTheme (darkTheme = themeToUse){
            Dialog(
                onDismissRequest = { floatingButtonErrorState = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                ErrorWidget(
                    message = floatingButtonErrorState!!.message,
                    type = floatingButtonErrorState!!.type,
                    assistantId = assistantId,
                    onRetry = {
                        viewModel.initialize(context, assistantId, iconOnly)
                        floatingButtonErrorState = null
                              },
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
    type: ErrorType,
    assistantId: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("error_widget")
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                text = stringResource(R.string.error_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (type == ErrorType.Initialization) {
                val uriHandler = LocalUriHandler.current
                
                // First paragraph
                Text(
                    text = stringResource(R.string.error_initialization_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                // Second paragraph with clickable link
                val linkText = stringResource(R.string.error_settings_link_text)
                val fullText = stringResource(R.string.error_settings_instruction, linkText)
                val annotatedText = buildAnnotatedString {
                    val linkStartIndex = fullText.indexOf(linkText)
                    val linkEndIndex = linkStartIndex + linkText.length
                    
                    append(fullText.substring(0, linkStartIndex))
                    
                    pushStringAnnotation(
                        tag = "URL",
                        annotation = "https://portal.telnyx.com/#/ai/assistants/edit/$assistantId?tab=telephony"
                    )
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(linkText)
                    }
                    pop()
                    
                    append(fullText.substring(linkEndIndex))
                }
                
                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(
                            tag = "URL",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                    }
                )
            } else {
                val messageToShow = when (type) {
                    ErrorType.Connection -> stringResource(R.string.error_connection_prefix, message)
                    ErrorType.Other -> stringResource(R.string.error_other_prefix, message)
                    else -> message
                }

                Text(
                    text = messageToShow,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            
            Button(
                onClick = onRetry,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.retry_button))
            }
        }
    }
}
