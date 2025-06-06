package com.module.notelycompose.modelDownloader

data class DownloaderUiState(
    val fileName:String ,
    val downloading:Boolean = false,
    val progress: Float = 0f,
    val downloaded: String = "0 MB ",
    val total: String = "0 MB"
)

sealed class DownloaderEffect() {
     class DownloadEffect : DownloaderEffect()
    class ModelsAreReady:DownloaderEffect()
    class AskForUserAcceptance:DownloaderEffect()
     class ErrorEffect : DownloaderEffect()
    class CheckingEffect : DownloaderEffect()
}