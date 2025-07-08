package audio.launcher

import audio.AudioFileResult
import audio.utils.savePickedAudioToAppStorage
import platform.Foundation.NSURL
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIPresentationController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeContent
import platform.darwin.NSObject

internal class IOSAudioPickerLauncher(
) : AudioPickerLauncher {

    private var launcherCallback: ((AudioFileResult) -> Unit)? = null

    private val pickerDelegate = object : NSObject(),
        UIDocumentPickerDelegateProtocol,
        UIAdaptivePresentationControllerDelegateProtocol {

        override fun documentPicker(
            controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>
        ) {

            val urls = didPickDocumentsAtURLs.filterIsInstance<NSURL>()
            val results = urls.map { nsUrl ->
                val path = nsUrl.savePickedAudioToAppStorage()?.path
                val name = nsUrl.lastPathComponent
                AudioFileResult(
                    name = name,
                    path = path,
                )
            }
            results.firstOrNull()?.run { launcherCallback?.invoke(this) }
        }

        override fun documentPickerWasCancelled(
            controller: UIDocumentPickerViewController
        ) {
            launcherCallback?.invoke(AudioFileResult(null, null))
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

    override fun launch(onResult: (AudioFileResult) -> Unit) {
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