# Material 3 Expressive Design Implementation Guide

## Overview

This document provides comprehensive technical specifications and implementation guidelines for enhancing Notely Capture with Material 3 Expressive design patterns. All development tasks should reference this document for consistency and compliance.

---

## Table of Contents

1. [Design Principles](#design-principles)
2. [File Structure & Organization](#file-structure--organization)
3. [Component Specifications](#component-specifications)
4. [Color System Implementation](#color-system-implementation)
5. [Typography Standards](#typography-standards)
6. [Animation & Motion Guidelines](#animation--motion-guidelines)
7. [Accessibility Requirements](#accessibility-requirements)
8. [Platform-Specific Considerations](#platform-specific-considerations)
9. [Performance Standards](#performance-standards)
10. [Testing & Validation](#testing--validation)

---

## Design Principles

### Material 3 Expressive Core Values
1. **Personal**: Dynamic colors that adapt to user preferences
2. **Adaptive**: Responsive design that works across all screen sizes
3. **Vibrant**: Rich animations and micro-interactions
4. **Accessible**: WCAG 2.1 AAA compliance minimum standard

### Implementation Philosophy
- **Systematic Approach**: Use established patterns before creating custom solutions
- **Performance First**: Optimize for 60fps animations and smooth interactions
- **Accessibility by Design**: Include accessibility considerations from the start
- **Platform Consistency**: Maintain design coherence across Android and iOS

---

## File Structure & Organization

### Current Theme Architecture
```
shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/theme/
├── Color.kt                              # Base color definitions
├── CustomColors.kt                       # Legacy custom colors (to be migrated)
├── Theme.kt                             # Main theme composition
├── MaterialSymbols.kt                   # Icon definitions
├── Material3ExpressiveTypography.kt     # Typography system
└── Material3ExpressiveShapes.kt         # Shape system
```

### Target Design System Structure
```
shared/src/commonMain/kotlin/com/module/notelycompose/designsystem/
├── theme/
│   ├── Material3ColorScheme.kt          # Dynamic color system
│   ├── Material3Typography.kt           # Optimized typography
│   ├── Material3Shapes.kt               # Shape tokens
│   ├── Material3Motion.kt               # Motion specifications
│   └── Theme.kt                         # Main theme provider
├── tokens/
│   ├── ColorTokens.kt                   # Semantic color tokens
│   ├── TypographyTokens.kt             # Typography tokens
│   ├── SpacingTokens.kt                # Layout spacing
│   └── ElevationTokens.kt              # Surface elevation
├── components/
│   ├── buttons/                         # Button component library
│   ├── cards/                          # Card variants
│   ├── inputs/                         # Input components
│   └── navigation/                     # Navigation components
└── foundations/
    ├── accessibility/                   # Accessibility utilities
    ├── animations/                     # Animation primitives
    └── layout/                         # Layout utilities
```

---

## Component Specifications

### 1. Calendar Components

#### Enhanced Date Selection
**Files to Modify:**
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/calendar/CalendarScreen.kt`

**Key Requirements:**
- Implement Material 3 state layers for date interaction
- Add proper focus indicators and ripple effects
- Use semantic accessibility roles (`Role.Button`)
- Implement proper color contrast ratios (4.5:1 minimum)

**Technical Specifications:**
```kotlin
// Required state layer implementation
Surface(
    onClick = onClick,
    modifier = Modifier.semantics { 
        role = Role.Button
        contentDescription = "Date ${date.dayOfMonth}, ${getMonthName(date.month)}"
    },
    interactionSource = remember { MutableInteractionSource() },
    shape = RoundedCornerShape(12.dp),
    color = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.secondaryContainer
        else -> Color.Transparent
    }
)
```

#### Note Indicators Enhancement
**Requirements:**
- Replace simple dots with expressive badges
- Differentiate voice notes with microphone icons
- Use semantic colors from theme system
- Add count indicators for multiple notes

### 2. Rich Text Editor System

#### Security Enhancements
**Files to Modify:**
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/presentation/helpers/RichTextEditorHelper.kt`
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/presentation/detail/TextEditorViewModel.kt`

**Critical Security Requirements:**
1. **HTML Sanitization**: All `setHtml()` calls must use OWASP HTML Sanitizer
2. **Path Validation**: Audio file paths must be validated against safe directories
3. **Input Validation**: All user inputs must be validated before processing

**Implementation Pattern:**
```kotlin
// Required HTML sanitization
private val htmlSanitizer = createHtmlSanitizer()

fun setContent(content: String) {
    val sanitizedContent = htmlSanitizer.sanitize(content)
    _richTextState.value = RichTextState().apply {
        setHtml(sanitizedContent)
    }
}

// Required path validation
private fun isPathSafe(filePath: String): Boolean {
    val safeDir = File(getSafeRecordingsDirectory()).canonicalPath
    val requestedFile = File(filePath)
    return requestedFile.canonicalPath.startsWith(safeDir)
}
```

### 3. Note List Components

#### Enhanced Note Visualization
**Files to Modify:**
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/list/EnhancedNoteItem.kt`
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/list/OptimizedNoteCard.kt`

**Design Requirements:**
- Implement dynamic color-based note categorization
- Add expressive note type indicators
- Enhance content preview with proper typography hierarchy
- Implement consistent animation patterns

### 4. Typography System

#### Performance Optimization
**Files to Modify:**
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/theme/Material3ExpressiveTypography.kt`
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/theme/Theme.kt`

**Critical Performance Fix:**
```kotlin
// BEFORE (Performance Issue)
@Composable
fun createMaterial3ExpressiveTypography(): Typography { /* ... */ }

// AFTER (Optimized)
val Material3ExpressiveTypography = Typography(
    // Typography definitions
)
```

---

## Color System Implementation

### Dynamic Color Integration
**Requirements:**
- Support Material You dynamic colors on Android 12+
- Provide fallback color palettes for older versions
- Implement user preference for enabling/disabling dynamic colors

**Implementation Pattern:**
```kotlin
@Composable
fun AppTheme(
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicColorScheme(LocalContext.current)
        }
        else -> {
            if (isSystemInDarkTheme()) darkScheme else lightScheme
        }
    }
    // Theme implementation
}
```

### Semantic Color Tokens
**Required Extensions:**
```kotlin
// Capture method colors
val ColorScheme.voiceNoteContainer: Color
val ColorScheme.onVoiceNoteContainer: Color
val ColorScheme.textNoteContainer: Color
val ColorScheme.onTextNoteContainer: Color

// State-specific colors
val ColorScheme.successContainer: Color
val ColorScheme.warningContainer: Color
val ColorScheme.infoContainer: Color
```

---

## Typography Standards

### Font Weight Requirements
**Missing Font Files to Add:**
- `poppins_medium.ttf` (FontWeight.Medium - 500)
- `poppins_semibold.ttf` (FontWeight.SemiBold - 600)

**File Location:**
`shared/src/commonMain/composeResources/font/`

### Typography Token Pattern
**Implementation Standard:**
```kotlin
// Use extension properties instead of recreation
val Typography.noteTitle: TextStyle
    get() = this.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.15.sp
    )
```

---

## Animation & Motion Guidelines

### Motion Tokens
**Standard Duration Values:**
```kotlin
object MotionTokens {
    const val DURATION_SHORT = 150        // Minor state changes
    const val DURATION_MEDIUM = 300       // Standard transitions
    const val DURATION_LONG = 500         // Emphasized transitions
    
    val EASING_STANDARD = CubicBezierEasing(0.2f, 0.0f, 0.8f, 1.0f)
    val EASING_EMPHASIZED = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EASING_DECELERATE = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
}
```

### Animation Best Practices
1. **Use `remember` for Animation State**: Prevent recomposition issues
2. **Stagger Animations**: Use delays for list item animations
3. **Respect Accessibility**: Honor `AccessibilityManager.isReduceMotionEnabled`
4. **Performance**: Keep animations at 60fps, use `graphicsLayer` for transforms

---

## Accessibility Requirements

### Minimum Standards
- **Contrast Ratio**: 4.5:1 for normal text, 3:1 for large text
- **Touch Targets**: Minimum 48dp for interactive elements
- **Focus Management**: Proper focus order and visual indicators
- **Screen Reader**: Comprehensive content descriptions and state announcements

### Required Semantic Markup
```kotlin
// Calendar dates
modifier.semantics {
    role = Role.Button
    contentDescription = "Date ${date.dayOfMonth}, ${monthName}"
    stateDescription = if (hasNotes) "$noteCount notes" else "No notes"
}

// Note items
modifier.semantics {
    contentDescription = buildNoteDescription(note)
    customActions = listOf(
        CustomAccessibilityAction("Edit note") { /* action */ },
        CustomAccessibilityAction("Delete note") { /* action */ }
    )
}
```

---

## Platform-Specific Considerations

### Android Specifications
- **Target SDK**: API 34 (Android 14)
- **Dynamic Colors**: Support Android 12+ Material You
- **Haptic Feedback**: Use appropriate `HapticFeedbackType` values
- **Navigation**: Predictive back gesture support

### iOS Specifications
- **Target Version**: iOS 15+
- **Font Loading**: Static Material Symbols font integration
- **Haptic Feedback**: Use iOS-appropriate feedback patterns
- **Navigation**: Native iOS navigation feel with Material design

---

## Performance Standards

### Requirements
- **Animation**: Maintain 60fps during all animations
- **Memory**: No memory leaks in audio components
- **Startup**: Typography system optimization for faster app launch
- **Scroll**: Smooth scrolling in note lists with large datasets

### Optimization Patterns
```kotlin
// Use derivedStateOf for expensive calculations
val isExpanded by remember {
    derivedStateOf {
        scrollState.firstVisibleItemIndex == 0 && 
        scrollState.firstVisibleItemScrollOffset < 400
    }
}

// Proper resource cleanup
DisposableEffect(controller) {
    onDispose {
        controller.dispose()
    }
}
```

---

## Testing & Validation

### Required Tests
1. **Visual Regression**: Screenshot tests for component variations
2. **Accessibility**: TalkBack/VoiceOver navigation tests
3. **Animation**: Performance tests for 60fps compliance
4. **Color Contrast**: Automated contrast ratio validation
5. **Security**: Input sanitization and path validation tests

### Validation Tools
- **Accessibility Scanner**: Android accessibility validation
- **Layout Inspector**: View hierarchy and constraint validation
- **Perfetto**: Animation performance profiling
- **Compose Inspector**: State and recomposition analysis

---

## Implementation Checklist

### Pre-Implementation
- [ ] Review this design document thoroughly
- [ ] Understand the specific component requirements
- [ ] Set up testing environment with accessibility tools
- [ ] Familiarize yourself with Material 3 design guidelines

### During Implementation
- [ ] Follow file structure guidelines
- [ ] Use semantic color tokens from theme system
- [ ] Implement proper accessibility markup
- [ ] Add appropriate animations with motion tokens
- [ ] Write unit tests for new components

### Post-Implementation
- [ ] Run accessibility scanner validation
- [ ] Test with TalkBack/VoiceOver enabled
- [ ] Validate color contrast ratios
- [ ] Performance test animations at 60fps
- [ ] Code review with focus on security and performance

---

## Questions & Support

For questions about this implementation guide, refer to:
1. [Material 3 Design Guidelines](https://m3.material.io/)
2. [Compose Accessibility Documentation](https://developer.android.com/jetpack/compose/accessibility)
3. [Project CLAUDE.md](../CLAUDE.md) for project-specific guidelines

This document should be updated as implementation patterns evolve and new requirements emerge.