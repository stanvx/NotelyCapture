package com.module.notelycompose.notes.presentation.list

import com.module.notelycompose.notes.presentation.list.model.NotePresentationModel
import com.module.notelycompose.notes.presentation.list.model.QuickRecordState

data class NoteListPresentationState(
    val originalNotes: List<NotePresentationModel> = emptyList(),
    val filteredNotes: List<NotePresentationModel> = emptyList(),
    val selectedTabTitle: String="All",
    val showEmptyContent: Boolean = false,
    val quickRecordState: QuickRecordState = QuickRecordState.Idle,
    val quickRecordError: String? = null
)
