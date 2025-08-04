package com.module.notelycompose.notes.ui.constants

/**
 * Centralized UI text constants to ensure consistency across the application
 * and prepare for future internationalization.
 */
object UIConstants {
    
    // Default values
    const val DEFAULT_TITLE = "New Note"
    const val DEFAULT_CONTENT = ""
    const val UNTITLED_NOTE = "Untitled Note"
    
    // Audio-related constants
    const val AUDIO_RECORDING = "Audio recording"
    const val VOICE_NOTE = "Voice Note"
    const val AUDIO_TRANSCRIPTION_UNAVAILABLE = "[Audio recording - transcription unavailable]"
    const val PROCESSING_TRANSCRIPTION = "Processing transcription..."
    
    // Note types
    const val TEXT_NOTE_LABEL = "Text"
    const val VOICE_NOTE_LABEL = "Voice"
    const val STARRED_NOTE_LABEL = "Starred"
    
    // Time-related constants
    const val NOW = "Now"
    const val TODAY = "Today"
    const val YESTERDAY = "Yesterday"
    const val RECENTLY = "Recently"
    
    // Content formatting
    const val ELLIPSIS = "..."
    const val NEW_LINE = "\n"
    const val AT_PREFIX = "at"
    
    // Accessibility descriptions
    const val STARRED_NOTE_DESCRIPTION = "Starred note"
    const val VOICE_NOTE_DESCRIPTION = "Voice note"
    const val EDIT_NOTE_ACTION = "Edit note"
    const val NOTE_OPTIONS_ACTION = "Note options"
    const val PLAY_AUDIO_ACTION = "Play audio"
    
    // Duration formatting
    const val DURATION_PREFIX = "Duration:"
    const val SECONDS_SUFFIX = "s"
    const val MINUTE_SEPARATOR = ":"
    
    // Default lengths and limits
    const val CONTENT_PREVIEW_LENGTH = 36
    const val TITLE_EXCERPT_LENGTH = 40
    const val SHORT_CONTENT_LENGTH = 30
    const val LARGE_CONTENT_THRESHOLD = 500
    const val MEANINGFUL_SENTENCE_MIN_LENGTH = 5
    
    // Padding and formatting
    const val MINUTE_PADDING_LENGTH = 2
    const val PADDING_CHAR = '0'
}