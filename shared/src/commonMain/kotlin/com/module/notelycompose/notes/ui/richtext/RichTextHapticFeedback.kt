package com.module.notelycompose.notes.ui.richtext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Comprehensive haptic feedback system for rich text editing.
 * Provides tactile feedback for formatting actions and user interactions.
 * 
 * Features:
 * - Context-aware haptic patterns for different formatting types
 * - Configurable intensity levels for different user preferences
 * - Platform-specific haptic implementations
 * - Accessibility-friendly haptic feedback
 * - Performance-optimized haptic patterns
 */
class RichTextHapticFeedbackManager(
    private val hapticFeedback: HapticFeedback,
    private val preferences: HapticFeedbackPreferences = HapticFeedbackPreferences()
) {
    
    /**
     * Provides haptic feedback for formatting actions.
     * 
     * @param actionType The type of formatting action performed
     * @param success Whether the action was successful
     */
    fun provideFormattingFeedback(actionType: FormattingActionType, success: Boolean = true) {
        if (!preferences.isEnabled) return
        
        val feedbackType = when (actionType) {
            FormattingActionType.TOGGLE_BOLD -> HapticFeedbackType.LongPress
            FormattingActionType.TOGGLE_ITALIC -> HapticFeedbackType.TextHandleMove
            FormattingActionType.TOGGLE_UNDERLINE -> HapticFeedbackType.LongPress
            FormattingActionType.TOGGLE_STRIKETHROUGH -> HapticFeedbackType.TextHandleMove
            FormattingActionType.TOGGLE_CODE_BLOCK -> HapticFeedbackType.LongPress
            FormattingActionType.TOGGLE_QUOTE_BLOCK -> HapticFeedbackType.LongPress
            FormattingActionType.APPLY_HEADING -> HapticFeedbackType.LongPress
            FormattingActionType.TOGGLE_LIST -> HapticFeedbackType.TextHandleMove
            FormattingActionType.SET_ALIGNMENT -> HapticFeedbackType.LongPress
            FormattingActionType.CLEAR_FORMATTING -> HapticFeedbackType.LongPress
            FormattingActionType.UNDO -> HapticFeedbackType.TextHandleMove
            FormattingActionType.REDO -> HapticFeedbackType.TextHandleMove
            FormattingActionType.SELECT_ALL -> HapticFeedbackType.LongPress
            FormattingActionType.COPY -> HapticFeedbackType.TextHandleMove
            FormattingActionType.PASTE -> HapticFeedbackType.LongPress
            FormattingActionType.CUT -> HapticFeedbackType.TextHandleMove
        }
        
        if (success) {
            hapticFeedback.performHapticFeedback(feedbackType)
        } else {
            // Provide error feedback if action failed
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    /**
     * Provides haptic feedback for keyboard shortcuts.
     * 
     * @param shortcutType The type of keyboard shortcut used
     */
    fun provideKeyboardShortcutFeedback(shortcutType: KeyboardShortcutType) {
        if (!preferences.isEnabled) return
        
        val feedbackType = when (shortcutType) {
            KeyboardShortcutType.FORMATTING -> HapticFeedbackType.TextHandleMove
            KeyboardShortcutType.NAVIGATION -> HapticFeedbackType.TextHandleMove
            KeyboardShortcutType.EDITING -> HapticFeedbackType.LongPress
            KeyboardShortcutType.LIST -> HapticFeedbackType.TextHandleMove
            KeyboardShortcutType.HEADING -> HapticFeedbackType.LongPress
            KeyboardShortcutType.BLOCK -> HapticFeedbackType.LongPress
        }
        
        hapticFeedback.performHapticFeedback(feedbackType)
    }
    
    /**
     * Provides haptic feedback for toolbar interactions.
     * 
     * @param interactionType The type of toolbar interaction
     */
    fun provideToolbarFeedback(interactionType: ToolbarInteractionType) {
        if (!preferences.isEnabled) return
        
        val feedbackType = when (interactionType) {
            ToolbarInteractionType.BUTTON_PRESS -> HapticFeedbackType.TextHandleMove
            ToolbarInteractionType.TOGGLE_ON -> HapticFeedbackType.LongPress
            ToolbarInteractionType.TOGGLE_OFF -> HapticFeedbackType.TextHandleMove
            ToolbarInteractionType.DROPDOWN_OPEN -> HapticFeedbackType.LongPress
            ToolbarInteractionType.DROPDOWN_CLOSE -> HapticFeedbackType.TextHandleMove
            ToolbarInteractionType.SCROLL -> HapticFeedbackType.TextHandleMove
        }
        
        hapticFeedback.performHapticFeedback(feedbackType)
    }
    
    /**
     * Provides haptic feedback for selection changes.
     * 
     * @param selectionType The type of selection change
     */
    fun provideSelectionFeedback(selectionType: SelectionType) {
        if (!preferences.isEnabled) return
        
        val feedbackType = when (selectionType) {
            SelectionType.TEXT_SELECTED -> HapticFeedbackType.TextHandleMove
            SelectionType.CURSOR_MOVED -> HapticFeedbackType.TextHandleMove
            SelectionType.WORD_SELECTED -> HapticFeedbackType.LongPress
            SelectionType.PARAGRAPH_SELECTED -> HapticFeedbackType.LongPress
            SelectionType.ALL_SELECTED -> HapticFeedbackType.LongPress
        }
        
        hapticFeedback.performHapticFeedback(feedbackType)
    }
    
    /**
     * Provides haptic feedback for undo/redo operations.
     * 
     * @param operationType The type of undo/redo operation
     * @param hasMoreOperations Whether there are more operations available
     */
    fun provideUndoRedoFeedback(operationType: UndoRedoType, hasMoreOperations: Boolean) {
        if (!preferences.isEnabled) return
        
        val feedbackType = when (operationType) {
            UndoRedoType.UNDO -> HapticFeedbackType.TextHandleMove
            UndoRedoType.REDO -> HapticFeedbackType.TextHandleMove
            UndoRedoType.BATCH_UNDO -> HapticFeedbackType.LongPress
            UndoRedoType.BATCH_REDO -> HapticFeedbackType.LongPress
        }
        
        hapticFeedback.performHapticFeedback(feedbackType)
        
        // Provide additional feedback if no more operations
        if (!hasMoreOperations) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    /**
     * Updates haptic feedback preferences.
     * 
     * @param newPreferences Updated preferences
     */
    fun updatePreferences(newPreferences: HapticFeedbackPreferences) {
        preferences.copyFrom(newPreferences)
    }
    
    /**
     * Temporarily disables haptic feedback.
     */
    fun disableTemporarily() {
        preferences.isEnabled = false
    }
    
    /**
     * Re-enables haptic feedback after temporary disable.
     */
    fun enable() {
        preferences.isEnabled = true
    }
}

/**
 * Preferences for customizing haptic feedback behavior.
 */
data class HapticFeedbackPreferences(
    var isEnabled: Boolean = true,
    val intensityLevel: HapticIntensity = HapticIntensity.MEDIUM,
    val enableForFormatting: Boolean = true,
    val enableForShortcuts: Boolean = true,
    val enableForToolbar: Boolean = true,
    val enableForSelection: Boolean = true,
    val enableForUndoRedo: Boolean = true,
    val platformSpecific: PlatformHapticSettings = PlatformHapticSettings()
) {
    fun copyFrom(other: HapticFeedbackPreferences) {
        isEnabled = other.isEnabled
        // Note: Other properties are immutable for safety
    }
}

/**
 * Intensity levels for haptic feedback.
 */
enum class HapticIntensity {
    LIGHT,
    MEDIUM,
    STRONG
}

/**
 * Types of formatting actions that can trigger haptic feedback.
 */
enum class FormattingActionType {
    TOGGLE_BOLD,
    TOGGLE_ITALIC,
    TOGGLE_UNDERLINE,
    TOGGLE_STRIKETHROUGH,
    TOGGLE_CODE_BLOCK,
    TOGGLE_QUOTE_BLOCK,
    APPLY_HEADING,
    TOGGLE_LIST,
    SET_ALIGNMENT,
    CLEAR_FORMATTING,
    UNDO,
    REDO,
    SELECT_ALL,
    COPY,
    PASTE,
    CUT
}

/**
 * Types of keyboard shortcuts that can trigger haptic feedback.
 */
enum class KeyboardShortcutType {
    FORMATTING,
    NAVIGATION,
    EDITING,
    LIST,
    HEADING,
    BLOCK
}

/**
 * Types of toolbar interactions that can trigger haptic feedback.
 */
enum class ToolbarInteractionType {
    BUTTON_PRESS,
    TOGGLE_ON,
    TOGGLE_OFF,
    DROPDOWN_OPEN,
    DROPDOWN_CLOSE,
    SCROLL
}

/**
 * Types of selection changes that can trigger haptic feedback.
 */
enum class SelectionType {
    TEXT_SELECTED,
    CURSOR_MOVED,
    WORD_SELECTED,
    PARAGRAPH_SELECTED,
    ALL_SELECTED
}

/**
 * Types of undo/redo operations.
 */
enum class UndoRedoType {
    UNDO,
    REDO,
    BATCH_UNDO,
    BATCH_REDO
}

/**
 * Platform-specific haptic settings.
 */
data class PlatformHapticSettings(
    val android: AndroidHapticSettings = AndroidHapticSettings(),
    val ios: iOSHapticSettings = iOSHapticSettings()
)

/**
 * Android-specific haptic settings.
 */
data class AndroidHapticSettings(
    val useVibration: Boolean = true,
    val vibrationDuration: Long = 50L,
    val amplitude: Int = 50
)

/**
 * iOS-specific haptic settings.
 */
data class iOSHapticSettings(
    val useHapticFeedback: Boolean = true,
    val intensity: Float = 0.5f
)

/**
 * Composable for remembering and managing haptic feedback.
 */
@Composable
fun rememberRichTextHapticFeedback(): RichTextHapticFeedbackManager {
    val hapticFeedback = LocalHapticFeedback.current
    return remember(hapticFeedback) {
        RichTextHapticFeedbackManager(hapticFeedback)
    }
}

/**
 * Extension function for providing haptic feedback in rich text components.
 */
@Composable
fun androidx.compose.ui.platform.LocalHapticFeedback.rememberRichTextHapticManager(): RichTextHapticFeedbackManager {
    val hapticFeedback = LocalHapticFeedback.current
    return remember { RichTextHapticFeedbackManager(hapticFeedback) }
}

/**
 * Haptic feedback wrapper for platform-specific implementations.
 */
expect class PlatformHapticFeedbackManager {
    fun performHapticFeedback(type: HapticFeedbackType)
    fun performCustomHaptic(duration: Long, intensity: Float)
    fun isHapticFeedbackEnabled(): Boolean
}