package com.telnyx.voiceai.widget.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telnyx.voiceai.widget.state.AgentStatus
import com.telnyx.voiceai.widget.state.ErrorType
import com.telnyx.voiceai.widget.state.TranscriptItem
import com.telnyx.voiceai.widget.state.WidgetState
import com.telnyx.webrtc.sdk.Call
import com.telnyx.webrtc.sdk.TelnyxClient
import com.telnyx.webrtc.sdk.model.LogLevel
import com.telnyx.webrtc.sdk.model.SocketError
import com.telnyx.webrtc.sdk.model.SocketMethod
import com.telnyx.webrtc.sdk.model.SocketStatus
import com.telnyx.webrtc.sdk.model.WidgetSettings
import com.telnyx.webrtc.sdk.stats.CallQualityMetrics
import com.telnyx.webrtc.sdk.verto.receive.AiConversationResponse
import com.telnyx.webrtc.sdk.verto.receive.ReceivedMessageBody
import com.telnyx.webrtc.sdk.verto.receive.SocketResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the AI Assistant Widget state and interactions
 */
class WidgetViewModel : ViewModel() {

    private val AI_ASSISTANT_DESTINATION = "ai-assistant"
    private val MAX_LEVELS = 20

    private val _widgetState = MutableStateFlow<WidgetState>(WidgetState.Idle)
    val widgetState: StateFlow<WidgetState> = _widgetState.asStateFlow()

    private val _widgetSettings = MutableStateFlow<WidgetSettings>(WidgetSettings())
    val widgetSettings: StateFlow<WidgetSettings> = _widgetSettings.asStateFlow()

    private val _transcriptItems = MutableStateFlow<List<TranscriptItem>>(emptyList())
    val transcriptItems: StateFlow<List<TranscriptItem>> = _transcriptItems.asStateFlow()
    
    private val _userInput = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput.asStateFlow()

    private val _audioLevels = MutableStateFlow<MutableList<Float>>(emptyList<Float>().toMutableList())
    val audioLevels: StateFlow<List<Float>> = _audioLevels.asStateFlow()

    // Telnyx client and connection state
    private var isConnected = false
    private var isMuted = false
    private var handlingResponses = false
    private var currentCall: Call? = null

    private lateinit var telnyxClient: TelnyxClient

    /**
     * Initialize the widget with assistant ID
     */
    fun initialize(context: Context, assistantId: String) {
        viewModelScope.launch {
            try {
                _widgetState.value = WidgetState.Loading

                telnyxClient = TelnyxClient(context)

                telnyxClient.connectAnonymously(
                    targetId = assistantId,
                    logLevel = LogLevel.ALL
                )
                
                // Start observing socket responses
                viewModelScope.launch {
                    telnyxClient.socketResponseFlow.collect { response ->
                        handleSocketResponse(response)
                    }
                }

                telnyxClient.transcriptUpdateFlow.collect { transcript ->
                    _transcriptItems.value = transcript.map { TranscriptItem(it.id, it.content, (it.role == com.telnyx.webrtc.sdk.model.TranscriptItem.ROLE_USER), it.timestamp.time) }
                }


            } catch (e: Exception) {
                _widgetState.value = WidgetState.Error("", ErrorType.Initialization)
            }
        }
    }
    
    /**
     * Start a call to the AI assistant
     */
    fun startCall() {
        val currentState = _widgetState.value
        if (currentState is WidgetState.Collapsed) {
            _widgetState.value = WidgetState.Connecting(currentState.settings)
            
            viewModelScope.launch {
                try {
                    currentCall = telnyxClient.newInvite(
                        "",
                        "",
                        AI_ASSISTANT_DESTINATION,
                        "",
                        debug = true
                    )

                    currentCall?.onCallQualityChange = { metrics: CallQualityMetrics ->
                        // Ensure level is within 0.0 to 1.0
                        val clampedLevel = metrics.inboundAudioLevel.coerceIn(0f, 1f)
                        _audioLevels.update { currentList ->
                            (currentList + clampedLevel).takeLast(MAX_LEVELS).toMutableList()
                        }
                    }

                } catch (e: Exception) {
                    _widgetState.value = WidgetState.Error("${e.message}", ErrorType.Connection)
                }
            }
        }
    }
    
