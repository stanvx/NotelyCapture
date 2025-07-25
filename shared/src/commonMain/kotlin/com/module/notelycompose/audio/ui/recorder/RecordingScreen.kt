package com.module.notelycompose.audio.ui.recorder

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.module.notelycompose.audio.ui.uicomponents.AudioReactiveLottie
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.audio.presentation.AudioRecorderViewModel
import com.module.notelycompose.core.constants.AppConstants
import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.notes.presentation.detail.TextEditorViewModel
import com.module.notelycompose.transcription.BackgroundTranscriptionService
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.HandlePlatformBackNavigation
import com.module.notelycompose.platform.getPlatform
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.recording_ui_checkmark
import com.module.notelycompose.resources.recording_ui_microphone
import com.module.notelycompose.resources.recording_ui_tap_start_record
import com.module.notelycompose.resources.recording_ui_tap_stop_record
import com.module.notelycompose.resources.top_bar_back
import com.module.notelycompose.resources.transcription_icon
import com.module.notelycompose.resources.vectors.IcChevronLeft
import com.module.notelycompose.resources.vectors.IcPause
import com.module.notelycompose.resources.vectors.IcRecorder
import com.module.notelycompose.resources.vectors.Images
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class ScreenState {
    Initial,
    Recording,
    Success
}

@Composable
fun RecordingScreen(
    noteId: Long?,
    navigateBack: () -> Unit,
    viewModel: AudioRecorderViewModel = koinViewModel(),
    editorViewModel: TextEditorViewModel = koinViewModel(),
    isQuickRecordMode: Boolean = false,
    backgroundTranscriptionService: BackgroundTranscriptionService = koinInject()
) {
    val recordingState by viewModel.audioRecorderPresentationState.collectAsState()
    var screenState by remember { mutableStateOf(if (isQuickRecordMode) ScreenState.Recording else ScreenState.Initial) }

    DisposableEffect(Unit){
        viewModel.setupRecorder()
        // Auto-start recording in quick record mode
        if (isQuickRecordMode) {
            viewModel.onStartRecording(noteId) {
                // Already in Recording state, no state change needed
            }
        }
        onDispose {
            viewModel.onStopRecording()
            viewModel.finishRecorder()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.bodyBackgroundColor)
            .windowInsetsPadding(WindowInsets(0))
            .padding(0.dp)
    ) {

        when (screenState) {
            ScreenState.Initial -> RecordingInitialScreen(
                onNavigateBack = navigateBack,
                onTapToRecord = {
                    viewModel.onStartRecording(noteId) {
                        screenState = ScreenState.Recording
                    }
                },
                onStopRecording = viewModel::onStopRecording
            )

            ScreenState.Recording -> RecordingInProgressScreen(
                counterTimeString = recordingState.recordCounterString,
                currentAmplitude = recordingState.currentAmplitude,
                onStopRecording = {
                    debugPrintln { "onStop recording" }
                    viewModel.onStopRecording()
                    screenState = ScreenState.Success
                },
                onNavigateBack = navigateBack,
                isRecordPaused = recordingState.isRecordPaused,
                onPauseRecording = viewModel::onPauseRecording,
                onResumeRecording = viewModel::onResumeRecording
            )

            ScreenState.Success -> {
                RecordingSuccessScreen()
                LaunchedEffect(Unit) {
                    if (isQuickRecordMode) {
                        // Wait for recording path to be available using reactive approach
                        val recordingPath = withTimeoutOrNull(AppConstants.Recording.RECORDING_PATH_TIMEOUT) {
                            viewModel.audioRecorderPresentationState.first { it.recordingPath.isNotEmpty() }
                        }?.recordingPath
                        
                        if (!recordingPath.isNullOrEmpty()) {
                            debugPrintln { "Quick record completed: $recordingPath" }
                            
                            backgroundTranscriptionService.startTranscription(
                                audioFilePath = recordingPath,
                                onComplete = { noteId ->
                                    debugPrintln { "Background transcription completed for note: $noteId" }
                                    // Navigate back to note list after successful transcription and note creation
                                    navigateBack()
                                },
                                onError = { error ->
                                    debugPrintln { "Background transcription failed: ${error.message}" }
                                    // Still update editor with recording path and navigate back
                                    editorViewModel.onUpdateRecordingPath(recordingPath)
                                    navigateBack()
                                }
                            )
                        } else {
                            debugPrintln { "Quick record failed: Recording path not available after ${AppConstants.Recording.RECORDING_PATH_TIMEOUT}" }
                            // Fallback: navigate back without transcription
                            navigateBack()
                        }
                    } else {
                        // Traditional flow with configured delay
                        delay(AppConstants.Recording.TRADITIONAL_FLOW_DELAY)
                        debugPrintln { "%%%%%%%%%%% ${recordingState.recordingPath}" }
                        editorViewModel.onUpdateRecordingPath(recordingState.recordingPath)
                        navigateBack()
                    }
                }
            }

        }
    }

    HandlePlatformBackNavigation(enabled = true) {
        navigateBack()
    }
}

