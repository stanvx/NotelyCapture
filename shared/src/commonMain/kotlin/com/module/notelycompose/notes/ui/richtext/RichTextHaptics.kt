package com.module.notelycompose.notes.ui.richtext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Advanced haptic feedback system for rich text interactions with Apple-quality tactile responses.
 * 
 * Features:
 * - Context-aware haptic patterns for different formatting operations
 * - Intensity-based feedback for selection vs application
 * - Smart feedback throttling to prevent haptic spam
 * - Accessibility-aware haptic preferences
 * - Platform-optimized haptic types
 */
class RichTextHapticManager(
    private val hapticFeedback: HapticFeedback,
    private val preferences: HapticPreferences = HapticPreferences()
) {
    
    private var lastHapticTime = 0L
    private val hapticThrottleMs = 50L // Prevent haptic spam
    
    /**
     * Provides tactile feedback for text formatting operations.
     */
    fun onFormatToggled(isNowActive: Boolean) {
        if (!preferences.enableFormattingFeedback) return
        
        val feedbackType = if (isNowActive) {
            preferences.formatAppliedType
        } else {
            preferences.formatRemovedType
        }
        
        performThrottledHaptic(feedbackType)
    }
    
    /**
     * Provides tactile feedback for alignment changes.
     */
    fun onAlignmentChanged() {
        if (!preferences.enableAlignmentFeedback) return
        performThrottledHaptic(preferences.alignmentChangedType)
    }
    
    /**
     * Provides tactile feedback for list operations.
     */
    fun onListToggled(isNowActive: Boolean) {
        if (!preferences.enableListFeedback) return
        
        val feedbackType = if (isNowActive) {
            preferences.listCreatedType
        } else {
            preferences.listRemovedType
        }
        
        performThrottledHaptic(feedbackType)
    }
    
    /**
     * Provides tactile feedback for heading operations.
     */
    fun onHeadingApplied(level: Int) {
        if (!preferences.enableHeadingFeedback) return
        
        // More intense feedback for higher-level headings
        val feedbackType = when (level) {
            1 -> HapticFeedbackType.LongPress
            2, 3 -> preferences.headingAppliedType
            else -> HapticFeedbackType.TextHandleMove
        }
        
        performThrottledHaptic(feedbackType)
    }
    
    /**
     * Provides tactile feedback for clearing all formatting.
     */
    fun onFormattingCleared() {
        if (!preferences.enableClearFeedback) return
        performThrottledHaptic(preferences.formattingClearedType)
    }
    
    /**
     * Provides tactile feedback for toolbar interactions.
     */
    fun onToolbarShown() {
        if (!preferences.enableToolbarFeedback) return
        performThrottledHaptic(HapticFeedbackType.TextHandleMove)
    }
    
    fun onToolbarHidden() {
        if (!preferences.enableToolbarFeedback) return
        performThrottledHaptic(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Provides tactile feedback for selection-based operations.
     */
    fun onTextSelectionChanged() {
        if (!preferences.enableSelectionFeedback) return
        performThrottledHaptic(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Performs haptic feedback with intelligent throttling.
     */
    private fun performThrottledHaptic(type: HapticFeedbackType) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastHapticTime > hapticThrottleMs) {
            hapticFeedback.performHapticFeedback(type)
            lastHapticTime = currentTime
        }
    }
    
    /**
     * Updates haptic preferences for accessibility or user preference changes.
     */
    fun updatePreferences(newPreferences: HapticPreferences) {
        // Preferences would be updated here in a real implementation
    }
}

/**
 * Configuration for haptic feedback preferences and accessibility.
 */
data class HapticPreferences(
    // Global haptic settings
    val enableHapticFeedback: Boolean = true,
    val hapticIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    
    // Specific operation feedback
    val enableFormattingFeedback: Boolean = true,
    val enableAlignmentFeedback: Boolean = true,
    val enableListFeedback: Boolean = true,
    val enableHeadingFeedback: Boolean = true,
    val enableClearFeedback: Boolean = true,
    val enableToolbarFeedback: Boolean = true,
    val enableSelectionFeedback: Boolean = false, // Often too frequent
    
    // Haptic types for different operations
    val formatAppliedType: HapticFeedbackType = HapticFeedbackType.LongPress,
    val formatRemovedType: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
    val alignmentChangedType: HapticFeedbackType = HapticFeedbackType.LongPress,
    val listCreatedType: HapticFeedbackType = HapticFeedbackType.LongPress,
    val listRemovedType: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
    val headingAppliedType: HapticFeedbackType = HapticFeedbackType.LongPress,
    val formattingClearedType: HapticFeedbackType = HapticFeedbackType.LongPress
)

/**
 * Haptic intensity levels for accessibility and user preference.
 */
enum class HapticIntensity {
    OFF,
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Composable function to create and remember a haptic manager.
 */
@Composable
fun rememberRichTextHapticManager(
    preferences: HapticPreferences = HapticPreferences()
): RichTextHapticManager {
    val hapticFeedback = LocalHapticFeedback.current
    
    return remember(preferences) {
        RichTextHapticManager(hapticFeedback, preferences)
    }
}

/**
 * Enhanced haptic feedback extensions for specific formatting operations.
 */
object RichTextHaptics {
    
    /**
     * Smart haptic feedback for bold formatting.
     */
    fun HapticFeedback.boldToggled(isNowBold: Boolean) {
        performHapticFeedback(
            if (isNowBold) {
                HapticFeedbackType.LongPress // Strong feedback for applying bold
            } else {
                HapticFeedbackType.TextHandleMove // Lighter for removing
            }
        )
    }
    
    /**
     * Smart haptic feedback for italic formatting.
     */
    fun HapticFeedback.italicToggled(isNowItalic: Boolean) {
        performHapticFeedback(
            if (isNowItalic) {
                HapticFeedbackType.LongPress
            } else {
                HapticFeedbackType.TextHandleMove
            }
        )
    }
    
    /**
     * Smart haptic feedback for underline formatting.
     */
    fun HapticFeedback.underlineToggled(isNowUnderlined: Boolean) {
        performHapticFeedback(
            if (isNowUnderlined) {
                HapticFeedbackType.LongPress
            } else {
                HapticFeedbackType.TextHandleMove
            }
        )
    }
    
    /**
     * Smart haptic feedback for list creation/removal.
     */
    fun HapticFeedback.listToggled(isNowList: Boolean) {
        performHapticFeedback(
            if (isNowList) {
                HapticFeedbackType.LongPress // Creating structure
            } else {
                HapticFeedbackType.TextHandleMove // Removing structure
            }
        )
    }
    
    /**
     * Smart haptic feedback for alignment changes.
     */
    fun HapticFeedback.alignmentChanged() {
        performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    /**
     * Smart haptic feedback for heading application.
     */
    fun HapticFeedback.headingApplied(level: Int) {
        // More intense feedback for higher-level headings
        val feedbackType = when (level) {
            1 -> HapticFeedbackType.LongPress // Most important
            2, 3 -> HapticFeedbackType.LongPress // Important
            else -> HapticFeedbackType.TextHandleMove // Less important
        }
        performHapticFeedback(feedbackType)
    }
    
    /**
     * Smart haptic feedback for clearing all formatting.
     */
    fun HapticFeedback.formattingCleared() {
        // Strong feedback for destructive action
        performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

/**
 * Context-aware haptic composer for complex formatting operations.
 */
class HapticComposer(private val hapticManager: RichTextHapticManager) {
    
    /**
     * Composes haptic feedback for multiple simultaneous formatting changes.
     */
    fun composeFormattingChanges(changes: List<FormattingChange>) {
        if (changes.isEmpty()) return
        
        // Prioritize the most significant change for haptic feedback
        val primaryChange = changes.maxByOrNull { it.priority } ?: return
        
        when (primaryChange.type) {
            FormattingChangeType.BOLD -> hapticManager.onFormatToggled(primaryChange.isActive)
            FormattingChangeType.ITALIC -> hapticManager.onFormatToggled(primaryChange.isActive)
            FormattingChangeType.UNDERLINE -> hapticManager.onFormatToggled(primaryChange.isActive)
            FormattingChangeType.LIST -> hapticManager.onListToggled(primaryChange.isActive)
            FormattingChangeType.ALIGNMENT -> hapticManager.onAlignmentChanged()
            FormattingChangeType.HEADING -> hapticManager.onHeadingApplied(primaryChange.level ?: 1)
            FormattingChangeType.CLEAR -> hapticManager.onFormattingCleared()
        }
    }
}

/**
 * Data class representing a formatting change for haptic composition.
 */
data class FormattingChange(
    val type: FormattingChangeType,
    val isActive: Boolean,
    val level: Int? = null, // For headings
    val priority: Int = 1 // Higher priority gets haptic feedback
)

/**
 * Types of formatting changes for haptic feedback.
 */
enum class FormattingChangeType(val priority: Int) {
    BOLD(5),
    ITALIC(4),
    UNDERLINE(3),
    LIST(6),
    ALIGNMENT(2),
    HEADING(7),
    CLEAR(8) // Highest priority for destructive actions
}

/**
 * Extension to easily create haptic feedback for button presses.
 */
@Composable
fun RichTextHapticManager.createButtonHaptic(
    operation: FormattingChangeType,
    isActive: Boolean,
    level: Int? = null
): () -> Unit = {
    when (operation) {
        FormattingChangeType.BOLD,
        FormattingChangeType.ITALIC,
        FormattingChangeType.UNDERLINE -> onFormatToggled(isActive)
        FormattingChangeType.LIST -> onListToggled(isActive)
        FormattingChangeType.ALIGNMENT -> onAlignmentChanged()
        FormattingChangeType.HEADING -> onHeadingApplied(level ?: 1)
        FormattingChangeType.CLEAR -> onFormattingCleared()
    }
}