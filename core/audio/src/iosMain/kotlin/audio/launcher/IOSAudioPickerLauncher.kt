package audio.launcher

import platform.Foundation.NSURL
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

    private val pickerDelegate = object : NSObject(),
        UIDocumentPickerDelegateProtocol,
        UIAdaptivePresentationControllerDelegateProtocol {

        override fun documentPicker(
            controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>
        ) {
            val urls = didPickDocumentsAtURLs.filterIsInstance<NSURL>()
            val results = urls.map { nsUrl ->
                nsUrl.path
            }
            results.firstOrNull()?.run { launcherCallback?.invoke(this) }
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
        get() = listOf("wav", "mp3").mapNotNull { UTType.Companion.typeWithFilenameExtension(it) }
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