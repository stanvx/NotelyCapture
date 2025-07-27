package com.module.notelycompose.notes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.detail.DeleteConfirmationDialog
import com.module.notelycompose.notes.ui.theme.MaterialSymbols

/**
 * Reusable dropdown menu component for note actions across different note card implementations.
 * 
 * Features:
 * - Consistent Material 3 design patterns
 * - Material Symbols iconography
 * - Haptic feedback for all interactions
 * - Integrated delete confirmation dialog
 * - Flexible trigger component support
 * - Internal state management
 * - Error styling for destructive actions
 * 
 * @param noteId The ID of the note for which actions are being performed
 * @param expanded Whether the dropdown menu is currently visible
 * @param onDismissRequest Callback when the dropdown should be dismissed
 * @param onShareClick Callback for share action
 * @param onEditClick Callback for edit action
 * @param onDeleteClick Callback for delete action (called after confirmation)
 * @param modifier Modifier to be applied to the dropdown menu
 */
@Composable
fun NoteActionsDropdown(
    noteId: Long,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onShareClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.background(
            MaterialTheme.colorScheme.surface,
            RoundedCornerShape(12.dp)
        )
    ) {
        // Share action
        DropdownMenuItem(
            text = { 
                Text(
                    text = "Share",
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onShareClick(noteId)
                onDismissRequest()
            },
            leadingIcon = {
                MaterialIcon(
                    symbol = MaterialSymbols.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 20.dp,
                    style = MaterialIconStyle.Outlined
                )
            }
        )
        
        // Edit action
        DropdownMenuItem(
            text = { 
                Text(
                    text = "Edit",
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onEditClick(noteId)
                onDismissRequest()
            },
            leadingIcon = {
                MaterialIcon(
                    symbol = MaterialSymbols.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 20.dp,
                    style = MaterialIconStyle.Outlined
                )
            }
        )
        
        // Divider before destructive action
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            thickness = 1.dp
        )
        
        // Delete action (destructive styling)
        DropdownMenuItem(
            text = { 
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                ) 
            },
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                showDeleteDialog = true
                onDismissRequest()
            },
            leadingIcon = {
                MaterialIcon(
                    symbol = MaterialSymbols.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    size = 20.dp,
                    style = MaterialIconStyle.Outlined
                )
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            showDialog = showDeleteDialog,
            onConfirm = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onDeleteClick(noteId)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

/**
 * Reusable note actions dropdown with integrated trigger management.
 * 
 * This variant manages the dropdown visibility state internally and provides
 * a complete solution for note actions with customizable trigger content.
 * 
 * @param noteId The ID of the note for which actions are being performed
 * @param onShareClick Callback for share action
 * @param onEditClick Callback for edit action
 * @param onDeleteClick Callback for delete action (called after confirmation)
 * @param modifier Modifier to be applied to the container
 * @param trigger Composable content for the trigger (e.g., IconButton, custom button)
 */
@Composable
fun NoteActionsDropdownWithTrigger(
    noteId: Long,
    onShareClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    trigger: @Composable (onClick: () -> Unit) -> Unit
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        // Trigger content
        trigger { showOptionsMenu = !showOptionsMenu }
        
        // Dropdown menu
        NoteActionsDropdown(
            noteId = noteId,
            expanded = showOptionsMenu,
            onDismissRequest = { showOptionsMenu = false },
            onShareClick = onShareClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}

/**
 * Convenience composable for the most common use case: IconButton trigger with MoreVert icon.
 * 
 * @param noteId The ID of the note for which actions are being performed
 * @param onShareClick Callback for share action
 * @param onEditClick Callback for edit action
 * @param onDeleteClick Callback for delete action (called after confirmation)
 * @param modifier Modifier to be applied to the container
 * @param iconTint Color for the more options icon
 */
@Composable
fun NoteActionsIconButton(
    noteId: Long,
    onShareClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    NoteActionsDropdownWithTrigger(
        noteId = noteId,
        onShareClick = onShareClick,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick,
        modifier = modifier,
        trigger = { onClick ->
            IconButton(
                onClick = { 
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                modifier = Modifier.size(32.dp)
            ) {
                MaterialIcon(
                    symbol = MaterialSymbols.MoreVert,
                    contentDescription = "More options",
                    tint = iconTint,
                    size = 20.dp,
                    style = MaterialIconStyle.Outlined
                )
            }
        }
    )
}