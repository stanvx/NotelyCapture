package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors

@Composable
fun DownloadModelDialog(
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onCancel,
        title = {
            Text(text = "Download Required")
        },
        text = {
            Column {
                Text("For accurate transcription, we need to download the AI model.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("This may take a few minutes and requires a stable internet connection.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("File size: approximately 142MB")
            }
        },
        confirmButton = {
            Button(
                onClick = onDownload,
                colors = ButtonDefaults.buttonColors(
                    contentColor = LocalCustomColors.current.bodyContentColor
                )
            ) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text("Cancel")
            }
        }
    )
}