package com.module.notelycompose.audio.ui.recorder

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.module.notelycompose.audio.ui.uicomponents.AudioReactiveLottie
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    val recordingState by viewModel.audioRecorderPresentationState.collectAsStateWithLifecycle()
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

            ScreenState.Recording -> {
                if (isQuickRecordMode) {
                    QuickRecordingScreen(
                        counterTimeString = recordingState.recordCounterString,
                        currentAmplitude = recordingState.currentAmplitude,
                        onStopRecording = {
                            debugPrintln { "onStop quick recording" }
                            viewModel.onStopRecording()
                            screenState = ScreenState.Success
                        },
                        onNavigateBack = {
                            viewModel.onStopRecording()
                            navigateBack()
                        }
                    )
                } else {
                    RecordingInProgressScreen(
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
                }
            }

            ScreenState.Success -> {
                // Skip the tick animation and go straight to processing
                LaunchedEffect(Unit) {
                    if (isQuickRecordMode) {
                        // Simplified path retrieval - direct state access
                        val recordingPath = viewModel.audioRecorderPresentationState.first { it.recordingPath.isNotEmpty() }.recordingPath
                        
                        if (!recordingPath.isNullOrEmpty()) {
                            backgroundTranscriptionService.startTranscription(
                                audioFilePath = recordingPath,
                                onComplete = { noteId ->
                                    navigateBack()
                                },
                                onError = { error ->
                                    // Fallback: create audio-only note
                                    editorViewModel.onUpdateRecordingPath(recordingPath)
                                    navigateBack()
                                }
                            )
                        } else {
                            navigateBack()
                        }
                    } else {
                        // Traditional flow - no longer needs delay since no animation
                        editorViewModel.onUpdateRecordingPath(recordingState.recordingPath)
                        navigateBack()
                    }
                }
                
                // Show a subtle processing indicator while transcription happens
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LocalCustomColors.current.bodyBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
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
                .padding(bottom = AppConstants.UI.BOTTOM_PADDING_DP.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(AppConstants.UI.LARGE_BUTTON_SIZE_DP.dp)
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
    ImmersiveRecordingScreen(
        counterTimeString = counterTimeString,
        currentAmplitude = currentAmplitude,
        onNavigateBack = onNavigateBack,
        onStopRecording = onStopRecording,
        onPauseRecording = onPauseRecording,
        onResumeRecording = onResumeRecording,
        isRecordPaused = isRecordPaused,
        showControls = true
    )
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
            Text(
                text = stringResource(Res.string.top_bar_back),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalCustomColors.current.bodyContentColor
            )
        }
    }
}

/**
 * Minimal recording interface for quick record mode.
 * Shows a streamlined UI with recording visualization and stop button.
 */
@Composable
private fun QuickRecordingScreen(
    counterTimeString: String,
    currentAmplitude: Float,
    onStopRecording: () -> Unit,
    onNavigateBack: () -> Unit
) {
    ImmersiveRecordingScreen(
        counterTimeString = counterTimeString,
        currentAmplitude = currentAmplitude,
        onNavigateBack = onNavigateBack,
        onStopRecording = onStopRecording,
        onPauseRecording = { /* Quick record doesn't support pause */ },
        onResumeRecording = { /* Quick record doesn't support resume */ },
        isRecordPaused = false,
        showControls = false // Quick record only shows stop button
    )
}

/**
 * Enhanced immersive recording screen following Material 3 design principles.
 * 
 * Features:
 * - Larger, more prominent AudioReactiveLottie visualization (40-50% of screen height)
 * - Material 3 surface elevation and dynamic theming
 * - Proper accessibility with minimum 48dp touch targets
 * - Material 3 color system and typography scale
 * - Motion and animation using Material 3 tokens
 * - Supporting visual elements (pulse rings, enhanced surfaces)
 * - Configurable controls for full recording vs quick record modes
 */
