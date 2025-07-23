package com.module.notelycompose.audio.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.audio.ui.formatTimeToMMSS
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.platform.HapticFeedback

/**
 * Compact inline audio player designed for minimal screen space usage.
 * 
 * Features:
 * - Single row horizontal layout
 * - Play/pause controls
 * - Progress indicator
 * - Time display
 * - Material 3 design
 * - Simplified speed control (x1, x1.5, x2)
 * - No waveform visualization for simplicity
 */
@Composable
fun CompactAudioPlayer(
    filePath: String,
    uiState: AudioPlayerUiState,
    onLoadAudio: (String) -> Unit,
    onTogglePlayPause: () -> Unit,
    onTogglePlaybackSpeed: () -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedback: HapticFeedback? = null
) {
    val progress = if (uiState.duration > 0) {
        uiState.currentPosition.toFloat() / uiState.duration.toFloat()
    } else 0f

    // Audio loading is now manual - user must tap play to load and start audio

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Main controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause button
                CompactPlayButton(
                    isPlaying = uiState.isPlaying,
                    isLoaded = uiState.isLoaded,
                    onClick = {
                        hapticFeedback?.light()
                        if (!uiState.isLoaded && filePath.isNotEmpty()) {
                            onLoadAudio(filePath)
                        } else {
                            onTogglePlayPause()
                        }
                    }
                )

                // Time and speed info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.currentPosition.formatTimeToMMSS()} / ${uiState.duration.formatTimeToMMSS()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        // Simplified speed control: x1, x1.5, x2
                        SpeedControl(
                            playbackSpeed = uiState.playbackSpeed,
                            isEnabled = uiState.isLoaded,
                            onToggleSpeed = onTogglePlaybackSpeed
                        )
                    }
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

/**
 * Compact play/pause button with minimal design
 */
@Composable
private fun CompactPlayButton(
    isPlaying: Boolean,
    isLoaded: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isPlaying) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = tween(200, easing = LinearEasing)
    )

    val contentColor by animateColorAsState(
        targetValue = if (isPlaying) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        animationSpec = tween(200, easing = LinearEasing)
    )

    Surface(
        onClick = onClick,
        enabled = isLoaded,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp)
        ) {
            if (!isLoaded) {
                // Simple loading indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            CircleShape
                        )
                )
            } else {
                if (isPlaying) {
                    // Simple pause indicator using a square
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(contentColor, RoundedCornerShape(2.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Simplified speed control for x1, x1.5, x2 speeds.
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
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
    ) {
        Text(
            text = "${playbackSpeed}x",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}