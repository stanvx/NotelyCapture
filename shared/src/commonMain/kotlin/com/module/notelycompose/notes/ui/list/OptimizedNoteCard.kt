package com.module.notelycompose.notes.ui.list

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.module.notelycompose.notes.ui.components.MaterialIcon
import com.module.notelycompose.notes.ui.components.MaterialIconStyle
import com.module.notelycompose.notes.ui.components.Material3SmartContentPreview
import com.module.notelycompose.notes.ui.detail.DeleteConfirmationDialog
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens
import com.module.notelycompose.notes.ui.theme.*
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

/**
 * Material 3 Expressive Note Card with dynamic color categorization.
 * 
 * Features:
 * - Dynamic color-based note categorization (voice/starred/text)
 * - Expressive note type indicators with duration display
 * - Material 3 typography hierarchy for content preview
 * - Consistent Material 3 card design patterns
 * - Comprehensive accessibility markup
 * - Performance-optimized animations
 */

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
 * Generate dynamic colors based on note characteristics
 */
@Composable
fun generateNoteColors(note: NoteUiModel): NoteColorScheme {
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
 * Material 3 Expressive Note Type Indicator with dynamic colors and icons
 */
@Composable
fun Material3NoteTypeIndicator(
    noteType: NoteType,
    audioDurationMs: Int? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    val (containerColor, contentColor, icon, label) = when (noteType) {
        NoteType.Voice -> NoteTypeTheme(
            container = colorScheme.primaryContainer,
            content = colorScheme.onPrimaryContainer,
            icon = MaterialSymbols.Mic,
            label = audioDurationMs?.let { formatDuration(it) } ?: "Voice"
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), // Increased padding to prevent cutoff
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MaterialIcon(
                symbol = icon,
                size = 16.dp, // Increased size for better visibility
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
 * Material 3 Expressive Note Card with enhanced design patterns
 */
@Composable
fun Material3NoteCard(
    note: NoteUiModel,
    isExpanded: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    audioPlayerViewModel: AudioPlayerViewModel? = null,
    audioPlayerUiState: AudioPlayerUiState? = null,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Generate dynamic colors based on note type
    val noteColors = generateNoteColors(note)
    
    // Optimized scale animation
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
            .semantics {
                contentDescription = buildNoteAccessibilityDescription(note)
                stateDescription = buildNoteStateDescription(note)
                
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
                    
                    if (note.isVoice) {
                        add(CustomAccessibilityAction("Play audio") {
                            // Audio play action
                            true
                        })
                    }
                }
            },
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large, // 16dp corner radius
        colors = CardDefaults.cardColors(
            containerColor = noteColors.container,
            contentColor = noteColors.onContainer
        ),
        elevation = CardElevationPresets.noteCard()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Dynamic accent strip on the left
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                noteColors.accent,
                                noteColors.accent.copy(alpha = 0.6f),
                                noteColors.accent.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
            
            // Main content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp, // Account for accent strip
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with note type and metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically // Better alignment
                ) {
                    // Note type indicator - prioritize voice over starred
                    Material3NoteTypeIndicator(
                        noteType = when {
                            note.isVoice && note.isStarred -> NoteType.Voice // Show as voice even if starred
                            note.isVoice -> NoteType.Voice
                            note.isStarred -> NoteType.Starred
                            else -> NoteType.Text
                        },
                        audioDurationMs = if (note.isVoice) note.audioDurationMs else null
                    )
                    
                    // Date and time - aligned with icon
                    Text(
                        text = formatRelativeTime(note.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = noteColors.onContainer.copy(alpha = 0.7f)
                    )
                }
                
                // Enhanced smart content preview
                Material3SmartContentPreview(
                    note = note,
                    isExpanded = isExpanded,
                    noteColors = noteColors
                )
                
                // Audio player for voice notes when expanded
                if (note.isVoice && isExpanded && audioPlayerViewModel != null && audioPlayerUiState != null) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        CompactAudioPlayer(
                            filePath = note.recordingPath,
                            noteId = note.id,
                            noteDurationMs = note.audioDurationMs,
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
                
                // Footer with additional metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice note duration or other metadata
                    if (note.isVoice && note.audioDurationMs != null) {
                        Text(
                            text = "Duration: ${formatDuration(note.audioDurationMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = noteColors.onContainer.copy(alpha = 0.6f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    // Star indicator
                    if (note.isStarred) {
                        MaterialIcon(
                            symbol = MaterialSymbols.Star,
                            size = 16.dp,
                            tint = noteColors.accent,
                            style = MaterialIconStyle.Filled,
                            contentDescription = "Starred note"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptimizedNoteCard(
    note: NoteUiModel,
    onNoteClick: (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    onShareClick: (Long) -> Unit = {},
    onEditClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    index: Int = 0,
    audioPlayerViewModel: AudioPlayerViewModel,
    audioPlayerUiState: AudioPlayerUiState,
    maxContentLines: Int = 4
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    
    // Remove problematic staggered animations to allow smooth scrolling
    Material3NoteCard(
        note = note,
        isExpanded = isExpanded,
        onClick = { 
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onNoteClick(note.id)
        },
        onLongClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            showOptionsMenu = true
        },
        audioPlayerViewModel = audioPlayerViewModel,
        audioPlayerUiState = audioPlayerUiState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 300f
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        showOptionsMenu = true
                    }
                )
            }
    )
    
    // Options menu overlay
    if (showOptionsMenu) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                ) {
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onShareClick(note.id)
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            MaterialIcon(
                                symbol = MaterialSymbols.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                size = 20.dp
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEditClick(note.id)
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            MaterialIcon(
                                symbol = MaterialSymbols.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                size = 20.dp
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        thickness = 1.dp
                    )
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error
                            ) 
                        },
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDeleteDialog = true
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            MaterialIcon(
                                symbol = MaterialSymbols.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                size = 20.dp
                            )
                        }
                    )
                }
            }
        }
        
        // Delete confirmation dialog
        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                showDialog = showDeleteDialog,
                onConfirm = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDeleteClick(note.id)
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
}

