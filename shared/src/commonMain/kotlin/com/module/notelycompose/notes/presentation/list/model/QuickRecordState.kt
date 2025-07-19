package com.module.notelycompose.notes.presentation.list.model

/**
 * Represents the different states of the quick record feature
 */
enum class QuickRecordState {
    /**
     * Initial state - no quick recording in progress
     */
    Idle,
    
    /**
     * Recording is in progress
     */
    Recording,
    
    /**
     * Recording completed, transcription is being processed in background
     */
    Processing,
    
    /**
     * Quick record flow completed successfully, note created with transcription
     */
    Complete,
    
    /**
     * An error occurred during the quick record process
     */
    Error
}