    /**
     * End the current call
     */
    fun endCall() {
        val currentState = _widgetState.value
        if (currentState is WidgetState.Expanded || currentState is WidgetState.TranscriptView) {
            val settings = when (currentState) {
                is WidgetState.Expanded -> currentState.settings
                is WidgetState.TranscriptView -> currentState.settings
                else -> return
            }
            
            currentCall?.let {
                telnyxClient.endCall(it.callId)
                currentCall = null
            }

            isConnected = false
            isMuted = false
            _transcriptItems.value = emptyList()
            _widgetState.value = WidgetState.Collapsed(settings)
        }
    }
    
    /**
     * Toggle mute state
     */
    fun toggleMute() {
        when (val currentState = _widgetState.value) {
            is WidgetState.Expanded -> {
                isMuted = !isMuted
                _widgetState.value = currentState.copy(isMuted = isMuted)
            }
            is WidgetState.TranscriptView -> {
                isMuted = !isMuted
                _widgetState.value = currentState.copy(isMuted = isMuted)
            }
            else -> {}
        }

        currentCall?.onMuteUnmutePressed()
    }
    
    /**
     * Expand to transcript view
     */
    fun showTranscriptView() {
        val currentState = _widgetState.value
        if (currentState is WidgetState.Expanded) {
            _widgetState.value = WidgetState.TranscriptView(
                settings = currentState.settings,
                isConnected = currentState.isConnected,
                isMuted = currentState.isMuted,
                agentStatus = currentState.agentStatus
            )
        }
    }
    
    /**
     * Collapse from transcript view to expanded view
     */
    fun collapseFromTranscriptView() {
        val currentState = _widgetState.value
        if (currentState is WidgetState.TranscriptView) {
            _widgetState.value = WidgetState.Expanded(
                settings = currentState.settings,
                isConnected = currentState.isConnected,
                isMuted = currentState.isMuted,
                agentStatus = currentState.agentStatus
            )
        }
    }
    
    /**
     * Update user input text
     */
    fun updateUserInput(input: String) {
        _userInput.value = input
    }
    
    /**
     * Send user message
     */
    fun sendMessage() {
        val message = _userInput.value.trim()
        if (message.isNotEmpty()) {
            viewModelScope.launch {
                telnyxClient.sendAIAssistantMessage(message)
            }
            _userInput.value = ""
        }
    }
    
    /**
     * Handle socket responses from TelnyxClient
     */
    private fun handleSocketResponse(
        response: SocketResponse<ReceivedMessageBody>
    ) {
        if (handlingResponses) {
            return
        }
        when (response.status) {
            SocketStatus.ESTABLISHED -> handleEstablished()
            SocketStatus.MESSAGERECEIVED -> handleMessageReceived(response)
            SocketStatus.LOADING -> handleLoading()
            SocketStatus.ERROR -> handleError(response)
            SocketStatus.DISCONNECT -> handleDisconnect()
        }
    }

    /**
     * Handle message received responses
     */
    private fun handleMessageReceived(
        response: SocketResponse<ReceivedMessageBody>
    ) {
        val data = response.data
        when (data?.method) {
            SocketMethod.CLIENT_READY.methodName -> handleClientReady()
            SocketMethod.LOGIN.methodName -> handleLogin(data)
            SocketMethod.INVITE.methodName -> handleInvite(data)
            SocketMethod.ANSWER.methodName -> handleAnswer(data)
            SocketMethod.RINGING.methodName -> handleRinging(data)
            SocketMethod.MEDIA.methodName -> handleMedia()
            SocketMethod.BYE.methodName -> handleBye(data)
            SocketMethod.AI_CONVERSATION.methodName -> handleAiConversation(data)
        }
    }
    
    // Socket status handlers
    private fun handleEstablished() {
        Log.d("AiAssistantWidget", "Socket connection established")
    }
    
