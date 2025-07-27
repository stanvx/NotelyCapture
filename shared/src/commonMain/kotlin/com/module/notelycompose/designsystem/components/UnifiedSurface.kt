package com.module.notelycompose.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.style.LayoutGuide

/**
 * Unified Surface component following Material 3 Expressive design principles
 * 
 * Provides consistent elevation, shapes, and coloring across all surfaces.
 * Based on Material 3 elevation system with semantic naming.
 */
@Composable
fun UnifiedSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LayoutGuide.BorderRadius.md),
    color: Color = LocalCustomColors.current.bodyBackgroundColor,
    contentColor: Color = LocalCustomColors.current.bodyContentColor,
    tonalElevation: Dp = LayoutGuide.Elevation.none,
    shadowElevation: Dp = LayoutGuide.Elevation.none,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content
    )
}

/**
 * Card surface with consistent styling for content containers
 */
@Composable
fun CardSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val surfaceModifier = if (onClick != null) {
        modifier.fillMaxWidth()
    } else {
        modifier
    }
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = surfaceModifier,
            enabled = enabled,
            shape = RoundedCornerShape(LayoutGuide.BorderRadius.md),
            colors = CardDefaults.cardColors(
                containerColor = LocalCustomColors.current.bodyBackgroundColor,
                contentColor = LocalCustomColors.current.bodyContentColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = LayoutGuide.Elevation.level1
            )
        ) {
            Box(modifier = Modifier.padding(LayoutGuide.Spacing.md)) {
                content()
            }
        }
    } else {
        UnifiedSurface(
            modifier = surfaceModifier,
            shadowElevation = LayoutGuide.Elevation.level1,
            tonalElevation = LayoutGuide.Elevation.level1
        ) {
            Box(modifier = Modifier.padding(LayoutGuide.Spacing.md)) {
                content()
            }
        }
    }
}

/**
 * Dialog surface with elevated appearance
 */
@Composable
fun DialogSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    UnifiedSurface(
        modifier = modifier,
        shape = RoundedCornerShape(LayoutGuide.BorderRadius.lg),
        shadowElevation = LayoutGuide.Elevation.level5,
        tonalElevation = LayoutGuide.Elevation.level5,
        content = {
            Box(modifier = Modifier.padding(LayoutGuide.Spacing.lg)) {
                content()
            }
        }
    )
}

/**
 * FAB surface with consistent elevation and theming
 */
@Composable
fun FABSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalCustomColors.current.bodyBackgroundColor,
    contentColor: Color = LocalCustomColors.current.bodyContentColor,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(LayoutGuide.BorderRadius.full),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = LayoutGuide.Elevation.level3
        ),
        content = content
    )
}

/**
 * Search surface with appropriate elevation for input fields
 */
@Composable
fun SearchSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    UnifiedSurface(
        modifier = modifier,
        shape = RoundedCornerShape(LayoutGuide.BorderRadius.xl),
        shadowElevation = LayoutGuide.Elevation.level2,
        tonalElevation = LayoutGuide.Elevation.level2,
        content = {
            Box(modifier = Modifier.padding(LayoutGuide.Spacing.sm)) {
                content()
            }
        }
    )
}

/**
 * Bottom sheet surface with appropriate styling
 */
@Composable
fun BottomSheetSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    UnifiedSurface(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = LayoutGuide.BorderRadius.lg,
            topEnd = LayoutGuide.BorderRadius.lg
        ),
        shadowElevation = LayoutGuide.Elevation.level4,
        tonalElevation = LayoutGuide.Elevation.level4,
        content = {
            Column(modifier = Modifier.padding(LayoutGuide.Spacing.md)) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .padding(bottom = LayoutGuide.Spacing.sm)
                ) {
                    UnifiedSurface(
                        shape = RoundedCornerShape(LayoutGuide.BorderRadius.full),
                        color = LocalCustomColors.current.bodyContentColor.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
                content()
            }
        }
    )
}

/**
 * Navigation surface for app bars and navigation components
 */
@Composable
fun NavigationSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    UnifiedSurface(
        modifier = modifier,
        shadowElevation = LayoutGuide.Elevation.level3,
        tonalElevation = LayoutGuide.Elevation.level3,
        content = content
    )
}