package com.module.notelycompose.notes.ui.theme

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Material 3 Elevation Tokens for consistent shadow and elevation system.
 * 
 * These tokens ensure proper elevation hierarchy throughout the app,
 * matching the "gold standard" header card design.
 */
object Material3ElevationTokens {
    
    /**
     * Surface level 0 - No elevation (flush with background)
     */
    val level0 = 0.dp
    
    /**
     * Surface level 1 - Subtle elevation for container elements
     * Used for: Basic cards, contained elements
     */
    val level1 = 1.dp
    
    /**
     * Surface level 2 - Low elevation for interactive elements  
     * Used for: Note cards, list items
     */
    val level2 = 3.dp
    
    /**
     * Surface level 3 - Medium elevation for prominent elements
     * Used for: Featured cards, important content
     */
    val level3 = 6.dp
    
    /**
     * Surface level 4 - High elevation for navigation and key UI
     * Used for: App bars, bottom navigation, header cards
     */
    val level4 = 8.dp
    
    /**
     * Surface level 5 - Maximum elevation for modal and floating elements
     * Used for: FABs, modal sheets, dialogs
     */
    val level5 = 12.dp
}

/**
 * Standard card elevation configurations for different use cases
 */
object CardElevationPresets {
    
    /**
     * Default note card elevation - matches calendar cards for consistency
     */
    @Composable
    fun noteCard(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = Material3ElevationTokens.level2,
        pressedElevation = Material3ElevationTokens.level3,
        focusedElevation = Material3ElevationTokens.level3,
        hoveredElevation = Material3ElevationTokens.level3,
        draggedElevation = Material3ElevationTokens.level4,
        disabledElevation = Material3ElevationTokens.level0
    )
    
    /**
     * Header card elevation - "gold standard" reference
     */
    @Composable
    fun headerCard(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = Material3ElevationTokens.level4,
        pressedElevation = Material3ElevationTokens.level5,
        focusedElevation = Material3ElevationTokens.level4,
        hoveredElevation = Material3ElevationTokens.level4,
        draggedElevation = Material3ElevationTokens.level5,
        disabledElevation = Material3ElevationTokens.level1
    )
    
    /**
     * Compact elevation for smaller elements
     */
    @Composable
    fun compact(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = Material3ElevationTokens.level1,
        pressedElevation = Material3ElevationTokens.level2,
        focusedElevation = Material3ElevationTokens.level2,
        hoveredElevation = Material3ElevationTokens.level2,
        draggedElevation = Material3ElevationTokens.level3,
        disabledElevation = Material3ElevationTokens.level0
    )
    
    /**
     * Enhanced elevation for important interactive elements
     */
    @Composable
    fun enhanced(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = Material3ElevationTokens.level3,
        pressedElevation = Material3ElevationTokens.level4,
        focusedElevation = Material3ElevationTokens.level4,
        hoveredElevation = Material3ElevationTokens.level4,
        draggedElevation = Material3ElevationTokens.level5,
        disabledElevation = Material3ElevationTokens.level0
    )
}