@Composable
private fun RecordingInitialScreen(
    onNavigateBack: () -> Unit,
    onTapToRecord: () -> Unit,
    onStopRecording: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.bodyBackgroundColor)
    ) {
        RecordingUiComponentBackButton(
            onNavigateBack = onNavigateBack,
            onStopRecording = onStopRecording
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 80.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onTapToRecord() },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Images.Icons.IcRecorder,
                    contentDescription = stringResource(Res.string.recording_ui_microphone),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = stringResource(Res.string.recording_ui_tap_start_record),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RecordingInProgressScreen(
    counterTimeString: String,
    currentAmplitude: Float,
    onNavigateBack: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    isRecordPaused: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.bodyBackgroundColor)
    ) {
        RecordingUiComponentBackButton(
            onNavigateBack = onNavigateBack,
            onStopRecording = onStopRecording
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Main recording indicator - full-size gradient animation
            AudioReactiveLottie(
                amplitude = if (isRecordPaused) 0f else currentAmplitude,
                isRecording = !isRecordPaused,
                modifier = Modifier
                    .size(320.dp)  // Large, prominent size for beautiful gradient display
            )
            
            // Timer display positioned below the animation
            Text(
                text = counterTimeString,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 32.sp
            )
            
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable {
                                if (isRecordPaused) {
                                    onResumeRecording()
                                } else {
                                    onPauseRecording()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (!isRecordPaused) Images.Icons.IcPause else Icons.Filled.PlayArrow,
                            contentDescription = if (!isRecordPaused) "Pause recording" else "Resume recording",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = CircleShape
                            )
                            .clickable { onStopRecording() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onError)
                        )
                    }
                }

                Text(
                    text = stringResource(Res.string.recording_ui_tap_stop_record),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun LoadingAnimation(
    isRecordPaused: Boolean
) {
    val drawArcColor = LocalCustomColors.current.bodyContentColor
    val rotationAngle = remember { Animatable(0f) }

    LaunchedEffect(isRecordPaused) {
        if (!isRecordPaused) {
            rotationAngle.animateTo(
                targetValue = rotationAngle.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotationAngle.stop()
        }
    }

    Canvas(modifier = Modifier.size(200.dp)) {
        drawArc(
            color = drawArcColor,
            startAngle = rotationAngle.value,
            sweepAngle = 300f,
            useCenter = false,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
    }
}

@Composable
internal fun RecordingSuccessScreen() {
    val pathColor = LocalCustomColors.current.bodyContentColor
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.bodyBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        var animationPlayed by remember { mutableStateOf(false) }
        val pathProgress by animateFloatAsState(
            targetValue = if (animationPlayed) 1f else 0f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            label = stringResource(Res.string.recording_ui_checkmark)
        )

        LaunchedEffect(Unit) {
            animationPlayed = true
        }

        Canvas(modifier = Modifier.size(100.dp)) {
            val path = Path().apply {

                addArc(
                    Rect(
                        offset = Offset(0f, 0f),
                        size = Size(size.width, size.height)
                    ),
                    0f,
                    360f * pathProgress
                )

                if (pathProgress > 0.5f) {
                    val checkProgress = (pathProgress - 0.5f) * 2f
                    moveTo(size.width * 0.2f, size.height * 0.5f)
                    lineTo(
                        size.width * 0.45f,
                        size.height * 0.7f * checkProgress
                    )
                    lineTo(
                        size.width * 0.8f,
                        size.height * 0.3f * checkProgress
                    )
                }
            }

            drawPath(
                path = path,
                color = pathColor,
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Composable
private fun RecordingUiComponentBackButton(
    onNavigateBack: () -> Unit,
    onStopRecording: () -> Unit
) {
    if (getPlatform().isAndroid) {
        IconButton(
            onClick = {
                onStopRecording()
                onNavigateBack()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.top_bar_back),
                tint = LocalCustomColors.current.bodyContentColor
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    onStopRecording()
                    onNavigateBack()
                }
        ) {
            Icon(
                imageVector = Images.Icons.IcChevronLeft,
                contentDescription = stringResource(Res.string.top_bar_back),
                modifier = Modifier.size(28.dp),
                tint = LocalCustomColors.current.bodyContentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.material.Text(
                text = stringResource(Res.string.top_bar_back),
                style = androidx.compose.material.MaterialTheme.typography.body1,
                color = LocalCustomColors.current.bodyContentColor
            )
        }
    }
}
