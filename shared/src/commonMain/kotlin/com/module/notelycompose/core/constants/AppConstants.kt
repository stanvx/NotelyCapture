package com.module.notelycompose.core.constants

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Application-wide constants for timeouts, delays, and timing configurations
 */
object AppConstants {
    
    /**
     * Recording and transcription timeouts
     */
    object Recording {
        /** Timeout for waiting for recording path to become available */
        val RECORDING_PATH_TIMEOUT: Duration = 2.seconds
        
        /** Default delay before navigation in traditional recording flow */
        val TRADITIONAL_FLOW_DELAY: Duration = 2.seconds
    }
    
    /**
     * Animation timing constants following Material 3 guidelines
     */
    object Animation {
        /** Duration for medium transitions (FAB expand) */
        val MEDIUM_TRANSITION_DURATION: Duration = 300.milliseconds
        
        /** Duration for short transitions (FAB collapse) */
        val SHORT_TRANSITION_DURATION: Duration = 150.milliseconds
        
        /** Duration for scrim fade animations */
        val SCRIM_FADE_DURATION: Duration = 150.milliseconds
        
        /** Stagger delay between FAB action animations */
        val FAB_STAGGER_DELAY: Duration = 50.milliseconds
    }
    
    /**
     * Error handling and retry configurations
     */
    object ErrorHandling {
        /** Maximum number of retry attempts for operations */
        const val MAX_RETRY_ATTEMPTS = 3
        
        /** Base delay for exponential backoff */
        val BASE_RETRY_DELAY: Duration = 100.milliseconds
    }
    
    /**
     * Audio processing constants
     */
    object Audio {
        /** Maximum history size for amplitude collection */
        const val AMPLITUDE_HISTORY_MAX_SIZE = 100
        
        /** Default length for demo amplitude generation */
        const val DEMO_AMPLITUDE_LENGTH = 50
        
        /** Maximum file size in bytes (100MB) */
        const val MAX_FILE_SIZE_BYTES = 100 * 1024 * 1024
        
        /** Bytes per megabyte for file size calculations */
        const val BYTES_PER_MB = 1024 * 1024
        
        /** Race condition protection delay in milliseconds */
        val RACE_CONDITION_DELAY: Duration = 100.milliseconds
    }
    
    /**
     * Text editor timing and performance configurations
     */
    object Editor {
        /** Debounce delay for save operations in milliseconds */
        val SAVE_DEBOUNCE_DELAY: Duration = 500.milliseconds
        
        /** Debounce delay for rich text synchronization in milliseconds */
        val SYNC_DEBOUNCE_DELAY: Duration = 150.milliseconds
    }
    
    /**
     * UI dimension constants (in DP)
     */
    object UI {
        /** Standard padding for bottom UI elements */
        const val BOTTOM_PADDING_DP = 80
        
        /** Standard large button size */
        const val LARGE_BUTTON_SIZE_DP = 80
        
        /** Standard spacing between elements */
        const val STANDARD_SPACING_DP = 24
        
        /** Large recording animation size */
        const val LARGE_RECORDING_ANIMATION_DP = 320
        
        /** Medium recording animation size */
        const val MEDIUM_RECORDING_ANIMATION_DP = 200
        
        /** Success animation size */
        const val SUCCESS_ANIMATION_DP = 100
        
        /** Small icon size */
        const val SMALL_ICON_SIZE_DP = 24
    }
}