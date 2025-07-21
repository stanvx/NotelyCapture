package com.module.notelycompose.designsystem

/**
 * Central Design System for Notely Capture
 * 
 * This file serves as the main entry point for the unified design system,
 * providing consistent components, tokens, and patterns following
 * Material 3 Expressive design principles.
 * 
 * Import this file to access all design system components:
 * ```
 * import com.module.notelycompose.designsystem.*
 * ```
 */

// Design system components are accessible via their full package names:
// - com.module.notelycompose.designsystem.components.*
// - com.module.notelycompose.resources.style.LayoutGuide
// - com.module.notelycompose.notes.ui.theme.Material3TypographyTokens

// Import all components for internal access
import com.module.notelycompose.designsystem.components.*
import com.module.notelycompose.resources.style.LayoutGuide
import com.module.notelycompose.notes.ui.theme.Material3TypographyTokens

/**
 * Design System Documentation
 * 
 * ## Usage Guidelines
 * 
 * ### Spacing System
 * Use LayoutGuide.Spacing for consistent spacing:
 * - xs (4dp) - tight spacing
 * - sm (8dp) - base unit, primary spacing
 * - md (16dp) - standard spacing between components
 * - lg (24dp) - large spacing for sections
 * - xl (32dp) - extra large spacing
 * - xxl (40dp) - maximum spacing
 * 
 * ### Elevation System
 * Use LayoutGuide.Elevation for consistent elevations:
 * - none (0dp) - flat surfaces
 * - level1 (1dp) - cards, chips
 * - level2 (3dp) - search bars, text fields
 * - level3 (6dp) - FABs, app bars
 * - level4 (8dp) - navigation drawer
 * - level5 (12dp) - modal dialogs
 * 
 * ### Typography
 * Use Material3TypographyTokens for semantic typography:
 * - noteTitle() - for note titles
 * - noteBody() - for note content
 * - noteTimestamp() - for metadata
 * - buttonText() - for button labels
 * - appBarTitle() - for app bar titles
 * - cardTitle() - for card headers
 * - caption() - for supporting text
 * 
 * ### Layouts
 * Choose appropriate layout components:
 * - UnifiedScreenLayout - basic screen structure
 * - ScrollableScreenLayout - for scrolling content
 * - ListScreenLayout - for list-based screens
 * - DetailScreenLayout - for editing/viewing content
 * - DialogLayout - for modal dialogs
 * 
 * ### Surfaces
 * Use semantic surface components:
 * - CardSurface - for content cards
 * - DialogSurface - for modal content
 * - FABSurface - for floating actions
 * - SearchSurface - for search inputs
 * - BottomSheetSurface - for bottom sheets
 * - NavigationSurface - for navigation components
 * 
 * ### Animations
 * Apply consistent motion patterns:
 * - FadeTransition - for content appearing/disappearing
 * - SlideTransition - for directional content changes
 * - ScaleTransition - for emphasis and attention
 * - SharedElementTransition - for seamless transitions
 * - AnimatedCard - for interactive cards
 * 
 * ## Migration Guide
 * 
 * ### From existing components:
 * 1. Replace TopAppBar with UnifiedTopBar or specific variants
 * 2. Replace Surface with appropriate semantic surfaces
 * 3. Replace manual Scaffold usage with layout components
 * 4. Replace custom animations with unified transitions
 * 5. Update spacing to use LayoutGuide.Spacing tokens
 * 6. Update elevations to use LayoutGuide.Elevation tokens
 * 
 * ### Best Practices:
 * - Always use design system components over custom implementations
 * - Prefer semantic components over generic ones
 * - Use consistent spacing and elevation tokens
 * - Apply animations for better user experience
 * - Follow platform-specific patterns where appropriate
 */