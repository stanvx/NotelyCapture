package com.module.notelycompose.notes.domain.interfaces

import com.module.notelycompose.notes.domain.model.TextAlignDomainModel
import com.module.notelycompose.notes.domain.model.TextFormatDomainModel

/**
 * Interface for updating an existing note in the repository.
 * This contract defines the business logic for note update operations.
 */
interface UpdateNoteUseCase {
    /**
     * Execute the use case to update an existing note
     * @param id The ID of the note to update
     * @param title The updated title
     * @param content The updated content
     * @param starred Whether the note is starred
     * @param formatting List of text formatting options
     * @param textAlign Text alignment setting
     * @param recordingPath Path to associated audio recording
     */
    suspend fun execute(
        id: Long,
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextFormatDomainModel>,
        textAlign: TextAlignDomainModel,
        recordingPath: String
    )
}