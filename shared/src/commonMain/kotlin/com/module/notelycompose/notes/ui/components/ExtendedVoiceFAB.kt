package com.module.notelycompose.notes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.module.notelycompose.core.constants.AppConstants
import com.module.notelycompose.platform.HapticFeedback
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.vectors.IcRecorder
import com.module.notelycompose.resources.vectors.Images
import androidx.compose.ui.text.font.FontWeight

// ScrollState interface for unified scroll handling
interface ScrollState {
    val firstVisibleItemIndex: Int
    val firstVisibleItemScrollOffset: Int
}

// Adapter for LazyListState
class LazyListScrollState(private val state: LazyListState) : ScrollState {
    override val firstVisibleItemIndex: Int get() = state.firstVisibleItemIndex
    override val firstVisibleItemScrollOffset: Int get() = state.firstVisibleItemScrollOffset
}

// Adapter for LazyStaggeredGridState  
class LazyStaggeredGridScrollState(private val state: LazyStaggeredGridState) : ScrollState {
    override val firstVisibleItemIndex: Int get() = state.firstVisibleItemIndex
    override val firstVisibleItemScrollOffset: Int get() = state.firstVisibleItemScrollOffset
}

@Composable
fun ExtendedVoiceFAB(
    onQuickRecordClick: () -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = remember { HapticFeedback() }
    
    // Determine if FAB should be expanded based on scroll state
    val isExpanded by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex == 0 && 
            scrollState.firstVisibleItemScrollOffset < 400
        }
    }

    ExtendedFloatingActionButton(
        onClick = {
            hapticFeedback.medium()
            onQuickRecordClick()
        },
        expanded = isExpanded,
        icon = {
            Icon(
                imageVector = Images.Icons.IcRecorder,
                contentDescription = null, // Handled by parent semantics
                modifier = Modifier.size(28.dp) // Larger icon
            )
        },
        text = {
            Text(
                text = "Record",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 16.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 12.dp
        ),
        modifier = modifier
            .semantics {
                contentDescription = "Quick record button. Tap to start voice recording."
            }
    )
}

@Composable
fun ExtendedVoiceFAB(
    onQuickRecordClick: () -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    ExtendedVoiceFAB(
        onQuickRecordClick = onQuickRecordClick,
        scrollState = LazyListScrollState(lazyListState),
        modifier = modifier
    )
}

@Composable
fun ExtendedVoiceFAB(
    onQuickRecordClick: () -> Unit,
    lazyStaggeredGridState: LazyStaggeredGridState,
    modifier: Modifier = Modifier
) {
    ExtendedVoiceFAB(
        onQuickRecordClick = onQuickRecordClick,
        scrollState = LazyStaggeredGridScrollState(lazyStaggeredGridState),
        modifier = modifier
    )
}