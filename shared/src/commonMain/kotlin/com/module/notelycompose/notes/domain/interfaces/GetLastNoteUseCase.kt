package com.module.notelycompose.notes.domain.interfaces

import com.module.notelycompose.notes.domain.model.NoteDomainModel

/**
 * Interface for retrieving the most recently created note.
 * This contract defines the business logic for fetching the last note.
 */
interface GetLastNoteUseCase {
    /**
     * Execute the use case to get the most recent note
     * @return The most recent note if any exists, null otherwise
     */
    fun execute(): NoteDomainModel?
}