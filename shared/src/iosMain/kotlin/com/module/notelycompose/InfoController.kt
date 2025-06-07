package com.module.notelycompose

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.module.notelycompose.notes.ui.list.InfoBottomSheet
import com.module.notelycompose.notes.ui.theme.MyApplicationTheme

fun InfoController(
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
        val navigateToWebPage: (String, String) -> Unit = { title, url -> }
        InfoBottomSheet(
            onNavigateBack = {
                onNavigateBack()
            },
            onNavigateToWebPage = navigateToWebPage,
            appVersion = platformState.appVersion
        )
    }
}
