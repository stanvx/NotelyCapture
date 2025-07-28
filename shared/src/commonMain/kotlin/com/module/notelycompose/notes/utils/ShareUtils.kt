package com.module.notelycompose.notes.utils

import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.notes.presentation.list.model.NotePresentationModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Utility functions for sharing note content across different formats and platforms.
 * Provides consistent formatting for text sharing and handles both NoteUiModel and NotePresentationModel.
 */
object ShareUtils {

    /**
     * Formats a NoteUiModel for text sharing with consistent structure.
     * Includes title, content, creation date, and metadata.
     */
    fun buildShareText(note: NoteUiModel): String {
        return buildString {
            // Title section
            if (note.title.isNotEmpty()) {
                appendLine(note.title)
                appendLine("=".repeat(note.title.length)) // Title underline
                appendLine()
            }

            // Content section
            if (note.content.isNotEmpty() && 
                !note.content.contains("[Audio recording - transcription unavailable]")) {
                appendLine(note.content)
                appendLine()
            }

            // Metadata section
            appendLine("Created: ${note.createdAt}") // createdAt is already formatted as String
            
            if (note.isVoice && note.audioDurationMs > 0) {
                appendLine("Voice Note Duration: ${formatDuration(note.audioDurationMs.toLong())}")
            }
            
            if (note.isStarred) {
                appendLine("⭐ Starred Note")
            }
            
            appendLine()
            appendLine("Shared from Notely Capture")
        }
    }

    /**
     * Formats a NotePresentationModel for text sharing with consistent structure.
     * Overloaded function to handle calendar view notes.
     */
    fun buildShareText(note: NotePresentationModel): String {
        return buildString {
            // Title section
            if (note.title.isNotEmpty()) {
                appendLine(note.title)
                appendLine("=".repeat(note.title.length)) // Title underline
                appendLine()
            }

            // Content section
            if (note.content.isNotEmpty() && 
                !note.content.contains("[Audio recording - transcription unavailable]")) {
                appendLine(note.content)
                appendLine()
            }

            // Metadata section
            appendLine("Created: ${note.createdAt}") // createdAt is already formatted as String
            
            if (note.isVoice && note.audioDurationMs > 0) {
                appendLine("Voice Note Duration: ${formatDuration(note.audioDurationMs.toLong())}")
            }
            
            if (note.isStarred) {
                appendLine("⭐ Starred Note")
            }
            
            appendLine()
            appendLine("Shared from Notely Capture")
        }
    }

    /**
     * Formats a timestamp for sharing in a human-readable format.
     */
    private fun formatDateForSharing(timestamp: Long): String {
        return try {
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            
            val month = localDateTime.monthNumber.toString().padStart(2, '0')
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val year = localDateTime.year
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')
            
            "$month/$day/$year at $hour:$minute"
        } catch (e: Exception) {
            "Unknown date"
        }
    }

    /**
     * Formats duration in milliseconds to a human-readable format (e.g., "2:34").
     */
    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    /**
     * Validates if a recording path exists and is valid for sharing.
     * Returns true if the recording can be shared, false otherwise.
     */
    fun canShareRecording(recordingPath: String?): Boolean {
        return !recordingPath.isNullOrEmpty() && recordingPath != "null"
    }

    /**
     * Determines the primary share content type for a note.
     * Returns "audio" if it's a voice note with valid recording, "text" otherwise.
     */
    fun getPrimaryShareType(isVoice: Boolean, recordingPath: String?): String {
        return if (isVoice && canShareRecording(recordingPath)) "audio" else "text"
    }
}