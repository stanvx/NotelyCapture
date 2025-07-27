package com.module.notelycompose.notes.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of Material You dynamic color support
 * 
 * Provides Material You integration for Android 12+ (API 31+) with
 * appropriate fallbacks for older versions.
 */
actual object DynamicColorSupport {
    /**
     * Check if dynamic color is supported on Android
     * Requires Android 12+ (API 31+)
     */
    actual fun isSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    
    /**
     * Get Material You dynamic color scheme from Android system
     * 
     * @param isDark Whether to get dark or light dynamic colors
     * @return Dynamic ColorScheme if supported (Android 12+), null otherwise
     */
    @Composable
    actual fun getDynamicColorScheme(isDark: Boolean): ColorScheme? {
        if (!isSupported()) return null
        
        val context = LocalContext.current
        return try {
            if (isDark) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } catch (e: Exception) {
            // Fallback to null if dynamic colors fail to load
            null
        }
    }
    
    /**
     * Android requires API 31 (Android 12) for Material You
     */
    actual fun getMinimumApiLevel(): Int = Build.VERSION_CODES.S
}