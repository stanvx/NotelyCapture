package com.module.notelycompose.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.style.LayoutGuide

/**
 * Unified layout containers following Material 3 Expressive design principles
 * 
 * Provides consistent screen structure, spacing, and organization patterns
 * across all screens in the application.
 */

/**
 * Standard screen layout with consistent padding and background
 */
@Composable
fun UnifiedScreenLayout(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    backgroundColor: Color = LocalCustomColors.current.bodyBackgroundColor,
    contentColor: Color = LocalCustomColors.current.bodyContentColor,
    verticalPadding: Boolean = true,
    horizontalPadding: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar ?: {},
        bottomBar = bottomBar ?: {},
        floatingActionButton = floatingActionButton ?: {},
        containerColor = backgroundColor,
        contentColor = contentColor
    ) { paddingValues ->
        val contentModifier = Modifier.run {
            if (verticalPadding && horizontalPadding) {
                padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = LayoutGuide.Spacing.md,
                    end = LayoutGuide.Spacing.md
                )
            } else if (verticalPadding) {
                padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
            } else if (horizontalPadding) {
                padding(
                    horizontal = LayoutGuide.Spacing.md
                ).padding(paddingValues)
            } else {
                padding(paddingValues)
            }
        }
        
        content(paddingValues)
    }
}

/**
 * Scrollable screen layout for content that may overflow
 */
@Composable
fun ScrollableScreenLayout(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    UnifiedScreenLayout(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = LayoutGuide.Spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(LayoutGuide.Spacing.md)
        ) {
            content()
        }
    }
}

/**
 * List screen layout optimized for RecyclerView/LazyColumn content
 */
@Composable
fun ListScreenLayout(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    searchBar: @Composable (() -> Unit)? = null,
    filterBar: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    UnifiedScreenLayout(
        modifier = modifier,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        horizontalPadding = false,
        verticalPadding = false
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar with horizontal padding
            searchBar?.let { searchBarContent ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LayoutGuide.Spacing.md)
                        .padding(bottom = LayoutGuide.Spacing.sm)
                ) {
                    searchBarContent()
                }
            }
            
            // Filter bar without horizontal padding (full width)
            filterBar?.let { filterBarContent ->
                filterBarContent()
            }
            
            // List content without horizontal padding (full width)
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}

/**
 * Detail screen layout for editing/viewing content
 */
@Composable
fun DetailScreenLayout(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    header: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    UnifiedScreenLayout(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = LayoutGuide.Spacing.md)
        ) {
            // Header section (e.g., date, metadata)
            header?.let { headerContent ->
                Box(
                    modifier = Modifier.padding(bottom = LayoutGuide.Spacing.md)
                ) {
                    headerContent()
                }
            }
            
            // Main content area
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}

/**
 * Dialog layout with consistent padding and structure
 */
@Composable
fun DialogLayout(
    title: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable RowScope.() -> Unit
) {
    DialogSurface(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LayoutGuide.Spacing.md)
        ) {
            // Header section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LayoutGuide.Spacing.sm)
            ) {
                icon?.invoke()
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LocalCustomColors.current.bodyContentColor
                )
            }
            
            // Content section
            Column(
                verticalArrangement = Arrangement.spacedBy(LayoutGuide.Spacing.sm)
            ) {
                content()
            }
            
            // Button section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LayoutGuide.Spacing.sm, Alignment.End)
            ) {
                buttons()
            }
        }
    }
}

/**
 * Loading layout with consistent styling
 */
@Composable
fun LoadingLayout(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LayoutGuide.Spacing.md)
        ) {
            CircularProgressIndicator(
                color = LocalCustomColors.current.bodyContentColor
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalCustomColors.current.bodyContentColor
            )
        }
    }
}

/**
 * Empty state layout with consistent styling
 */
@Composable
fun EmptyStateLayout(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LayoutGuide.Spacing.md)
        ) {
            icon?.invoke()
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = LocalCustomColors.current.bodyContentColor
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalCustomColors.current.bodyContentColor.copy(alpha = 0.7f)
            )
            
            action?.invoke()
        }
    }
}