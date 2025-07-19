package com.module.notelycompose.platform

/**
 * Platform-specific haptic feedback interface
 */
expect class HapticFeedback() {
    /**
     * Provides light haptic feedback for UI interactions
     */
    fun light()
    
    /**
     * Provides medium haptic feedback for confirmations
     */
    fun medium()
    
    /**
     * Provides strong haptic feedback for important actions
     */
    fun heavy()
    
    /**
     * Provides success haptic feedback
     */
    fun success()
    
    /**
     * Provides error haptic feedback
     */
    fun error()
}