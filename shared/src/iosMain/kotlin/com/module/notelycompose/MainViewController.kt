package com.module.notelycompose

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.compose.KoinApplication
import com.module.notelycompose.di.init
fun MainViewController() = ComposeUIViewController {
    KoinApplication(application = {
        init()
    }) {
        App()
    }
}

