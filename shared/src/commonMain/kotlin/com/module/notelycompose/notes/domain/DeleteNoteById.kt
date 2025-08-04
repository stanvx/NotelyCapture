package com.module.notelycompose.notes.domain

import com.module.notelycompose.notes.domain.interfaces.DeleteNoteByIdUseCase

class DeleteNoteById(
    private val noteDataSource: NoteDataSource
) : DeleteNoteByIdUseCase {
    override suspend fun execute(id: Long) {
        return noteDataSource.deleteNoteById(id)
    }
}