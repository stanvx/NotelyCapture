package com.module.notelycompose.notes.ui.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.detail.DeleteConfirmationDialog
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens
import com.module.notelycompose.resources.vectors.IcArrowUpRight
import com.module.notelycompose.resources.vectors.Images
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.note_item_delete
import com.module.notelycompose.resources.note_item_edit
import com.module.notelycompose.resources.words
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val ZERO_WORDS = 0

@Composable
fun NoteItem(
    note: NoteUiModel,
    onNoteClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current
    
    // Material 3 Motion: Physics-based spring animations
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "card_scale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 8.dp.value else 3.dp.value,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "card_elevation"
    )
    
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) 
            MaterialTheme.colorScheme.surfaceContainerHigh
        else 
            MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(durationMillis = 150),
        label = "card_container_color"
    )
    
    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = { onDeleteClick(note.id) }
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onNoteClick(note.id)
            },
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 4.dp
        ),
        shape = Material3ShapeTokens.noteCard,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with date and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.createdAt,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteDialog = true
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = stringResource(Res.string.note_item_delete),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Content section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Footer with tags and action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NoteType(
                        isStarred = note.isStarred,
                        isVoice = note.isVoice
                    )
                    if (note.words > ZERO_WORDS) {
                        WordCountChip(wordCount = note.words)
                    }
                }

                ActionButton(
                    onClick = { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNoteClick(note.id) 
                    }
                )
            }
        }
    }
}

@Composable
private fun WordCountChip(wordCount: Int) {
    ElevatedCard(
        shape = Material3ShapeTokens.chip,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = pluralStringResource(Res.plurals.words, wordCount, wordCount),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ActionButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 500f
        ),
        label = "action_button_scale"
    )
    
    ElevatedCard(
        shape = Material3ShapeTokens.fabButton,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier.scale(scale)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            interactionSource = interactionSource
        ) {
            Icon(
                imageVector = Images.Icons.IcArrowUpRight,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                contentDescription = stringResource(Res.string.note_item_edit),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}