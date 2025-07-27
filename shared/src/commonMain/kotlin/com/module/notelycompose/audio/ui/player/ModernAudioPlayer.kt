package com.module.notelycompose.audio.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.audio.ui.formatTimeToMMSS
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens
import com.module.notelycompose.platform.HapticFeedback
import kotlinx.coroutines.delay

/**
 * Modern Material 3 audio player with waveform visualization.
 * 
 * Features:
 * - Integrated waveform display
 * - Modern Material 3 design language
 * - Enhanced playback controls
 * - Speed control with smooth animations
 * - Loading and error states
 * - Accessibility support
 */
@Composable
fun ModernAudioPlayer(
    filePath: String,
    uiState: AudioPlayerUiState,
    onLoadAudio: (String) -> Unit,
    onClear: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onTogglePlayPause: () -> Unit,
    onTogglePlaybackSpeed: () -> Unit,
    modifier: Modifier = Modifier,
    amplitudes: List<Float> = emptyList(),
    hapticFeedback: HapticFeedback? = null
) {
    val isLoading = !uiState.isLoaded
    val progress = if (uiState.duration > 0) {
        uiState.currentPosition.toFloat() / uiState.duration.toFloat()
    } else 0f
    
    // Audio loading is now manual - user must tap play to load and start audio
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = Material3ShapeTokens.surfaceContainer,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Waveform section with enhanced styling
            WaveformSection(
                amplitudes = uiState.waveformAmplitudes,
                progress = progress,
                isPlaying = uiState.isPlaying,
                isLoading = isLoading,
                onSeekTo = onSeekTo,
                duration = uiState.duration
            )
            
            // Control panel with modern design
            ControlPanel(
                isPlaying = uiState.isPlaying,
                isLoaded = uiState.isLoaded,
                currentPosition = uiState.currentPosition,
                duration = uiState.duration,
                playbackSpeed = uiState.playbackSpeed,
                filePath = filePath,
                onLoadAudio = onLoadAudio,
                onTogglePlayPause = onTogglePlayPause,
                onTogglePlaybackSpeed = onTogglePlaybackSpeed,
                onSkipBack = { onSeekTo((uiState.currentPosition - 10000).coerceAtLeast(0)) },
                onSkipForward = { onSeekTo((uiState.currentPosition + 10000).coerceAtMost(uiState.duration)) },
                hapticFeedback = hapticFeedback
            )
        }
    }
}

/**
 * Waveform visualization section.
 */
@Composable
private fun WaveformSection(
    amplitudes: List<Float>,
    progress: Float,
    isPlaying: Boolean,
    isLoading: Boolean,
    onSeekTo: (Int) -> Unit,
    duration: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
    ) {
        if (isLoading) {
            LoadingWaveform()
        } else {
            // Placeholder for waveform - PlaybackWaveform was removed
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Audio Waveform",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Loading state for waveform.
 */
@Composable
private fun LoadingWaveform() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val animatedAlpha by rememberInfiniteTransition().animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 200),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha),
                            CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Enhanced control panel with modern Material 3 design.
 */
@Composable
private fun ControlPanel(
    isPlaying: Boolean,
    isLoaded: Boolean,
    currentPosition: Int,
    duration: Int,
    playbackSpeed: Float,
    filePath: String,
    onLoadAudio: (String) -> Unit,
    onTogglePlayPause: () -> Unit,
    onTogglePlaybackSpeed: () -> Unit,
    onSkipBack: () -> Unit,
    onSkipForward: () -> Unit,
    hapticFeedback: HapticFeedback?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip backward 10s
            ControlButton(
                onClick = {
                    hapticFeedback?.light()
                    onSkipBack()
                },
                enabled = isLoaded,
                icon = Icons.Rounded.Refresh,
                contentDescription = "Skip back 10 seconds"
            )
            
            // Main play/pause button
            PlayPauseButton(
                isPlaying = isPlaying,
                isLoaded = isLoaded,
                onClick = {
                    hapticFeedback?.medium()
                    if (!isLoaded && filePath.isNotEmpty()) {
                        onLoadAudio(filePath)
                    } else {
                        onTogglePlayPause()
                    }
                }
            )
            
            // Skip forward 10s
            ControlButton(
                onClick = {
                    hapticFeedback?.light()
                    onSkipForward()
                },
                enabled = isLoaded,
                icon = Icons.Rounded.KeyboardArrowRight,
                contentDescription = "Skip forward 10 seconds"
            )
        }
        
        // Time and speed row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time display
            TimeDisplay(
                currentPosition = currentPosition,
                duration = duration
            )
            
            // Speed control
            SpeedControl(
                playbackSpeed = playbackSpeed,
                isEnabled = isLoaded,
                onToggleSpeed = {
                    hapticFeedback?.light()
                    onTogglePlaybackSpeed()
                }
            )
        }
    }
}

/**
 * Enhanced play/pause button with animations.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    isLoaded: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                scaleIn() + fadeIn() with scaleOut() + fadeOut()
            }
        ) { playing ->
            Icon(
                imageVector = if (playing) Icons.Rounded.Close else Icons.Rounded.PlayArrow,
                contentDescription = if (playing) "Pause" else "Play",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Control button with consistent styling.
 */
@Composable
private fun ControlButton(
    onClick: () -> Unit,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Enhanced time display.
 */
@Composable
private fun TimeDisplay(
    currentPosition: Int,
    duration: Int
) {
    Text(
        text = "${currentPosition.formatTimeToMMSS()} / ${duration.formatTimeToMMSS()}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
    )
}

/**
 * Enhanced speed control with modern styling.
 */
@Composable
private fun SpeedControl(
    playbackSpeed: Float,
    isEnabled: Boolean,
    onToggleSpeed: () -> Unit
) {
    Surface(
        onClick = onToggleSpeed,
        enabled = isEnabled,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${playbackSpeed}x",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}