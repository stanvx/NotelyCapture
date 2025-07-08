package com.module.notelycompose.audio.domain

import androidx.compose.ui.text.style.TextAlign
import audio.recorder.AudioRecorder
import com.module.notelycompose.notes.domain.GetNoteById
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.UpdateNoteUseCase
import com.module.notelycompose.notes.presentation.mapper.TextAlignPresentationMapper

internal interface SaveAudioNoteInteractor {
    suspend fun save(
        noteId: Long?,
    )
}

internal class SaveAudioNoteInteractorImpl(
    private val audioRecorder: AudioRecorder,
    private val getNoteByIdUseCase: GetNoteById,
    private val insertNoteUseCase: InsertNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val textAlignPresentationMapper: TextAlignPresentationMapper
) : SaveAudioNoteInteractor {

    override suspend fun save(
        noteId: Long?,
    ) {
        if (noteId == null) {
            insertNote()
        } else {
            updateNote(noteId)
        }
    }

    private suspend fun insertNote() {
        insertNoteUseCase.execute(
            title = "",
            content = "",
            starred = false,
            formatting = emptyList(),
            textAlign = textAlignPresentationMapper.mapToDomainModel(TextAlign.Left),
            recordingPath = audioRecorder.getRecordingFilePath(),
        )
    }

    private suspend fun updateNote(noteId: Long) {
        val note = getNoteByIdUseCase.execute(noteId) ?: return
        updateNoteUseCase.execute(
            id = note.id,
            title = note.title,
            content = note.content,
            starred = note.starred,
            formatting = note.formatting,
            textAlign = note.textAlign,
            recordingPath = audioRecorder.getRecordingFilePath(),
        )
    }
}