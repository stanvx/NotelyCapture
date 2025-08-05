package com.module.notelycompose.domain.model

/**
 * Domain model representing a note in the application.
 * This is the core entity used throughout the domain layer.
 */
data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isStarred: Boolean = false,
    val audioFilePath: String? = null,
    val hasAudio: Boolean = false,
    val transcription: String? = null,
    val tags: List<String> = emptyList()
) {
    /**
     * Check if this note contains a search query in title or content
     */
    fun containsQuery(query: String): Boolean {
        if (query.isBlank()) return true
        val lowercaseQuery = query.lowercase()
        return title.lowercase().contains(lowercaseQuery) ||
                content.lowercase().contains(lowercaseQuery) ||
                transcription?.lowercase()?.contains(lowercaseQuery) == true
    }
    
    /**
     * Check if this note is a voice note (has audio)
     */
    fun isVoiceNote(): Boolean = hasAudio && audioFilePath != null
    
    /**
     * Get formatted timestamp for display
     */
    fun getFormattedTimestamp(): String {
        // This would typically use a proper date formatter
        return "Timestamp: $timestamp"
    }
    
    /**
     * Validate note data integrity
     */
    fun isValid(): Boolean {
        return title.isNotBlank() || content.isNotBlank() || hasAudio
    }
}