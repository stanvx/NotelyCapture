---
id: task-046
title: Enhance Calendar UI with Material 3 Expressive Design
status: To Do
assignee: []
created_date: '2025-07-26'
labels: []
dependencies: []
---
# task-046 - Enhance Calendar UI with Material 3 Expressive Design

## Description

Transform the calendar interface to fully embrace Material 3 Expressive design patterns, making it more visually engaging and accessible while maintaining excellent usability. The current calendar has a good foundation but lacks proper Material 3 interaction patterns and expressive visual elements.

**Reference Document**: See [Material 3 Expressive Design Implementation Guide](../docs/doc-001%20-%20Material%203%20Expressive%20Design%20Implementation%20Guide.md) - Section "Calendar Components"

## Acceptance Criteria

- [ ] Calendar date selection implements proper Material 3 state layers
- [ ] Note indicators use expressive badges instead of simple dots
- [ ] Calendar navigation uses Material 3 IconButton patterns
- [ ] Accessibility is enhanced with proper semantic roles
- [ ] Calendar animations follow Material 3 motion specifications

## Implementation Plan

### Phase 1: Enhanced Date Selection with Material 3 State Layers

**Files to Modify:**
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/calendar/CalendarScreen.kt`

**Current Problem:**
```kotlin
// Current basic scale animation (Lines 450-460)
val scale by animateFloatAsState(
    targetValue = when {
        isPressed -> 0.92f
        isSelected -> 1.08f
        else -> 1f
    }
)
```

**Step 1: Implement Material 3 State Layer System**
```kotlin
// Create new composable for proper Material 3 date cell
@Composable
private fun Material3DateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    notesCount: Int,
    hasVoiceNotes: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    
    // Material 3 compliant surface with proper interaction feedback
    Surface(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .semantics { 
                role = Role.Button
                contentDescription = buildDateDescription(date, notesCount, hasVoiceNotes)
                if (isSelected) stateDescription = "Selected"
                if (isToday) stateDescription = "Today"
            },
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium, // 12dp corner radius
        color = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            isToday -> MaterialTheme.colorScheme.secondaryContainer
            else -> Color.Transparent
        },
        border = if (isToday && !isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        } else null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Date number with proper typography
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = when {
                        isSelected -> FontWeight.Bold
                        isToday -> FontWeight.SemiBold
                        else -> FontWeight.Medium
                    },
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                // Enhanced note indicators
                if (notesCount > 0) {
                    Material3NoteIndicator(
                        count = notesCount,
                        hasVoiceNotes = hasVoiceNotes,
                        isSelected = isSelected,
                        isToday = isToday
                    )
                }
            }
        }
    }
}

