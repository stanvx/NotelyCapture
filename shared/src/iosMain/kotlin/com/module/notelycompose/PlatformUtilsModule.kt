package com.module.notelycompose

import com.module.notelycompose.audio.ui.expect.Downloader
import com.module.notelycompose.audio.ui.expect.PlatformUtils
import com.module.notelycompose.audio.ui.expect.Transcriber

class PlatformModule {


    val platformUtils: PlatformUtils by lazy {
        PlatformUtils()
    }
    val platformInfo: IOSPlatform by lazy {
        IOSPlatform()
    }


}
