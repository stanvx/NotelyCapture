package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
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
    isRecordingExist: Boolean
) {
    var showExistingRecordConfirmDialog by remember { mutableStateOf(false) }
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
            }
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
            }
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
}

@Composable
fun DetailAndroidNoteTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onShare: () -> Unit,
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit,
    elevation: Dp = AppBarDefaults.TopAppBarElevation
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
            IconButton(onClick = { onShare() }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share note"
                )
            }
            // Hide dropdown menu
            // TODO: Implement Export Audio to Folder / Import Audio
            DetailDropDownMenu(
                onExportAudio = onExportAudio,
                onImportClick = onImportClick
            )
        },
        backgroundColor = LocalCustomColors.current.bodyBackgroundColor,
        contentColor = LocalCustomColors.current.bodyContentColor,
        elevation = elevation
    )
}

@Composable
fun DetailIOSNoteTopBar(
    onNavigateBack: () -> Unit,
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit,
    onShare: () -> Unit
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
                    style = MaterialTheme.typography.body1,
                )
            }
        },
        actions = {
            IconButton(onClick = { onShare() }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share note",
                    modifier = Modifier.size(24.dp)
                )
            }
            DetailDropDownMenu(
                onExportAudio = onExportAudio,
                onImportClick = onImportClick
            )
        },
        contentColor = LocalCustomColors.current.iOSBackButtonColor,
        backgroundColor = LocalCustomColors.current.bodyBackgroundColor,
        modifier = Modifier.padding(start = 0.dp),
        elevation = 0.dp
    )
}

@Composable
fun DetailDropDownMenu(
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit = {}
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
                onClick = {
                    dropdownExpanded = false
                    onExportAudio()
                }
            ) {
                Text(stringResource(Res.string.top_bar_export_audio_folder))
            }

            DropdownMenuItem(
                onClick = {
                    dropdownExpanded = false
                    onImportClick()
                }
            ) {
                Text(stringResource(Res.string.top_bar_import_audio))
            }
        }
    }
}
