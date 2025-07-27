package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.download_required
import com.module.notelycompose.resources.for_accurate_transcription
import com.module.notelycompose.resources.take_few_minutes
import com.module.notelycompose.resources.file_size_approx
import com.module.notelycompose.resources.download
import com.module.notelycompose.resources.cancel

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
            Text(text = stringResource(Res.string.download_required))
        },
        text = {
            Column {
                Text(stringResource(Res.string.for_accurate_transcription))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(Res.string.take_few_minutes))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(Res.string.file_size_approx))
            }
        },
        confirmButton = {
            Button(
                onClick = onDownload,
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(Res.string.download))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}