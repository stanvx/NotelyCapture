package com.module.notelycompose.whisper

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDownloadDelegateProtocol
import platform.Foundation.NSURLSessionDownloadTask
import platform.Foundation.NSURLSessionTask
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithFormat
import platform.darwin.NSObject
import platform.posix.int64_t

class DownloadDelegate(
    private val fileName:String,
): NSObject(), NSURLSessionDownloadDelegateProtocol {

     var onProgressUpdated: ((progress: Int, downloadedMB: String, totalMB: String) -> Unit)? = null
     var onSuccess: (() -> Unit)? = null
     var onFailed: ((String) -> Unit)? = null

    // progress
    override fun URLSession(
        session: NSURLSession,
        downloadTask: NSURLSessionDownloadTask,
        didWriteData: int64_t,
        totalBytesWritten: int64_t,
        totalBytesExpectedToWrite: int64_t
    ) {
        val totalWrittenMB = totalBytesWritten.toDouble() / (1024 * 1024)
        val totalExpectedMB = totalBytesExpectedToWrite.toDouble() / (1024 * 1024)
        val progressPercent = (totalBytesWritten.toDouble() / totalBytesExpectedToWrite.toDouble()) * 100
        onProgressUpdated?.invoke(
            progressPercent.toInt(),
            NSString.stringWithFormat("%.2f MB", totalWrittenMB),
            NSString.stringWithFormat("%.2f MB", totalExpectedMB)

        )
    }

    // Delegate method: called when download finishes
    @OptIn(ExperimentalForeignApi::class)
    override fun URLSession(
        session: NSURLSession,
        downloadTask: NSURLSessionDownloadTask,
        didFinishDownloadingToURL: NSURL
    ) {
        println("Download finished at: ${didFinishDownloadingToURL.path}")

            try {
                val fileManager = NSFileManager.defaultManager
                val documentsDirectory = NSFileManager.defaultManager.URLsForDirectory(
                    NSDocumentDirectory,
                    NSUserDomainMask
                ).first() as NSURL

                documentsDirectory.URLByAppendingPathComponent(fileName)?.let {
                    fileManager.copyItemAtURL(didFinishDownloadingToURL, it, null)
                    println("Writing to $it completed")
                    onSuccess?.invoke()
                }
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
    }

    // Error handler
    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        didCompleteWithError: NSError?
    ) {
        println("Error --------------- ${didCompleteWithError?.description}")
        if(didCompleteWithError != null)
        onFailed?.invoke(didCompleteWithError.description?:"Error")
    }
}