package com.telnyx.voiceai.widget.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.util.Log
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.telnyx.voiceai.widget.R
import com.telnyx.voiceai.widget.ui.theme.VoiceAIWidgetTheme
import com.telnyx.webrtc.sdk.model.WidgetSettings

/**
 * Collapsed widget button component
 */
@Composable
fun WidgetButton(
    settings: WidgetSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    buttonTextModifier: Modifier = Modifier,
    buttonImageModifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("widget_button")
            .clickable { onClick() }
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Logo or default icon
            if (!settings.logoIconUrl.isNullOrEmpty()) {
                Log.d("WidgetButton", "Loading image from URL: ${settings.logoIconUrl}")
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(settings.logoIconUrl)
                        .crossfade(false)
                        .fallback(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .addHeader("User-Agent", "Mozilla/5.0 (Android) Telnyx-Widget/1.0")
                        .listener(
                            onStart = { Log.d("WidgetButton", "Image loading started") },
                            onSuccess = { _, _ -> Log.d("WidgetButton", "Image loaded successfully") },
                            onError = { _, result -> 
                                Log.e("WidgetButton", "Image loading failed: ${result.throwable.message}")
                            }
                        )
                        .build(),
                    contentDescription = stringResource(R.string.widget_logo_content_description),
                    modifier = Modifier.size(32.dp).then(buttonImageModifier),
                    contentScale = ContentScale.Fit,
                    colorFilter = null
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.default_avatar),
                    contentDescription = stringResource(R.string.ai_assistant_content_description),
                    modifier = Modifier.size(32.dp).then(buttonImageModifier),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Start call text
            Text(
                text = settings.startCallText?.takeIf { it.isNotEmpty() } ?: stringResource(R.string.default_start_call_text),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = buttonTextModifier
            )
        }
    }
}

@Preview
@Composable
fun WidgetButtonPreview() {
    VoiceAIWidgetTheme(darkTheme = false) {
        WidgetButton(
            settings = WidgetSettings(),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun WidgetButtonDarkPreview() {
    VoiceAIWidgetTheme(darkTheme = true) {
        WidgetButton(
            settings = WidgetSettings(),
            onClick = {},
            isDarkTheme = true
        )
    }
}