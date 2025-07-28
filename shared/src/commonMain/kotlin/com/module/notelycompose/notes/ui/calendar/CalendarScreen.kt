package com.module.notelycompose.notes.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.presentation.list.NoteListViewModel
import com.module.notelycompose.notes.presentation.list.model.NotePresentationModel
import com.module.notelycompose.notes.ui.components.ExtendedVoiceFAB
import com.module.notelycompose.notes.ui.components.MaterialIcon
import com.module.notelycompose.notes.ui.components.MaterialIconStyle
import com.module.notelycompose.notes.ui.theme.voiceNoteIndicatorContainer
import com.module.notelycompose.notes.ui.theme.onVoiceNoteIndicatorContainer
import com.module.notelycompose.notes.ui.theme.textNoteIndicatorContainer
import com.module.notelycompose.notes.ui.theme.onTextNoteIndicatorContainer
import com.module.notelycompose.notes.ui.theme.MaterialSymbols
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import com.module.notelycompose.notes.ui.calendar.CalendarDateMatcher
import com.module.notelycompose.notes.ui.calendar.YearMonthKt
import com.module.notelycompose.notes.ui.calendar.parseToLocalDate
import com.module.notelycompose.notes.ui.calendar.parseToTimeString
import com.module.notelycompose.notes.ui.theme.CardElevationPresets
import com.module.notelycompose.notes.ui.calendar.formatToDisplayString
import com.module.notelycompose.notes.ui.calendar.MonthlyStatsSummary
import com.module.notelycompose.notes.ui.components.UnifiedNoteCard
import com.module.notelycompose.notes.ui.components.NoteCardLayoutMode
import com.module.notelycompose.notes.ui.share.ShareDialog
import com.module.notelycompose.notes.utils.ShareUtils
import com.module.notelycompose.platform.presentation.PlatformViewModel
import com.module.notelycompose.notes.ui.detail.DeleteConfirmationDialog
import com.module.notelycompose.notes.presentation.list.NoteListIntent

// Material 3 Motion Specifications
private object CalendarMotionTokens {
    val EMPHASIZED_EASING = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val STANDARD_EASING = CubicBezierEasing(0.2f, 0.0f, 0.8f, 1.0f)
    const val EMPHASIZED_DURATION = 500
    const val STANDARD_DURATION = 300
    const val SHORT_DURATION = 150
}

/**
 * Helper function to build accessible content descriptions for calendar dates
 */
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

/**
 * Material 3 Enhanced Date Cell with proper state layers and accessibility
 */
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

/**
 * Material 3 Expressive Note Indicator with badges and voice note differentiation
 */
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

/**
 * Material 3 Enhanced Calendar Header with proper navigation patterns
 */