@Composable
private fun ImmersiveRecordingScreen(
    counterTimeString: String,
    currentAmplitude: Float,
    onNavigateBack: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    isRecordPaused: Boolean,
    showControls: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Back button with proper accessibility
        RecordingUiComponentBackButton(
            onNavigateBack = onNavigateBack,
            onStopRecording = onStopRecording
        )

        // Main content with enhanced visual hierarchy
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Top spacer for visual balance
            Spacer(modifier = Modifier.height(80.dp))
            
            // Recording status with Material 3 typography
            if (!showControls) {
                Text(
                    text = "Recording...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
            
            // Enhanced central visualization area with Material 3 surface treatment
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp), // ~40-45% of typical screen height for prominence
                contentAlignment = Alignment.Center
            ) {
                // Background pulse rings for enhanced visual feedback
                repeat(3) { index ->
                    val delay = index * 200
                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (isRecordPaused) 0f else (currentAmplitude * 0.3f),
                        animationSpec = tween(
                            durationMillis = 800 + delay,
                            easing = FastOutSlowInEasing
                        ),
                        label = "pulseRing${index}"
                    )
                    
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isRecordPaused) 1f else (1f + currentAmplitude * 0.2f + index * 0.1f),
                        animationSpec = tween(
                            durationMillis = 1000 + delay,
                            easing = FastOutSlowInEasing
                        ),
                        label = "pulseScale${index}"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(280.dp + (index * 20).dp)
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                                alpha = animatedAlpha
                            }
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
                
                // Material 3 surface container for the main visualization
                androidx.compose.material3.Surface(
                    modifier = Modifier.size(280.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shadowElevation = if (isRecordPaused) 2.dp else 8.dp,
                    tonalElevation = if (isRecordPaused) 1.dp else 4.dp
                ) {
                    // Main AudioReactiveLottie visualization - now larger and more prominent
                    AudioReactiveLottie(
                        amplitude = if (isRecordPaused) 0f else currentAmplitude,
                        isRecording = !isRecordPaused,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp) // Slight padding within the surface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Enhanced timer display with Material 3 typography
            androidx.compose.material3.Surface(
                modifier = Modifier.wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp
            ) {
                Text(
                    text = counterTimeString,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
            
            // Flexible spacer to push controls to bottom
            Spacer(modifier = Modifier.weight(1f))
            
            // Control buttons area with proper accessibility
            if (showControls) {
                EnhancedRecordingControls(
                    isRecordPaused = isRecordPaused,
                    onPauseRecording = onPauseRecording,
                    onResumeRecording = onResumeRecording,
                    onStopRecording = onStopRecording
                )
            } else {
                // Quick record mode - only stop button
                QuickRecordStopControl(
                    onStopRecording = onStopRecording
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Enhanced recording controls with Material 3 design and proper accessibility.
 */
@Composable
private fun EnhancedRecordingControls(
    isRecordPaused: Boolean,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pause/Resume button with Material 3 styling
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .size(64.dp) // Minimum 48dp touch target with padding
                    .clickable {
                        if (isRecordPaused) {
                            onResumeRecording()
                        } else {
                            onPauseRecording()
                        }
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (!isRecordPaused) Images.Icons.IcPause else Icons.Filled.PlayArrow,
                        contentDescription = if (!isRecordPaused) "Pause recording" else "Resume recording",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Stop button with enhanced Material 3 error styling
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .size(80.dp) // Larger for primary action
                    .clickable { onStopRecording() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                tonalElevation = 3.dp,
                shadowElevation = 6.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.onError)
                    )
                }
            }
        }

        // Action label with Material 3 typography
        Text(
            text = stringResource(Res.string.recording_ui_tap_stop_record),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Quick record stop control with Material 3 design.
 */
@Composable
private fun QuickRecordStopControl(
    onStopRecording: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Large stop button for quick record
        androidx.compose.material3.Surface(
            modifier = Modifier
                .size(88.dp) // Large touch target for quick access
                .clickable { onStopRecording() },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onError)
                )
            }
        }

        // Action label
        Text(
            text = stringResource(Res.string.recording_ui_tap_stop_record),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}
