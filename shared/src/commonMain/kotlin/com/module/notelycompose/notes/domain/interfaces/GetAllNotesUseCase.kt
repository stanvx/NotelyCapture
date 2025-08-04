package com.module.notelycompose.notes.domain.interfaces

import com.module.notelycompose.core.CommonFlow
import com.module.notelycompose.notes.domain.model.NoteDomainModel

/**
 * Interface for retrieving all notes from the repository.
 * This contract defines the business logic for fetching and ordering notes.
 */
interface GetAllNotesUseCase {
    /**
     * Execute the use case to get all notes
     * @return Flow of all notes, sorted by timestamp (most recent first)
     */
    fun execute(): CommonFlow<List<NoteDomainModel>>
}