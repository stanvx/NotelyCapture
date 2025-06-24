package com.module.notelycompose.platform

import com.module.notelycompose.whisper.DownloadDelegate
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration

actual class Downloader {
    private var backgroundSession: NSURLSession? = null
    private var downloadDelegate: DownloadDelegate? = null



    actual suspend fun startDownload(url: String, fileName: String) {
        val (session, delegate) = createDownloadSession(fileName)
        backgroundSession = session
        downloadDelegate = delegate
        val task = backgroundSession?.downloadTaskWithURL(url = NSURL(string = url))
        task?.resume()
    }

    actual suspend fun hasRunningDownload(): Boolean {
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