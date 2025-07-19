---
id: task-016
title: >-
  Fix post-recording UI freeze by moving LibWhisper initialization to background
  thread
status: To Do
assignee: []
created_date: '2025-07-19'
updated_date: '2025-07-19'
labels:
  - bug
  - critical
  - performance
  - ui
  - transcription
dependencies: []
---

## Description

The app experiences a critical UI freeze immediately after completing quick record functionality. The freeze occurs because LibWhisper model initialization (which is CPU and I/O intensive) executes on Android's main UI thread, blocking user interactions and potentially triggering ANR (Application Not Responding) dialogs. This severely impacts user experience and app stability.

## Acceptance Criteria

- [ ] UI remains responsive during post-recording transcription process
- [ ] LibWhisper model loading occurs on background thread (not main UI thread)
- [ ] No ANR (Application Not Responding) dialogs during transcription
- [ ] User can interact with app while transcription is processing in background
- [ ] TranscriptionViewModel uses proper coroutine dispatchers for heavy operations
- [ ] Loading progress indicators display properly during transcription
- [ ] Background transcription completes successfully and creates notes
- [ ] Error handling works correctly for background transcription failures

## Implementation Plan

1. Analyze current TranscriptionViewModel.initRecognizer() flow to confirm main thread execution
2. Create dedicated TranscriptionRepository following clean architecture pattern:
   - Abstract transcription data operations from UI layer
   - Implement background threading using withContext(Dispatchers.IO)
   - Wrap all transcriber operations in Result<T> for proper error handling
3. Refactor Android Transcriber implementation:
   - Ensure all LibWhisper operations (initialize, loadBaseModel) are thread-safe
   - Document any thread-safety requirements in the interface
4. Update TranscriptionViewModel to use repository pattern:
   - Replace direct transcriber calls with repository calls
   - Ensure all heavy operations use proper coroutine dispatchers
   - Maintain existing UI state management patterns
5. Update BackgroundTranscriptionService integration:
   - Ensure compatibility with new repository pattern
   - Verify background transcription still works correctly
6. Add comprehensive error handling:
   - Handle initialization failures gracefully
   - Provide meaningful error messages to users
   - Implement fallback behavior for transcription failures
7. Testing and validation:
   - Test quick record flow with various audio lengths
   - Verify UI responsiveness during transcription
   - Test error scenarios and recovery
   - Perform ANR testing on different Android versions
   - Validate existing functionality remains intact
