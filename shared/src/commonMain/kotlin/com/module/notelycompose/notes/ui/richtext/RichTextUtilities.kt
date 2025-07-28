package com.module.notelycompose.notes.ui.richtext

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Utilities for rich text editor positioning and keyboard awareness.
 */

/**
 * Remembers the current keyboard height in dp.
 * This is useful for positioning toolbars above the keyboard.
 */
@Composable
fun rememberKeyboardHeight(): Dp {
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    
    return with(density) {
        imeInsets.getBottom(density).toDp()
    }
}

/**
 * Remembers the current system insets for proper positioning.
 */
@Composable
fun rememberSystemInsets(): SystemInsets {
    val density = LocalDensity.current
    val systemBarsInsets = WindowInsets.systemBars
    
    return with(density) {
        SystemInsets(
            top = systemBarsInsets.getTop(density).toDp(),
            bottom = systemBarsInsets.getBottom(density).toDp(),
            left = systemBarsInsets.getLeft(density, layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr).toDp(),
            right = systemBarsInsets.getRight(density, layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr).toDp()
        )
    }
}

/**
 * Data class representing system insets.
 */
data class SystemInsets(
    val top: Dp = 0.dp,
    val bottom: Dp = 0.dp,
    val left: Dp = 0.dp,
    val right: Dp = 0.dp
)

/**
 * Toolbar display modes for rich text editor.
 */
enum class ToolbarMode {
    /** Always visible at the bottom */
    Bottom,
    /** Floating toolbar that appears when text is focused */
    Floating,
    /** Hidden toolbar */
    Hidden
}