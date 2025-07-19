---
id: task-017
title: Fix threading issue causing IllegalStateException in quick record flow
status: In Progress
assignee:
  - '@copilot'
created_date: '2025-07-19'
updated_date: '2025-07-19'
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
