package com.module.notelycompose

import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.Transcriper

class TranscriptionModule {

    val transcriper: Transcriper by lazy {
        Transcriper()
    }

    val downloader: Downloader by lazy {
        Downloader()
    }


}
