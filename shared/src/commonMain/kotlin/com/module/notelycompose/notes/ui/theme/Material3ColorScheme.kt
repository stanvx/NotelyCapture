package com.module.notelycompose.notes.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Material 3 ColorScheme generation with custom accent colors
 */
object Material3ColorScheme {

    /**
     * Predefined accent color palettes following Material 3 guidelines
     */
    private val accentColorPalettes = mapOf(
        "Material Red" to AccentColorPalette(
            light = Color(0xFFD32F2F),
            lightVariant = Color(0xFFEF5350),
            dark = Color(0xFFEF5350),
            darkVariant = Color(0xFFE57373)
        ),
        "Material Green" to AccentColorPalette(
            light = Color(0xFF388E3C),
            lightVariant = Color(0xFF66BB6A),
            dark = Color(0xFF66BB6A),
            darkVariant = Color(0xFF81C784)
        ),
        "Material Blue" to AccentColorPalette(
            light = Color(0xFF1976D2),
            lightVariant = Color(0xFF42A5F5),
            dark = Color(0xFF42A5F5),
            darkVariant = Color(0xFF64B5F6)
        ),
        "Material Purple" to AccentColorPalette(
            light = Color(0xFF7B1FA2),
            lightVariant = Color(0xFFAB47BC),
            dark = Color(0xFFAB47BC),
            darkVariant = Color(0xFFBA68C8)
        ),
        "Material Orange" to AccentColorPalette(
            light = Color(0xFFF57C00),
            lightVariant = Color(0xFFFF9800),
            dark = Color(0xFFFF9800),
            darkVariant = Color(0xFFFFB74D)
        ),
        "Material Teal" to AccentColorPalette(
            light = Color(0xFF00796B),
            lightVariant = Color(0xFF26A69A),
            dark = Color(0xFF26A69A),
            darkVariant = Color(0xFF4DB6AC)
        )
    )

    private data class AccentColorPalette(
        val light: Color,
        val lightVariant: Color,
        val dark: Color,
        val darkVariant: Color
    )

    /**
     * Generate a Material 3 ColorScheme based on theme mode and accent color
     */
    fun createColorScheme(isDark: Boolean, accentColorName: String): ColorScheme {
        val accentPalette = accentColorPalettes[accentColorName] 
            ?: accentColorPalettes["Material Blue"]!!

        return if (isDark) {
            createDarkColorScheme(accentPalette)
        } else {
            createLightColorScheme(accentPalette)
        }
    }

    private fun createLightColorScheme(accent: AccentColorPalette): ColorScheme {
        return lightColorScheme(
            primary = accent.light,
            onPrimary = Color.White,
            primaryContainer = accent.lightVariant.copy(alpha = 0.12f),
            onPrimaryContainer = accent.light,
            
            secondary = accent.lightVariant,
            onSecondary = Color.White,
            secondaryContainer = accent.lightVariant.copy(alpha = 0.08f),
            onSecondaryContainer = accent.light,
            
            tertiary = accent.light.copy(alpha = 0.8f),
            onTertiary = Color.White,
            tertiaryContainer = accent.lightVariant.copy(alpha = 0.05f),
            onTertiaryContainer = accent.light,
            
            error = Color(0xFFBA1A1A),
            onError = Color.White,
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),
            
            background = Color(0xFFFFFBFE),
            onBackground = Color(0xFF1C1B1F),
            
            surface = Color(0xFFFFFBFE),
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFFE7E0EC),
            onSurfaceVariant = Color(0xFF49454F),
            
            outline = Color(0xFF79747E),
            outlineVariant = Color(0xFFCAC4D0),
            
            scrim = Color(0xFF000000),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = accent.lightVariant,
            
            surfaceDim = Color(0xFFDDD8DD),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F2F7),
            surfaceContainer = Color(0xFFF1ECF1),
            surfaceContainerHigh = Color(0xFFECE6EB),
            surfaceContainerHighest = Color(0xFFE6E0E5)
        )
    }

    private fun createDarkColorScheme(accent: AccentColorPalette): ColorScheme {
        return darkColorScheme(
            primary = accent.dark,
            onPrimary = Color(0xFF1C1B1F),
            primaryContainer = accent.darkVariant.copy(alpha = 0.16f),
            onPrimaryContainer = accent.darkVariant,
            
            secondary = accent.darkVariant,
            onSecondary = Color(0xFF1C1B1F),
            secondaryContainer = accent.dark.copy(alpha = 0.12f),
            onSecondaryContainer = accent.darkVariant,
            
            tertiary = accent.darkVariant.copy(alpha = 0.9f),
            onTertiary = Color(0xFF1C1B1F),
            tertiaryContainer = accent.dark.copy(alpha = 0.08f),
            onTertiaryContainer = accent.darkVariant,
            
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
            
            background = Color(0xFF1C1B1F),
            onBackground = Color(0xFFE6E1E5),
            
            surface = Color(0xFF1C1B1F),
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = Color(0xFF49454F),
            onSurfaceVariant = Color(0xFFCAC4D0),
            
            outline = Color(0xFF938F99),
            outlineVariant = Color(0xFF49454F),
            
            scrim = Color(0xFF000000),
            inverseSurface = Color(0xFFE6E1E5),
            inverseOnSurface = Color(0xFF313033),
            inversePrimary = accent.light,
            
            surfaceDim = Color(0xFF1C1B1F),
            surfaceBright = Color(0xFF423F42),
            surfaceContainerLowest = Color(0xFF0F0D13),
            surfaceContainerLow = Color(0xFF1C1B1F),
            surfaceContainer = Color(0xFF201F23),
            surfaceContainerHigh = Color(0xFF2B292D),
            surfaceContainerHighest = Color(0xFF36343B)
        )
    }
}