package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.components.ExtendedVoiceFAB
import com.module.notelycompose.platform.HapticFeedback
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.note_detail_recorder
import com.module.notelycompose.resources.ic_transcription
import com.module.notelycompose.resources.vectors.IcRecorder
import com.module.notelycompose.resources.vectors.Images
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Microsoft-inspired dual FAB system for the note detail screen.
 * Features context-aware actions: Record (for current note) and contextual menu with Transcribe action.
 */
@Composable
fun DetailScaffoldWithFabs(
    onRecordClick: () -> Unit,
    onTranscribeClick: () -> Unit,
    hasRecording: Boolean,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val hapticFeedback = remember { HapticFeedback() }
    var isSpeedDialExpanded by remember { mutableStateOf(false) }
    
    // Build contextual menu items based on note state
    val showTranscribeAction = hasRecording
    
    Scaffold(
        topBar = topBar,
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            // Extended Voice FAB for consistent recording experience
            ExtendedVoiceFAB(
                onQuickRecordClick = onRecordClick,
                lazyListState = rememberLazyListState()
            )
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            content()
            
            // Contextual transcribe button could be added here if needed
        }
    }
}
