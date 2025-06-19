package com.module.notelycompose.platform

interface Platform {
    val name: String
    val isAndroid: Boolean
    val appVersion: String
}

expect fun getPlatform(): Platform