/**
 * Format duration in milliseconds to MM:SS format
 */
private fun formatDuration(durationMillis: Int): String {
    val seconds = (durationMillis / 1000) % 60
    val minutes = (durationMillis / (1000 * 60)) % 60
    return if (minutes > 0) {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${seconds}s"
    }
}

/**
 * Build comprehensive accessibility description for note cards
 */
private fun buildNoteAccessibilityDescription(note: NoteUiModel): String {
    return buildString {
        if (note.title.isNotEmpty()) {
            append("${note.title}. ")
        }
        
        if (note.content.isNotEmpty()) {
            append("${note.content.take(100)}. ")
        }
        
        append("Created ${formatAccessibleDate(note.createdAt)}. ")
        
        if (note.isVoice) {
            append("Voice note")
            note.audioDurationMs?.let { duration ->
                append(", ${formatDuration(duration)}")
            }
            append(". ")
        }
        
        if (note.isStarred) {
            append("Starred note. ")
        }
    }
}

/**
 * Build state description for accessibility services
 */
private fun buildNoteStateDescription(note: NoteUiModel): String {
    return buildList {
        if (note.isVoice) add("Voice note")
        if (note.isStarred) add("Starred")
    }.joinToString(", ")
}

/**
 * Format relative time for display
 */
private fun formatRelativeTime(timestamp: String): String {
    // Simple implementation - could be enhanced with actual relative time calculation
    return try {
        // Extract time portion if it's a full timestamp
        when {
            timestamp.contains("T") -> {
                val timePart = timestamp.substringAfter("T").substringBefore(".")
                val hourMinute = timePart.substringBeforeLast(":")
                hourMinute
            }
            timestamp.contains(":") -> timestamp.substringBeforeLast(":")
            else -> "Now"
        }
    } catch (e: Exception) {
        "Now"
    }
}

/**
 * Format date for accessibility services
 */
private fun formatAccessibleDate(timestamp: String): String {
    return try {
        // Extract date portion if it's a full timestamp
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
