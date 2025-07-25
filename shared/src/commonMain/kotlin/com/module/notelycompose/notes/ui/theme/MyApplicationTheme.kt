package com.module.notelycompose.notes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * Simplified Material 3 Theme for Notely Capture
 * 
 * Uses clean Material Theme Builder generated colors.
 * Removed accent color options for cleaner theming.
 * 
 * This is a compatibility wrapper that redirects to the new Theme.kt implementation.
 * 
 * @param darkTheme Whether to use dark theme colors
 * @param content The composable content to wrap with the theme
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Call the new clean theme implementation
    AppTheme(
        darkTheme = darkTheme,
        content = content
    )
}
