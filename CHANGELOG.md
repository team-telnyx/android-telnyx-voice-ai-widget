## [Unreleased]

### Dependency Updates
- Bump `telnyx-webrtc-android` from `3.4.1` to `3.7.0`
  - **3.5.0:** Adds automatic call stats reporting and JSON export, ICE candidate pair details, transport stats, audio level metrics, SDK latency measurement, `telnyx_call_control_id` handling in answer, fixes `clientState` truncation and SIGSEGV in stats timer
  - **3.6.0:** Adds `pushWhenActive` support for receiving push notifications while a client is active, `answered_device_token` auto-include from FCM token, "Answered Elsewhere" push cleanup to dismiss incoming call notifications when picked up on another device, persisted environment for push answer/decline flows
  - **3.6.1:** Adds `pn_late_fanout` in login `userVariables` for push-when-active multi-device routing, fixes push payload `call_id` as stable app-facing ID for push-when-active remaps
  - **3.7.0:** Adds TURNS port 443 fallback ICE server, exposes active calls by app-facing call ID for push remaps, fixes WebSocket login race condition, trickle ICE race condition (`ConcurrentModificationException`), thread-safety via `ConcurrentHashMap` for `TelnyxClient.calls`, removes `google-services.json` tracking from sample apps, adds disconnect button during connecting phase, proper call teardown on reconnection timeout

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
