---
id: task-028
title: >-
  Replace LargeFloatingActionButton with Extended FAB featuring Voice
  functionality
status: In Progress
assignee:
  - '@claude'
created_date: '2025-07-20'
updated_date: '2025-07-20'
labels: []
dependencies: []
---

## Description

Replace the current LargeFloatingActionButton on the homescreen with an Extended FAB that displays 'Voice' with a microphone icon. The FAB should be expressive, link to the existing Quick Record function, and use LazyListState to minimize when scrolling through captures. This should integrate with the ongoing unified action buttons system work.

## Acceptance Criteria

- [ ] Extended FAB replaces LargeFloatingActionButton on home screen
- [ ] FAB displays 'Voice' text with microphone icon
- [ ] FAB links to existing Quick Record functionality
- [ ] FAB minimizes/extends based on LazyListState scroll behavior
- [ ] FAB follows Material 3 expressive design guidelines
- [ ] Integration with unified action buttons system is seamless
- [ ] Existing Quick Record functionality remains intact

## Implementation Plan

1. Research current LargeFloatingActionButton implementation and homescreen layout\n2. Examine existing Quick Record functionality integration\n3. Study the Extended FAB examples and Material 3 guidelines\n4. Implement Extended FAB with Voice text and microphone icon\n5. Add LazyListState scroll behavior for minimize/expand functionality\n6. Test integration with existing Quick Record flow\n7. Ensure coordination with ongoing unified action buttons system (task-019)\n8. Validate Material 3 expressive design compliance

## Implementation Notes

Successfully implemented Extended FAB with Voice functionality:\n\n**Approach taken:**\n1. Created new ExtendedVoiceFAB component using Material 3 ExtendedFloatingActionButton\n2. Added scroll-based expand/collapse behavior using LazyStaggeredGridState\n3. Updated NoteList to accept LazyStaggeredGridState parameter\n4. Modified NoteListScreen to use new Extended FAB instead of SpeedDialFAB\n\n**Features implemented:**\n- Extended FAB with 'Voice' text and microphone icon (IcRecorder)\n- Scroll-aware behavior: expands when at top, collapses when scrolling\n- Direct integration with existing Quick Record functionality\n- Material 3 compliant design with proper semantics\n\n**Technical decisions:**\n- Used LazyStaggeredGridState instead of LazyListState to match existing grid layout\n- Simplified text to 'Voice' instead of adding new string resource\n- Maintained existing haptic feedback integration\n- Used primaryContainer colors for consistent theming\n\n**Modified files:**\n- /shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/components/ExtendedVoiceFAB.kt (new)\n- /shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/list/NoteList.kt\n- /shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/list/NoteListScreen.kt\n\nBuild tested successfully on Android. This implementation provides a clean, expressive Extended FAB that integrates seamlessly with the existing unified action buttons system.
