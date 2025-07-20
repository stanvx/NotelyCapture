---
id: task-020
title: Complete Material 3 Component Migration
status: In Progress
assignee:
  - '@claude'
created_date: '2025-07-20'
updated_date: '2025-07-20'
labels: []
dependencies: []
---

## Description

Migrate remaining Material 2 components to Material 3 equivalents for consistent design language across all screens. Focus on NoteDetailScreen.kt which still uses M2 Scaffold, FloatingActionButton, and AlertDialog components.

## Acceptance Criteria

- [ ] All Material 2 components replaced with Material 3 equivalents
- [ ] NoteDetailScreen uses Material 3 Scaffold with proper behavior
- [ ] All FloatingActionButtons migrated to Material 3 variants
- [ ] AlertDialogs replaced with Material 3 dialog components
- [ ] No mixed M2/M3 component usage across the app

## Implementation Plan

1. Audit current codebase for remaining M2 components using grep
2. Focus on NoteDetailScreen.kt as the primary target
3. Replace M2 Scaffold with M3 Scaffold and update imports
4. Migrate FloatingActionButton to M3 variants with proper theming
5. Replace AlertDialog with M3 dialog components
6. Test all migrated components for proper behavior and styling
7. Verify no mixed M2/M3 component usage remains across the app