@Composable
private fun Material3CalendarHeader(
    currentMonth: YearMonthKt,
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

/**
 * Material 3 Enhanced Calendar Grid with accessibility and smooth transitions
 */
@Composable
private fun Material3CalendarGrid(
    currentMonth: YearMonthKt,
    selectedDate: LocalDate?,
    today: LocalDate,
    notesData: List<NotePresentationModel>,
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
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal % 7
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
                            val dayNotes = notesData.filter { note ->
                                CalendarDateMatcher.matchesDate(
                                    noteCreatedAt = note.createdAt,
                                    targetDate = date,
                                    noteId = note.id,
                                    enableDebugLogging = false
                                )
                            }
                            
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

/**
 * Material 3 Calendar Transitions with proper motion specifications
 */
@Composable
private fun Material3CalendarTransitions(
    currentMonth: YearMonthKt,
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

/**
 * Material 3 Expressive Calendar Screen for Notely Capture.
 * 
 * Displays a calendar view with capture indicators and allows filtering
 * notes by selected date. Inspired by iOS Calendar but using Material 3 design.
 */
/**
 * CalendarScreen composable.
 *
 * IMPORTANT: The navigateToQuickRecord lambda MUST be connected to your navigation system (e.g., navController.navigateSingleTop(Routes.QuickRecord))
 * for the FAB to work. If not set, the FAB will do nothing.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    navigateBack: () -> Unit,
    navigateToNoteDetails: (String) -> Unit,
    navigateToQuickRecord: (() -> Unit)? = null,
    audioPlayerViewModel: com.module.notelycompose.audio.presentation.AudioPlayerViewModel? = null,
    viewModel: NoteListViewModel = koinViewModel()
) {
    val notesState by viewModel.state.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Platform utilities for sharing functionality
    val platformViewModel: PlatformViewModel = koinViewModel()
    
    // Share dialog state management
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedNoteForSharing by remember { mutableStateOf<com.module.notelycompose.notes.presentation.list.model.NotePresentationModel?>(null) }
    
    // Delete confirmation dialog state management
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedNoteForDeletion by remember { mutableStateOf<com.module.notelycompose.notes.presentation.list.model.NotePresentationModel?>(null) }
    
    // Collect delete operation state for user feedback
    val deleteOperationState by viewModel.deleteOperationState.collectAsState()
    
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var currentMonth by remember { mutableStateOf(YearMonthKt(today.year, today.month)) }
    var selectedDate by remember { mutableStateOf(today) }
    
    // Run date parsing validation on first load (can be disabled in production)
    LaunchedEffect(Unit) {
        val validationResults = CalendarDateMatcher.validateDateParsing()
        println("[Calendar] Date parsing validation completed. Results: $validationResults")
        println(CalendarDateMatcher.checkTimezoneConsistency())
    }
    
    // Memoize calendar data calculations
    val calendarData by remember(notesState.filteredNotes, currentMonth, selectedDate) {
        derivedStateOf {
            CalendarData(
                notesForSelectedDate = notesState.filteredNotes.filter { note ->
                    CalendarDateMatcher.matchesDate(
                        noteCreatedAt = note.createdAt,
                        targetDate = selectedDate,
                        noteId = note.id,
                        enableDebugLogging = false // Individual note logging disabled to reduce noise
                    )
                },
                notesData = notesState.filteredNotes,
                selectedDate = selectedDate,
                currentMonth = currentMonth
            )
        }
    }
    
    // Debug logging moved outside derivedStateOf to reduce duplicate processing
    LaunchedEffect(notesState.filteredNotes, selectedDate) {
        if (notesState.filteredNotes.isNotEmpty()) {
            val debugInfo = CalendarDateMatcher.debugNotesForDate(
                notes = notesState.filteredNotes.map { it.id to it.createdAt },
                targetDate = selectedDate
            )
            println(debugInfo)
        }
    }
    
    // Handle delete operation feedback
    LaunchedEffect(deleteOperationState) {
        when (val state = deleteOperationState) {
            is NoteListViewModel.DeleteOperationState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Note '${state.noteTitle}' deleted successfully",
                    withDismissAction = true
                )
            }
            is NoteListViewModel.DeleteOperationState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    withDismissAction = true
                )
            }
            else -> {} // Idle and InProgress states don't need snackbar
        }
    }
    
    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Calendar",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                            selectedDate = todayDate
                            currentMonth = YearMonthKt(todayDate.year, todayDate.month)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Go to today"
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedVoiceFAB(
                onQuickRecordClick = {
                    if (navigateToQuickRecord != null) {
                        navigateToQuickRecord()
                    } else {
                        // Optionally show a warning or log
                        println("[CalendarScreen] WARNING: navigateToQuickRecord is not set!")
                    }
                },
                lazyListState = lazyListState
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 104.dp // Account for navigation bar + FAB space
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calendar Header with Month Navigation
            item(key = "calendar_header") {
                CalendarHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentMonth = currentMonth.minusMonths(1) 
                    },
                    onNextMonth = { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentMonth = currentMonth.plusMonths(1) 
                    }
                )
            }
            
            // Calendar Grid - Fixed height, no internal scrolling
            item(key = "calendar_grid") {
                CompactCalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    onDateSelected = { date ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedDate = date
                    },
                    notesData = calendarData.notesData
                )
            }
            
            // Selected Date Header
            item(key = "selected_date_header") {
                Text(
                    text = selectedDate.formatToDisplayString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Notes for Selected Date
            if (calendarData.notesForSelectedDate.isEmpty()) {
                item(key = "empty_view") {
                    EmptyDateView()
                }
            } else {
                items(
                    items = calendarData.notesForSelectedDate,
                    key = { it.id }
                ) { note ->
                    UnifiedNoteCard(
                        note = note,
                        layoutMode = NoteCardLayoutMode.CALENDAR,
                        onClick = {
                            // Expand instead of navigate - Edit option in menu navigates
                        },
                        onDeleteClick = { noteId ->
                            selectedNoteForDeletion = note
                            showDeleteDialog = true
                        },
                        onShareClick = { noteId ->
                            selectedNoteForSharing = note
                            showShareDialog = true
                        },
                        onEditClick = { noteId ->
                            navigateToNoteDetails(noteId.toString())
                        },
                        audioPlayerViewModel = audioPlayerViewModel,
                        audioPlayerUiState = audioPlayerViewModel?.onGetUiState(audioPlayerViewModel.uiState.collectAsState().value),
                        maxContentLines = 4
                    )
                }
            }
        }
    }
    
    // Share dialog for note sharing functionality
    if (showShareDialog && selectedNoteForSharing != null) {
        ShareDialog(
            onShareAudioRecording = {
                selectedNoteForSharing?.let { note ->
                    if (ShareUtils.canShareRecording(note.recordingPath)) {
                        platformViewModel.shareRecording(note.recordingPath!!)
                    }
                }
                showShareDialog = false
                selectedNoteForSharing = null
            },
            onShareTexts = {
                selectedNoteForSharing?.let { note ->
                    val shareText = ShareUtils.buildShareText(note)
                    platformViewModel.shareText(shareText)
                }
                showShareDialog = false
                selectedNoteForSharing = null
            },
            onDismiss = { 
                showShareDialog = false
                selectedNoteForSharing = null
            }
        )
    }
    
    // Delete confirmation dialog
    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = {
            showDeleteDialog = false
            selectedNoteForDeletion = null
        },
        onConfirm = {
            selectedNoteForDeletion?.let { note ->
                // Convert NotePresentationModel to NoteUiModel using the mapper
                val noteUiModel = viewModel.onGetUiState(
                    viewModel.state.value.copy(filteredNotes = listOf(note))
                ).firstOrNull()
                
                noteUiModel?.let { uiModel ->
                    viewModel.onProcessIntent(NoteListIntent.OnNoteDeleted(uiModel))
                }
            }
            showDeleteDialog = false
            selectedNoteForDeletion = null
        }
    )
}

// Data class for memoized calendar calculations
private data class CalendarData(
    val notesForSelectedDate: List<NotePresentationModel>,
    val notesData: List<NotePresentationModel>,
    val selectedDate: LocalDate,
    val currentMonth: YearMonthKt
)

@Composable
private fun CalendarHeaderWithStats(
    currentMonth: YearMonthKt,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    monthlyNotes: List<NotePresentationModel>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardElevationPresets.headerCard() // Use gold standard header elevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Increased slightly to accommodate stats
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 100f)
                    )
                )
        ) {
            // Subtle pattern overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw decorative circles
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = 150f,
                    center = Offset(0f, size.height)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = 200f,
                    center = Offset(size.width, 0f)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous month button
                    var isPrevPressed by remember { mutableStateOf(false) }
                    val prevScale by animateFloatAsState(
                        targetValue = if (isPrevPressed) 0.9f else 1f,
                        animationSpec = spring(dampingRatio = 0.4f),
                        label = "prev_scale"
                    )
                    
                    Surface(
                        onClick = onPreviousMonth,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        modifier = Modifier
                            .size(40.dp)
                            .scale(prevScale)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPrevPressed = true
                                        tryAwaitRelease()
                                        isPrevPressed = false
                                    }
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Previous month",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // Month/Year display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentMonth.month.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = currentMonth.year.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Next month button
                    var isNextPressed by remember { mutableStateOf(false) }
                    val nextScale by animateFloatAsState(
                        targetValue = if (isNextPressed) 0.9f else 1f,
                        animationSpec = spring(dampingRatio = 0.4f),
                        label = "next_scale"
                    )
                    
                    Surface(
                        onClick = onNextMonth,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        modifier = Modifier
                            .size(40.dp)
                            .scale(nextScale)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isNextPressed = true
                                        tryAwaitRelease()
                                        isNextPressed = false
                                    }
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Next month",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                // Bottom row: Compact stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CompactStatItem(
                        value = monthlyNotes.size.toString(),
                        label = "Total",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    CompactStatItem(
                        value = monthlyNotes.count { it.isVoice }.toString(),
                        label = "Voice",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    CompactStatItem(
                        value = monthlyNotes.count { !it.isVoice }.toString(),
                        label = "Text",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactStatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonthKt,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardElevationPresets.headerCard() // Use gold standard header elevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 100f)
                    )
                )
        ) {
            // Subtle pattern overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw decorative circles
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = 150f,
                    center = Offset(0f, size.height)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = 200f,
                    center = Offset(size.width, 0f)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous month button with animation
                var isPrevPressed by remember { mutableStateOf(false) }
                val prevScale by animateFloatAsState(
                    targetValue = if (isPrevPressed) 0.9f else 1f,
                    animationSpec = spring(dampingRatio = 0.4f),
                    label = "prev_scale"
                )
                
                Surface(
                    onClick = onPreviousMonth,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .size(48.dp)
                        .scale(prevScale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPrevPressed = true
                                    tryAwaitRelease()
                                    isPrevPressed = false
                                }
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous month",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Month/Year display with enhanced typography
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentMonth.month.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = currentMonth.year.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                // Next month button
                var isNextPressed by remember { mutableStateOf(false) }
                val nextScale by animateFloatAsState(
                    targetValue = if (isNextPressed) 0.9f else 1f,
                    animationSpec = spring(dampingRatio = 0.4f),
                    label = "next_scale"
                )
                
                Surface(
                    onClick = onNextMonth,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .size(48.dp)
                        .scale(nextScale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isNextPressed = true
                                    tryAwaitRelease()
                                    isNextPressed = false
                                }
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next month",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonthKt,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    notesData: List<NotePresentationModel>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardElevationPresets.compact(), // Use compact elevation for grid
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Enhanced day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (index >= 5) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Calendar days grid
            val firstDayOfMonth = currentMonth.atDay(1)
            val daysInMonth = currentMonth.lengthOfMonth()
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal + 1
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Empty cells
                items(firstDayOfWeek - 1) {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }
                
                // Days with enhanced design
                items(daysInMonth) { day ->
                    val date = currentMonth.atDay(day + 1)
                    val notesForDay = notesData.filter { note ->
                        val noteDate = note.createdAt.parseToLocalDate()
                        noteDate == date
                    }
                    
                    ModernCalendarDay(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date,
                        notesCount = notesForDay.size,
                        hasVoiceNotes = notesForDay.any { it.isVoice },
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernCalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    notesCount: Int,
    hasVoiceNotes: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isSelected -> 1.08f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = 400f
        ),
        label = "day_scale"
    )
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    isSelected -> Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                    isToday -> Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                    )
                    notesCount > 0 -> Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    )
                    else -> Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                }
            )
            .border(
                width = if (isToday && !isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    notesCount > 0 -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = when {
                    isSelected || isToday -> FontWeight.Bold
                    else -> FontWeight.Medium
                }
            )
            
            // Visual indicators for notes
            if (notesCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Show different indicators based on note types
                    if (hasVoiceNotes) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                    if (notesCount > 1) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(3.dp)
                                .background(
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    },
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactCalendarGrid(
    currentMonth: YearMonthKt,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    notesData: List<NotePresentationModel>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardElevationPresets.noteCard(), // Use note card elevation for content
        border = BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Compact day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Add a subtle divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Static calendar grid - No LazyVerticalGrid, just regular layout
            val firstDayOfMonth = currentMonth.atDay(1)
            val daysInMonth = currentMonth.lengthOfMonth()
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal + 1
            
            // Calculate weeks needed
            val totalCells = firstDayOfWeek - 1 + daysInMonth
            val weeksNeeded = (totalCells + 6) / 7 // Round up
            
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                var cellIndex = 0
                repeat(weeksNeeded) { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        repeat(7) { dayOfWeek ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            ) {
                                if (cellIndex >= firstDayOfWeek - 1 && cellIndex < firstDayOfWeek - 1 + daysInMonth) {
                                    val day = cellIndex - (firstDayOfWeek - 1) + 1
                                    val date = currentMonth.atDay(day)
                                    val notesForDay = notesData.filter { note ->
                                        val noteDate = note.createdAt.parseToLocalDate()
                                        noteDate == date
                                    }
                                    
                                    CompactCalendarDay(
                                        date = date,
                                        isSelected = date == selectedDate,
                                        isToday = date == Clock.System.now()
                                            .toLocalDateTime(TimeZone.currentSystemDefault()).date,
                                        notesCount = notesForDay.size,
                                        hasVoiceNotes = notesForDay.any { it.isVoice },
                                        onClick = { onDateSelected(date) }
                                    )
                                }
                            }
                            cellIndex++
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactCalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    notesCount: Int,
    hasVoiceNotes: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = "compact_day_scale"
    )
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    notesCount > 0 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            )
            .border(
                width = when {
                    isSelected -> 2.dp
                    isToday && !isSelected -> 2.dp
                    else -> 0.5.dp
                },
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday && !isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
            )
            
            // Compact note indicators
            if (notesCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    repeat(minOf(notesCount, 3)) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDateView() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardElevationPresets.compact(), // Use compact elevation for empty state
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background pattern
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Draw subtle decorative elements
                val dotRadius = 2.dp.toPx()
                for (x in 0..size.width.toInt() step 30) {
                    for (y in 0..size.height.toInt() step 30) {
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.05f),
                            radius = dotRadius,
                            center = Offset(x.toFloat(), y.toFloat())
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated icon
                val infiniteTransition = rememberInfiniteTransition(label = "empty_animation")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "icon_scale"
                )
                
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "No captures yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your Captures for this day will appear here",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun CalendarNoteItem(
    note: NotePresentationModel,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 300f
                )
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { 
            isExpanded = !isExpanded
            // Also call the onClick for navigation
            onClick()
        }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (note.isVoice) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
                                )
                            )
                        }
                    )
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated icon
                val iconScale by animateFloatAsState(
                    targetValue = if (isExpanded) 1.1f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f),
                    label = "icon_scale"
                )
                
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .scale(iconScale),
                    shape = CircleShape,
                    color = if (note.isVoice) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        MaterialIcon(
                            symbol = if (note.isVoice) {
                                MaterialSymbols.Mic
                            } else {
                                MaterialSymbols.Edit
                            },
                            contentDescription = if (note.isVoice) "Voice note" else "Text note",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            size = 24.dp
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Time badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = note.createdAt.parseToTimeString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                            )
                        }
                    }
                    
                    if (note.content.isNotEmpty()) {
                        Text(
                            text = if (isExpanded) {
                                note.content
                            } else {
                                note.content.take(60) + if (note.content.length > 60) "..." else ""
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                    
                    // Expand indicator
                    if (note.content.length > 60) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) {
                                    Icons.Filled.KeyboardArrowUp
                                } else {
                                    Icons.Filled.KeyboardArrowDown
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isExpanded) "Show less" else "Show more",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

