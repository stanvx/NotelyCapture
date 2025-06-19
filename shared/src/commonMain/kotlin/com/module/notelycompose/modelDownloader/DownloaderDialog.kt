package com.module.notelycompose.modelDownloader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.module.notelycompose.notes.ui.theme.LocalCustomColors


@Composable
fun DownloaderDialog(
    modifier: Modifier,
    downloaderUiState: DownloaderUiState,
    onDismiss:()->Unit
) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
              dismissOnClickOutside = false,
                dismissOnBackPress = false,
                usePlatformDefaultWidth = true
            )

        ){
            Surface (
                shape = RoundedCornerShape(16.dp), // Rounded corners
                elevation = 12.dp,
                color = LocalCustomColors.current.bodyBackgroundColor) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.Start,

                    ) {
                    Text(
                        "Downloading Transcription model ${downloaderUiState.fileName}",
                        color = LocalCustomColors.current.bodyContentColor
                    )
                    LinearProgressIndicator(
                        (downloaderUiState.progress/100),
                        modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                        strokeCap = StrokeCap.Round
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            downloaderUiState.downloaded,
                            color = LocalCustomColors.current.bodyContentColor
                        )
                        Text(
                            "/",
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = LocalCustomColors.current.bodyContentColor
                        )
                        Text(
                            downloaderUiState.total,
                            color = LocalCustomColors.current.bodyContentColor
                        )
                    }
                }
            }
        }

}





