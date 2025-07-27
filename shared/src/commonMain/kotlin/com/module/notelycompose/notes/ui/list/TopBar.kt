package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.top_bar_notes
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = stringResource(Res.string.top_bar_notes),
    isLeftIconVisible: Boolean = true,
    isRightIconVisible: Boolean = true,
    onMenuClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current
    
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            if (isLeftIconVisible) {
                IconButton(
                    onClick = { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMenuClicked()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Open navigation menu"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null // Set to null since parent has semantics
                    )
                }
            }
        },
        actions = {
            if (isRightIconVisible) {
                IconButton(
                    onClick = { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSettingsClicked()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Open settings"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null // Set to null since parent has semantics
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}
