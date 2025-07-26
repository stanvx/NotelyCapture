package com.module.notelycompose.notes.ui.richtext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.presentation.detail.RichTextFormattingState

/**
 * Context-aware toolbar system that adapts layout and content based on:
 * - Current text selection and formatting state
 * - Available screen space and orientation
 * - User preferences and usage patterns
 * - Content type and editing context
 * 
 * Features:
 * - Smart button prioritization based on usage frequency
 * - Adaptive layouts for different screen sizes
 * - Context-sensitive formatting suggestions
 * - Accessibility-optimized configurations
 * - Performance-aware lazy loading
 */
class ContextAwareToolbarConfig(
    private val preferences: ToolbarPreferences = ToolbarPreferences()
) {
    
    /**
     * Determines the optimal toolbar configuration for the given context.
     */
    fun getOptimalConfiguration(
        context: ToolbarContext
    ): ToolbarConfiguration {
        val screenSize = context.screenWidth
        val formattingState = context.formattingState
        val contentType = context.contentType
        
        return when {
            // Small screens - compact layout
            screenSize < 360.dp -> createCompactConfiguration(formattingState, contentType)
            
            // Medium screens - standard layout
            screenSize < 600.dp -> createStandardConfiguration(formattingState, contentType)
            
            // Large screens - expanded layout
            else -> createExpandedConfiguration(formattingState, contentType)
        }.copy(
            prioritizeByUsage = preferences.prioritizeByUsage,
            showTooltips = preferences.showTooltips,
            enableAnimations = preferences.enableAnimations
        )
    }
    
    private fun createCompactConfiguration(
        state: RichTextFormattingState,
        contentType: ContentType
    ): ToolbarConfiguration {
        val buttons = mutableListOf<ToolbarButton>()
        
        // Always show most used buttons
        buttons.add(ToolbarButton.Bold)
        buttons.add(ToolbarButton.Italic)
        buttons.add(ToolbarButton.List)
        
        // Add context-specific buttons
        if (contentType == ContentType.CODE) {
            buttons.add(ToolbarButton.Code)
            buttons.add(ToolbarButton.Preformatted)
        }
        
        if (contentType == ContentType.QUOTE) {
            buttons.add(ToolbarButton.Quote)
            buttons.add(ToolbarButton.Indent)
        }
        
        return ToolbarConfiguration(
            layout = ToolbarLayout.Compact,
            buttons = buttons,
            groupSpacing = 4.dp,
            buttonSpacing = 2.dp
        )
    }
    
    private fun createStandardConfiguration(
        state: RichTextFormattingState,
        contentType: ContentType
    ): ToolbarConfiguration {
        val buttons = mutableListOf<ToolbarButton>()
        
        // Primary formatting
        buttons.addAll(listOf(
            ToolbarButton.Bold,
            ToolbarButton.Italic,
            ToolbarButton.Underline
        ))
        
        // Lists
        buttons.add(ToolbarButton.List)
        
        // Headings if text is selected
        if (state.hasSelection) {
            buttons.add(ToolbarButton.Heading)
        }
        
        // Alignment
        buttons.add(ToolbarButton.Alignment)
        
        // Context-specific additions
        when (contentType) {
            ContentType.CODE -> {
                buttons.add(ToolbarButton.Code)
                buttons.add(ToolbarButton.Preformatted)
            }
            ContentType.QUOTE -> {
                buttons.add(ToolbarButton.Quote)
                buttons.add(ToolbarButton.Indent)
            }
            ContentType.LINK -> {
                buttons.add(ToolbarButton.Link)
                buttons.add(ToolbarButton.Unlink)
            }
            else -> {}
        }
        
        return ToolbarConfiguration(
            layout = ToolbarLayout.Standard,
            buttons = buttons,
            groupSpacing = 8.dp,
            buttonSpacing = 4.dp
        )
    }
    
    private fun createExpandedConfiguration(
        state: RichTextFormattingState,
        contentType: ContentType
    ): ToolbarConfiguration {
        val buttons = mutableListOf<ToolbarButton>()
        
        // Text styling
        buttons.addAll(listOf(
            ToolbarButton.Bold,
            ToolbarButton.Italic,
            ToolbarButton.Underline,
            ToolbarButton.Strikethrough
        ))
        
        // Lists
        buttons.addAll(listOf(
            ToolbarButton.UnorderedList,
            ToolbarButton.OrderedList
        ))
        
        // Headings
        buttons.add(ToolbarButton.Heading)
        
        // Alignment
        buttons.addAll(listOf(
            ToolbarButton.AlignLeft,
            ToolbarButton.AlignCenter,
            ToolbarButton.AlignRight
        ))
        
        // Context-specific
        when (contentType) {
            ContentType.CODE -> {
                buttons.addAll(listOf(
                    ToolbarButton.Code,
                    ToolbarButton.Preformatted,
                    ToolbarButton.Monospace
                ))
            }
            ContentType.QUOTE -> {
                buttons.addAll(listOf(
                    ToolbarButton.Quote,
                    ToolbarButton.Indent,
                    ToolbarButton.Outdent
                ))
            }
            ContentType.LINK -> {
                buttons.addAll(listOf(
                    ToolbarButton.Link,
                    ToolbarButton.Unlink,
                    ToolbarButton.EditLink
                ))
            }
            else -> {
                buttons.add(ToolbarButton.ClearFormatting)
            }
        }
        
        return ToolbarConfiguration(
            layout = ToolbarLayout.Expanded,
            buttons = buttons,
            groupSpacing = 12.dp,
            buttonSpacing = 6.dp
        )
    }
    
    /**
     * Analyzes text content to determine its type for context-aware configuration.
     */
    fun analyzeContentType(text: String): ContentType {
        return when {
            text.contains("```") || text.contains("`") -> ContentType.CODE
            text.startsWith(">") -> ContentType.QUOTE
            text.contains("http") -> ContentType.LINK
            text.trim().split("\n").size > 3 -> ContentType.LONG_FORM
            else -> ContentType.GENERAL
        }
    }
}

