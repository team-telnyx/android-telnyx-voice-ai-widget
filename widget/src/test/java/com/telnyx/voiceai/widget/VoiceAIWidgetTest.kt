package com.telnyx.voiceai.widget

import org.junit.Test
import org.junit.Assert.*

/**
 * Basic unit tests for the Voice AI Widget
 */
class VoiceAIWidgetTest {

    @Test
    fun `widget package name is correct`() {
        val expectedPackage = "com.telnyx.voiceai.widget"
        val actualPackage = VoiceAIWidgetTest::class.java.packageName
        assertEquals(expectedPackage, actualPackage)
    }

    @Test
    fun `basic math operations work`() {
        assertEquals(4, 2 + 2)
        assertEquals(0, 2 - 2)
        assertEquals(4, 2 * 2)
        assertEquals(1, 2 / 2)
    }

    @Test
    fun `string operations work`() {
        val testString = "Telnyx Voice AI Widget"
        assertTrue(testString.contains("Telnyx"))
        assertTrue(testString.contains("Voice AI"))
        assertTrue(testString.contains("Widget"))
        assertEquals(22, testString.length)
    }
}