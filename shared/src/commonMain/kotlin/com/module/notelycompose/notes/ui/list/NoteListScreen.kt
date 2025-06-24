package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.presentation.list.NoteListIntent
import com.module.notelycompose.notes.presentation.list.NoteListViewModel
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.presentation.PlatformUiState
import kotlinx.coroutines.launch
import notelycompose.shared.generated.resources.Res
import notelycompose.shared.generated.resources.note_list_add_note
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NoteListScreen(
    navigateToSettings: () -> Unit,
    navigateToMenu: () -> Unit,
    navigateToNoteDetails: (String) -> Unit,
    viewModel: NoteListViewModel = koinViewModel(),
    platformUiState: PlatformUiState
) {
    val notesListState by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
            topBar = {
                TopBar(
                    onMenuClicked = {
                       navigateToMenu()
                    },
                    onSettingsClicked = {
                      navigateToSettings()
                    }
                )
            },
            isFloatingActionButtonDocked = true,
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navigateToNoteDetails("0")
                    },
                    backgroundColor = LocalCustomColors.current.backgroundViewColor
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Icon(
                            modifier = Modifier.padding(4.dp),
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.note_list_add_note),
                            tint = LocalCustomColors.current.floatActionButtonIconColor
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(LocalCustomColors.current.bodyBackgroundColor)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
            ) {
                SearchBar(
                    onSearchByKeyword = { keyword ->
                        viewModel.onProcessIntent(NoteListIntent.OnSearchNote(keyword))
                    }
                )
                FilterTabBar(
                    selectedTabTitle = notesListState.selectedTabTitle,
                    onFilterTabItemClicked = { title ->
                        viewModel.onProcessIntent(NoteListIntent.OnFilterNote(title))
                    }
                )
                NoteList(
                    noteList = viewModel.onGetUiState(notesListState),
                    onNoteClicked = { id ->
                        navigateToNoteDetails("$id")
                    },
                    onNoteDeleteClicked = {
                        viewModel.onProcessIntent(NoteListIntent.OnNoteDeleted(it))
                    }
                )
                if(notesListState.showEmptyContent) EmptyNoteUi(platformUiState.isTablet)
            }
        }

}
