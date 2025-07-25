---
id: task-019
title: Implement Microsoft-inspired unified action buttons system
status: In Progress
assignee:
  - '@copilot'
created_date: '2025-07-20'
updated_date: '2025-07-20'
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

## Implementation Plan

Phase 1: Review existing codebase and understand current UI structure
Phase 2: Define accent color and update Material 3 theme system
Phase 3: Analyze and remove existing SpeedDialFAB implementation
Phase 4: Create reusable HomeScaffoldWithFabs component for NoteListScreen
Phase 5: Implement dual-FAB system on NoteListScreen with scroll-aware behavior
Phase 6: Create DetailScaffoldWithFabs component for NoteDetailScreen
Phase 7: Implement context-aware FAB system on NoteDetailScreen with transcribe functionality
Phase 8: Test integration and ensure all existing functionality works
Phase 9: Update documentation and verify Microsoft Material Design compliance
