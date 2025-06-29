package com.module.notelycompose.platform

expect class Downloader {
    suspend fun startDownload(url: String, fileName: String)
     suspend fun hasRunningDownload():Boolean
   suspend fun trackDownloadProgress(
       fileName: String,
        onProgressUpdated: (progress: Int, downloadedMB: String, totalMB: String) -> Unit,
        onSuccess:()->Unit,
        onFailed:(String)->Unit
    )
}
