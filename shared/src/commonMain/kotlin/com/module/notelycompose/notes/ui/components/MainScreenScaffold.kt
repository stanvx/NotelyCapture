package com.module.notelycompose.notes.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.module.notelycompose.core.Routes
import com.module.notelycompose.core.navigateSingleTop
import com.module.notelycompose.notes.ui.theme.MaterialSymbols
import com.module.notelycompose.notes.ui.components.MaterialIcon
import com.module.notelycompose.notes.ui.components.ExtendedVoiceFAB
import com.module.notelycompose.platform.HapticFeedback

/**
 * Main screen scaffold that includes the NavigationBar for primary app screens.
 * 
 * Manages navigation state and provides consistent NavigationBar across
 * Home, Calendar, and Capture screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenScaffold(
    navController: NavHostController,
    onSearchActivated: () -> Unit = {},
    onQuickRecordClick: () -> Unit = {},
    onGoToToday: (() -> Unit)? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    content: @Composable (onScrollStateChanged: (LazyStaggeredGridState) -> Unit) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // State to hold the scroll state from NoteListScreen
    var lazyStaggeredGridState by remember { mutableStateOf<LazyStaggeredGridState?>(null) }
    
    // Create a default LazyListState for Calendar and Capture screens
    val defaultLazyListState = rememberLazyListState()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            when (currentRoute) {
                Routes.List::class.qualifiedName -> {
                    androidx.compose.material3.CenterAlignedTopAppBar(
                        title = { androidx.compose.material3.Text("Notely", style = MaterialTheme.typography.headlineMedium) },
                        actions = {
                            androidx.compose.material3.IconButton(onClick = onNavigateToSettings) {
                                MaterialIcon(
                                    symbol = MaterialSymbols.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                            androidx.compose.material3.IconButton(onClick = onNavigateToMenu) {
                                MaterialIcon(
                                    symbol = MaterialSymbols.Info,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    )
                }
                // Calendar and Capture screens have their own topBars, so don't add one here
            }
        },
        bottomBar = {
            if (shouldShowNavigationBar(currentRoute)) {
                AppNavigationBar(
                    currentRoute = currentRoute ?: "",
                    onNavigateToHome = {
                        if (currentRoute != Routes.List::class.qualifiedName) {
                            navController.navigateSingleTop(Routes.List)
                        }
                    },
                    onNavigateToCalendar = {
                        if (currentRoute != Routes.Calendar::class.qualifiedName) {
                            navController.navigateSingleTop(Routes.Calendar)
                        }
                    },
                    onNavigateToCapture = {
                        if (currentRoute != Routes.Capture::class.qualifiedName) {
                            navController.navigateSingleTop(Routes.Capture)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            when (currentRoute) {
                Routes.List::class.qualifiedName -> {
                    lazyStaggeredGridState?.let { scrollState ->
                        ExtendedVoiceFAB(
                            onQuickRecordClick = onQuickRecordClick,
                            lazyStaggeredGridState = scrollState
                        )
                    }
                }
                Routes.Calendar::class.qualifiedName, Routes.Capture::class.qualifiedName -> {
                    ExtendedVoiceFAB(
                        onQuickRecordClick = onQuickRecordClick,
                        lazyListState = defaultLazyListState
                    )
                }
            }
        }
    ) { paddingValues ->
        content { scrollState ->
            lazyStaggeredGridState = scrollState
        }
    }
}

/**
 * Determines whether to show the NavigationBar based on the current route.
 */
private fun shouldShowNavigationBar(currentRoute: String?): Boolean {
    return when (currentRoute) {
        Routes.List::class.qualifiedName,
        Routes.Calendar::class.qualifiedName,
        Routes.Capture::class.qualifiedName -> true
        else -> false
    }
}


/**
 * Simple FAB component for quick voice recording.
 */
@Composable
private fun SimpleVoiceFAB(
    onQuickRecordClick: () -> Unit
) {
    val hapticFeedback = remember { HapticFeedback() }
    
    FloatingActionButton(
        onClick = {
            hapticFeedback.medium()
            onQuickRecordClick()
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        MaterialIcon(
            symbol = MaterialSymbols.Mic,
            contentDescription = "Quick record",
            tint = MaterialTheme.colorScheme.onPrimary,
            size = 24.dp
        )
    }
}