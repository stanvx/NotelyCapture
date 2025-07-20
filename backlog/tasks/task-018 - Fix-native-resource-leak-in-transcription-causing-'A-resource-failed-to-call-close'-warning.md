---
id: task-018
title: >-
  Fix native resource leak in transcription causing 'A resource failed to call
  close' warning
status: In Progress
assignee:
  - '@copilot'
created_date: '2025-07-20'
updated_date: '2025-07-20'
labels: []
dependencies: []
---

## Description

Fix resource management issues in Whisper transcription that cause Android system warnings about unclosed resources. The leak occurs during transcription initialization and can prevent proper navigation flow.

## Acceptance Criteria

- [ ] No more 'A resource failed to call close' warnings in logs
- [ ] WhisperContext resources are properly released in all scenarios
- [ ] TranscriptionViewModel cleanup is reliable
- [ ] Navigation works correctly after transcription completion

## Implementation Plan

1. Fix TranscriptionViewModel.onCleared() to ensure finish() is called
2. Enhance error handling in BackgroundTranscriptionService with robust finally block
3. Improve Android Transcriber resource management with proper exception handling
4. Add null safety and cleanup validation to WhisperContext operations
5. Test resource cleanup under various failure scenarios
6. Verify no resource leak warnings in Android logs
