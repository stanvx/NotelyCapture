package audio.launcher

import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIPresentationController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeContent
import platform.darwin.NSObject

internal class IOSAudioPickerLauncher() {

    private var launcherCallback: ((String?) -> Unit)? = null

    @OptIn(ExperimentalForeignApi::class)
    private val pickerDelegate = object : NSObject(),
        UIDocumentPickerDelegateProtocol,
        UIAdaptivePresentationControllerDelegateProtocol {

        override fun documentPicker(
            controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>
        ) {
            val urls = didPickDocumentsAtURLs.filterIsInstance<NSURL>()
            val firstUrl = urls.firstOrNull()

            val fileManager = platform.Foundation.NSFileManager.defaultManager
            val sandboxDir = fileManager.URLsForDirectory(
                directory = NSCachesDirectory,
                inDomains = NSUserDomainMask
            ).first() as NSURL

            firstUrl?.let { nsUrl ->
                if (nsUrl.startAccessingSecurityScopedResource()) {
                    try {
                        val fileName = nsUrl.lastPathComponent ?: "imported_audio.mp3"
                        val destinationUrl = sandboxDir.URLByAppendingPathComponent(fileName)

                        // Remove if already exists
                        if (fileManager.fileExistsAtPath(destinationUrl?.path!!)) {
                            fileManager.removeItemAtURL(destinationUrl, null)
                        }

                        // Copy file to sandbox
                        fileManager.copyItemAtURL(nsUrl, destinationUrl, null)

                        launcherCallback?.invoke(destinationUrl.path)
                    } catch (e: Exception) {
                        Napier.e("❌ File copy failed: ${e.message}")
                        launcherCallback?.invoke(null)
                    } finally {
                        nsUrl.stopAccessingSecurityScopedResource()
                    }
                } else {
                    Napier.e("❌ Cannot access security-scoped file")
                    launcherCallback?.invoke(null)
                }
            } ?: launcherCallback?.invoke(null)
        }

        override fun documentPickerWasCancelled(
            controller: UIDocumentPickerViewController
        ) {
            launcherCallback?.invoke(null)
        }

        override fun presentationControllerWillDismiss(
            presentationController: UIPresentationController
        ) {
            (presentationController.presentedViewController as? UIDocumentPickerViewController)
                ?.let { documentPickerWasCancelled(it) }
        }
    }

    private val contentTypes: List<UTType>
        get() = listOf("aac", "aiff", "caf","flac", "wma", "m4a", "mp3", "wav")
            .mapNotNull { UTType.typeWithFilenameExtension(it) }
            .ifEmpty { listOf(UTTypeContent) }

    fun launch(onResult: (String?) -> Unit) {
        this.launcherCallback = onResult
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = contentTypes)
        picker.delegate = pickerDelegate
        UIApplication.Companion.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            picker,
            animated = true,
            completion = {
                picker.allowsMultipleSelection = false
            },
        )
    }
}