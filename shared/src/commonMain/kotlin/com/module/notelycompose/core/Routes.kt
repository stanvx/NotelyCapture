package com.module.notelycompose.core

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes {
    @Serializable
    data object Home : Routes

    @Serializable
    data object List : Routes

    @Serializable
    data object DetailsGraph : Routes

    @Serializable
    data class Details(val noteId: String?) : Routes

    @Serializable
    data class Recorder(val noteId: String?) : Routes

    @Serializable
    data object QuickRecord : Routes

    @Serializable
    data object Web : Routes

    @Serializable
    data object Transcription : Routes

    @Serializable
    data object Share : Routes

    @Serializable
    data object Settings : Routes

    @Serializable
    data object Language : Routes

    @Serializable
    data object Menu : Routes

    @Serializable
    data object Downloader : Routes

}