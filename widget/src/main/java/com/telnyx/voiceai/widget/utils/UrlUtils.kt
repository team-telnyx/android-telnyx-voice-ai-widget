package com.telnyx.voiceai.widget.utils

import android.util.Patterns

/**
 * Utility class for URL validation and processing
 */
object UrlUtils {

    /**
     * Validates if a given string is a valid URL
     *
     * @param url The URL string to validate
     * @return true if the URL is valid, false otherwise
     */
    fun isValidUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }
}