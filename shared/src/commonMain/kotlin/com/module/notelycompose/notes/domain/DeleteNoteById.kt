package com.module.notelycompose.notes.domain

import com.module.notelycompose.notes.domain.interfaces.DeleteNoteByIdUseCaseContract

class DeleteNoteById(
    private val noteDataSource: NoteDataSource
) : DeleteNoteByIdUseCaseContract {
    override suspend fun execute(id: Long) {
        return noteDataSource.deleteNoteById(id)
    }
}