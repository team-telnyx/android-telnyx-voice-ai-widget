package com.telnyx.voiceai.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telnyx.voiceai.example.ui.theme.VoiceAIWidgetExampleTheme
import com.telnyx.voiceai.widget.AIAssistantWidget
import com.telnyx.voiceai.widget.model.CallParams

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceAIWidgetExampleTheme {
                ExampleApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleApp() {
    var assistantId by remember { mutableStateOf("demo-assistant-id") }
    var showAlertDialog by remember { mutableStateOf(false) }
    var showWidget by remember { mutableStateOf(false) }
    var iconOnly by remember { mutableStateOf(false) }
    
    // Example CallParams - you can customize these values
    val callParams = remember {
        CallParams(
            callerName = "John Doe",
            callerNumber = "+1234567890",
            destinationNumber = null, // Use default AI assistant destination
            customHeaders = mapOf(
                "X-Custom-Header" to "example-value",
                "X-User-ID" to "user123"
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.top_app_bar_title),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Assistant ID field
            OutlinedTextField(
                value = assistantId,
                onValueChange = { assistantId = it },
                label = { Text(stringResource(R.string.assistant_id_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true
            )

            // Widget Mode selection
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.widget_mode_label),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Regular mode radio button
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !iconOnly,
                            onClick = { iconOnly = false }
                        )
                        Text(
                            text = stringResource(R.string.widget_mode_regular),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    
                    // Icon Only mode radio button
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = iconOnly,
                            onClick = { iconOnly = true }
                        )
                        Text(
                            text = stringResource(R.string.widget_mode_icon_only),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Create Widget button
            Button(
                onClick = {
                    if (assistantId.trim().isEmpty()) {
                        showAlertDialog = true
                    } else {
                        showWidget = true
                    }
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(stringResource(R.string.create_widget_button))
            }

            // Widget demo section and actual widget - only show when widget is initialized
            if (showWidget) {
                Text(
                    text = stringResource(R.string.widget_demo_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            // The actual widget
            AIAssistantWidget(
                assistantId = assistantId,
                shouldInitialize = showWidget,
                iconOnly = iconOnly,
                callParams = callParams,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Instructions section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.how_to_use_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = stringResource(R.string.instructions_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Spacer for bottom padding
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Alert Dialog
        if (showAlertDialog) {
            AlertDialog(
                onDismissRequest = { showAlertDialog = false },
                title = null,
                text = {
                    Text(text = stringResource(R.string.alert_empty_assistant_id))
                },
                confirmButton = {
                    TextButton(
                        onClick = { showAlertDialog = false }
                    ) {
                        Text(stringResource(R.string.alert_ok_button))
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExampleAppPreview() {
    VoiceAIWidgetExampleTheme {
        ExampleApp()
    }
}
