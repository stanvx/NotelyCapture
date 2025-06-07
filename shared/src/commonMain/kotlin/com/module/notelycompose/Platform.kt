package com.module.notelycompose

interface Platform {
    val name: String
    val isAndroid: Boolean
    val appVersion: String
    val isTablet: Boolean
    val isLandscape: Boolean
}

expect fun getPlatform(): Platform