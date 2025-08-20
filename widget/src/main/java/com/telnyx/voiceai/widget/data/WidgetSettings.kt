package com.telnyx.voiceai.widget.data

import com.google.gson.annotations.SerializedName

/**
 * Data class representing widget settings for AI conversations
 */
data class WidgetSettings(
    @SerializedName("agent_thinking_text")
    val agentThinkingText: String? = null,
    @SerializedName("audio_visualizer_config")
    val audioVisualizerConfig: AudioVisualizerConfig? = null,
    @SerializedName("default_state")
    val defaultState: String? = null,
    @SerializedName("give_feedback_url")
    val giveFeedbackUrl: String? = null,
    @SerializedName("logo_icon_url")
    val logoIconUrl: String? = null,
    @SerializedName("position")
    val position: String? = null,
    @SerializedName("report_issue_url")
    val reportIssueUrl: String? = null,
    @SerializedName("speak_to_interrupt_text")
    val speakToInterruptText: String? = null,
    @SerializedName("start_call_text")
    val startCallText: String? = null,
    @SerializedName("theme")
    val theme: String? = null,
    @SerializedName("view_history_url")
    val viewHistoryUrl: String? = null
)

/**
 * Data class representing audio visualizer configuration
 */
data class AudioVisualizerConfig(
    @SerializedName("enabled")
    val enabled: Boolean? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("color")
    val color: String? = null,
    @SerializedName("preset")
    val preset: String? = null
)