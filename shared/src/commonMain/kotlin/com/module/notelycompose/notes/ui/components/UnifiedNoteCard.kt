package com.module.notelycompose.notes.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.audio.presentation.AudioPlayerViewModel
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.audio.ui.player.CompactAudioPlayer
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.notes.presentation.list.model.NotePresentationModel
import com.module.notelycompose.notes.ui.list.NoteColorScheme
import com.module.notelycompose.notes.ui.list.generateNoteColors
import com.module.notelycompose.notes.ui.theme.CardElevationPresets
import com.module.notelycompose.notes.ui.theme.MaterialSymbols
import com.module.notelycompose.notes.ui.calendar.parseToTimeString
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Unified Note Card component that replaces the previous separate note card implementations.
 * This component consolidates functionality from multiple card types into a single, flexible component.
 * 
 * Features:
 * - Flexible layout modes (LIST, CALENDAR)
 * - Audio player integration for voice notes
 * - Consistent action menu using NoteActionsDropdown
 * - Smart content preview with expansion
 * - Proper accent strips and visual indicators
 * - Material 3 elevation and color schemes
 * - Haptic feedback throughout
 * - Accessibility support
 * - Support for both NoteUiModel and NotePresentationModel
 */

/**
 * Layout mode enumeration for the unified note card
 */
enum class NoteCardLayoutMode {
    LIST,       // Optimized for list display with relative time
    CALENDAR    // Optimized for calendar display with date/time formatting
}

/**
 * Note type enumeration for Material 3 categorization
 */
enum class NoteType {
    Voice, Text, Starred
}

/**
 * Dynamic color scheme for note categorization
 */
data class NoteColorScheme(
    val container: Color,
    val onContainer: Color,
    val accent: Color,
    val outline: Color
)

/**
 * Common interface for note data to support both models
 */
interface NoteCardData {
    val id: Long
    val title: String
    val content: String
    val isStarred: Boolean
    val isVoice: Boolean
    val createdAt: String
    val recordingPath: String
    val words: Int
    val audioDurationMs: Int
}

/**
 * Adapter for NoteUiModel to implement NoteCardData
 */
class NoteUiModelAdapter(private val note: NoteUiModel) : NoteCardData {
    override val id: Long = note.id
    override val title: String = note.title
    override val content: String = note.content
    override val isStarred: Boolean = note.isStarred
    override val isVoice: Boolean = note.isVoice
    override val createdAt: String = note.createdAt
    override val recordingPath: String = note.recordingPath
    override val words: Int = note.words
    override val audioDurationMs: Int = note.audioDurationMs
}

/**
 * Adapter for NotePresentationModel to implement NoteCardData
 */
class NotePresentationModelAdapter(private val note: NotePresentationModel) : NoteCardData {
    override val id: Long = note.id
    override val title: String = note.title
    override val content: String = note.content
    override val isStarred: Boolean = note.isStarred
    override val isVoice: Boolean = note.isVoice
    override val createdAt: String = note.createdAt
    override val recordingPath: String = note.recordingPath
    override val words: Int = note.words
    override val audioDurationMs: Int = note.audioDurationMs
}

/**
 * Generate dynamic colors based on note characteristics
 */
