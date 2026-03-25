## [Unreleased]

### Dependency Updates
- Bump `telnyx-webrtc-android` from `3.4.1` to `3.5.0`
  - Adds automatic call stats reporting and JSON export
  - Adds ICE candidate pair details, transport stats, and audio level metrics
  - Adds SDK latency measurement for call establishment
  - Adds `telnyx_call_control_id` handling in answer for Call Control integration
  - Fixes `clientState` truncation for payloads exceeding 57 bytes
  - Fixes potential SIGSEGV crash in stats timer after PeerConnection teardown

## [1.2.0](https://github.com/team-telnyx/android-telnyx-voice-ai-widget/releases/tag/1.2.0) (2026-03-06)

### Enhancement
- Add `conversationId` parameter to `CallParams` to allow joining existing conversations

## [1.1.0](https://github.com/team-telnyx/android-telnyx-voice-ai-widget/releases/tag/1.1.0) (2025-12-02)

### Enhancement
- Allow for image upload in conversation view
- Show URL menu in conversation view

## [1.0.0](https://github.com/team-telnyx/android-telnyx-voice-ai-widget/releases/tag/1.0.0) (2025-10-29)

### Enhancement
- Initial release of the Android Telnyx Voice AI Widget package, allowing easy integration of a voice AI assistant into Android applications with multiple UI states and real-time voice interaction.
