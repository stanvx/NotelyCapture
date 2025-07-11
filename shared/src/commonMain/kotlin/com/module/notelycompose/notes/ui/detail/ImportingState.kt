package com.module.notelycompose.notes.ui.detail

sealed interface ImportingState {
    object Idle : ImportingState
    object Importing : ImportingState
}