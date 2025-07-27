package com.module.notelycompose.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.style.LayoutGuide
import com.module.notelycompose.platform.getPlatform
import com.module.notelycompose.resources.vectors.IcChevronLeft
import com.module.notelycompose.resources.vectors.Images

/**
 * Unified TopBar component following Material 3 Expressive design principles
 * 
 * Provides consistent navigation, actions, and styling across all screens.
 * Supports both Android and iOS platform-specific behaviors.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = LocalCustomColors.current.bodyBackgroundColor,
        titleContentColor = LocalCustomColors.current.bodyContentColor,
        navigationIconContentColor = LocalCustomColors.current.bodyContentColor,
        actionIconContentColor = LocalCustomColors.current.bodyContentColor
    ),
    elevation: androidx.compose.ui.unit.Dp = LayoutGuide.Elevation.level3
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.titleContentColor
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}

/**
 * Simplified TopBar for detail screens with back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    showBackButtonText: Boolean = !getPlatform().isAndroid
) {
    UnifiedTopBar(
        title = title,
        modifier = modifier,
        navigationIcon = {
            if (showBackButtonText) {
                // iOS-style back button with text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onNavigateBack() }
                        .padding(LayoutGuide.Spacing.sm)
                ) {
                    Icon(
                        imageVector = Images.Icons.IcChevronLeft,
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp),
                        tint = LocalCustomColors.current.iOSBackButtonColor
                    )
                    Spacer(modifier = Modifier.width(LayoutGuide.Spacing.xs))
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalCustomColors.current.iOSBackButtonColor
                    )
                }
            } else {
                // Android-style back button
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = actions
    )
}

/**
 * TopBar for list screens with menu and settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    UnifiedTopBar(
        title = title,
        modifier = modifier,
        navigationIcon = onMenuClick?.let { menuClick ->
            {
                IconButton(onClick = menuClick) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu"
                    )
                }
            }
        },
        actions = {
            onSettingsClick?.let { settingsClick ->
                IconButton(onClick = settingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

/**
 * Action button for TopBar with consistent styling
 */
@Composable
fun TopBarAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalCustomColors.current.bodyContentColor
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

/**
 * Dropdown menu action for TopBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarDropdownAction(
    items: List<DropdownMenuItem>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options"
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.text) },
                    onClick = {
                        expanded = false
                        item.onClick()
                    }
                )
            }
        }
    }
}

/**
 * Data class for dropdown menu items
 */
data class DropdownMenuItem(
    val text: String,
    val onClick: () -> Unit
)