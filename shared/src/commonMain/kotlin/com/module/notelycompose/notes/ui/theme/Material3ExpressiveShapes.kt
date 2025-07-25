package com.module.notelycompose.notes.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Shape system with 5-tier scale
 * 
 * Implements the complete M3 shape scale for enhanced visual expressiveness:
 * - Extra Small (4dp): For small components like chips, badges
 * - Small (8dp): For buttons, text fields  
 * - Medium (12dp): For cards, dialogs
 * - Large (16dp): For sheets, large surfaces
 * - Extra Large (28dp): For prominent surfaces, hero cards
 * 
 * Based on Material 3 Shape Guidelines:
 * https://m3.material.io/styles/shape/overview
 */
fun createMaterial3ExpressiveShapes(): Shapes {
    return Shapes(
        extraSmall = RoundedCornerShape(4.dp),   // Chips, badges, small buttons
        small = RoundedCornerShape(8.dp),        // Buttons, text fields, small cards
        medium = RoundedCornerShape(12.dp),      // Cards, dialogs, containers
        large = RoundedCornerShape(16.dp),       // Sheets, navigation components  
        extraLarge = RoundedCornerShape(28.dp)   // Hero cards, prominent surfaces
    )
}

/**
 * Material 3 Expressive Shape Tokens
 * 
 * Provides semantic access to shape styles for specific use cases
 */
object Material3ShapeTokens {
    // Note-specific shapes
    val noteCard = RoundedCornerShape(12.dp)           // Medium - for note cards
    val noteCardHero = RoundedCornerShape(16.dp)       // Large - for featured notes
    
    // Button shapes
    val buttonPrimary = RoundedCornerShape(8.dp)       // Small - standard buttons
    val buttonSecondary = RoundedCornerShape(6.dp)     // Between extraSmall and small
    val fabButton = RoundedCornerShape(16.dp)          // Large - floating action buttons
    
    // Input shapes
    val textField = RoundedCornerShape(8.dp)           // Small - text inputs
    val searchField = RoundedCornerShape(28.dp)        // Extra large - prominent search
    
    // Container shapes
    val dialogContainer = RoundedCornerShape(12.dp)    // Medium - dialogs, bottom sheets
    val cardContainer = RoundedCornerShape(12.dp)      // Medium - content cards
    val surfaceContainer = RoundedCornerShape(16.dp)   // Large - major surfaces
    val richTextToolbar = RoundedCornerShape(16.dp)    // Large - floating rich text toolbar
    
    // Chip and badge shapes
    val chip = RoundedCornerShape(4.dp)                // Extra small - chips, tags
    val badge = RoundedCornerShape(4.dp)               // Extra small - badges, labels
    
    // Navigation shapes
    val navigationBar = RoundedCornerShape(0.dp)       // No rounding - navigation bars
    val navigationRail = RoundedCornerShape(0.dp)      // No rounding - navigation rails
    val bottomSheet = RoundedCornerShape(
        topStart = 16.dp,                              // Large - only top corners
        topEnd = 16.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
}

/**
 * Expressive shape variations for enhanced visual interest
 * 
 * These provide additional shape options beyond the standard 5-tier system
 * for components that benefit from varied expressiveness
 */
object ExpressiveShapeVariations {
    // Asymmetric shapes for visual interest
    val asymmetricCard = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 4.dp
    )
    
    // Subtle variations for hierarchy
    val cardElevated = RoundedCornerShape(14.dp)       // Slightly larger than medium
    val cardSubtle = RoundedCornerShape(10.dp)         // Slightly smaller than medium
    
    // Voice recording specific shapes
    val voiceRecordingButton = RoundedCornerShape(24.dp)  // Extra expressive for recording
    val voiceNoteCard = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 8.dp
    )
}