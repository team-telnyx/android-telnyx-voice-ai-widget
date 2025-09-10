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
    implementation("com.telnyx:android-voice-ai-widget:1.0.0")
}
```

### Maven Central

This library is published to Maven Central. Make sure you have `mavenCentral()` in your repositories:

```kotlin
repositories {
    mavenCentral()
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
    var showWidget by remember { mutableStateOf(false) }
    
    AIAssistantWidget(
        assistantId = "your-assistant-id",
        shouldInitialize = showWidget,
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 3. Understanding `shouldInitialize` Parameter

The `shouldInitialize` parameter controls when the widget establishes its network connection to Telnyx servers. This is crucial for controlling:

- **Network Usage**: Prevents unnecessary connections until needed
- **User Consent**: Initialize only after user grants permissions
- **Performance**: Defer connection for better app startup performance
- **Conditional Loading**: Connect based on user subscription, feature flags, etc.

#### Key Behaviors:

- **`false`**: Widget remains dormant with no network activity or UI display
- **`true`**: Triggers socket connection and loads assistant configuration
- **State Change**: Changing from `false` to `true` will initialize the connection
- **Active Sessions**: Changing from `true` to `false` does NOT disconnect active calls

#### Network Connection Lifecycle:

1. **shouldInitialize = false**: No socket connection, widget in idle state
2. **shouldInitialize = true**: Socket connects to Telnyx, widget settings load
3. **User initiates call**: WebRTC connection established for audio
4. **Call ends**: WebRTC disconnects, socket remains for future calls  
5. **shouldInitialize = false**: Does NOT affect active socket (by design)

#### Common Patterns:

```kotlin
// 1. Initialize immediately (default behavior)
AIAssistantWidget(
    assistantId = "your-assistant-id",
    shouldInitialize = true  // Can be omitted as it defaults to true
)

// 2. Conditional initialization based on user action
@Composable
fun ConditionalWidget() {
    var userWantsAssistant by remember { mutableStateOf(false) }
    
    Column {
        Button(onClick = { userWantsAssistant = true }) {
            Text("Enable AI Assistant")
        }
        
        AIAssistantWidget(
            assistantId = "your-assistant-id",
            shouldInitialize = userWantsAssistant
        )
    }
}

// 3. Initialize after permissions are granted
@Composable
fun PermissionAwareWidget() {
    var hasPermissions by remember { mutableStateOf(false) }
    
    // Check permissions first
    LaunchedEffect(Unit) {
        hasPermissions = checkAudioPermissions()
    }
    
    AIAssistantWidget(
        assistantId = "your-assistant-id",
        shouldInitialize = hasPermissions
    )
}

// 4. Deferred initialization for performance
@Composable
fun DeferredWidget() {
    var initializeWidget by remember { mutableStateOf(false) }
    
    // Initialize after a delay or user interaction
    LaunchedEffect(Unit) {
        delay(2000) // Wait 2 seconds after app start
        initializeWidget = true
    }
    
    AIAssistantWidget(
        assistantId = "your-assistant-id",
        shouldInitialize = initializeWidget
    )
}
```

## Customization with Modifiers

The `AIAssistantWidget` supports additional modifier parameters for fine-tuned UI customization:

### Available Modifiers

```kotlin
AIAssistantWidget(
    assistantId = "your-assistant-id",
    modifier = Modifier.fillMaxWidth(), // Overall widget positioning
    shouldInitialize = true,
    
    // New customization modifiers:
    widgetButtonModifier = Modifier.padding(8.dp), // Applied to collapsed button
    expandedWidgetModifier = Modifier.shadow(4.dp), // Applied to expanded widget
    buttonTextModifier = Modifier.alpha(0.8f), // Applied to button text
    buttonImageModifier = Modifier.clip(CircleShape) // Applied to button icon/logo
)
```

### Modifier Parameters

| Parameter | Description | Applied To |
|-----------|-------------|------------|
| `widgetButtonModifier` | Styling for the collapsed widget button | The entire button Card in collapsed state |
| `expandedWidgetModifier` | Styling for the expanded widget | The entire expanded widget Card |
| `buttonTextModifier` | Styling for the button text | The start call text in the collapsed button |
| `buttonImageModifier` | Styling for the button icon/logo | The image or icon displayed in the collapsed button |

### Usage Examples

```kotlin
// Custom button styling with rounded corners and shadow
AIAssistantWidget(
    assistantId = "your-assistant-id",
    widgetButtonModifier = Modifier
        .shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp))
        .border(2.dp, Color.Blue, RoundedCornerShape(32.dp))
)

// Styled text and circular icon
AIAssistantWidget(
    assistantId = "your-assistant-id",
    buttonTextModifier = Modifier
        .padding(horizontal = 8.dp)
        .alpha(0.9f),
    buttonImageModifier = Modifier
        .clip(CircleShape)
        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
)

// Custom expanded widget appearance
AIAssistantWidget(
    assistantId = "your-assistant-id",
    expandedWidgetModifier = Modifier
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Blue.copy(alpha = 0.1f), Color.Transparent)
            ),
            shape = RoundedCornerShape(24.dp)
        )
)
```

### Combining with Overall Layout

```kotlin
// Complete customization example
AIAssistantWidget(
    assistantId = "your-assistant-id",
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp), // Overall positioning
    widgetButtonModifier = Modifier
        .height(64.dp), // Custom button height
    buttonTextModifier = Modifier
        .padding(start = 4.dp), // Text spacing
    buttonImageModifier = Modifier
        .size(40.dp) // Larger icon
        .clip(RoundedCornerShape(8.dp))
)
```

**Note**: All modifier parameters are optional and default to `Modifier`. Existing implementations will continue to work without modification.

## Widget States

The widget automatically transitions between different states:

### 1. Collapsed State
- Shows a compact button with customizable text
- Displays logo icon if configured
- Tap to initiate a call

### 2. Loading/Connecting State
- Shows loading indicator during initialization and connection

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

## Development

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Running Lint

```bash
./gradlew lint
```

### Publishing

The project includes automated publishing to Maven Central via GitHub Actions. Publishing is triggered on:
- New releases
- Manual workflow dispatch

### CI/CD

This project includes:
- **Automated Testing**: Unit tests and lint checks on every PR
- **Static Analysis**: Semgrep security scanning
- **Dependency Management**: Dependabot for automated dependency updates
- **Maven Publishing**: Automated publishing to Maven Central

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass and lint checks succeed
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue on GitHub
- Contact Telnyx support
- Check the documentation at [telnyx.com](https://telnyx.com)