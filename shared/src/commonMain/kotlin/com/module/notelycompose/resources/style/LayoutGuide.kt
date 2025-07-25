package com.module.notelycompose.resources.style

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Material 3 Design System Layout Guide
 * 
 * Provides consistent spacing, elevation, and sizing tokens following
 * Material 3 Expressive design principles with 8dp base grid system.
 */
object LayoutGuide {

    // MARK: - Material 3 Spacing System (8dp base grid)
    object Spacing {
        val none = 0.dp
        val xs = 4.dp    // 0.5x base
        val sm = 8.dp    // 1x base - primary spacing unit
        val md = 16.dp   // 2x base
        val lg = 24.dp   // 3x base
        val xl = 32.dp   // 4x base
        val xxl = 40.dp  // 5x base
        val xxxl = 48.dp // 6x base
        
        // Semantic spacing aliases for backward compatibility
        val extraExtraSmall = xs
        val extraSmall = xs
        val small = sm
        val medium = md
        val large = lg
        val extraLarge = xl
        val extraExtraLarge = xxl
    }

    // MARK: - Material 3 Elevation System
    object Elevation {
        val none = 0.dp
        val level1 = 1.dp  // Cards, Chips
        val level2 = 3.dp  // Search bars, Text fields
        val level3 = 6.dp  // FABs, App bars
        val level4 = 8.dp  // Navigation drawer
        val level5 = 12.dp // Modal dialogs
    }

    // MARK: - Component Sizing
    object ComponentSize {
        // Touch targets (minimum 48dp)
        val minTouchTarget = 48.dp
        val iconButton = 40.dp
        val smallIconButton = 32.dp
        
        // FAB sizes
        val fabSmall = 40.dp
        val fabMedium = 56.dp
        val fabLarge = 96.dp
        
        // App bar heights
        val topAppBarHeight = 64.dp
        val bottomAppBarHeight = 80.dp
    }

    // MARK: - Border Radius
    object BorderRadius {
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val full = 1000.dp // For circular shapes
    }

    // MARK: - Fonts (Deprecated - use Material3TypographyTokens instead)
    @Deprecated("Use Material3TypographyTokens instead", ReplaceWith("Material3TypographyTokens"))
    object FontSize {
        val small = 14.sp
        val smallPlusPlus = 16.sp
        val medium = 18.sp
    }

    // MARK: - Padding (Deprecated - use Spacing instead)
    @Deprecated("Use Spacing instead", ReplaceWith("LayoutGuide.Spacing"))
    object Padding {
        val none = Spacing.none
        val extraExtraSmall = Spacing.extraExtraSmall
        val extraSmall = Spacing.extraSmall
        val small = Spacing.small
        val medium = Spacing.medium
        val large = Spacing.large
        val extraLarge = Spacing.extraLarge
        val extraExtraLarge = Spacing.extraExtraLarge
    }

    // MARK: - Platform Audio Player Ui
    object PlatformAudio {
        val playContainerWeight = 0.08f
        val playTimeContainerWeight = 0.10f
        val sliderContainerWeight = 0.6f
        val durationContainerWeight = 0.14f
    }
}