@Composable
fun generateUnifiedNoteColors(note: NoteCardData): NoteColorScheme {
    val colorScheme = MaterialTheme.colorScheme
    
    return when {
        note.isStarred && note.isVoice -> NoteColorScheme(
            container = colorScheme.tertiaryContainer,
            onContainer = colorScheme.onTertiaryContainer,
            accent = colorScheme.tertiary,
            outline = colorScheme.tertiary.copy(alpha = 0.3f)
        )
        note.isVoice -> NoteColorScheme(
            container = colorScheme.primaryContainer,
            onContainer = colorScheme.onPrimaryContainer,
            accent = colorScheme.primary,
            outline = colorScheme.primary.copy(alpha = 0.3f)
        )
        note.isStarred -> NoteColorScheme(
            container = colorScheme.secondaryContainer,
            onContainer = colorScheme.onSecondaryContainer,
            accent = colorScheme.secondary,
            outline = colorScheme.secondary.copy(alpha = 0.3f)
        )
        else -> NoteColorScheme(
            container = colorScheme.surfaceContainer,
            onContainer = colorScheme.onSurface,
            accent = colorScheme.outline,
            outline = colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

/**
 * Material 3 Note Type Indicator with dynamic colors and icons
 */
@Composable
fun UnifiedNoteTypeIndicator(
    noteType: NoteType,
    audioDurationMs: Int? = null,
    layoutMode: NoteCardLayoutMode = NoteCardLayoutMode.LIST,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    val (containerColor, contentColor, icon, label) = when (noteType) {
        NoteType.Voice -> NoteTypeTheme(
            container = colorScheme.primaryContainer,
            content = colorScheme.onPrimaryContainer,
            icon = MaterialSymbols.Mic,
            label = audioDurationMs?.let { formatDuration(it.toLong()) } ?: "Voice"
        )
        NoteType.Text -> NoteTypeTheme(
            container = colorScheme.secondaryContainer,
            content = colorScheme.onSecondaryContainer,
            icon = MaterialSymbols.TextFields,
            label = "Text"
        )
        NoteType.Starred -> NoteTypeTheme(
            container = colorScheme.tertiaryContainer,
            content = colorScheme.onTertiaryContainer,
            icon = MaterialSymbols.Star,
            label = "Starred"
        )
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MaterialIcon(
                symbol = icon,
                size = 16.dp,
                tint = contentColor,
                style = MaterialIconStyle.Filled,
                contentDescription = null
            )
            
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Theme data for note type indicators
 */
private data class NoteTypeTheme(
    val container: Color,
    val content: Color,
    val icon: String,
    val label: String
)

/**
 * Main UnifiedNoteCard component that supports both NoteUiModel and NotePresentationModel
 */
@Composable
fun UnifiedNoteCard(
    note: NoteUiModel,
    layoutMode: NoteCardLayoutMode = NoteCardLayoutMode.LIST,
    isExpanded: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onShareClick: (Long) -> Unit = {},
    onEditClick: (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    audioPlayerViewModel: AudioPlayerViewModel? = null,
    audioPlayerUiState: AudioPlayerUiState? = null,
    modifier: Modifier = Modifier,
    maxContentLines: Int = if (layoutMode == NoteCardLayoutMode.CALENDAR) 4 else 3
) {
    val noteData = NoteUiModelAdapter(note)
    UnifiedNoteCardInternal(
        noteData = noteData,
        layoutMode = layoutMode,
        isExpanded = isExpanded,
        onClick = onClick,
        onLongClick = onLongClick,
        onShareClick = onShareClick,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick,
        audioPlayerViewModel = audioPlayerViewModel,
        audioPlayerUiState = audioPlayerUiState,
        modifier = modifier,
        maxContentLines = maxContentLines
    )
}

/**
 * UnifiedNoteCard overload for NotePresentationModel
 */
@Composable
fun UnifiedNoteCard(
    note: NotePresentationModel,
    layoutMode: NoteCardLayoutMode = NoteCardLayoutMode.CALENDAR,
    isExpanded: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onShareClick: (Long) -> Unit = {},
    onEditClick: (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    audioPlayerViewModel: AudioPlayerViewModel? = null,
    audioPlayerUiState: AudioPlayerUiState? = null,
    modifier: Modifier = Modifier,
    maxContentLines: Int = if (layoutMode == NoteCardLayoutMode.CALENDAR) 4 else 3
) {
    val noteData = NotePresentationModelAdapter(note)
    UnifiedNoteCardInternal(
        noteData = noteData,
        layoutMode = layoutMode,
        isExpanded = isExpanded,
        onClick = onClick,
        onLongClick = onLongClick,
        onShareClick = onShareClick,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick,
        audioPlayerViewModel = audioPlayerViewModel,
        audioPlayerUiState = audioPlayerUiState,
        modifier = modifier,
        maxContentLines = maxContentLines
    )
}

/**
 * Internal implementation of the unified note card
 */
@Composable
private fun UnifiedNoteCardInternal(
    noteData: NoteCardData,
    layoutMode: NoteCardLayoutMode,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    onShareClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    audioPlayerViewModel: AudioPlayerViewModel?,
    audioPlayerUiState: AudioPlayerUiState?,
    modifier: Modifier,
    maxContentLines: Int
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Generate dynamic colors based on note type
    val noteColors = generateUnifiedNoteColors(noteData)
    
    // Animation for press feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "note_card_scale"
    )
    
    Card(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 300f
                )
            )
            .semantics {
                contentDescription = buildNoteAccessibilityDescription(noteData)
                stateDescription = buildNoteStateDescription(noteData)
                
                // Custom actions for screen readers
                customActions = buildList {
                    add(CustomAccessibilityAction("Edit note") {
                        onClick()
                        true
                    })
                    
                    if (onLongClick != null) {
                        add(CustomAccessibilityAction("Note options") {
                            onLongClick()
                            true
                        })
                    }
                    
                    if (noteData.isVoice) {
                        add(CustomAccessibilityAction("Play audio") {
                            // Audio play action
                            true
                        })
                    }
                }
            }
            .let { currentModifier ->
                if (onLongClick != null) {
                    currentModifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongClick()
                            }
                        )
                    }
                } else currentModifier
            },
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = noteColors.container,
            contentColor = noteColors.onContainer
        ),
        elevation = CardElevationPresets.noteCard(),
        border = if (layoutMode == NoteCardLayoutMode.CALENDAR) {
            androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Dynamic accent strip on the left
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (layoutMode == NoteCardLayoutMode.CALENDAR) 3.dp else 4.dp)
                    .background(
                        if (layoutMode == NoteCardLayoutMode.LIST) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    noteColors.accent,
                                    noteColors.accent.copy(alpha = 0.6f),
                                    noteColors.accent.copy(alpha = 0.3f)
                                )
                            )
                        } else {
                            androidx.compose.ui.graphics.SolidColor(noteColors.accent)
                        }
                    )
            )
            
            // Main content area with layout-specific content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = if (layoutMode == NoteCardLayoutMode.CALENDAR) 12.dp else 16.dp,
                        bottom = if (layoutMode == NoteCardLayoutMode.CALENDAR) 12.dp else 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(
                    if (layoutMode == NoteCardLayoutMode.CALENDAR) 6.dp else 12.dp
                )
            ) {
                when (layoutMode) {
                    NoteCardLayoutMode.LIST -> {
                        ListModeContent(
                            noteData = noteData,
                            noteColors = noteColors,
                            isExpanded = isExpanded,
                            maxContentLines = maxContentLines,
                            audioPlayerViewModel = audioPlayerViewModel,
                            audioPlayerUiState = audioPlayerUiState,
                            onShareClick = onShareClick,
                            onEditClick = onEditClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                    NoteCardLayoutMode.CALENDAR -> {
                        CalendarModeContent(
                            noteData = noteData,
                            noteColors = noteColors,
                            isExpanded = isExpanded,
                            maxContentLines = maxContentLines,
                            onShareClick = onShareClick,
                            onEditClick = onEditClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * Content layout optimized for list mode
 */
@Composable
private fun ListModeContent(
    noteData: NoteCardData,
    noteColors: NoteColorScheme,
    isExpanded: Boolean,
    maxContentLines: Int,
    audioPlayerViewModel: AudioPlayerViewModel?,
    audioPlayerUiState: AudioPlayerUiState?,
    onShareClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    // Header with note type and metadata
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Note type indicator - prioritize voice over starred
        UnifiedNoteTypeIndicator(
            noteType = when {
                noteData.isVoice && noteData.isStarred -> NoteType.Voice
                noteData.isVoice -> NoteType.Voice
                noteData.isStarred -> NoteType.Starred
                else -> NoteType.Text
            },
            audioDurationMs = if (noteData.isVoice) noteData.audioDurationMs else null,
            layoutMode = NoteCardLayoutMode.LIST
        )
        
        // Date and time - relative time for list mode
        Text(
            text = formatRelativeTime(noteData.createdAt),
            style = MaterialTheme.typography.labelMedium,
            color = noteColors.onContainer.copy(alpha = 0.7f)
        )
    }
    
    // Enhanced smart content preview using existing component
    val noteUiModel = NoteUiModel(
        id = noteData.id,
        title = noteData.title,
        content = noteData.content,
        isStarred = noteData.isStarred,
        isVoice = noteData.isVoice,
        createdAt = noteData.createdAt,
        recordingPath = noteData.recordingPath,
        words = noteData.words,
        audioDurationMs = noteData.audioDurationMs
    )
    
    Material3SmartContentPreview(
        note = noteUiModel,
        isExpanded = isExpanded,
        noteColors = noteColors
    )
    
    // Audio player for voice notes when expanded
    if (noteData.isVoice && isExpanded && audioPlayerViewModel != null && audioPlayerUiState != null) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            CompactAudioPlayer(
                filePath = noteData.recordingPath,
                noteId = noteData.id,
                noteDurationMs = noteData.audioDurationMs,
                uiState = audioPlayerUiState,
                onLoadAudio = audioPlayerViewModel::onLoadAudio,
                onTogglePlayPause = audioPlayerViewModel::onTogglePlayPause,
                onTogglePlaybackSpeed = audioPlayerViewModel::onTogglePlaybackSpeed,
                isNoteCurrentlyPlaying = audioPlayerViewModel::isNoteCurrentlyPlaying,
                isNoteLoaded = audioPlayerViewModel::isNoteLoaded,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
    
    // Footer with additional metadata and actions
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Voice note duration or other metadata
        if (noteData.isVoice && noteData.audioDurationMs > 0) {
            Text(
                text = "Duration: ${formatDuration(noteData.audioDurationMs.toLong())}",
                style = MaterialTheme.typography.labelSmall,
                color = noteColors.onContainer.copy(alpha = 0.6f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star indicator
            if (noteData.isStarred) {
                MaterialIcon(
                    symbol = MaterialSymbols.Star,
                    size = 16.dp,
                    tint = noteColors.accent,
                    style = MaterialIconStyle.Filled,
                    contentDescription = "Starred note"
                )
            }
            
            // Note actions dropdown
            NoteActionsIconButton(
                noteId = noteData.id,
                onShareClick = onShareClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                iconTint = noteColors.onContainer.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Content layout optimized for calendar mode
 */
@Composable
private fun CalendarModeContent(
    noteData: NoteCardData,
    noteColors: NoteColorScheme,
    isExpanded: Boolean,
    maxContentLines: Int,
    onShareClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    // Date at the top (calendar mode specific)
    Text(
        text = noteData.createdAt.parseToTimeString(),
        style = MaterialTheme.typography.labelSmall,
        color = noteColors.onContainer.copy(alpha = 0.6f),
        fontWeight = FontWeight.Medium
    )
    
    // Title with enhanced typography
    Text(
        text = if (noteData.title.isNotEmpty()) noteData.title else generateSmartTitle(noteData),
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp
        ),
        color = noteColors.onContainer,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
    
    // Content preview with responsive sizing
    if (noteData.content.isNotEmpty() && !noteData.content.contains("[Audio recording - transcription unavailable]")) {
        Text(
            text = noteData.content,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 18.sp
            ),
            color = noteColors.onContainer.copy(alpha = 0.7f),
            maxLines = if (isExpanded) Int.MAX_VALUE else maxContentLines,
            overflow = TextOverflow.Ellipsis
        )
    } else if (noteData.isVoice) {
        // Audio metadata for voice notes without transcription
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    noteColors.accent.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MaterialIcon(
                symbol = MaterialSymbols.Mic,
                size = 16.dp,
                tint = noteColors.accent,
                style = MaterialIconStyle.Filled,
                contentDescription = "Voice note"
            )
            
            Text(
                text = buildString {
                    append("Audio recording")
                    if (noteData.audioDurationMs > 0) {
                        append(" • ${formatDuration(noteData.audioDurationMs.toLong())}")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = noteColors.onContainer.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    
    // Bottom row with note actions
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NoteActionsIconButton(
            noteId = noteData.id,
            onShareClick = onShareClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            iconTint = noteColors.onContainer.copy(alpha = 0.6f)
        )
    }
}

/**
 * Generate smart title from note content for calendar mode
 */
private fun generateSmartTitle(noteData: NoteCardData): String {
    return when {
        noteData.title.isNotEmpty() -> noteData.title
        
        noteData.isVoice && noteData.content.contains("[Audio recording - transcription unavailable]") -> {
            "Voice Note • ${formatRelativeTime(noteData.createdAt)}"
        }
        
        noteData.content.isNotEmpty() -> {
            extractTitleFromContent(noteData.content)
        }
        
        else -> "Untitled Note"
    }
}

/**
 * Extract title from content using intelligent text processing
 */
private fun extractTitleFromContent(content: String): String {
    return content
        .take(40)
        .split('.', '!', '?').firstOrNull()?.trim()
        ?.takeIf { it.length > 5 }
        ?: content.take(30).trim() + "..."
}

/**
 * Format duration from milliseconds to human-readable format
 */
private fun formatDuration(durationMs: Long): String {
    val duration = durationMs.milliseconds
    val minutes = duration.inWholeMinutes
    val seconds = duration.inWholeSeconds % 60
    
    return if (minutes > 0) {
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${seconds}s"
    }
}

/**
 * Format relative time for display (e.g., "2 hours ago", "Yesterday")
 */
private fun formatRelativeTime(dateTimeString: String): String {
    return try {
        val noteDateTime = Instant.parse(dateTimeString)
        val now = Clock.System.now()
        val diff = now - noteDateTime
        
        when {
            diff.inWholeDays > 0 -> "${diff.inWholeDays} day${if (diff.inWholeDays > 1) "s" else ""} ago"
            diff.inWholeHours > 0 -> "${diff.inWholeHours} hour${if (diff.inWholeHours > 1) "s" else ""} ago"
            diff.inWholeMinutes > 0 -> "${diff.inWholeMinutes} min ago"
            else -> "Just now"
        }
    } catch (e: Exception) {
        // Fallback for simple time extraction
        try {
            when {
                dateTimeString.contains("T") -> {
                    val timePart = dateTimeString.substringAfter("T").substringBefore(".")
                    val hourMinute = timePart.substringBeforeLast(":")
                    hourMinute
                }
                dateTimeString.contains(":") -> dateTimeString.substringBeforeLast(":")
                else -> "Now"
            }
        } catch (e: Exception) {
            "Recently"
        }
    }
}

/**
 * Build comprehensive accessibility description for note cards
 */
private fun buildNoteAccessibilityDescription(noteData: NoteCardData): String {
    return buildString {
        if (noteData.title.isNotEmpty()) {
            append("${noteData.title}. ")
        }
        
        if (noteData.content.isNotEmpty()) {
            append("${noteData.content.take(100)}. ")
        }
        
        append("Created ${formatAccessibleDate(noteData.createdAt)}. ")
        
        if (noteData.isVoice) {
            append("Voice note")
            if (noteData.audioDurationMs > 0) {
                append(", ${formatDuration(noteData.audioDurationMs.toLong())}")
            }
            append(". ")
        }
        
        if (noteData.isStarred) {
            append("Starred note. ")
        }
    }
}

/**
 * Build state description for accessibility services
 */
private fun buildNoteStateDescription(noteData: NoteCardData): String {
    return buildList {
        if (noteData.isVoice) add("Voice note")
        if (noteData.isStarred) add("Starred")
    }.joinToString(", ")
}

/**
 * Format date for accessibility services
 */
private fun formatAccessibleDate(timestamp: String): String {
    return try {
        when {
            timestamp.contains("T") -> {
                val datePart = timestamp.substringBefore("T")
                datePart
            }
            timestamp.contains("-") -> timestamp
            else -> "today"
        }
    } catch (e: Exception) {
        "today"
    }
}