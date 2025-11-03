package com.telnyx.voiceai.widget.model

/**
 * Call parameters for customizing call behavior when initializing the AI Assistant Widget.
 * 
 * This class allows you to override default call parameters such as caller information
 * and destination details. All parameters are optional and will only be used if provided.
 * 
 * @param destinationNumber The destination number for the call. If provided, overrides the default destination.
 * @param callerNumber The caller number to display for the call. If provided, overrides the default caller number.
 * @param callerName The caller name to display for the call. If provided, overrides the default caller name.
 * @param customHeaders Additional custom headers to include with the call. These will be merged with any existing headers.
 */
data class CallParams(
    val destinationNumber: String? = null,
    val callerNumber: String? = null,
    val callerName: String? = null,
    val customHeaders: Map<String, String>? = null
)
