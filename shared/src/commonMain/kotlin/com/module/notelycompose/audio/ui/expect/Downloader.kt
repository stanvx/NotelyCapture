package com.module.notelycompose.audio.ui.expect

import kotlinx.coroutines.CoroutineScope

expect class Downloader {
     fun startDownload(url: String, fileName: String)
     fun hasRunningDownload():Boolean
   suspend fun trackDownloadProgress(
       fileName: String,
        onProgressUpdated: (progress: Int, downloadedMB: String, totalMB: String) -> Unit,
        onSuccess:()->Unit,
        onFailed:(String)->Unit
    )
}
