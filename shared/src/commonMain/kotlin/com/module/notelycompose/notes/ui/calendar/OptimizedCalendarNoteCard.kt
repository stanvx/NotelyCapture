package com.module.notelycompose.notes.ui.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.presentation.list.model.NotePresentationModel
import com.module.notelycompose.notes.ui.detail.DeleteConfirmationDialog
import com.module.notelycompose.notes.ui.calendar.parseToTimeString

/**
 * Optimized calendar note card matching the dark mode example design.
 * 
 * Features:
 * - Date at the top
 * - Title below date
 * - Content preview with 4-line max
 * - Options menu (Share/Edit/...)
 * - Click to expand instead of navigate
 */
@Composable
fun OptimizedCalendarNoteCard(
    note: NotePresentationModel,
    onNoteClick: (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    onShareClick: (Long) -> Unit = {},
    onEditClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    maxContentLines: Int = 4
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    
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
                // Date at the top
                Text(
                    text = note.createdAt.parseToTimeString(),
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
                
                // Bottom row with options menu only
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            HorizontalDivider()
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
