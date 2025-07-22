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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.detail.DeleteConfirmationDialog
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens
import com.module.notelycompose.notes.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Modern note item with expressive Material 3 design.
 * 
 * Features:
 * - Gradient accent strips for visual hierarchy
 * - Enhanced typography and spacing
 * - Staggered entrance animations
 * - Voice/text indicators with semantic colors
 * - Improved interaction feedback
 * - Accessibility support
 */
@Composable
fun ModernNoteItem(
    note: NoteUiModel,
    onNoteClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleteHovered by remember { mutableStateOf(false) }
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
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .scale(scale),
            onClick = { 
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onNoteClick(note.id) 
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
                    // Header Row with enhanced layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Enhanced title typography
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 28.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Time and metadata row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = note.createdAt,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Enhanced delete button
                        IconButton(
                            onClick = { 
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDeleteDialog = true 
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .scale(deleteScale)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isDeleteHovered = true
                                            tryAwaitRelease()
                                            isDeleteHovered = false
                                        }
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Delete note",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    // Content preview with better spacing
                    if (note.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Enhanced bottom row with chips and indicators
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Enhanced note type indicator
                            NoteTypeChip(
                                isVoice = note.isVoice,
                                isStarred = note.isStarred
                            )
                            
                            // Word count chip with better styling
                            if (note.words > 0) {
                                WordCountChip(wordCount = note.words)
                            }
                        }
                        
                        // Enhanced action button
                        EnhancedActionButton(
                            onClick = { 
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNoteClick(note.id) 
                            }
                        )
                    }
                }
            }
        }
    }
    
    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = { 
            onDeleteClick(note.id)
            showDeleteDialog = false
        }
    )
}

/**
 * Enhanced note type chip with better visual design.
 */
@Composable
private fun NoteTypeChip(
    isVoice: Boolean,
    isStarred: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isVoice) {
            MaterialTheme.colorScheme.voiceNoteIndicatorContainer
        } else {
            MaterialTheme.colorScheme.textNoteIndicatorContainer
        },
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isVoice) Icons.Rounded.Phone else Icons.Rounded.Star,
                contentDescription = null,
                tint = if (isVoice) {
                    MaterialTheme.colorScheme.onVoiceNoteIndicatorContainer
                } else {
                    MaterialTheme.colorScheme.onTextNoteIndicatorContainer
                },
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isVoice) "Voice" else "Text",
                style = MaterialTheme.typography.labelMedium,
                color = if (isVoice) {
                    MaterialTheme.colorScheme.onVoiceNoteIndicatorContainer
                } else {
                    MaterialTheme.colorScheme.onTextNoteIndicatorContainer
                },
                fontWeight = FontWeight.Medium
            )
            
            // Star indicator if starred
            if (isStarred) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = "Starred",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Enhanced word count chip.
 */
@Composable
private fun WordCountChip(wordCount: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Text(
            text = "$wordCount words",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Enhanced action button with better visual feedback.
 */
@Composable
private fun EnhancedActionButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "action_button_scale"
    )
    
    Surface(
        onClick = onClick,
        shape = Material3ShapeTokens.fabButton,
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 2.dp,
        modifier = Modifier
            .scale(scale)
            .size(40.dp),
        interactionSource = interactionSource
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = "Open note",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}