    private fun handleLoading() {
        Log.d("AiAssistantWidget", "Socket loading")
        _widgetState.value = WidgetState.Loading
    }
    
    private fun handleError(response: SocketResponse<ReceivedMessageBody>) {
        Log.d("AiAssistantWidget", "Socket error: ${response.errorMessage}")
        _widgetState.value = if (response.errorCode == SocketError.CREDENTIAL_ERROR.errorCode)
            WidgetState.Error("", ErrorType.Initialization)
        else
            WidgetState.Error("${response.errorMessage}", ErrorType.Other)
    }
    
    private fun handleDisconnect() {
        Log.d("AiAssistantWidget", "Socket disconnected")
        isConnected = false
        val currentState = _widgetState.value
        if (currentState is WidgetState.Expanded || currentState is WidgetState.TranscriptView) {
            _widgetState.value = WidgetState.Collapsed(_widgetSettings.value)
        }
    }
    
    // Socket method handlers
    private fun handleClientReady() {
        Log.d("AiAssistantWidget", "Client ready")
        _widgetState.value = WidgetState.Collapsed(_widgetSettings.value)
    }
    
    private fun handleLogin(data: ReceivedMessageBody) {
        Log.d("AiAssistantWidget", "Login received")
    }
    
    private fun handleInvite(data: ReceivedMessageBody) {
        Log.d("AiAssistantWidget", "Invite received")
    }
    
    private fun handleAnswer(data: ReceivedMessageBody) {
        Log.d("AiAssistantWidget", "Answer received")
        isConnected = true
        val currentState = _widgetState.value
        if (currentState is WidgetState.Connecting) {
            _widgetState.value = WidgetState.Expanded(
                settings = currentState.settings,
                isConnected = true,
                isMuted = false,
                agentStatus = AgentStatus.Waiting
            )
        }
    }
    
    private fun handleRinging(data: ReceivedMessageBody) {
        Log.d("AiAssistantWidget", "Ringing received")
    }
    
    private fun handleMedia() {
        Log.d("AiAssistantWidget", "Media received")
    }
    
    private fun handleBye(data: ReceivedMessageBody) {
        Log.d("AiAssistantWidget", "Bye received")
        isConnected = false
        val currentState = _widgetState.value
        if (currentState is WidgetState.Expanded || currentState is WidgetState.TranscriptView) {
            _widgetState.value = WidgetState.Collapsed(_widgetSettings.value)
        }
    }
    
    private fun handleAiConversation(data: ReceivedMessageBody) {
        Log.d("AiAssistantWidget", "AI Conversation received ${data.result?.toString()}")

        // Handle AI widget settings and conversation status
        data.result?.let { response ->
            try {
                val aiConversationResponse = response as AiConversationResponse
                val params = aiConversationResponse.aiConversationParams

                params?.widgetSettings?.let { widgetSettings ->
                    _widgetSettings.value = widgetSettings
                }
                
                // Update agent status based on conversation type
                params?.type?.let { type ->
                    val newAgentStatus = when (type) {
                        "conversation.item.created" -> AgentStatus.Thinking
                        "response.text.delta", "response.created" -> AgentStatus.Waiting
                        "response.done", "response.text.done" -> AgentStatus.Idle
                        else -> null
                    }
                    
                    newAgentStatus?.let { status ->
                        updateAgentStatus(status)
                    }
                }
            } catch (e: Throwable) {
                Log.e("AiAssistantWidget", "AI Conversation parsing error ${e.message}")
            }
        }
    }
    
    /**
     * Update agent status in current widget state
     */
    private fun updateAgentStatus(newStatus: AgentStatus) {
        when (val currentState = _widgetState.value) {
            is WidgetState.Expanded -> {
                _widgetState.value = currentState.copy(agentStatus = newStatus)
            }
            is WidgetState.TranscriptView -> {
                _widgetState.value = currentState.copy(agentStatus = newStatus)
            }
            else -> {
                // Don't update status for other states
            }
        }
    }
}
