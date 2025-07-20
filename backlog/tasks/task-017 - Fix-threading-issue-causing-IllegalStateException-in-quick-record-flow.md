---
id: task-017
title: Fix threading issue causing IllegalStateException in quick record flow
status: In Progress
assignee:
  - '@copilot'
created_date: '2025-07-19'
updated_date: '2025-07-20'
labels: []
dependencies: []
---

## Description

Fix IllegalStateException: Method setCurrentState must be called on the main thread error that occurs when using quick record functionality. The error is caused by background thread operations triggering UI state changes.

## Acceptance Criteria

- [ ] Error no longer occurs during quick record flow
- [ ] Navigation callbacks execute on main thread
- [ ] Transcription callbacks execute on main thread
- [ ] All UI state changes happen on main thread

## Implementation Plan

1. Analyze stack trace to identify root cause\n2. Fix Transcriber.android.kt WhisperCallback threading\n3. Fix BackgroundTranscriptionService callback threading\n4. Ensure all UI state changes happen on main thread\n5. Test quick record flow to verify fix
