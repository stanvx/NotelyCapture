---
id: task-016
title: >-
  Fix post-recording UI freeze by moving LibWhisper initialization to background
  thread
status: To Do
assignee: []
created_date: '2025-07-19'
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
