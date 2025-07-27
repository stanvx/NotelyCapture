package com.module.notelycompose.notes.domain


import com.module.notelycompose.core.CommonFlow
import com.module.notelycompose.core.asFlow
import com.module.notelycompose.core.toCommonFlow
import com.module.notelycompose.notes.domain.mapper.NoteDomainMapper
import com.module.notelycompose.notes.domain.model.NoteDomainModel
import kotlinx.coroutines.flow.map

/**
 * @deprecated Search functionality has been removed from the UI.
 * This use case is kept for backward compatibility but should not be used in new code.
 * Will be removed in a future version.
 */
@Deprecated(
    message = "Search functionality has been removed from the UI",
    level = DeprecationLevel.WARNING
)
class SearchNotesUseCase(
    private val noteDataSource: NoteDataSource,
    private val noteDomainMapper: NoteDomainMapper
) {
    fun execute(keyword: String): CommonFlow<List<NoteDomainModel>> {
        return noteDataSource.getNotesByKeyword(keyword).asFlow().map { notes ->
            notes.map { noteDataModel ->
                noteDomainMapper.mapToDomainModel(noteDataModel)
            }
        }.toCommonFlow()
    }
}
