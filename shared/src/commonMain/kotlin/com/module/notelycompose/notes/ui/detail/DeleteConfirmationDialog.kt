package com.module.notelycompose.notes.ui.detail

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import notelycompose.shared.generated.resources.Res
import notelycompose.shared.generated.resources.confirmation
import notelycompose.shared.generated.resources.confirmation_text
import notelycompose.shared.generated.resources.confirmation_delete
import notelycompose.shared.generated.resources.confirmation_cancel
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
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
                    text = stringResource(Res.string.confirmation_text),
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
                        containerColor = Color(0xFFC23636),
                        contentColor = LocalCustomColors.current.bodyContentColor
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.confirmation_delete),
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
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
