package com.module.notelycompose.notes.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * iOS implementation of dynamic color support
 * 
 * iOS doesn't support Material You, so this provides appropriate fallbacks
 * while maintaining the same interface as Android.
 */
actual object DynamicColorSupport {
    /**
     * Dynamic color is not supported on iOS
     */
    actual fun isSupported(): Boolean = false
    
    /**
     * iOS doesn't support Material You dynamic colors
     * Always returns null to trigger fallback behavior
     */
    @Composable
    actual fun getDynamicColorScheme(isDark: Boolean): ColorScheme? = null
    
    /**
     * iOS doesn't have API level requirements for dynamic colors
     * Returns 0 to indicate no minimum requirement (since it's not supported)
     */
    actual fun getMinimumApiLevel(): Int = 0
}