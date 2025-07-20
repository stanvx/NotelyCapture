---
id: task-018
title: >-
  Fix native resource leak in transcription causing 'A resource failed to call
  close' warning
status: To Do
assignee:
  - '@copilot'
created_date: '2025-07-20'
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
