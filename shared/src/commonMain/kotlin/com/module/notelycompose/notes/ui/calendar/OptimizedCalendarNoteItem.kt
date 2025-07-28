package com.module.notelycompose.notes.ui.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.audio.presentation.AudioPlayerViewModel
import com.module.notelycompose.audio.presentation.AudioPlayerPresentationState
import com.module.notelycompose.audio.ui.formatTimeToMMSS
import com.module.notelycompose.notes.presentation.list.model.NotePresentationModel
import com.module.notelycompose.notes.ui.components.MaterialIcon
import com.module.notelycompose.notes.ui.theme.*
import com.module.notelycompose.notes.ui.calendar.parseToTimeString
import com.module.notelycompose.notes.utils.ShareUtils
import com.module.notelycompose.platform.presentation.PlatformViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Optimized calendar note item matching the dark mode example design.
 */
@Composable
fun OptimizedCalendarNoteItem(
    note: NotePresentationModel,
    onClick: () -> Unit = {},
    onEditClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    maxContentLines: Int = 4
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    
    // Platform utilities for sharing functionality
    val platformViewModel: PlatformViewModel = koinViewModel()
    
    // Audio player for voice notes
    val audioPlayerViewModel: AudioPlayerViewModel = koinViewModel()
    val audioPlayerState by audioPlayerViewModel.uiState.collectAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 300f
                )
            ),
        onClick = { 
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            isExpanded = !isExpanded
            onClick()
        },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Box {
            // Left accent strip
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(
                        if (note.isVoice) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
            ) {
                // Header row with time and options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Date/Time at the top
                    Text(
                        text = note.createdAt.parseToTimeString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    // More options menu
                    Box {
                        IconButton(
                            onClick = { 
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showOptionsMenu = !showOptionsMenu 
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(12.dp)
                            )
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    
                                    // Share text content by default, or audio if available and preferred
                                    if (note.isVoice && ShareUtils.canShareRecording(note.recordingPath)) {
                                        // For voice notes with valid recordings, share the audio
                                        platformViewModel.shareRecording(note.recordingPath!!)
                                    } else {
                                        // For text notes or voice notes without recordings, share text
                                        val shareText = ShareUtils.buildShareText(note)
                                        platformViewModel.shareText(shareText)
                                    }
                                    
                                    showOptionsMenu = false
                                },
                                leadingIcon = {
                                    MaterialIcon(
                                        symbol = MaterialSymbols.Share,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        size = 20.dp
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onEditClick(note.id)
                                    showOptionsMenu = false
                                },
                                leadingIcon = {
                                    MaterialIcon(
                                        symbol = MaterialSymbols.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        size = 20.dp
                                    )
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Title
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Content preview with responsive sizing
                if (note.content.isNotEmpty()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = if (isExpanded) Int.MAX_VALUE else maxContentLines,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Bottom row with play button for voice notes
                if (note.isVoice) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalendarAudioPlayButton(
                            note = note,
                            audioPlayerViewModel = audioPlayerViewModel,
                            audioPlayerState = audioPlayerState,
                            hapticFeedback = hapticFeedback
                        )
                        
                        Text(
                            text = note.audioDurationMs.formatTimeToMMSS(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact audio play button for calendar items with state management
 */
@Composable
private fun CalendarAudioPlayButton(
    note: NotePresentationModel,
    audioPlayerViewModel: AudioPlayerViewModel,
    audioPlayerState: AudioPlayerPresentationState,
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val isCurrentlyLoaded = audioPlayerViewModel.isNoteLoaded(note.id)
    val isCurrentlyPlaying = audioPlayerViewModel.isNoteCurrentlyPlaying(note.id)
    val hasValidAudio = note.isVoice && note.recordingPath.isNotEmpty()
    
    // Determine button state and appearance
    val (containerColor, contentColor, icon, isEnabled) = when {
        !hasValidAudio -> {
            // No audio file - disabled state
            Quadruple(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                MaterialSymbols.PlayArrow,
                false
            )
        }
        isCurrentlyPlaying -> {
            // Currently playing - show pause button
            Quadruple(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.onPrimary,
                MaterialSymbols.Pause,
                true
            )
        }
        isCurrentlyLoaded -> {
            // Loaded but paused - show play button with accent
            Quadruple(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialSymbols.PlayArrow,
                true
            )
        }
        else -> {
            // Not loaded - show play button
            Quadruple(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.onPrimary,
                MaterialSymbols.PlayArrow,
                true
            )
        }
    }
    
    Surface(
        onClick = {
            if (hasValidAudio && isEnabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                
                when {
                    !isCurrentlyLoaded -> {
                        // Load and start playing
                        audioPlayerViewModel.onLoadAudio(note.recordingPath, note.id)
                        // Auto-play after loading - we'll handle this via a LaunchedEffect
                    }
                    isCurrentlyLoaded -> {
                        // Toggle play/pause
                        audioPlayerViewModel.onTogglePlayPause(note.id)
                    }
                }
            }
        },
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier.size(32.dp),
        enabled = isEnabled
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Show loading indicator for brief moment when transitioning states
            if (audioPlayerState.currentPlayingNoteId == note.id && 
                audioPlayerState.isLoaded && 
                !audioPlayerState.isPlaying && 
                audioPlayerState.currentPosition == 0 &&
                isCurrentlyLoaded) {
                // Brief loading state
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
            } else {
                MaterialIcon(
                    symbol = icon,
                    contentDescription = when {
                        !hasValidAudio -> "No audio"
                        isCurrentlyPlaying -> "Pause"
                        else -> "Play"
                    },
                    tint = contentColor,
                    size = 16.dp
                )
            }
        }
    }
    
    // Auto-play after loading
    LaunchedEffect(audioPlayerState.isLoaded, audioPlayerState.currentPlayingNoteId) {
        if (audioPlayerState.isLoaded && 
            audioPlayerState.currentPlayingNoteId == note.id && 
            !audioPlayerState.isPlaying &&
            audioPlayerState.currentPosition == 0) {
            // Briefly delay to allow UI to update, then auto-play
            kotlinx.coroutines.delay(100)
            audioPlayerViewModel.onTogglePlayPause(note.id)
        }
    }
}

/**
 * Data class to hold four values for button state
 */
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
