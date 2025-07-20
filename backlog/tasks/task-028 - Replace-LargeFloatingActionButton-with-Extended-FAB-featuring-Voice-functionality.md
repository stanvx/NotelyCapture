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
