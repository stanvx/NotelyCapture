package com.module.notelycompose.audio.ui.uicomponents

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.Material3ShapeTokens
import kotlin.math.*
import kotlin.random.Random

/**
 * A modern waveform visualizer component for audio playback and recording.
 * 
 * Features:
 * - Real-time amplitude visualization
 * - Smooth animations and transitions
 * - Material 3 styling with theme colors
 * - Progress indication for playback
 * - Responsive design for different sizes
 */
@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    amplitudes: List<Float> = emptyList(),
    progress: Float = 0f,
    isPlaying: Boolean = false,
    isRecording: Boolean = false,
    maxBars: Int = 50,
    barWidth: Dp = 3.dp,
    barSpacing: Dp = 2.dp,
    minBarHeight: Dp = 4.dp,
    maxBarHeight: Dp = 40.dp
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
    val recordingColor = MaterialTheme.colorScheme.error
    
    // Animation for recording state
    val recordingAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Animation for bars when playing
    val playingAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(maxBarHeight + 8.dp)
            .clip(Material3ShapeTokens.cardContainer)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2f
        
        val totalBarWidth = barWidth.toPx() + barSpacing.toPx()
        val visibleBars = min(maxBars, (canvasWidth / totalBarWidth).toInt())
        
        val processedAmplitudes = if (amplitudes.isEmpty()) {
            // Generate demo waveform for empty state
            List(visibleBars) { i ->
                val normalizedPosition = i.toFloat() / visibleBars
                (sin(normalizedPosition * PI * 4) * 0.5f + 0.5f).toFloat()
            }
        } else {
            // Resample amplitudes to fit visible bars
            resampleAmplitudes(amplitudes, visibleBars)
        }
        
        processedAmplitudes.forEachIndexed { index, amplitude ->
            val x = index * totalBarWidth + barWidth.toPx() / 2f
            val normalizedProgress = progress.coerceIn(0f, 1f)
            val barProgress = (normalizedProgress * visibleBars).toInt()
            
            // Determine bar color based on state
            val barColor = when {
                isRecording -> {
                    recordingColor.copy(alpha = recordingAnimation)
                }
                index <= barProgress -> activeColor
                else -> inactiveColor
            }
            
            // Calculate bar height with animation
            val clampedAmplitude = amplitude.coerceIn(0f, 1f)
            val baseHeight = minBarHeight.toPx() + clampedAmplitude * (maxBarHeight.toPx() - minBarHeight.toPx())
            
            val animatedHeight = if (isPlaying && index <= barProgress) {
                baseHeight * (0.8f + 0.2f * sin(playingAnimation * PI * 2 + index * 0.5).toFloat())
            } else {
                baseHeight
            }
            
            drawWaveformBar(
                x = x,
                centerY = centerY,
                height = animatedHeight,
                width = barWidth.toPx(),
                color = barColor
            )
        }
        
        // Draw progress indicator
        if (progress > 0f && !isRecording) {
            val progressX = progress * canvasWidth
            drawLine(
                color = activeColor,
                start = Offset(progressX, 0f),
                end = Offset(progressX, canvasHeight),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Draws an individual waveform bar with rounded corners.
 */
private fun DrawScope.drawWaveformBar(
    x: Float,
    centerY: Float,
    height: Float,
    width: Float,
    color: Color
) {
    val halfHeight = height / 2f
    val path = Path().apply {
        val radius = width / 2f
        
        // Top rounded rectangle
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = x - width / 2f,
                top = centerY - halfHeight,
                right = x + width / 2f,
                bottom = centerY + halfHeight,
                radiusX = radius,
                radiusY = radius
            )
        )
    }
    
    drawPath(
        path = path,
        color = color
    )
}

/**
 * Resamples amplitude data to fit the available number of bars.
 */
private fun resampleAmplitudes(amplitudes: List<Float>, targetCount: Int): List<Float> {
    if (amplitudes.isEmpty()) return emptyList()
    if (amplitudes.size <= targetCount) return amplitudes
    
    val step = amplitudes.size.toFloat() / targetCount
    return (0 until targetCount).map { i ->
        val index = (i * step).toInt().coerceIn(0, amplitudes.size - 1)
        amplitudes[index]
    }
}

/**
 * Simplified waveform for recording state - shows animated bars.
 */
@Composable
fun RecordingWaveform(
    modifier: Modifier = Modifier,
    isRecording: Boolean = false,
    barCount: Int = 20
) {
    val recordingAmplitudes = remember {
        mutableStateListOf<Float>().apply {
            repeat(barCount) { add(0.1f) }
        }
    }
    
    // Animate recording amplitudes
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                recordingAmplitudes.forEachIndexed { index, _ ->
                    recordingAmplitudes[index] = Random.nextFloat() * 0.9f + 0.1f
                }
                kotlinx.coroutines.delay(100)
            }
        } else {
            for (i in recordingAmplitudes.indices) {
                recordingAmplitudes[i] = 0.1f
            }
        }
    }
    
    WaveformVisualizer(
        modifier = modifier,
        amplitudes = recordingAmplitudes.toList(),
        isRecording = isRecording,
        maxBars = barCount,
        maxBarHeight = 32.dp
    )
}

/**
 * Playback waveform with progress indication.
 */
@Composable
fun PlaybackWaveform(
    modifier: Modifier = Modifier,
    amplitudes: List<Float>,
    progress: Float,
    isPlaying: Boolean = false
) {
    WaveformVisualizer(
        modifier = modifier,
        amplitudes = amplitudes,
        progress = progress,
        isPlaying = isPlaying,
        maxBarHeight = 28.dp
    )
}