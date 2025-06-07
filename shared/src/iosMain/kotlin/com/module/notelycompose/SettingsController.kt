package com.module.notelycompose

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.module.notelycompose.notes.ui.settings.SettingsScreen
import com.module.notelycompose.notes.ui.theme.MyApplicationTheme

fun SettingsController(
    onNavigateBack: () -> Unit
) = ComposeUIViewController {
    MyApplicationTheme {
        val appModule = remember { AppModule() }
        val platformViewmodel = remember {
            IOSPlatformViewModel(
                platformInfo = appModule.platformInfo,
                platformUtils = appModule.platformUtils
            )
        }
        val platformState by platformViewmodel.state.collectAsState()
        SettingsScreen(
            onNavigateBack = {
                onNavigateBack()
            },
            selectedTheme = platformState.selectedTheme,
            selectedLanguage = platformState.selectedLanguage,
            onThemeSelected = platformViewmodel::changeTheme,
            onLanguageClicked = {platformViewmodel.setDefaultTranscriptionLanguage(it.first)}
        )
    }
}
