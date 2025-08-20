package com.telnyx.voiceai.widget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telnyx.voiceai.widget.data.WidgetSettings
import com.telnyx.voiceai.widget.state.AgentStatus
import com.telnyx.voiceai.widget.state.TranscriptItem
import com.telnyx.voiceai.widget.state.WidgetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the AI Assistant Widget state and interactions
 */
class WidgetViewModel : ViewModel() {
    
    private val _widgetState = MutableStateFlow<WidgetState>(WidgetState.Loading)
    val widgetState: StateFlow<WidgetState> = _widgetState.asStateFlow()
    
    private val _transcriptItems = MutableStateFlow<List<TranscriptItem>>(emptyList())
    val transcriptItems: StateFlow<List<TranscriptItem>> = _transcriptItems.asStateFlow()
    
    private val _userInput = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput.asStateFlow()
    
    // Mock Telnyx client - in real implementation this would be the actual TelnyxClient
    private var isConnected = false
    private var isMuted = false
    
    /**
     * Initialize the widget with assistant ID
     */
    fun initialize(assistantId: String) {
        viewModelScope.launch {
            try {
                // Simulate connecting anonymously and fetching widget settings
                // In real implementation: TelnyxClient.connectAnonymously(assistantId)
                val mockSettings = createMockWidgetSettings()
                _widgetState.value = WidgetState.Collapsed(mockSettings)
            } catch (e: Exception) {
                _widgetState.value = WidgetState.Error("Failed to initialize widget: ${e.message}")
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
                    // Simulate call connection
                    // In real implementation: TelnyxClient.call("xxx")
                    kotlinx.coroutines.delay(2000) // Simulate connection delay
                    
                    isConnected = true
                    _widgetState.value = WidgetState.Expanded(
                        settings = currentState.settings,
                        isConnected = true,
                        isMuted = false,
                        agentStatus = AgentStatus.Waiting
                    )
                    
                    // Add initial greeting to transcript
                    addTranscriptItem("Hello! How can I help you today?", isUser = false)
                    
                } catch (e: Exception) {
                    _widgetState.value = WidgetState.Error("Failed to connect: ${e.message}")
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
            
            // In real implementation: TelnyxClient.hangup()
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
        val currentState = _widgetState.value
        when (currentState) {
            is WidgetState.Expanded -> {
                isMuted = !isMuted
                // In real implementation: TelnyxClient.mute() or TelnyxClient.unmute()
                _widgetState.value = currentState.copy(isMuted = isMuted)
            }
            is WidgetState.TranscriptView -> {
                isMuted = !isMuted
                _widgetState.value = currentState.copy(isMuted = isMuted)
            }
            else -> {}
        }
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
            addTranscriptItem(message, isUser = true)
            _userInput.value = ""
            
            // Simulate agent thinking
            updateAgentStatus(AgentStatus.Thinking)
            
            viewModelScope.launch {
                // Simulate processing delay
                kotlinx.coroutines.delay(1500)
                
                // Add mock response
                val responses = listOf(
                    "I understand. Let me help you with that.",
                    "That's a great question. Here's what I think...",
                    "I can definitely assist you with that request.",
                    "Let me process that information for you."
                )
                addTranscriptItem(responses.random(), isUser = false)
                
                // Agent is now waiting
                updateAgentStatus(AgentStatus.Waiting)
            }
        }
    }
    
    private fun updateAgentStatus(status: AgentStatus) {
        val currentState = _widgetState.value
        when (currentState) {
            is WidgetState.Expanded -> {
                _widgetState.value = currentState.copy(agentStatus = status)
            }
            is WidgetState.TranscriptView -> {
                _widgetState.value = currentState.copy(agentStatus = status)
            }
            else -> {}
        }
    }
    
    private fun addTranscriptItem(text: String, isUser: Boolean) {
        val newItem = TranscriptItem(
            id = System.currentTimeMillis().toString(),
            text = text,
            isUser = isUser
        )
        _transcriptItems.value = _transcriptItems.value + newItem
    }
    
    private fun createMockWidgetSettings(): WidgetSettings {
        return WidgetSettings(
            agentThinkingText = "Agent is thinking...",
            startCallText = "Let's chat",
            speakToInterruptText = "Speak to interrupt",
            theme = "light",
            logoIconUrl = null // Will use default icon
        )
    }
}