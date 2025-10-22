package com.telnyx.voiceai.widget.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.telnyx.voiceai.widget.R
import com.telnyx.voiceai.widget.ui.theme.VoiceAIWidgetTheme
import com.telnyx.webrtc.sdk.model.WidgetSettings

/**
 * Floating Action Button component for icon-only mode
 * 
 * This component displays only the icon in a circular widget with the background color
 * provided from the settings. When tapped, it starts the call and opens directly into
 * the full screen text view.
 */
@Composable
fun FloatingButton(
    settings: WidgetSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    buttonImageModifier: Modifier = Modifier
) {
    val backgroundColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = modifier
            .testTag("floating_button")
            .clickable { onClick() }
            .size(56.dp),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isError) {
                // Show error icon when there's an error
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = stringResource(R.string.error_icon_content_description),
                    modifier = Modifier.size(32.dp).then(buttonImageModifier),
                    tint = Color.White
                )
            } else {
                // Show logo or default icon
                if (!settings.logoIconUrl.isNullOrEmpty()) {
                    Log.d("FloatingButton", "Loading image from URL: ${settings.logoIconUrl}")
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(settings.logoIconUrl)
                            .crossfade(false)
                            .fallback(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .addHeader("User-Agent", "Mozilla/5.0 (Android) Telnyx-Widget/1.0")
                            .listener(
                                onStart = { Log.d("FloatingButton", "Image loading started") },
                                onSuccess = { _, _ -> Log.d("FloatingButton", "Image loaded successfully") },
                                onError = { _, result -> 
                                    Log.e("FloatingButton", "Image loading failed: ${result.throwable.message}")
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
            }
        }
    }
}

@Preview
@Composable
fun FloatingButtonPreview() {
    VoiceAIWidgetTheme(darkTheme = false) {
        FloatingButton(
            settings = WidgetSettings(),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FloatingButtonErrorPreview() {
    VoiceAIWidgetTheme(darkTheme = false) {
        FloatingButton(
            settings = WidgetSettings(),
            onClick = {},
            isError = true
        )
    }
}

@Preview
@Composable
fun FloatingButtonDarkPreview() {
    VoiceAIWidgetTheme(darkTheme = true) {
        FloatingButton(
            settings = WidgetSettings(),
            onClick = {}
        )
    }
}
