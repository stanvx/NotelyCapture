package com.module.notelycompose

import com.module.notelycompose.platform.IOSPlatform
import com.module.notelycompose.platform.PlatformUtils

class PlatformModule {


    val platformUtils: PlatformUtils by lazy {
        PlatformUtils()
    }
    val platformInfo: IOSPlatform by lazy {
        IOSPlatform()
    }


}
