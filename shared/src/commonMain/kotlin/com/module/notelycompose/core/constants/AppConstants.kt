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
}