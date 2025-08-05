package com.module.notelycompose.audio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.Stable
import com.module.notelycompose.audio.presentation.mappers.AudioPlayerPresentationToUiMapper
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.audio.domain.AudioWaveformExtractor
import com.module.notelycompose.platform.PlatformAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.security.AudioPathValidator
import com.module.notelycompose.security.AudioPathValidator.ValidationResult

/**
 * Platform-independent ViewModel for audio playback functionality
 */
class AudioPlayerViewModel(
    private val audioPlayer: PlatformAudioPlayer,
    private val mapper: AudioPlayerPresentationToUiMapper,
    private val preferencesRepository: PreferencesRepository,
    private val waveformExtractor: AudioWaveformExtractor,
):ViewModel(){
    private var progressUpdateJob: Job? = null
    private val speedUpdateMutex = Mutex()

    private val _uiState = MutableStateFlow(AudioPlayerPresentationState())
    val uiState: StateFlow<AudioPlayerPresentationState> = _uiState.asStateFlow()

    init {
        // Load saved playback speed
        viewModelScope.launch {
            try {
                val savedSpeed = preferencesRepository.getPlaybackSpeed().first()
                _uiState.update { it.copy(playbackSpeed = savedSpeed) }
                // Note: Speed will be applied when media is prepared via loadAudio()
            } catch (e: Exception) {
                // Use default speed if unable to load preferences
            }
        }
    }

    fun onGetUiState(presentationState: AudioPlayerPresentationState): AudioPlayerUiState {
        return mapper.mapToUiState(presentationState)
    }

    fun isNoteCurrentlyPlaying(noteId: Long): Boolean {
        val currentState = _uiState.value
        return currentState.currentPlayingNoteId == noteId && currentState.isPlaying
    }

    fun isNoteLoaded(noteId: Long): Boolean {
        return _uiState.value.currentPlayingNoteId == noteId && _uiState.value.isLoaded
    }

    fun onTogglePlaybackSpeed() {
        viewModelScope.launch {
            speedUpdateMutex.withLock {
                try {
                    val currentSpeed = _uiState.value.playbackSpeed
                    val nextSpeed = when (currentSpeed) {
                        1.0f -> 1.5f
                        1.5f -> 2.0f
                        else -> 1.0f
                    }
                    
                    // Apply speed to audio player first
                    audioPlayer.setPlaybackSpeed(nextSpeed)
                    
                    // Update UI state
                    _uiState.update { it.copy(playbackSpeed = nextSpeed) }
                    
                    // Save to preferences (validation happens here)
                    preferencesRepository.setPlaybackSpeed(nextSpeed)
                    
                } catch (e: Exception) {
                    // On error, keep current state unchanged
                }
            }
        }
    }

    fun onLoadAudio(filePath: String, noteId: Long, autoPlay: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // SECURITY: Validate audio path before processing (defense-in-depth)
                // This provides an additional security layer beyond UI validation
                when (val validation = AudioPathValidator.validateAudioPath(filePath)) {
                    is ValidationResult.Valid -> {
                        loadValidatedAudio(filePath, noteId, autoPlay)
                    }
                    is ValidationResult.Invalid -> {
                        // Security threat detected - log and reject
                        val threatLevel = validation.securityThreat
                        val reason = validation.reason
                        
                        _uiState.update { it.copy(
                            errorMessage = when (threatLevel) {
                                AudioPathValidator.SecurityThreat.CRITICAL -> "Audio file access denied for security reasons"
                                AudioPathValidator.SecurityThreat.HIGH -> "Audio file format not supported"
                                AudioPathValidator.SecurityThreat.MEDIUM -> "Audio file path is invalid"
                                AudioPathValidator.SecurityThreat.LOW -> "Audio file is unavailable"
                            },
                            isLoaded = false,
                            isPlaying = false
                        ) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Failed to load audio"
                ) }
            }
        }
    }
    
    /**
     * Internal method to load audio after path validation has passed
     */
    private suspend fun loadValidatedAudio(validatedFilePath: String, noteId: Long, autoPlay: Boolean = false) {
        try {
            // Stop any currently playing audio
            if (_uiState.value.isPlaying) {
                audioPlayer.pause()
                onStopProgressUpdates()
            }
            
            val duration = audioPlayer.prepare(validatedFilePath)
            val currentSpeed = _uiState.value.playbackSpeed
            audioPlayer.setPlaybackSpeed(currentSpeed)
            
            // Extract waveform data in parallel
            val amplitudes = waveformExtractor.extractAmplitudesForDuration(validatedFilePath, duration)
            
            _uiState.update { it.copy(
                isLoaded = true,
                duration = duration,
                isPlaying = false,
                currentPosition = 0,
                filePath = validatedFilePath,
                currentPlayingNoteId = noteId,
                waveformAmplitudes = amplitudes,
                errorMessage = null // Clear any previous errors
            ) }
            
            // Auto-play if requested
            if (autoPlay) {
                onPlay()
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                errorMessage = e.message ?: "Failed to load audio",
                isLoaded = false,
                isPlaying = false
            ) }
        }
    }

    fun onTogglePlayPause(noteId: Long) {
        val currentState = _uiState.value
        
        // Only allow play/pause if this note is the currently loaded note
        if (currentState.currentPlayingNoteId == noteId) {
            if (currentState.isPlaying) {
                onPause()
            } else {
                onPlay()
            }
        }
    }

    private fun onPlay() {
        audioPlayer.play()
        _uiState.update { it.copy(isPlaying = true) }
        onStartProgressUpdates()
    }

    private fun onPause() {
        audioPlayer.pause()
        _uiState.update { it.copy(isPlaying = false) }
        onStopProgressUpdates()
    }

    fun onSeekTo(position: Int) {
        audioPlayer.seekTo(position)
        _uiState.update { it.copy(currentPosition = position) }
    }


    private fun onStartProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                val currentPosition = audioPlayer.getCurrentPosition()
                val duration = _uiState.value.duration

                _uiState.update { it.copy(currentPosition = currentPosition) }

                if (duration > 0 && currentPosition >= (duration - 300)) {
                    audioPlayer.pause()
                    audioPlayer.seekTo(0)
                    _uiState.update { it.copy(isPlaying = false, currentPosition = 0) }
                    onStopProgressUpdates()
                    break
                }

                delay(100)
            }
        }
    }

    private fun onStopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    internal fun releasePlayer() = viewModelScope.launch{
        audioPlayer.release()
    }

    fun onClear(){
        onStopProgressUpdates()
        audioPlayer.release()
        viewModelScope.cancel()
    }



    /**
     * Call this method when the ViewModel is no longer needed
     * to clean up resources and cancel ongoing jobs
     */
    override fun onCleared() {
      onClear()
    }
}

/**
 * Data class representing the UI state of the audio player
 */
@Stable
data class AudioPlayerPresentationState(
    val isLoaded: Boolean = false,
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val duration: Int = 0,
    val errorMessage: String? = null,
    val filePath: String = "",
    val playbackSpeed: Float = 1.0f,
    val currentPlayingNoteId: Long? = null,
    val waveformAmplitudes: List<Float> = emptyList()
)
