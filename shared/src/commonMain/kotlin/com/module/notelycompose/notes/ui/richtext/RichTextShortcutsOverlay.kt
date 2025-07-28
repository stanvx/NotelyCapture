package com.module.notelycompose.notes.ui.richtext

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Keyboard shortcuts overlay for rich text editing.
 * Shows available keyboard shortcuts when requested by the user.
 */
@Composable
fun RichTextShortcutsOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Keyboard Shortcuts",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close shortcuts"
                            )
                        }
                    }
                    
                    // Shortcuts list
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ShortcutItem("Ctrl+B", "Bold")
                        ShortcutItem("Ctrl+I", "Italic")
                        ShortcutItem("Ctrl+U", "Underline")
                        ShortcutItem("Ctrl+Z", "Undo")
                        ShortcutItem("Ctrl+Y", "Redo")
                        ShortcutItem("Ctrl+L", "Align Left")
                        ShortcutItem("Ctrl+E", "Align Center")
                        ShortcutItem("Ctrl+R", "Align Right")
                        ShortcutItem("Ctrl+1", "Heading 1")
                        ShortcutItem("Ctrl+2", "Heading 2")
                        ShortcutItem("Ctrl+3", "Heading 3")
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutItem(
    shortcut: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Text(
                text = shortcut,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}