/**
 * Data class representing the context for toolbar configuration.
 */
data class ToolbarContext(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val orientation: ScreenOrientation,
    val formattingState: RichTextFormattingState,
    val contentType: ContentType,
    val hasSelection: Boolean,
    val selectionLength: Int,
    val isKeyboardVisible: Boolean,
    val availableSpace: Dp
)

/**
 * Configuration for toolbar layout and behavior.
 */
data class ToolbarConfiguration(
    val layout: ToolbarLayout,
    val buttons: List<ToolbarButton>,
    val groupSpacing: Dp,
    val buttonSpacing: Dp,
    val prioritizeByUsage: Boolean = true,
    val showTooltips: Boolean = true,
    val enableAnimations: Boolean = true,
    val accessibleMode: Boolean = false,
    val highContrast: Boolean = false
)

/**
 * Types of toolbar layouts.
 */
enum class ToolbarLayout {
    COMPACT,    // Minimal buttons, tight spacing
    STANDARD,   // Balanced layout for most screens
    EXPANDED,   // All buttons, generous spacing
    ADAPTIVE    // Dynamically adjusts based on content
}

/**
 * Available toolbar buttons.
 */
enum class ToolbarButton {
    BOLD, ITALIC, UNDERLINE, STRIKETHROUGH,
    UNORDERED_LIST, ORDERED_LIST, LIST,
    HEADING, HEADING_1, HEADING_2, HEADING_3,
    ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT, ALIGNMENT,
    CODE, QUOTE, PRE, PREFORMATTED, MONOSPACE,
    LINK, UNLINK, EDIT_LINK,
    INDENT, OUTDENT,
    CLEAR, CLEAR_FORMATTING,
    UNDO, REDO
}

/**
 * Types of content for context-aware configuration.
 */
enum class ContentType {
    GENERAL,
    CODE,
    QUOTE,
    LINK,
    LONG_FORM,
    HEADING
}

/**
 * Screen orientations.
 */
enum class ScreenOrientation {
    PORTRAIT, LANDSCAPE, SQUARE
}

/**
 * User preferences for toolbar behavior.
 */
data class ToolbarPreferences(
    val prioritizeByUsage: Boolean = true,
    val showTooltips: Boolean = true,
    val enableAnimations: Boolean = true,
    val compactMode: Boolean = false,
    val highContrast: Boolean = false,
    val largeTextMode: Boolean = false,
    val reducedMotion: Boolean = false
)

/**
 * Usage analytics for button prioritization.
 */
class ToolbarUsageAnalytics {
    private val usageCounts = mutableMapOf<ToolbarButton, Int>()
    
    fun recordUsage(button: ToolbarButton) {
        usageCounts[button] = usageCounts.getOrDefault(button, 0) + 1
    }
    
    fun getMostUsedButtons(limit: Int): List<ToolbarButton> {
        return usageCounts.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }
    
    fun getUsageFrequency(button: ToolbarButton): Float {
        val total = usageCounts.values.sum()
        return if (total > 0) usageCounts.getOrDefault(button, 0).toFloat() / total else 0f
    }
}

/**
 * Composable for remembering context-aware toolbar configuration.
 */
@Composable
fun rememberContextAwareToolbarConfig(
    preferences: ToolbarPreferences = ToolbarPreferences()
): ContextAwareToolbarConfig {
    return remember(preferences) {
        ContextAwareToolbarConfig(preferences)
    }
}

/**
 * Creates toolbar context from current application state.
 */
@Composable
fun createToolbarContext(
    formattingState: RichTextFormattingState,
    contentType: ContentType,
    hasSelection: Boolean,
    selectionLength: Int,
    isKeyboardVisible: Boolean,
    availableSpace: Dp = LocalConfiguration.current.screenWidthDp.dp
): ToolbarContext {
    val configuration = LocalConfiguration.current
    
    return ToolbarContext(
        screenWidth = configuration.screenWidthDp.dp,
        screenHeight = configuration.screenHeightDp.dp,
        orientation = if (configuration.screenWidthDp > configuration.screenHeightDp) {
            ScreenOrientation.LANDSCAPE
        } else {
            ScreenOrientation.PORTRAIT
        },
        formattingState = formattingState,
        contentType = contentType,
        hasSelection = hasSelection,
        selectionLength = selectionLength,
        isKeyboardVisible = isKeyboardVisible,
        availableSpace = availableSpace
    )
}