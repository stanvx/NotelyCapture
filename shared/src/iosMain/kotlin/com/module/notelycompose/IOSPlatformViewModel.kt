package com.module.notelycompose

import com.module.notelycompose.audio.ui.expect.PlatformUtils
import com.module.notelycompose.audio.ui.expect.Theme
import com.module.notelycompose.platform.presentation.PlatformViewModel

class IOSPlatformViewModel (
    private val platformInfo: Platform,
    private val platformUtils: PlatformUtils
) {
    private val viewModel by lazy {
        PlatformViewModel(
            platformInfo = platformInfo,
            platformUtils = platformUtils
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
