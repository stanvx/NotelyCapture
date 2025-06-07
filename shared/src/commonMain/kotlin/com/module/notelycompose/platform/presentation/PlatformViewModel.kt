package com.module.notelycompose.platform.presentation

import com.module.notelycompose.Platform
import com.module.notelycompose.audio.ui.expect.PlatformUtils
import com.module.notelycompose.audio.ui.expect.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PlatformViewModel (
    private val platformInfo: Platform,
    private val platformUtils: PlatformUtils,
    coroutineScope: CoroutineScope? = null
) {
    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(PlatformUiState(selectedTheme = getSelectedTheme(),
        selectedLanguage = platformUtils.getDefaultTranscriptionLanguage()))
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


    fun changePlatformTheme(theme: Theme) {
        platformUtils.applyTheme(theme)
        _state.update { it.copy(selectedTheme = theme) }
    }

    fun setDefaultTranscriptionLanguage(languageCode: String) {
        _state.update { it.copy(selectedLanguage = languageCode) }
        platformUtils.setDefaultTranscriptionLanguage(languageCode)
    }

    private fun getSelectedTheme(): Theme {
        return platformUtils.getSelectedTheme()
    }

     fun shareText(text: String) {
         if (text.isNotBlank())
             platformUtils.shareText(text)
    }

     fun shareRecording(path: String) {
         if (path.isNotBlank())
         platformUtils.shareRecording(path)
    }
}

data class PlatformUiState(
    val appVersion: String = "",
    val platformName: String = "",
    val isAndroid: Boolean = false,
    val selectedTheme: Theme,
    val selectedLanguage: String,
    val isTablet: Boolean = false,
    val isLandscape: Boolean = false
)