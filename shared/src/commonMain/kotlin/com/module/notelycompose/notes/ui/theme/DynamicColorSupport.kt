package com.module.notelycompose.notes.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Platform-agnostic interface for dynamic color support
 * 
 * Provides a common interface for Material You and dynamic color features
 * across different platforms with appropriate fallbacks.
 */
expect object DynamicColorSupport {
    /**
     * Check if dynamic color is supported on the current platform
     */
    fun isSupported(): Boolean
    
    /**
     * Get dynamic color scheme from system (Material You on Android 12+)
     * 
     * @param isDark Whether to get dark or light dynamic colors
     * @return Dynamic ColorScheme if supported, null otherwise
     */
    @Composable
    fun getDynamicColorScheme(isDark: Boolean): ColorScheme?
    
    /**
     * Get the minimum API level required for dynamic color support
     */
    fun getMinimumApiLevel(): Int
}

/**
 * Enhanced theme configuration for Material 3 Expressive design
 * 
 * @param isDark Whether to use dark theme
 * @param accentColor Predefined accent color name
 * @param customSeedColor Optional custom seed color for dynamic generation
 * @param enableDynamicColor Whether to attempt using dynamic colors
 * @param enableExpressiveColors Whether to use expressive color enhancements
 */
data class ExpressiveThemeConfig(
    val isDark: Boolean = false,
    val accentColor: String = "Material Blue",
    val customSeedColor: Color? = null,
    val enableDynamicColor: Boolean = false,
    val enableExpressiveColors: Boolean = true
)

/**
 * Create an expressive Material 3 ColorScheme with dynamic color support
 * 
 * @param config Theme configuration options
 * @return A ColorScheme optimized for Material 3 Expressive design
 */
@Composable
fun createExpressiveColorScheme(config: ExpressiveThemeConfig): ColorScheme {
    // Try dynamic colors first if enabled and supported
    if (config.enableDynamicColor && DynamicColorSupport.isSupported()) {
        DynamicColorSupport.getDynamicColorScheme(config.isDark)?.let { dynamicScheme ->
            return dynamicScheme
        }
    }
    
    // Fall back to custom seed color if provided
    if (config.customSeedColor != null && config.enableExpressiveColors) {
        return Material3ColorScheme.createDynamicColorScheme(
            seedColor = config.customSeedColor,
            isDark = config.isDark
        )
    }
    
    // Fall back to predefined accent colors
    return Material3ColorScheme.createColorScheme(
        isDark = config.isDark,
        accentColorName = config.accentColor
    )
}

/**
 * Material 3 Expressive Color Tokens
 * 
 * Provides semantic access to expressive color variations
 */
object ExpressiveColorTokens {
    /**
     * Get all available seed colors for dynamic generation
     */
    fun getAvailableSeedColors(): Map<String, Color> = 
        Material3ColorScheme.SeedColors.getAllSeedColors()
    
    /**
     * Get all available predefined accent colors
     */
    fun getAvailableAccentColors(): Set<String> = setOf(
        "Material Red",
        "Material Green",
        "Material Blue", 
        "Material Purple",
        "Material Orange",
        "Material Teal"
    )
    
    /**
     * Validate if a given accent color name is supported
     */
    fun isValidAccentColor(accentColorName: String): Boolean =
        accentColorName in getAvailableAccentColors()
}