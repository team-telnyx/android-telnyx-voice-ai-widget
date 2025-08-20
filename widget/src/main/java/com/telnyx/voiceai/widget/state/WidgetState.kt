package com.telnyx.voiceai.widget.state

import com.telnyx.voiceai.widget.data.WidgetSettings

/**
 * Represents the different states of the AI Assistant Widget
 */
sealed class WidgetState {
    /**
     * Initial loading state while fetching widget settings
     */
    object Loading : WidgetState()
    
    /**
     * Collapsed state showing the widget button
     */
    data class Collapsed(
        val settings: WidgetSettings
    ) : WidgetState()
    
    /**
     * Loading state when initiating a call
     */
    data class Connecting(
        val settings: WidgetSettings
    ) : WidgetState()
    
    /**
     * Expanded state during an active call
     */
    data class Expanded(
        val settings: WidgetSettings,
        val isConnected: Boolean = false,
        val isMuted: Boolean = false,
        val agentStatus: AgentStatus = AgentStatus.Waiting
    ) : WidgetState()
    
    /**
     * Full transcript view state
     */
    data class TranscriptView(
        val settings: WidgetSettings,
        val isConnected: Boolean = false,
        val isMuted: Boolean = false,
        val agentStatus: AgentStatus = AgentStatus.Waiting
    ) : WidgetState()
    
    /**
     * Error state
     */
    data class Error(
        val message: String
    ) : WidgetState()
}

/**
 * Represents the agent's current status
 */
enum class AgentStatus {
    /**
     * Agent is thinking/processing user input
     */
    Thinking,
    
    /**
     * Agent is waiting and can be interrupted
     */
    Waiting
}

/**
 * Represents a transcript item in the conversation
 */
data class TranscriptItem(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)