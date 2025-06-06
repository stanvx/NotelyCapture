package com.module.notelycompose.android.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.module.notelycompose.AndroidPlatform
import com.module.notelycompose.Platform
import com.module.notelycompose.audio.ui.expect.PlatformUtils
import com.module.notelycompose.core.DatabaseDriverFactory
import com.module.notelycompose.database.NoteDatabase
import com.module.notelycompose.notes.data.NoteSqlDelightDataSource
import com.module.notelycompose.notes.domain.DeleteNoteById
import com.module.notelycompose.notes.domain.GetAllNotesUseCase
import com.module.notelycompose.notes.domain.GetLastNote
import com.module.notelycompose.notes.domain.GetNoteById
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.NoteDataSource
import com.module.notelycompose.notes.domain.SearchNotesUseCase
import com.module.notelycompose.notes.domain.UpdateNoteUseCase
import com.module.notelycompose.notes.domain.mapper.NoteDomainMapper
import com.module.notelycompose.notes.domain.mapper.TextFormatMapper
import com.module.notelycompose.notes.presentation.helpers.TextEditorHelper
import com.module.notelycompose.notes.presentation.list.mapper.NotesFilterMapper
import com.module.notelycompose.notes.presentation.mapper.EditorPresentationToUiStateMapper
import com.module.notelycompose.notes.presentation.mapper.NotePresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextAlignPresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextFormatPresentationMapper
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

}
