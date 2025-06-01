package com.module.notelycompose

import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.Transcriber

class TranscriptionModule {

    val mTranscriber: Transcriber by lazy {
        Transcriber()
    }

    val downloader: Downloader by lazy {
        Downloader()
    }


}
