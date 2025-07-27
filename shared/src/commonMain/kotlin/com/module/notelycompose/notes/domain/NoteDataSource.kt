package com.module.notelycompose.notes.domain

import com.module.notelycompose.core.CommonFlow
import com.module.notelycompose.notes.data.model.NoteDataModel
import com.module.notelycompose.notes.data.model.TextAlignDataModel
import com.module.notelycompose.notes.data.model.TextFormatDataModel
import com.module.notelycompose.notes.domain.model.Note


interface NoteDataSource {
    suspend fun insertNote(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextFormatDataModel>,
        textAlign: TextAlignDataModel,
        recordingPath: String
    ): Long?

    suspend fun updateNote(
        id: Long,
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextFormatDataModel>,
        textAlign: TextAlignDataModel,
        recordingPath: String
    )

    fun getNotes(): CommonFlow<List<NoteDataModel>>
    fun getStarredNotes(): CommonFlow<List<NoteDataModel>>
    fun getVoiceNotes(): CommonFlow<List<NoteDataModel>>
    fun getNotesByKeyword(keyword: String): CommonFlow<List<NoteDataModel>>
    fun getNoteById(id: Long): NoteDataModel?
    fun getLastNote(): NoteDataModel?
    fun getLastNoteId(): Long?
    suspend fun deleteNoteById(id: Long)
    
    // Optimized search methods that combine filtering and search at database level
    fun searchNotes(query: String): CommonFlow<List<NoteDataModel>>
    fun searchStarredNotes(query: String): CommonFlow<List<NoteDataModel>>
    fun searchVoiceNotes(query: String): CommonFlow<List<NoteDataModel>>
    
    // Pagination support methods
    fun getNotesPaged(limit: Long, offset: Long): CommonFlow<List<NoteDataModel>>
    suspend fun getNotesCount(): Long
}
