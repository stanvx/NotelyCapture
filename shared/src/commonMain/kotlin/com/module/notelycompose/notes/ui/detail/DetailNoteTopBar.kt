@file:OptIn(ExperimentalMaterial3Api::class)

package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import com.module.notelycompose.notes.ui.components.MaterialIcon
import com.module.notelycompose.notes.ui.components.MaterialIconStyle
import com.module.notelycompose.notes.ui.theme.MaterialSymbols
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.getPlatform
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.top_bar_back
import com.module.notelycompose.resources.top_bar_export_audio_folder
import com.module.notelycompose.resources.top_bar_import_audio
import com.module.notelycompose.resources.top_bar_my_note
import com.module.notelycompose.resources.vectors.IcChevronLeft
import com.module.notelycompose.resources.vectors.Images
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetailNoteTopBar(
    title: String = stringResource(Res.string.top_bar_my_note),
    onNavigateBack: () -> Unit,
    onShare: () -> Unit = {},
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit = {},
    onRecordClick: () -> Unit,
    onTranscribeClick: () -> Unit,
    onStarClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isRecordingExist: Boolean,
    isStarred: Boolean = false
) {
    var showExistingRecordConfirmDialog by remember { mutableStateOf(false) }
    var showExistingRecordForRecordConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    if (getPlatform().isAndroid) {
        DetailAndroidNoteTopBar(
            title = title,
            onNavigateBack = onNavigateBack,
            onShare = onShare,
            onExportAudio = onExportAudio,
            onImportClick = {
                if (!isRecordingExist) {
                    onImportClick()
                } else {
                    showExistingRecordConfirmDialog = true
                }
            },
            onRecordClick = {
                if (!isRecordingExist) {
                    onRecordClick()
                } else {
                    showExistingRecordForRecordConfirmDialog = true
                }
            },
            onTranscribeClick = onTranscribeClick,
            onStarClick = onStarClick,
            onDeleteClick = { showDeleteConfirmDialog = true },
            isStarred = isStarred
        )
    } else {
        DetailIOSNoteTopBar(
            onNavigateBack = onNavigateBack,
            onShare = onShare,
            onExportAudio = onExportAudio,
            onImportClick = {
                if (!isRecordingExist) {
                    onImportClick()
                } else {
                    showExistingRecordConfirmDialog = true
                }
            },
            onRecordClick = {
                if (!isRecordingExist) {
                    onRecordClick()
                } else {
                    showExistingRecordForRecordConfirmDialog = true
                }
            },
            onTranscribeClick = onTranscribeClick,
            onStarClick = onStarClick,
            onDeleteClick = { showDeleteConfirmDialog = true },
            isStarred = isStarred
        )
    }

    ReplaceRecordingConfirmationDialog(
        showDialog = showExistingRecordConfirmDialog,
        onDismiss = {
            showExistingRecordConfirmDialog = false
        },
        onConfirm = {
            onImportClick()
        },
        option = RecordingConfirmationUiModel.Import
    )

    ReplaceRecordingConfirmationDialog(
        showDialog = showExistingRecordForRecordConfirmDialog,
        onDismiss = {
            showExistingRecordForRecordConfirmDialog = false
        },
        onConfirm = {
            onRecordClick()
        },
        option = RecordingConfirmationUiModel.Record
    )

    DeleteNoteConfirmationDialog(
        showDialog = showDeleteConfirmDialog,
        onDismiss = { showDeleteConfirmDialog = false },
        onConfirm = onDeleteClick
    )
}

/**
 * Confirmation dialog for deleting a note
 */
