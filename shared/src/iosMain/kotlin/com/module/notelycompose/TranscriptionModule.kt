package com.module.notelycompose

import com.module.notelycompose.platform.Downloader
import com.module.notelycompose.platform.Transcriber

class TranscriptionModule {

    val mTranscriber: Transcriber by lazy {
        Transcriber()
    }

    val downloader: Downloader by lazy {
        Downloader()
    }


}
