package com.module.notelycompose.notes.ui.list.model

data class NoteUiModel(
    val id: Long,
    val title: String,
    val content: String,
    val isStarred: Boolean,
    val isVoice: Boolean,
    val createdAt: String,
    val recordingPath: String,
    val words: Int,
    val audioDurationMs: Int = 0 // Duration in milliseconds for voice notes
) {
    /**
     * Determines if this is an audio-only note (voice note without transcription)
     * Used for audio-first UI design patterns
     */
    val isAudioOnly: Boolean
        get() = isVoice && content.isBlank()
    
    /**
     * Determines if this is a text note with audio (transcribed voice note)
     */
    val isTextWithAudio: Boolean
        get() = isVoice && content.isNotBlank()
    
    /**
     * Gets the display title for the note, with audio-first considerations
     */
    val displayTitle: String
        get() = when {
            title.isNotBlank() -> title
            isAudioOnly -> "Voice Note"
            content.isNotBlank() -> content.take(50).let { 
                if (content.length > 50) "$it..." else it 
            }
            else -> "Note"
        }
    
    /**
     * Gets the formatted audio duration for display
     */
    val formattedDuration: String
        get() = if (audioDurationMs > 0) {
            val seconds = audioDurationMs / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            if (minutes > 0) {
                "${minutes}:${remainingSeconds.toString().padStart(2, '0')}"
            } else {
                "${seconds}s"
            }
        } else {
            "0s"
        }
}
