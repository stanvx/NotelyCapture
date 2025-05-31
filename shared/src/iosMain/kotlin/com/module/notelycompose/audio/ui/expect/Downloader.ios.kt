package com.module.notelycompose.audio.ui.expect


import com.module.notelycompose.modelDownloader.DownloaderDialog
import com.module.notelycompose.whisper.DownloadDelegate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSKeyValueObservingOptions
import platform.Foundation.NSProgress
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionDownloadTask
import platform.Foundation.NSUserDomainMask
import platform.Foundation.addObserver
import platform.Foundation.downloadTaskWithURL
import whisper.*
actual class Downloader {
    private var backgroundSession: NSURLSession? = null
    private var downloadDelegate: DownloadDelegate? = null



    actual fun startDownload(url: String, fileName: String) {
        val (session, delegate) = createDownloadSession(fileName)
        backgroundSession = session
        downloadDelegate = delegate
        val task = backgroundSession?.downloadTaskWithURL(url = NSURL(string = url))
        task?.resume()
    }

    actual fun hasRunningDownload(): Boolean {
        return false
    }

    actual suspend fun trackDownloadProgress(
        fileName: String,
        onProgressUpdated: (progress: Int, downloadedMB: String, totalMB: String) -> Unit,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        if(backgroundSession == null || downloadDelegate == null) {
            val (session, delegate) = createDownloadSession(fileName)
            backgroundSession = session
            downloadDelegate = delegate
        }
            downloadDelegate?.onProgressUpdated = onProgressUpdated
            downloadDelegate?.onSuccess = onSuccess
            downloadDelegate?.onFailed = onFailed

    }



    private fun createDownloadSession(fileName: String):Pair<NSURLSession, DownloadDelegate>{
        val delegate = DownloadDelegate(fileName)
        val config = NSURLSessionConfiguration.defaultSessionConfiguration()

        return NSURLSession.sessionWithConfiguration(
            configuration = config,
            delegate = delegate,
            delegateQueue = null
        ) to delegate
    }
}