package com.module.notelycompose

import com.module.notelycompose.platform.Platform
import com.module.notelycompose.platform.getPlatform

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${"Lorem Ipsum"}!"
    }
}