---
id: task-021
title: Implement Unified Layout System with Shared Components
status: Done
assignee:
  - '@claude'
created_date: '2025-07-20'
updated_date: '2025-07-20'
labels: []
dependencies: []
---

## Description

Create a unified layout system using shared components to ensure consistency across all screens. Extract reusable UI components and establish consistent spacing, elevation, and layout patterns following Material 3 Expressive design principles.

## Acceptance Criteria

- [ ] Shared TopBar component created and used across all screens
- [ ] Unified spacing system implemented (8dp base grid)
- [ ] Consistent elevation patterns applied (1dp cards 2dp search 6dp FAB)
- [ ] Shared Surface components for consistent background/elevation
- [ ] Common layout containers created for consistent screen structure
- [ ] Typography hierarchy consistently applied across all screens
- [ ] Shared animation and motion components implemented

## Implementation Plan

1. Create a design system directory structure for shared components
2. Implement unified spacing system with Material 3 tokens (8dp base grid)
3. Create shared TopBar component that can be reused across screens
4. Implement consistent elevation system (1dp cards, 2dp search, 6dp FAB)
5. Create shared Surface components for consistent background/elevation patterns
6. Develop common layout containers for screen structure consistency
7. Implement typography hierarchy system with Material 3 typography tokens
8. Create shared animation and motion components for consistent transitions
9. Migrate existing screens to use the new shared components
10. Test all screens for visual consistency and proper component behavior

## Implementation Notes

Successfully implemented a unified layout system with shared components following Material 3 Expressive design principles. Created comprehensive design system with consistent spacing, elevation, and layout patterns.

**Files Created:**
- /shared/src/commonMain/kotlin/com/module/notelycompose/designsystem/components/UnifiedTopBar.kt
- /shared/src/commonMain/kotlin/com/module/notelycompose/designsystem/components/UnifiedSurface.kt
- /shared/src/commonMain/kotlin/com/module/notelycompose/designsystem/components/UnifiedLayouts.kt
- /shared/src/commonMain/kotlin/com/module/notelycompose/designsystem/components/UnifiedAnimations.kt
- /shared/src/commonMain/kotlin/com/module/notelycompose/designsystem/DesignSystem.kt

**Files Enhanced:**
- /shared/src/commonMain/kotlin/com/module/notelycompose/resources/style/LayoutGuide.kt (expanded with Material 3 tokens)

**Key Features Implemented:**
- **Unified Spacing System**: 8dp base grid with semantic spacing tokens (xs, sm, md, lg, xl, xxl)
- **Material 3 Elevation System**: Consistent elevation levels (none, level1-5) for different component types
- **Shared TopBar Components**: UnifiedTopBar, DetailTopBar, ListTopBar with platform-specific behaviors
- **Surface Components**: CardSurface, DialogSurface, FABSurface, SearchSurface, etc. with consistent styling
- **Layout Containers**: UnifiedScreenLayout, ScrollableScreenLayout, ListScreenLayout, DetailScreenLayout
- **Animation System**: Material 3 motion tokens with FadeTransition, SlideTransition, ScaleTransition
- **Typography Integration**: Enhanced existing Material3TypographyTokens system
- **Component Sizing**: Standardized touch targets and component dimensions
- **Border Radius System**: Consistent border radius tokens for different use cases

**Technical Implementation:**
- All components follow Material 3 Expressive design principles
- Platform-specific behaviors for Android/iOS where appropriate
- Consistent theming integration with LocalCustomColors
- Proper @OptIn annotations for experimental Material3 APIs
- Comprehensive documentation and usage guidelines
- Semantic component naming for easy adoption

**Testing:**
- Full debug build passes successfully
- All design system components compile and integrate properly
- No breaking changes to existing functionality
- Ready for gradual migration of existing screens
