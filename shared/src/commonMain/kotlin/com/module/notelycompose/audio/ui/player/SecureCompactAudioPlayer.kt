package com.module.notelycompose.audio.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.module.notelycompose.audio.presentation.AudioPlayerViewModel
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.notes.ui.components.NoteCardData
import com.module.notelycompose.platform.HapticFeedback
import com.module.notelycompose.security.SecureAudioPlayerState
import com.module.notelycompose.security.getValidatedPathOrNull
import com.module.notelycompose.security.getUserErrorMessage
import com.module.notelycompose.security.isAudioSafelyAvailable

/**
 * Secure wrapper around CompactAudioPlayer that enforces path validation
 * and provides proper error handling for security violations.
 * 
 * This component addresses the critical security vulnerability identified in Apple QA review
 * by implementing UI-layer validation before audio player integration.
 * 
 * Security Features:
 * - Validates all audio paths before player integration
 * - Displays user-friendly error messages for security violations
 * - Logs security incidents for monitoring
 * - Prevents path traversal and protocol injection attacks
 * - Graceful degradation for invalid paths
 */
@Composable
fun SecureCompactAudioPlayer(
    noteData: NoteCardData,
    uiState: AudioPlayerUiState,
    audioPlayerViewModel: AudioPlayerViewModel,
    modifier: Modifier = Modifier,
    hapticFeedback: HapticFeedback? = null,
    allowedBaseDirectory: String? = null
) {
    // Create secure audio player state with comprehensive validation
    val secureState = remember(noteData.id, noteData.recordingPath, noteData.audioDurationMs) {
        SecureAudioPlayerState.validateForUi(
            recordingPath = noteData.recordingPath,
            noteId = noteData.id,
            noteDurationMs = noteData.audioDurationMs,
            expectedNoteId = noteData.id, // Additional validation
            allowedBaseDirectory = allowedBaseDirectory
        )
    }
    
    when (secureState) {
        is SecureAudioPlayerState.Validated -> {
            // Path is validated and safe - render normal audio player
            CompactAudioPlayer(
                filePath = secureState.validatedPath.path,
                noteId = secureState.noteId,
                noteDurationMs = secureState.noteDurationMs,
                uiState = uiState,
                onLoadAudio = audioPlayerViewModel::onLoadAudio,
                onTogglePlayPause = audioPlayerViewModel::onTogglePlayPause,
                onTogglePlaybackSpeed = audioPlayerViewModel::onTogglePlaybackSpeed,
                isNoteCurrentlyPlaying = audioPlayerViewModel::isNoteCurrentlyPlaying,
                isNoteLoaded = audioPlayerViewModel::isNoteLoaded,
                modifier = modifier,
                hapticFeedback = hapticFeedback
            )
        }
        
        is SecureAudioPlayerState.SecurityThreatDetected -> {
            // Security threat detected - show secure error message
            SecurityThreatAudioErrorCard(
                securityState = secureState,
                modifier = modifier
            )
        }
        
        is SecureAudioPlayerState.NoAudio -> {
            // No audio available - show appropriate message
            NoAudioAvailableCard(
                modifier = modifier
            )
        }
    }
}

/**
 * Error card displayed when security threat is detected in audio path
 */
@Composable
private fun SecurityThreatAudioErrorCard(
    securityState: SecureAudioPlayerState.SecurityThreatDetected,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Audio Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Audio Unavailable",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Text(
                    text = securityState.userMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
    
    // Log security incident in the background
    LaunchedEffect(securityState.noteId) {
        if (securityState.shouldLogIncident) {
            println("[SECURITY-ALERT] Blocked audio access for note ${securityState.noteId}: ${securityState.reason}")
        }
    }
}

/**
 * Card displayed when no audio is available for the note
 */
@Composable
private fun NoAudioAvailableCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No audio recording available",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