@Composable
fun DeleteNoteConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete Note") },
            text = { Text("This note will be permanently deleted. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailAndroidNoteTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onShare: () -> Unit,
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit,
    onRecordClick: () -> Unit,
    onTranscribeClick: () -> Unit,
    onStarClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isStarred: Boolean
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { onNavigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.top_bar_back)
                )
            }
        },
        actions = {
            IconButton(onClick = { onStarClick() }) {
                Icon(
                    imageVector = if (isStarred) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (isStarred) "Unstar note" else "Star note",
                    tint = if (isStarred) MaterialTheme.colorScheme.primary else LocalCustomColors.current.bodyContentColor
                )
            }
            IconButton(onClick = { onShare() }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share note"
                )
            }
            // Enhanced dropdown menu with all functionality
            DetailDropDownMenu(
                onExportAudio = onExportAudio,
                onImportClick = onImportClick,
                onRecordClick = onRecordClick,
                onTranscribeClick = onTranscribeClick,
                onDeleteClick = onDeleteClick
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = LocalCustomColors.current.bodyBackgroundColor,
            titleContentColor = LocalCustomColors.current.bodyContentColor,
            navigationIconContentColor = LocalCustomColors.current.bodyContentColor,
            actionIconContentColor = LocalCustomColors.current.bodyContentColor
        )
    )
}

@Composable
fun DetailIOSNoteTopBar(
    onNavigateBack: () -> Unit,
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit,
    onShare: () -> Unit,
    onRecordClick: () -> Unit,
    onTranscribeClick: () -> Unit,
    onStarClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isStarred: Boolean
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onNavigateBack()
                }
            ) {
                Icon(
                    imageVector = Images.Icons.IcChevronLeft,
                    contentDescription = stringResource(Res.string.top_bar_back),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.top_bar_back),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        actions = {
            IconButton(onClick = { onStarClick() }) {
                Icon(
                    imageVector = if (isStarred) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (isStarred) "Unstar note" else "Star note",
                    modifier = Modifier.size(24.dp),
                    tint = if (isStarred) MaterialTheme.colorScheme.primary else LocalCustomColors.current.iOSBackButtonColor
                )
            }
            IconButton(onClick = { onShare() }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share note",
                    modifier = Modifier.size(24.dp)
                )
            }
            DetailDropDownMenu(
                onExportAudio = onExportAudio,
                onImportClick = onImportClick,
                onRecordClick = onRecordClick,
                onTranscribeClick = onTranscribeClick,
                onDeleteClick = onDeleteClick
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = LocalCustomColors.current.bodyBackgroundColor,
            titleContentColor = LocalCustomColors.current.iOSBackButtonColor,
            navigationIconContentColor = LocalCustomColors.current.iOSBackButtonColor,
            actionIconContentColor = LocalCustomColors.current.iOSBackButtonColor
        ),
        modifier = Modifier.padding(start = 0.dp)
    )
}

@Composable
fun DetailDropDownMenu(
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit = {},
    onRecordClick: () -> Unit,
    onTranscribeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { dropdownExpanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options"
            )
        }

        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Record Audio") },
                onClick = {
                    dropdownExpanded = false
                    onRecordClick()
                },
                leadingIcon = {
                    MaterialIcon(
                        symbol = MaterialSymbols.Filled.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        size = 16.dp,
                        style = MaterialIconStyle.Filled
                    )
                }
            )

            DropdownMenuItem(
                text = { Text("Transcribe") },
                onClick = {
                    dropdownExpanded = false
                    onTranscribeClick()
                },
                leadingIcon = {
                    MaterialIcon(
                        symbol = MaterialSymbols.Filled.Translate,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        size = 16.dp,
                        style = MaterialIconStyle.Filled
                    )
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.top_bar_import_audio)) },
                onClick = {
                    dropdownExpanded = false
                    onImportClick()
                },
                leadingIcon = {
                    MaterialIcon(
                        symbol = MaterialSymbols.Filled.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        size = 16.dp,
                        style = MaterialIconStyle.Filled
                    )
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.top_bar_export_audio_folder)) },
                onClick = {
                    dropdownExpanded = false
                    onExportAudio()
                },
                leadingIcon = {
                    MaterialIcon(
                        symbol = MaterialSymbols.Filled.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        size = 16.dp,
                        style = MaterialIconStyle.Filled
                    )
                }
            )

            Divider()

            DropdownMenuItem(
                text = { Text("Delete Note", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    dropdownExpanded = false
                    onDeleteClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}
