package com.module.notelycompose.notes.ui.list

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.audio.presentation.AudioPlayerViewModel
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.notes.ui.components.MaterialIcon
import com.module.notelycompose.notes.ui.detail.DeleteConfirmationDialog
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens
import com.module.notelycompose.notes.ui.theme.*
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

/**
 * Optimized note card matching the dark mode example design.
 * 
 * Features:
 * - Date at the top
 * - Title below date
 * - Content preview with 4-line max
 * - Play button with duration for voice notes
 * - Options menu (Share/Edit/...)
 * - Click to expand instead of navigate
 * - Responsive sizing closer to screen edges
 */
@Composable
fun OptimizedNoteCard(
    note: NoteUiModel,
    onNoteClick: (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    onShareClick: (Long) -> Unit = {},
    onEditClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    index: Int = 0,
    audioPlayerViewModel: AudioPlayerViewModel = koinViewModel(),
    audioPlayerUiState: AudioPlayerUiState,
    maxContentLines: Int = 4
) {
    var isVisible by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    
    // Staggered entrance animation
    LaunchedEffect(index) {
        delay(index * 50L)
        isVisible = true
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut() + scaleOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 4.dp) // Minimal padding for closer to edges
                .scale(scale)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = 300f
                    )
                ),
            onClick = { 
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isExpanded = !isExpanded
            },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 6.dp,
                hoveredElevation = 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            ),
            interactionSource = interactionSource
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
                    // Date at the top
                    Text(
                        text = note.createdAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    
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
                    
                    // Bottom row with play button and options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play button with duration for voice notes
                        if (note.isVoice) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (!audioPlayerUiState.isLoaded && !note.recordingPath.isNullOrEmpty()) {
                                            audioPlayerViewModel.onLoadAudio(note.recordingPath)
                                        }
                                        audioPlayerViewModel.onTogglePlayPause()
                                    },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        MaterialIcon(
                                            symbol = if (audioPlayerUiState.isPlaying) MaterialSymbols.Pause else MaterialSymbols.PlayArrow,
                                            contentDescription = if (audioPlayerUiState.isPlaying) "Pause" else "Play",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            size = 16.dp
                                        )
                                    }
                                }
                                
                                // Duration text
                                Text(
                                    text = formatDuration(audioPlayerUiState.duration),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            // Empty space for text notes
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        
                        // Options menu
                        Box {
                            IconButton(
                                onClick = { 
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showOptionsMenu = !showOptionsMenu 
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
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
                                        onShareClick(note.id)
                                        showOptionsMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
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
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            "Delete",
                                            color = MaterialTheme.colorScheme.error
                                        ) 
                                    },
                                    onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showDeleteDialog = true
                                        showOptionsMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Delete confirmation dialog
        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                showDialog = showDeleteDialog,
                onConfirm = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDeleteClick(note.id)
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

/**
 * Format duration in milliseconds to MM:SS format
 */
private fun formatDuration(durationMillis: Int): String {
    val seconds = (durationMillis / 1000) % 60
    val minutes = (durationMillis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
