package com.module.notelycompose.platform

import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene


actual class PlatformUtils{
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    actual fun applyTheme(theme: Theme) {
        userDefaults.setObject(theme.name, "theme")
        val style = when (theme) {
            Theme.LIGHT -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
            Theme.DARK -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
            Theme.SYSTEM -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
        }


        UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { (it.windows() as List<UIWindow>) }
            .forEach { it.overrideUserInterfaceStyle = style }
    }

    actual fun setDefaultTranscriptionLanguage(languageCode: String) {
        userDefaults.setObject(languageCode, "language")
    }

    actual fun getSelectedTheme(): Theme {
        return when (userDefaults.stringForKey("theme")) {
            "LIGHT" -> Theme.LIGHT
            "DARK" -> Theme.DARK
            else -> Theme.SYSTEM
        }
    }

    actual fun getDefaultTranscriptionLanguage(): String {
        return userDefaults.stringForKey("language") ?: "auto"
    }

    actual fun shareText(text: String) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    }

    actual fun shareRecording(path: String) {
        val fileUrl = NSURL.fileURLWithPath(path)
        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    }
}