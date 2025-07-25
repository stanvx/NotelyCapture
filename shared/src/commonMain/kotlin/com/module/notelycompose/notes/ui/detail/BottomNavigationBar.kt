package com.module.notelycompose.notes.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.notes.presentation.detail.TextEditorViewModel
import com.module.notelycompose.notes.presentation.detail.RichTextFormattingState
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.ui.extensions.showKeyboard
import com.module.notelycompose.resources.vectors.IcDetailList
import com.module.notelycompose.resources.vectors.IcKeyboardHide
import com.module.notelycompose.resources.vectors.IcLetterAa
import com.module.notelycompose.resources.vectors.IcStar
import com.module.notelycompose.resources.vectors.IcStarFilled
import com.module.notelycompose.resources.vectors.Images
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.bottom_navigation_letter
import com.module.notelycompose.resources.bottom_navigation_bullet_list
import com.module.notelycompose.resources.bottom_navigation_delete
import com.module.notelycompose.resources.bottom_navigation_hide_keyboard
import com.module.notelycompose.resources.bottom_navigation_starred
import org.jetbrains.compose.resources.stringResource

private const val ZERO_DENSITY = 0

@Composable
fun BottomNavigationBar(
    isTextFieldFocused: Boolean,
    selectionSize: TextFormatUiOption,
    isStarred: Boolean,
    showFormatBar: Boolean,
    textFieldFocusRequester: FocusRequester,
    onShowTextFormatBar: (show: Boolean) -> Unit,
    editorViewModel: TextEditorViewModel,
    navigateBack: () -> Unit
) {

    var selectedFormat by remember { mutableStateOf(FormatOptionTextFormat.Body) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    val isKeyboardOpen by keyboardAsState() // true or false

    when(selectionSize) {
        TextUiFormats.Title -> selectedFormat = FormatOptionTextFormat.Title
        TextUiFormats.Heading -> selectedFormat = FormatOptionTextFormat.Heading
        TextUiFormats.SubHeading -> selectedFormat = FormatOptionTextFormat.Subheading
        TextUiFormats.Body -> selectedFormat = FormatOptionTextFormat.Body
        else -> Unit
    }

    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            editorViewModel.onDeleteNote()
            navigateBack()
        }
    )

    // Removed old floating toolbar - now using ScrollableRichTextToolbar in NoteDetailScreen

    if (!showFormatBar) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalCustomColors.current.bodyBackgroundColor)
                .padding(16.dp)
                .padding(end = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text formatting trigger button
            IconButton(onClick = {
                onShowTextFormatBar(true)
            }) {
                Icon(
                    imageVector = Images.Icons.IcLetterAa,
                    contentDescription = stringResource(Res.string.bottom_navigation_letter),
                    tint = LocalCustomColors.current.bodyContentColor
                )
            }
            
            // Star/favorite button
            IconButton(onClick = editorViewModel::onToggleStar) {
                Icon(
                    imageVector = if(isStarred) {
                        Images.Icons.IcStarFilled
                    } else {
                        Images.Icons.IcStar
                    },
                    contentDescription = stringResource(Res.string.bottom_navigation_starred),
                    tint = LocalCustomColors.current.starredColor
                )
            }
            
            // Delete button
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(Res.string.bottom_navigation_delete),
                    tint = LocalCustomColors.current.bodyContentColor
                )
            }
            
            // Keyboard toggle button
            IconButton(onClick = {
                debugPrintln{"****************** ${imeHeight}"}
                textFieldFocusRequester.showKeyboard(imeHeight>0, keyboardController)
            }) {
                Icon(
                    imageVector = Images.Icons.IcKeyboardHide,
                    contentDescription = stringResource(Res.string.bottom_navigation_hide_keyboard),
                    tint = LocalCustomColors.current.bodyContentColor
                )
            }
        }
    }
}

@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}