---
id: task-019
title: Implement Microsoft-inspired unified action buttons system
status: To Do
assignee: []
created_date: '2025-07-20'
labels: []
dependencies: []
---

## Description

Refactor the app's primary action system by replacing the custom SpeedDialFAB with a modern, ergonomic, and context-aware dual-button system, consistent with Microsoft's Material Design language to create a more intuitive and unified user experience across the application.

## Acceptance Criteria

- [ ] New accent color (RecordBlue) is defined and integrated into Material 3 theme
- [ ] SpeedDialFAB component is completely removed from codebase
- [ ] Dual-FAB system is implemented on NoteListScreen with Record and Speed Dial actions
- [ ] Record FAB uses ExtendedFloatingActionButton that shrinks on scroll
- [ ] Context-aware dual-FAB system is implemented on NoteDetailScreen
- [ ] Record button on detail screen initiates recording for current note
- [ ] Transcribe action is added to detail screen FAB menu
- [ ] All existing functionality remains intact after refactor
- [ ] UI follows Microsoft Material Design principles
- [ ] Code is well-documented and follows project patterns