private fun buildDateDescription(
    date: LocalDate,
    notesCount: Int,
    hasVoiceNotes: Boolean
): String {
    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return buildString {
        append("${monthName} ${date.dayOfMonth}")
        if (notesCount > 0) {
            append(", $notesCount ${if (notesCount == 1) "note" else "notes"}")
            if (hasVoiceNotes) {
                append(" including voice notes")
            }
        }
    }
}
```

**Step 2: Create Expressive Note Indicators**
```kotlin
@Composable
private fun Material3NoteIndicator(
    count: Int,
    hasVoiceNotes: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        hasVoiceNotes -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }
    
    val contentColor = when {
        hasVoiceNotes -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSecondary
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor.copy(
            alpha = if (isSelected || isToday) 1f else 0.8f
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            // Voice note microphone icon
            if (hasVoiceNotes) {
                MaterialIcon(
                    symbol = MaterialSymbols.Mic,
                    size = 8.dp,
                    tint = contentColor,
                    style = MaterialIconStyle.Filled
                )
            }
            
            // Note count for multiple notes
            if (count > 1) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp
                    ),
                    color = contentColor
                )
            }
        }
    }
}
```

### Phase 2: Enhanced Calendar Navigation

**Files to Modify:**
- `shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/calendar/CalendarScreen.kt`

**Step 1: Implement Material 3 Navigation Header**
```kotlin
@Composable
private fun Material3CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp, // Material 3 elevation token
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous month button
            FilledIconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPreviousMonth()
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.semantics {
                    contentDescription = "Previous month"
                }
            ) {
                MaterialIcon(
                    symbol = MaterialSymbols.KeyboardArrowLeft,
                    size = 24.dp
                )
            }
            
            // Month and year display with animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = currentMonth.month.name,
                    transitionSpec = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) togetherWith slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    },
                    label = "month_transition"
                ) { monthName ->
                    Text(
                        text = monthName.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Text(
                    text = currentMonth.year.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            // Next month button
            FilledIconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNextMonth()
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.semantics {
                    contentDescription = "Next month"
                }
            ) {
                MaterialIcon(
                    symbol = MaterialSymbols.KeyboardArrowRight,
                    size = 24.dp
                )
            }
        }
    }
}
```

### Phase 3: Enhanced Calendar Grid with Accessibility

**Step 1: Implement Accessible Calendar Grid**
```kotlin
@Composable
private fun Material3CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    today: LocalDate,
    notesData: Map<LocalDate, List<NoteUiModel>>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Column(
        modifier = modifier.semantics {
            contentDescription = "Calendar for ${currentMonth.month.name} ${currentMonth.year}"
        }
    ) {
        // Day headers with proper semantics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val fullDayNames = listOf(
                "Monday", "Tuesday", "Wednesday", "Thursday", 
                "Friday", "Saturday", "Sunday"
            )
            
            dayNames.forEachIndexed { index, dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = fullDayNames[index]
                            heading()
                        },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar dates grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val lastDayOfMonth = currentMonth.atEndOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val daysInMonth = lastDayOfMonth.dayOfMonth
        
        // Calculate weeks needed
        val weeksNeeded = ((firstDayOfWeek + daysInMonth - 1) / 7) + 1
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(weeksNeeded) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(7) { dayOfWeek ->
                        val dayOfMonth = week * 7 + dayOfWeek - firstDayOfWeek + 1
                        
                        if (dayOfMonth in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayOfMonth)
                            val dayNotes = notesData[date] ?: emptyList()
                            
                            Material3DateCell(
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == today,
                                notesCount = dayNotes.size,
                                hasVoiceNotes = dayNotes.any { it.isVoice },
                                onClick = {
                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.TextHandleMove
                                    )
                                    onDateSelected(date)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Empty space for days not in current month
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
```

### Phase 4: Enhanced Motion and Transitions

**Step 1: Implement Material 3 Motion Specifications**
```kotlin
// Add to CalendarScreen.kt
private object CalendarMotionTokens {
    val EMPHASIZED_EASING = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val STANDARD_EASING = CubicBezierEasing(0.2f, 0.0f, 0.8f, 1.0f)
    const val EMPHASIZED_DURATION = 500
    const val STANDARD_DURATION = 300
    const val SHORT_DURATION = 150
}

@Composable
private fun Material3CalendarTransitions(
    currentMonth: YearMonth,
    content: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = currentMonth,
        transitionSpec = {
            val isForward = targetState.isAfter(initialState)
            
            slideIntoContainer(
                towards = if (isForward) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                },
                animationSpec = tween(
                    durationMillis = CalendarMotionTokens.EMPHASIZED_DURATION,
                    easing = CalendarMotionTokens.EMPHASIZED_EASING
                )
            ) togetherWith slideOutOfContainer(
                towards = if (isForward) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                },
                animationSpec = tween(
                    durationMillis = CalendarMotionTokens.STANDARD_DURATION,
                    easing = CalendarMotionTokens.STANDARD_EASING
                )
            )
        },
        label = "calendar_month_transition"
    ) {
        content()
    }
}
```

### Phase 5: Integration and Testing

**Step 1: Update Main CalendarScreen**
```kotlin
// Update the main CalendarScreen composable to use new components
@Composable
fun CalendarScreen(
    // ... existing parameters
) {
    // ... existing logic
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Material3CalendarHeader(
                currentMonth = currentMonth,
                onPreviousMonth = { /* existing logic */ },
                onNextMonth = { /* existing logic */ }
            )
        }
        
        item {
            Material3CalendarTransitions(
                currentMonth = currentMonth
            ) {
                Material3CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    today = today,
                    notesData = notesData,
                    onDateSelected = onDateSelected
                )
            }
        }
        
        // ... existing note list items
    }
}
```

## Junior Developer Guidelines

### Understanding Material 3 State Layers

**What are State Layers?**
- State layers provide visual feedback for user interactions (hover, focus, press)
- They're semi-transparent overlays that change the appearance of interactive elements
- Material 3 uses them to create consistent interaction patterns

**How to Implement:**
1. Use `MutableInteractionSource` to track interaction states
2. Collect state using `collectIsPressedAsState()`, `collectIsFocusedAsState()`, etc.
3. Apply visual changes based on state (color, elevation, etc.)

### Material 3 Accessibility Best Practices

1. **Use Semantic Roles**: Always add `role = Role.Button` for clickable elements
2. **Provide Content Descriptions**: Describe what the element does, not just what it looks like
3. **Add State Descriptions**: Tell users about the current state (selected, today, etc.)
4. **Use Proper Headings**: Mark section headers with `heading()` modifier

### Animation Guidelines

1. **Use Standard Durations**: 150ms for minor changes, 300ms for standard, 500ms for emphasized
2. **Choose Appropriate Easing**: Standard easing for most transitions, emphasized for important ones
3. **Respect Accessibility**: Check for reduced motion preferences
4. **Keep Animations Purposeful**: Every animation should serve a clear purpose

### Testing Your Implementation

1. **Accessibility Testing**:
   ```bash
   # Enable TalkBack/VoiceOver and test navigation
   # Verify all elements are announced correctly
   # Check focus order makes sense
   ```

2. **Visual Testing**:
   ```kotlin
   // Test different states
   // - Selected dates
   // - Today indicator
   // - Note indicators (voice vs text)
   // - Month transitions
   ```

3. **Performance Testing**:
   ```bash
   # Check animation smoothness at 60fps
   # Verify no memory leaks during month navigation
   ```

### Common Mistakes to Avoid

1. **Don't skip interaction states** - Always implement hover, focus, and press states
2. **Don't use hardcoded colors** - Always use theme colors for consistency
3. **Don't ignore accessibility** - Screen reader support is essential
4. **Don't make animations too fast or slow** - Follow Material 3 timing guidelines

### Code Review Checklist

- [ ] All interactive elements use proper state layers
- [ ] Accessibility semantics are complete and accurate
- [ ] Colors come from Material 3 theme system
- [ ] Animations use Material 3 motion specifications
- [ ] Touch targets are at least 48dp
- [ ] Focus indicators are visible and clear
- [ ] Screen reader announcements are helpful
- [ ] No hardcoded values for colors or dimensions

## Implementation Notes

This enhancement transforms the calendar from a functional interface to an engaging, expressive experience that users will love interacting with. The improvements focus on:

- **Better Accessibility**: Comprehensive screen reader support and keyboard navigation
- **Enhanced Visual Feedback**: Proper state layers and interaction patterns
- **Expressive Design**: Rich note indicators and smooth animations
- **Material 3 Compliance**: Full adherence to Material 3 design guidelines

**Estimated Time**: 6-8 hours for complete implementation
**Priority**: P2 - High (User Experience Enhancement)
**Dependencies**: Typography optimization task should be completed first

The changes maintain all existing functionality while significantly improving the user experience and visual appeal.