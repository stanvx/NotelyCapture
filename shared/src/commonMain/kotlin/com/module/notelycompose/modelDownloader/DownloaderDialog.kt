package com.module.notelycompose.modelDownloader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.module.notelycompose.getPlatform
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.vectors.IcChevronLeft
import com.module.notelycompose.resources.vectors.IcRecorder
import com.module.notelycompose.resources.vectors.Images
import notelycompose.shared.generated.resources.Res
import notelycompose.shared.generated.resources.note_detail_recorder
import notelycompose.shared.generated.resources.top_bar_back
import notelycompose.shared.generated.resources.transcription_dialog_append
import notelycompose.shared.generated.resources.transcription_dialog_original
import notelycompose.shared.generated.resources.transcription_dialog_summarize
import org.jetbrains.compose.resources.stringResource


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





