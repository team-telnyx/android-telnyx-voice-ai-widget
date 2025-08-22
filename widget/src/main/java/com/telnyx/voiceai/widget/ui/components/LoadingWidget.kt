package com.telnyx.voiceai.widget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.telnyx.voiceai.widget.ui.theme.VoiceAIWidgetTheme

/**
 * Loading widget component shown during initialization and connection
 */
@Composable
fun LoadingWidget(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview
@Composable
fun LoadingWidgetPreview() {
    VoiceAIWidgetTheme(darkTheme = false) {
        LoadingWidget()
    }
}