package com.module.notelycompose.notes.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.notes.ui.list.NoteColorScheme
import com.module.notelycompose.notes.ui.theme.MaterialSymbols
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Note content type enumeration for smart preview handling
 */
enum class NoteContentType {
    TRANSCRIBED_VOICE,    // Voice note with successful transcription
    AUDIO_ONLY,           // Voice note without transcription
    TEXT_NOTE,            // Manual text note
    MIXED_CONTENT         // Voice note with additional manual text
}

/**
 * Preview strategy enumeration
 */
enum class PreviewStrategy {
    TITLE_AND_CONTENT,    // Show both title and content
    CONTENT_ONLY,         // Show content as primary
    AUDIO_METADATA,       // Show audio-specific information
    SMART_EXCERPT         // Show intelligent content excerpt
}

/**
 * Smart note preview model for enhanced content display
 */
data class NotePreviewModel(
    val displayTitle: String,
    val displayContent: String,
    val contentType: NoteContentType,
    val previewStrategy: PreviewStrategy
)

/**
 * Generate smart title from note content
 */
fun generateSmartTitle(note: NoteUiModel): String {
    return when {
        note.title.isNotEmpty() -> note.title
        
        note.isVoice && note.content.contains("[Audio recording - transcription unavailable]") -> {
            // Generate time-based title for audio-only notes
            "Voice Note • ${formatRelativeTime(note.createdAt)}"
        }
        
        note.content.isNotEmpty() -> {
            // Extract meaningful title from content (first meaningful sentence)
            extractTitleFromContent(note.content)
        }
        
        else -> "Untitled Note"
    }
}

/**
 * Extract title from content using intelligent text processing
 */
private fun extractTitleFromContent(content: String): String {
    return content
        .take(40) // Take first 40 characters
        .split('.', '!', '?').firstOrNull()?.trim() // First sentence
        ?.takeIf { it.length > 5 } // Ensure meaningful length
        ?: content.take(30).trim() + "..."
}

/**
 * Generate content preview model with smart strategy selection
 */
fun generateContentPreview(note: NoteUiModel): NotePreviewModel {
    val displayTitle = generateSmartTitle(note)
    
    return when {
        // Audio-only notes: Show metadata instead of placeholder
        note.isVoice && note.content.contains("[Audio recording - transcription unavailable]") -> {
            NotePreviewModel(
                displayTitle = displayTitle,
                displayContent = buildAudioMetadata(note),
                contentType = NoteContentType.AUDIO_ONLY,
                previewStrategy = PreviewStrategy.AUDIO_METADATA
            )
        }
        
        // Transcribed voice notes: Show transcription
        note.isVoice && !note.content.contains("[Audio recording - transcription unavailable]") -> {
            NotePreviewModel(
                displayTitle = displayTitle,
                displayContent = note.content,
                contentType = NoteContentType.TRANSCRIBED_VOICE,
                previewStrategy = PreviewStrategy.TITLE_AND_CONTENT
            )
        }
        
        // Text notes: Standard display
        else -> {
            NotePreviewModel(
                displayTitle = displayTitle,
                displayContent = note.content,
                contentType = NoteContentType.TEXT_NOTE,
                previewStrategy = PreviewStrategy.TITLE_AND_CONTENT
            )
        }
    }
}

/**
 * Build audio metadata string for audio-only notes
 */
private fun buildAudioMetadata(note: NoteUiModel): String {
    return buildString {
        append("Audio recording")
        if (note.audioDurationMs > 0) {
            append(" • ${formatDuration(note.audioDurationMs.toLong())}")
        }
        append(" • ${formatRelativeTime(note.createdAt)}")
        append("\nProcessing transcription...")
    }
}

/**
 * Format duration from milliseconds to human-readable format
 */
private fun formatDuration(durationMs: Long): String {
    val duration = durationMs.toLong().milliseconds
    val minutes = duration.inWholeMinutes
    val seconds = duration.inWholeSeconds % 60
    
    return if (minutes > 0) {
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${seconds}s"
    }
}

/**
 * Format relative time (e.g., "2 hours ago", "Yesterday")
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
        "Recently"
    }
}

/**
 * Material 3 Smart Content Preview Component
 */
@Composable
fun Material3SmartContentPreview(
    note: NoteUiModel,
    isExpanded: Boolean,
    noteColors: NoteColorScheme,
    modifier: Modifier = Modifier
) {
    val previewModel = remember(note) { generateContentPreview(note) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Smart title display
        Text(
            text = previewModel.displayTitle,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = when (previewModel.contentType) {
                    NoteContentType.AUDIO_ONLY -> FontWeight.Medium
                    else -> FontWeight.SemiBold
                },
                letterSpacing = 0.15.sp
            ),
            color = noteColors.onContainer,
            maxLines = if (isExpanded) 3 else 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics {
                heading()
            }
        )
        
        // Enhanced content display with type-specific styling
        when (previewModel.previewStrategy) {
            PreviewStrategy.AUDIO_METADATA -> {
                AudioMetadataPreview(
                    content = previewModel.displayContent,
                    noteColors = noteColors,
                    isExpanded = isExpanded
                )
            }
            
            else -> {
                StandardContentPreview(
                    content = previewModel.displayContent,
                    noteColors = noteColors,
                    isExpanded = isExpanded
                )
            }
        }
    }
}

/**
 * Audio metadata preview with special styling for processing states
 */
@Composable
private fun AudioMetadataPreview(
    content: String,
    noteColors: NoteColorScheme,
    isExpanded: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = noteColors.accent.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 18.sp
                ),
                color = noteColors.onContainer.copy(alpha = 0.8f),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Standard content preview for transcribed and text notes
 */
@Composable
private fun StandardContentPreview(
    content: String,
    noteColors: NoteColorScheme,
    isExpanded: Boolean
) {
    if (content.isNotEmpty()) {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp
            ),
            color = noteColors.onContainer.copy(alpha = 0.8f),
            maxLines = when {
                isExpanded -> Int.MAX_VALUE
                else -> 3
            },
            overflow = TextOverflow.Ellipsis
        )
    }
}