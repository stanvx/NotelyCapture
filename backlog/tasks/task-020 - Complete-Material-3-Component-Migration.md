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

## Implementation Notes

Successfully migrated NoteDetailScreen.kt, ShareDialog.kt, PrepairingDialog.kt, and DownloadModelDialog.kt from Material 2 to Material 3 components. Key changes include:

**Files Modified:**
- /shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/detail/NoteDetailScreen.kt
- /shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/share/ShareDialog.kt  
- /shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/detail/PrepairingDialog.kt
- /shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/detail/DownloadModelDialog.kt

**Technical Changes:**
- Replaced Material 2 Scaffold with Material 3 Scaffold (removed floatingActionButtonPosition parameter)
- Migrated FloatingActionButton to Material 3 (backgroundColor → containerColor, removed elevation parameter)
- Updated AlertDialog from Material 2 to Material 3 (buttons → confirmButton, backgroundColor → containerColor, contentColor → textContentColor)
- Replaced SwipeToDismiss with SwipeToDismissBox (DismissDirection → SwipeToDismissBoxValue, background → backgroundContent, dismissContent → content)
- Updated Surface component (elevation → shadowElevation)
- Changed MaterialTheme.typography.h6 to MaterialTheme.typography.headlineSmall
- Removed @OptIn(ExperimentalMaterialApi::class) annotations where no longer needed

**Testing:**
- Full debug build passes successfully
- All Material 3 components compile and integrate properly with existing theming system
- No breaking changes to existing functionality
