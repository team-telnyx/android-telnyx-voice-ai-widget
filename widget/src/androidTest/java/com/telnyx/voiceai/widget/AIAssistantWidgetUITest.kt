package com.telnyx.voiceai.widget

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telnyx.voiceai.widget.state.WidgetState
import com.telnyx.voiceai.widget.ui.theme.VoiceAIWidgetTheme
import com.telnyx.voiceai.widget.viewmodel.WidgetViewModel
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI test for AIAssistantWidget
 * 
 * This test verifies that the widget properly transitions to Collapsed state
 * after shouldInitialize changes from false to true and waits approximately 10 seconds
 */
@RunWith(AndroidJUnit4::class)
class AIAssistantWidgetUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aiAssistantWidget_shouldInitializeTransition_showsCollapsedState() {
        // Get assistant ID from BuildConfig (loaded from local.properties)
        val assistantId = BuildConfig.TEST_ASSISTANT_ID
        
        // State variable to control shouldInitialize
        var shouldInitialize by mutableStateOf(false)
        
        // Set up the widget with shouldInitialize = false initially
        composeTestRule.setContent {
            VoiceAIWidgetTheme {
                AIAssistantWidget(
                    assistantId = assistantId,
                    shouldInitialize = shouldInitialize
                )
            }
        }
        
        // Initially, the widget should be in Idle state (no UI shown)
        // We can verify this by checking that no button is displayed
        composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true)
            .assertDoesNotExist()
        
        // Change shouldInitialize to true to trigger initialization
        shouldInitialize = true
        composeTestRule.waitForIdle()
        
        // Wait for approximately 10 seconds to allow widget to initialize and transition
        // In a real scenario, this would depend on network calls and service initialization
        // For testing purposes, we'll wait in intervals and check the state
        
        // Wait for initial loading state to appear
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            // Check if we have either loading or collapsed state
            try {
                composeTestRule.onNodeWithTag("loading_widget", useUnmergedTree = true).assertExists()
                true
            } catch (e: AssertionError) {
                try {
                    composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true).assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }
        }
        
        // Now wait specifically for the collapsed state (widget button)
        // This should happen after the widget initializes successfully
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Verify that the widget is now in Collapsed state by checking for the widget button
        composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
        
        // Additional verification: check that loading state is no longer present
        composeTestRule.onNodeWithTag("loading_widget", useUnmergedTree = true)
            .assertDoesNotExist()
    }
    
    @Test
    fun aiAssistantWidget_initialState_shouldNotShowUI() {
        val assistantId = BuildConfig.TEST_ASSISTANT_ID
        
        composeTestRule.setContent {
            VoiceAIWidgetTheme {
                AIAssistantWidget(
                    assistantId = assistantId,
                    shouldInitialize = false // Keep initialization disabled
                )
            }
        }
        
        // Wait for compose to settle
        composeTestRule.waitForIdle()
        
        // Verify that no UI elements are shown when shouldInitialize is false
        composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag("loading_widget", useUnmergedTree = true)
            .assertDoesNotExist()
    }
    
    @Test
    fun aiAssistantWidget_withValidAssistantId_initializes() {
        val assistantId = BuildConfig.TEST_ASSISTANT_ID
        
        composeTestRule.setContent {
            VoiceAIWidgetTheme {
                AIAssistantWidget(
                    assistantId = assistantId,
                    shouldInitialize = true
                )
            }
        }
        
        // Wait for the widget to show some UI (either loading or collapsed state)
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                // Check if any widget UI is present
                composeTestRule.onNodeWithTag("loading_widget", useUnmergedTree = true).assertExists()
                true
            } catch (e: AssertionError) {
                try {
                    composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true).assertExists()
                    true
                } catch (e: AssertionError) {
                    try {
                        composeTestRule.onNodeWithTag("error_widget", useUnmergedTree = true).assertExists()
                        true
                    } catch (e: AssertionError) {
                        false
                    }
                }
            }
        }
        
        // Verify that some UI is displayed (loading, collapsed, or error state)
        val hasLoadingWidget = try {
            composeTestRule.onNodeWithTag("loading_widget", useUnmergedTree = true).assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
        
        val hasWidgetButton = try {
            composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true).assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
        
        val hasErrorWidget = try {
            composeTestRule.onNodeWithTag("error_widget", useUnmergedTree = true).assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
        
        // At least one of these should be true
        assert(hasLoadingWidget || hasWidgetButton || hasErrorWidget) {
            "Widget should display some UI when shouldInitialize is true"
        }
    }
    
    @Test
    fun aiAssistantWidget_fullCallFlow_collapsedToExpandedToCollapsed() {
        val assistantId = BuildConfig.TEST_ASSISTANT_ID
        
        composeTestRule.setContent {
            VoiceAIWidgetTheme {
                AIAssistantWidget(
                    assistantId = assistantId,
                    shouldInitialize = true
                )
            }
        }
        
        // Step 1: Wait for widget to reach Collapsed state (showing widget button)
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Verify we are in Collapsed state
        composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
        
        // Step 2: Click the widget button to start call (transition to Expanded state)
        composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true)
            .performClick()
        
        // Wait for transition to Expanded state (showing expanded widget with controls)
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithTag("expanded_widget", useUnmergedTree = true).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Verify we are in Expanded state
        composeTestRule.onNodeWithTag("expanded_widget", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
        
        // Verify the end call button is visible in the expanded state
        composeTestRule.onNodeWithTag("end_call_button", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
        
        // Verify that the widget button is no longer visible (we've transitioned out of Collapsed)
        composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true)
            .assertDoesNotExist()
        
        // Step 3: Wait for 20 seconds while in Expanded state
        // In a real test environment, you might want to reduce this time or use test-specific timing
        Thread.sleep(20_000) // 20 seconds
        
        // Verify we're still in Expanded state after 20 seconds
        composeTestRule.onNodeWithTag("expanded_widget", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
        
        // Step 4: Click the end call button to end the call
        composeTestRule.onNodeWithTag("end_call_button", useUnmergedTree = true)
            .performClick()
        
        // Step 5: Wait for transition back to Collapsed state
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Verify we are back in Collapsed state
        composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
        
        // Verify that the expanded widget is no longer visible (we've transitioned back to Collapsed)
        composeTestRule.onNodeWithTag("expanded_widget", useUnmergedTree = true)
            .assertDoesNotExist()
        
        // Verify that the end call button is no longer visible
        composeTestRule.onNodeWithTag("end_call_button", useUnmergedTree = true)
            .assertDoesNotExist()
    }
    
    @Test 
    fun aiAssistantWidget_expandedState_hasRequiredControls() {
        val assistantId = BuildConfig.TEST_ASSISTANT_ID
        
        composeTestRule.setContent {
            VoiceAIWidgetTheme {
                AIAssistantWidget(
                    assistantId = assistantId,
                    shouldInitialize = true
                )
            }
        }
        
        // Wait for Collapsed state and click to expand
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        composeTestRule.onNodeWithTag("widget_button", useUnmergedTree = true)
            .performClick()
        
        // Wait for Expanded state
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithTag("expanded_widget", useUnmergedTree = true).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Verify all required controls are present in expanded state
        composeTestRule.onNodeWithTag("expanded_widget", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("end_call_button", useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
    }
}
