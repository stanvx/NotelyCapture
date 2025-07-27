package com.module.notelycompose.notes.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.module.notelycompose.core.Routes
import com.module.notelycompose.notes.ui.theme.MaterialSymbols

/**
 * Material 3 NavigationBar component for Notely Capture.
 * 
 * Features 3 destinations:
 * - Home: Main note list
 * - Calendar: Calendar view with capture filtering
 * - Capture: Capture hub dashboard
 */
@Composable
fun AppNavigationBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Home Navigation Item
        NavigationBarItem(
            selected = currentRoute == Routes.List::class.qualifiedName,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onNavigateToHome()
            },
            icon = {
                MaterialIcon(
                    symbol = MaterialSymbols.Home,
                    contentDescription = "Home",
                    style = MaterialIconStyle.Filled
                )
            },
            label = {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        )
        
        // Calendar Navigation Item
        NavigationBarItem(
            selected = currentRoute == Routes.Calendar::class.qualifiedName,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onNavigateToCalendar()
            },
            icon = {
                MaterialIcon(
                    symbol = MaterialSymbols.DateRange,
                    contentDescription = "Calendar",
                    style = MaterialIconStyle.Filled
                )
            },
            label = {
                Text(
                    text = "Calendar",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        )
        
        // Capture Navigation Item
        NavigationBarItem(
            selected = currentRoute == Routes.Capture::class.qualifiedName,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onNavigateToCapture()
            },
            icon = {
                MaterialIcon(
                    symbol = MaterialSymbols.Dashboard,
                    contentDescription = "Capture Hub",
                    style = MaterialIconStyle.Filled
                )
            },
            label = {
                Text(
                    text = "Capture",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        )
    }
}