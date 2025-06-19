package com.module.notelycompose.notes.ui.detail

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import notelycompose.shared.generated.resources.Res
import notelycompose.shared.generated.resources.confirmation
import notelycompose.shared.generated.resources.recording_replace_continue
import notelycompose.shared.generated.resources.confirmation_cancel
import notelycompose.shared.generated.resources.recording_replace_error
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReplaceRecordingConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        LocalSoftwareKeyboardController.current?.hide()
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(Res.string.confirmation),
                    color = LocalCustomColors.current.bodyContentColor
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.recording_replace_error),
                    color = LocalCustomColors.current.bodyContentColor
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        onConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38761D),
                        contentColor = LocalCustomColors.current.bodyContentColor
                    ),
                    shape = RectangleShape
                ) {
                    Text(
                        text = stringResource(Res.string.recording_replace_continue),
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    shape = RectangleShape
                ) {
                    Text(
                        text = stringResource(Res.string.confirmation_cancel),
                        color = LocalCustomColors.current.bodyContentColor
                    )
                }
            },
            containerColor = LocalCustomColors.current.shareDialogBackgroundColor,
            titleContentColor = LocalCustomColors.current.bodyContentColor,
            shape = RectangleShape
        )
    }
}
