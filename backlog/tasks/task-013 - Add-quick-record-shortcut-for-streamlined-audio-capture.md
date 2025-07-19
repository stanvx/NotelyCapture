---
id: task-013
title: Add quick record shortcut for streamlined audio capture
status: To Do
assignee: []
created_date: '2025-07-19'
updated_date: '2025-07-19'
labels: []
dependencies: []
priority: high
---

## Description

Implement a direct recording flow that bypasses the current multi-step process (plus → microphone → record → stop → transcribe → append → back). Users should be able to start recording with one click, stop with another click, and have transcription/append happen automatically in the background.

## Acceptance Criteria

- [ ] Quick record button available on main screen
- [ ] Single click starts recording and navigates to recording screen
- [ ] Single click stops recording and returns to main screen
- [ ] Transcription happens automatically in background after recording stops
- [ ] New note is created and transcription is appended automatically
- [ ] No manual transcription or append button interaction required
- [ ] Reuses existing recording and transcription components
- [ ] Flow reduces current 7+ clicks to just 2 clicks
