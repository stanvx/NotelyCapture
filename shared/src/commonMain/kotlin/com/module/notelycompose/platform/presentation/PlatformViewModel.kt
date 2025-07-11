package com.module.notelycompose.platform.presentation

import androidx.lifecycle.ViewModel
import com.module.notelycompose.platform.Platform
import com.module.notelycompose.platform.PlatformUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock

class PlatformViewModel (
    private val platformInfo: Platform,
    private val platformUtils: PlatformUtils
) :ViewModel(){
    private val _state = MutableStateFlow(PlatformUiState())
    val state: StateFlow<PlatformUiState> = _state

    init {
        loadAppInfo()
    }

    private fun loadAppInfo() {
        _state.value = _state.value.copy(
            appVersion = platformInfo.appVersion,
            platformName = platformInfo.name,
            isAndroid = platformInfo.isAndroid,
            isTablet = platformInfo.isTablet,
            isLandscape = platformInfo.isLandscape
        )
    }

    fun shareText(text: String) {
         if (text.isNotBlank()) {
             platformUtils.shareText(text)
         }
    }

    fun shareRecording(path: String) {
         if (path.isNotBlank()) {
             platformUtils.shareRecording(path)
         }
    }

    fun onExportAudio(path: String) {
        if (path.isNotBlank()) {
            val defaultFileName = "recording_${Clock.System.now().toEpochMilliseconds()}.wav"
            _state.value = _state.value.copy(isExporting = true)

            platformUtils.exportRecordingWithFilePicker(
                sourcePath = path,
                fileName = defaultFileName
            ) { success, message ->
                _state.value = _state.value.copy(
                    isExporting = false,
                    exportSuccess = success,
                    exportMessage = message ?: if (success) "Audio exported successfully" else "Failed to export audio"
                )
            }
        }
    }

    fun clearExportStatus() {
        _state.value = _state.value.copy(
            exportSuccess = null,
            exportMessage = null
        )
    }
}

data class PlatformUiState(
    val appVersion: String = "",
    val platformName: String = "",
    val isAndroid: Boolean = false,
    val isTablet: Boolean = false,
    val isLandscape: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean? = null,
    val exportMessage: String? = null
)