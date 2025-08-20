# Android Telnyx Voice AI Widget

A drop-in Android widget library for integrating Telnyx AI Assistant functionality into your applications.

## Features

- 🎯 **Drop-in Solution**: Easy integration with minimal setup
- 🎨 **Multiple UI States**: Collapsed, loading, expanded, and transcript views
- 🎵 **Audio Visualizer**: Real-time audio visualization during conversations
- 🌓 **Theme Support**: Light and dark theme compatibility
- 📱 **Responsive Design**: Optimized for various screen sizes
- 🔊 **Voice Controls**: Mute/unmute and call management
- 💬 **Transcript View**: Full conversation history with text input

## Installation

Add the widget library to your Android project:

```kotlin
dependencies {
    implementation("com.telnyx:voice-ai-widget:1.0.0")
}
```

## Quick Start

### 1. Add Required Permissions

Add these permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 2. Basic Usage

```kotlin
import com.telnyx.voiceai.widget.AIAssistantWidget

@Composable
fun MyScreen() {
    AIAssistantWidget(
        assistantId = "your-assistant-id",
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 3. Advanced Configuration

```kotlin
AIAssistantWidget(
    assistantId = "your-assistant-id",
    darkTheme = false, // or true, or null for system default
    modifier = Modifier.fillMaxWidth()
)
```

## Widget States

The widget automatically transitions between different states:

### 1. Collapsed State
- Shows a compact button with customizable text
- Displays logo icon if configured
- Tap to initiate a call

### 2. Loading/Connecting State
- Shows loading indicator during initialization and connection
- Displays appropriate status messages

### 3. Expanded State
- Audio visualizer showing real-time activity
- Mute/unmute and end call controls
- Agent status indicators (thinking/waiting)
- Tap to open full transcript view

### 4. Transcript View
- Full conversation history
- Text input for typing messages
- Audio controls and visualizer
- Collapsible back to expanded view

## Customization

The widget automatically fetches configuration from your Telnyx Assistant settings, including:

- Custom button text
- Logo/icon URLs
- Theme preferences
- Audio visualizer settings
- Status messages

## Example App

Check out the included example app in the `example` module for a complete implementation:

```bash
./gradlew :example:installDebug
```

## Architecture

The widget is built using:

- **Jetpack Compose** for modern UI
- **Material 3** design system
- **ViewModel** for state management
- **Kotlin Coroutines** for async operations
- **Telnyx WebRTC SDK** for voice communication

## Requirements

- Android API 24+ (Android 7.0)
- Kotlin 1.9+
- Jetpack Compose

## Integration with Telnyx WebRTC SDK

This widget is designed to work with the Telnyx WebRTC SDK. The current implementation includes:

- Mock integration for development and testing
- Placeholder for actual TelnyxClient integration
- State management compatible with WebRTC events

To integrate with the actual Telnyx WebRTC SDK, replace the mock implementations in `WidgetViewModel` with real TelnyxClient calls.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue on GitHub
- Contact Telnyx support
- Check the documentation at [telnyx.com](https://telnyx.com)