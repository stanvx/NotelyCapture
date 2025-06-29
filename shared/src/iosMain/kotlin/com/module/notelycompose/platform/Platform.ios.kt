package com.module.notelycompose.platform

import platform.UIKit.UIDevice
import platform.Foundation.NSBundle
import platform.UIKit.UIApplication

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val isAndroid: Boolean get() = isAndroid()
    override val appVersion: String get() {
        return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "Unknown"
    }
    override val isTablet: Boolean get() = isTablet()
    override val isLandscape: Boolean get() = isDeviceLandscape()

    private fun isAndroid(): Boolean {
        val systemName = UIDevice.currentDevice.systemName.lowercase()
        val keywords = listOf("ios", "ipados")
        return keywords.any { systemName.contains(it) }.not()
    }

    private fun isTablet(): Boolean {
        val idiom = UIDevice.currentDevice.userInterfaceIdiom
        return idiom.toInt() == 1
    }

    private fun isDeviceLandscape(): Boolean {
        val orientation = UIApplication.sharedApplication.statusBarOrientation
        return orientation.toInt() == 3 || orientation.toInt() == 4 // Landscape left/right
    }
}

actual fun getPlatform(): Platform = IOSPlatform()