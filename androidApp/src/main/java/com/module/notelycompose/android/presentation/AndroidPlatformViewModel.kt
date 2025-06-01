package com.module.notelycompose.android.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.Platform
import com.module.notelycompose.audio.ui.expect.PlatformUtils
import com.module.notelycompose.audio.ui.expect.Theme
import com.module.notelycompose.platform.presentation.PlatformViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidPlatformViewModel @Inject constructor(
    private val platformInfo: Platform,
    private val platformUtils: PlatformUtils
) : ViewModel() {

    private val viewModel by lazy {
        PlatformViewModel(
            platformUtils = platformUtils,
            platformInfo = platformInfo,
            coroutineScope = viewModelScope
        )
    }
    val state = viewModel.state

    fun changeTheme(theme: Theme) {
        viewModel.changePlatformTheme(theme)
    }

    fun setDefaultTranscriptionLanguage(languageCode: String) {
        viewModel.setDefaultTranscriptionLanguage(languageCode)
    }

    fun shareText(text: String) {
        return viewModel.shareText(text)
    }

    fun shareRecording(path: String) {
        return viewModel.shareRecording(path)
    }


}
