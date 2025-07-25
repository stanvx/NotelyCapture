package com.module.notelycompose.notes.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.share_options
import com.module.notelycompose.resources.share_text
import com.module.notelycompose.resources.share_audio_recording
import com.module.notelycompose.resources.close
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShareDialog(
    onShareAudioRecording: () -> Unit,
    onShareTexts: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.share_options),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onShareAudioRecording()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalCustomColors.current.shareDialogButtonColor,
                        contentColor = LocalCustomColors.current.bodyBackgroundColor
                    )
                ) {
                    Text(stringResource(Res.string.share_audio_recording))
                }

                Button(
                    onClick = {
                        onShareTexts()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalCustomColors.current.shareDialogButtonColor,
                        contentColor = LocalCustomColors.current.bodyBackgroundColor
                    )
                ) {
                    Text(stringResource(Res.string.share_text))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(Res.string.close),
                    color = LocalCustomColors.current.bodyContentColor
                )
            }
        },
        containerColor = LocalCustomColors.current.shareDialogBackgroundColor,
        textContentColor = LocalCustomColors.current.bodyContentColor
    )
}
