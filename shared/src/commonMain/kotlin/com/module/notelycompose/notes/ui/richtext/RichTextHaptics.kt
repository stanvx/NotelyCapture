package com.module.notelycompose.notes.ui.richtext

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Rich text specific haptic feedback extensions.
 * Provides semantic haptic feedback for different rich text formatting actions.
 */
object RichTextHaptics {
    
    /**
     * Haptic feedback for bold text toggle
     */
    fun HapticFeedback.boldToggled(enabled: Boolean) {
        val feedbackType = if (enabled) {
            HapticFeedbackType.LongPress // Stronger feedback for enabling
        } else {
            HapticFeedbackType.TextHandleMove // Lighter feedback for disabling
        }
        performHapticFeedback(feedbackType)
    }
    
    /**
     * Haptic feedback for italic text toggle
     */
    fun HapticFeedback.italicToggled(enabled: Boolean) {
        val feedbackType = if (enabled) {
            HapticFeedbackType.TextHandleMove
        } else {
            HapticFeedbackType.TextHandleMove
        }
        performHapticFeedback(feedbackType)
    }
    
    /**
     * Haptic feedback for underline text toggle
     */
    fun HapticFeedback.underlineToggled(enabled: Boolean) {
        val feedbackType = if (enabled) {
            HapticFeedbackType.TextHandleMove
        } else {
            HapticFeedbackType.TextHandleMove
        }
        performHapticFeedback(feedbackType)
    }
    
    /**
     * Haptic feedback for list toggle (ordered/unordered)
     */
    fun HapticFeedback.listToggled(enabled: Boolean) {
        val feedbackType = if (enabled) {
            HapticFeedbackType.LongPress // Lists are structural changes
        } else {
            HapticFeedbackType.TextHandleMove
        }
        performHapticFeedback(feedbackType)
    }
    
    /**
     * Haptic feedback for text alignment changes
     */
    fun HapticFeedback.alignmentChanged() {
        performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Haptic feedback for heading application
     */
    fun HapticFeedback.headingApplied(level: Int) {
        // Different feedback intensity based on heading level
        val feedbackType = when (level) {
            1, 2 -> HapticFeedbackType.LongPress // Major headings get stronger feedback
            else -> HapticFeedbackType.TextHandleMove // Minor headings get lighter feedback
        }
        performHapticFeedback(feedbackType)
    }
    
    /**
     * Haptic feedback for clearing all formatting
     */
    fun HapticFeedback.formattingCleared() {
        // Strong feedback since this is a destructive action
        performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    /**
     * Haptic feedback for strikethrough toggle
     */
    fun HapticFeedback.strikethroughToggled(enabled: Boolean) {
        val feedbackType = if (enabled) {
            HapticFeedbackType.TextHandleMove
        } else {
            HapticFeedbackType.TextHandleMove
        }
        performHapticFeedback(feedbackType)
    }
    
    /**
     * Haptic feedback for code block toggle
     */
    fun HapticFeedback.codeBlockToggled(enabled: Boolean) {
        val feedbackType = if (enabled) {
            HapticFeedbackType.LongPress // Code blocks are structural
        } else {
            HapticFeedbackType.TextHandleMove
        }
        performHapticFeedback(feedbackType)
    }
    
    /**
     * Haptic feedback for quote block toggle
     */
    fun HapticFeedback.quoteBlockToggled(enabled: Boolean) {
        val feedbackType = if (enabled) {
            HapticFeedbackType.LongPress // Quote blocks are structural
        } else {
            HapticFeedbackType.TextHandleMove
        }
        performHapticFeedback(feedbackType)
    }
    
    /**
     * Haptic feedback for text color changes
     */
    fun HapticFeedback.textColorChanged() {
        performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Haptic feedback for highlight color changes
     */
    fun HapticFeedback.highlightChanged() {
        performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Haptic feedback for indent/outdent operations
     */
    fun HapticFeedback.indentChanged() {
        performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Haptic feedback for link operations
     */
    fun HapticFeedback.linkToggled(enabled: Boolean) {
        val feedbackType = if (enabled) {
            HapticFeedbackType.LongPress
        } else {
            HapticFeedbackType.TextHandleMove
        }
        performHapticFeedback(feedbackType)
    }
    
    /**
     * Haptic feedback for divider insertion
     */
    fun HapticFeedback.dividerInserted() {
        performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    /**
     * Generic formatting action feedback
     */
    fun HapticFeedback.formattingAction(isStructural: Boolean = false) {
        val feedbackType = if (isStructural) {
            HapticFeedbackType.LongPress
        } else {
            HapticFeedbackType.TextHandleMove
        }
        performHapticFeedback(feedbackType)
    }
}