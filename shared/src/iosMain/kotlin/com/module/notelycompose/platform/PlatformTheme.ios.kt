package com.module.notelycompose.platform

import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.popoverPresentationController

actual class PlatformUtils {

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

    // iOS implementation using UIActivityViewController for sharing/exporting
    // iOS uses a unified approach where UIActivityViewController handles both sharing to other apps AND saving to locations like the Files app.
    @OptIn(ExperimentalForeignApi::class)
    actual fun exportRecordingWithFilePicker(
        sourcePath: String,
        fileName: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            val sourceUrl = NSURL.fileURLWithPath(sourcePath)
            val sourceData = NSData.dataWithContentsOfURL(sourceUrl)
            if (sourceData == null) {
                onResult(false, "Source file not found")
                return
            }
            val activityController = UIActivityViewController(
                activityItems = listOf(sourceUrl),
                applicationActivities = null
            )
            activityController.popoverPresentationController?.let { popover ->
                // Set source view if available, otherwise center
                val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                popover.sourceView = rootViewController?.view
                popover.sourceRect = CGRectMake(0.0, 0.0, 1.0, 1.0)
            }
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                activityController,
                animated = true,
                completion = null
            )

            onResult(true, "Export options presented")
        } catch (e: Exception) {
            onResult(false, "Export failed: ${e.message}")
        }
    }

    actual fun requestStoragePermission(): Boolean {
        // iOS doesn't require explicit storage permissions like Android
        return true
    }
}
