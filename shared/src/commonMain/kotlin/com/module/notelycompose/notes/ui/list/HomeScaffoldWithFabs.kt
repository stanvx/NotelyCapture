package com.module.notelycompose.notes.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.module.notelycompose.notes.ui.components.SpeedDialFAB
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.HapticFeedback
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.note_list_quick_record
import com.module.notelycompose.resources.note_list_add_note
import com.module.notelycompose.resources.vectors.IcRecorder
import com.module.notelycompose.resources.vectors.Images
import org.jetbrains.compose.resources.stringResource

/**
 * Microsoft-inspired dual FAB system for the home screen.
 * Features an ExtendedFloatingActionButton for primary "Record" action that shrinks on scroll,
 * and a secondary speed dial menu for other creation actions.
 */
@Composable
fun HomeScaffoldWithFabs(
    onRecordClick: () -> Unit,
    onNewNoteClick: () -> Unit,
    onNewCanvasClick: () -> Unit = {},
    lazyListState: LazyListState = rememberLazyListState(),
    topBar: @Composable () -> Unit = {},
    content: @Composable (LazyListState) -> Unit
) {
    val hapticFeedback = remember { HapticFeedback() }
    var isSpeedDialExpanded by remember { mutableStateOf(false) }
    
    
    Scaffold(
        topBar = topBar,
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            // Speed Dial FAB for multiple creation actions
            SpeedDialFAB(
                onNewNoteClick = onNewNoteClick,
                onQuickRecordClick = onRecordClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            content(lazyListState)
            
            // Full screen dim overlay when speed dial is expanded
            if (isSpeedDialExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { isSpeedDialExpanded = false }
                )
            }
        }
    }
}
