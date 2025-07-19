---
id: task-013
title: Add quick record shortcut for streamlined audio capture
status: In Progress
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

## Implementation Plan

## Implementation Plan

### Phase 1: Speed Dial FAB Component (Material 3)
1. Create SpeedDialFAB.kt with Material 3 animations and accessibility
2. Replace existing FAB in NoteListScreen.kt with SpeedDialFAB
3. Test expand/collapse animations and touch targets

### Phase 2: Navigation & State Management  
1. Add Routes.QuickRecord to Routes.kt
2. Create QuickRecordState.kt enum for state management
3. Extend NoteListViewModel with quick record state
4. Add navigation handler in App.kt

### Phase 3: Recording Flow Enhancement
1. Add isQuickRecordMode parameter to RecordingScreen.kt
2. Implement auto-flow logic (skip initial screen, auto-navigate)
3. Test recording flow end-to-end

### Phase 4: Background Processing Engine
1. Create BackgroundTranscriptionService.kt wrapping TranscriptionViewModel
2. Implement auto-note creation with timestamp titles
3. Add progress indicators and error handling

### Phase 5: Integration & Polish
1. End-to-end testing of 2-click flow
2. Accessibility validation and performance optimization
3. Error scenario testing

## Technical Approach
- Speed Dial FAB following Material 3 guidelines
- 95%+ component reuse strategy
- Background transcription with existing TranscriptionViewModel
- Auto-note creation using existing InsertNoteUseCase
