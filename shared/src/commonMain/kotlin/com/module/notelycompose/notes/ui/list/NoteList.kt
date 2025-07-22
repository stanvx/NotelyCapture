package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.list.model.NoteUiModel

@Composable
fun NoteList(
    noteList: List<NoteUiModel>,
    onNoteClicked: (Long) -> Unit,
    onNoteDeleteClicked: (NoteUiModel) -> Unit,
    lazyStaggeredGridState: LazyStaggeredGridState
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
        state = lazyStaggeredGridState,
        modifier = Modifier.padding(top = 8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            bottom = 88.dp // Extra padding for FAB
        )
    ) {
        itemsIndexed(items = noteList) { index, note ->
            ModernNoteItem(
                note = note,
                onNoteClick = { noteId ->
                    onNoteClicked(noteId)
                },
                onDeleteClick = { noteId ->
                    onNoteDeleteClicked(note)
                },
                index = index // Pass index for staggered animation
            )
        }
    }
}
