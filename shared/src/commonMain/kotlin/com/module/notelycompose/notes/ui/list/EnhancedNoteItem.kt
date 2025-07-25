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
import com.module.notelycompose.audio.ui.player.CompactAudioPlayer
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.notes.ui.components.MaterialIcon
import com.module.notelycompose.notes.ui.detail.DeleteConfirmationDialog
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens
import com.module.notelycompose.notes.ui.theme.*
import com.module.notelycompose.resources.vectors.IcRecorderSmall
import com.module.notelycompose.resources.vectors.Images
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

/**
 * Enhanced note item with integrated audio controls and clean layout.
 * 
 * Features:
 * - Integrated play/pause controls for voice notes in the card footer
 * - Expandable card content with animations
 * - Options menu (Share/Edit/Delete) with haptic feedback
 * - Material 3 design with proper theming
 * - Staggered animation support for lists
 */
@Composable
fun EnhancedNoteItem(
    note: NoteUiModel,
    onNoteClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0,
    audioPlayerViewModel: AudioPlayerViewModel = koinViewModel(),
    audioPlayerUiState: AudioPlayerUiState
) {
    var isVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleteHovered by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    
    // Staggered entrance animation
    LaunchedEffect(index) {
        delay(index * 50L) // 50ms delay between items
        isVisible = true
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Material 3 motion: Physics-based spring animations
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )
    
    val deleteScale by animateFloatAsState(
        targetValue = if (isDeleteHovered) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "delete_scale"
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
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                isExpanded = !isExpanded
            },
            shape = Material3ShapeTokens.noteCard,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp,
                hoveredElevation = 6.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            ),
            interactionSource = interactionSource
        ) {
            Box {
                // Gradient accent strip on the left
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = if (note.isVoice) {
                                    listOf(
                                        MaterialTheme.colorScheme.voiceNoteIndicatorContainer,
                                        MaterialTheme.colorScheme.voiceNoteIndicatorContainer.copy(alpha = 0.6f)
                                    )
                                } else {
                                    listOf(
                                        MaterialTheme.colorScheme.textNoteIndicatorContainer,
                                        MaterialTheme.colorScheme.textNoteIndicatorContainer.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                ) {
                    // Header with title and actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Date at the top  
                            Text(
                                text = note.createdAt,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                            
                            // Title with improved typography
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
                            
                            // Content preview with max 4 lines
                            if (note.content.isNotEmpty()) {
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        // More options menu
                        var showOptionsMenu by remember { mutableStateOf(false) }
                        Box {
                                IconButton(
                                    onClick = { 
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showOptionsMenu = !showOptionsMenu 
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    MaterialIcon(
                                        symbol = MaterialSymbols.MoreVert,
                                        contentDescription = "More options",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        size = 18.dp
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
                                            // TODO: Implement share functionality
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
                                            onNoteClick(note.id)
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
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                        thickness = 1.dp
                                    )
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
                                            MaterialIcon(
                                                symbol = MaterialSymbols.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                size = 20.dp
                                            )
                                        }
                                    )
                                }
                            }
                    }
                    
                    // Expandable audio player for voice notes
                    AnimatedVisibility(
                        visible = note.isVoice && isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        CompactAudioPlayer(
                            filePath = note.recordingPath,
                            noteId = note.id,
                            noteDurationMs = note.audioDurationMs,
                            uiState = audioPlayerUiState,
                            onLoadAudio = audioPlayerViewModel::onLoadAudio,
                            onTogglePlayPause = audioPlayerViewModel::onTogglePlayPause,
                            onTogglePlaybackSpeed = audioPlayerViewModel::onTogglePlaybackSpeed,
                            isNoteCurrentlyPlaying = audioPlayerViewModel::isNoteCurrentlyPlaying,
                            isNoteLoaded = audioPlayerViewModel::isNoteLoaded,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Footer with metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Note type indicator
                        NoteType(
                            isStarred = note.isStarred,
                            isVoice = note.isVoice
                        )
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
    }
}

