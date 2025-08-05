package com.module.notelycompose.notes.domain.interfaces

import com.module.notelycompose.notes.domain.model.TextAlignDomainModel
import com.module.notelycompose.notes.domain.model.TextFormatDomainModel

/**
 * Interface for inserting a new note into the repository.
 * This contract defines the business logic for note creation operations.
 */
interface InsertNoteUseCaseContract {
    /**
     * Execute the use case to insert a new note
     * @param title The title of the note
     * @param content The content of the note
     * @param starred Whether the note is starred
     * @param formatting List of text formatting options
     * @param textAlign Text alignment setting
     * @param recordingPath Path to associated audio recording
     */
    suspend fun execute(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextFormatDomainModel>,
        textAlign: TextAlignDomainModel,
        recordingPath: String
    )
}