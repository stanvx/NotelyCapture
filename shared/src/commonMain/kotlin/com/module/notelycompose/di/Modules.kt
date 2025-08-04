package com.module.notelycompose.di


import com.module.notelycompose.audio.domain.AmplitudeCollector
import com.module.notelycompose.audio.domain.AudioWaveformExtractor
import com.module.notelycompose.audio.presentation.AudioPlayerViewModel
import com.module.notelycompose.audio.presentation.AudioRecorderViewModel
import com.module.notelycompose.audio.presentation.mappers.AudioPlayerPresentationToUiMapper
import com.module.notelycompose.audio.presentation.mappers.AudioRecorderPresentationToUiMapper
import com.module.notelycompose.database.NoteDatabase
import com.module.notelycompose.modelDownloader.ModelAvailabilityService
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel
import com.module.notelycompose.notes.data.NoteSqlDelightDataSource
import com.module.notelycompose.notes.domain.DeleteNoteById
import com.module.notelycompose.notes.domain.GetAllNotesUseCase
import com.module.notelycompose.notes.domain.GetLastNote
import com.module.notelycompose.notes.domain.GetNoteById
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.NoteDataSource
import com.module.notelycompose.notes.domain.SearchNotesUseCase
import com.module.notelycompose.notes.domain.SearchSuggestionProvider
import com.module.notelycompose.notes.domain.SearchHistoryManager
import com.module.notelycompose.notes.domain.SearchHistoryDataSource
import com.module.notelycompose.notes.domain.TextContentPredictor
import com.module.notelycompose.notes.domain.LanguageAwareAutoComplete
import com.module.notelycompose.notes.data.SearchHistoryDataSourceImpl
import com.module.notelycompose.notes.domain.UpdateNoteUseCase
import com.module.notelycompose.notes.domain.mapper.NoteDomainMapper
import com.module.notelycompose.notes.domain.mapper.TextFormatMapper
import com.module.notelycompose.audio.presentation.AudioImportViewModel
import com.module.notelycompose.notes.presentation.detail.NoteDetailScreenViewModel
import com.module.notelycompose.notes.presentation.detail.TextEditorViewModel
import com.module.notelycompose.notes.presentation.helpers.TextEditorHelper
import com.module.notelycompose.notes.presentation.helpers.RichTextEditorHelper
import com.module.notelycompose.notes.presentation.list.NoteListViewModel
import com.module.notelycompose.notes.presentation.list.mapper.NotesFilterMapper
import com.module.notelycompose.notes.presentation.mapper.EditorPresentationToUiStateMapper
import com.module.notelycompose.notes.presentation.mapper.NotePresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextAlignPresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextFormatPresentationMapper
import com.module.notelycompose.notes.presentation.settings.LanguageSelectionViewModel
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.onboarding.presentation.OnboardingViewModel
import com.module.notelycompose.platform.presentation.PlatformViewModel
import com.module.notelycompose.transcription.BackgroundTranscriptionService
import com.module.notelycompose.transcription.TranscriptionViewModel
import com.module.notelycompose.transcription.data.repository.TranscriptionRepositoryImpl
import com.module.notelycompose.transcription.domain.repository.TranscriptionRepository
import com.module.notelycompose.transcription.domain.WhisperModelManager
import com.module.notelycompose.transcription.domain.WhisperModelLoader
import com.module.notelycompose.core.security.SecurityHelper
import com.module.notelycompose.core.security.SecurityMonitoringService
import com.module.notelycompose.openai.data.repository.OpenAIRepositoryImpl
import com.module.notelycompose.openai.domain.repository.OpenAIRepository
import com.module.notelycompose.openai.domain.usecase.SummarizeTextUseCase
import com.module.notelycompose.openai.domain.usecase.TranscribeAudioUseCase
import com.module.notelycompose.summary.TFIDFSummarizer
import com.module.notelycompose.core.security.AiSettingsRepository
import com.module.notelycompose.core.security.SecurePreferencesRepository
import com.module.notelycompose.notes.presentation.settings.AISettingsViewModel
import com.module.notelycompose.notes.domain.interfaces.DeleteNoteByIdUseCase
import com.module.notelycompose.notes.domain.interfaces.GetAllNotesUseCase
import com.module.notelycompose.notes.domain.interfaces.GetLastNoteUseCase
import com.module.notelycompose.notes.domain.interfaces.GetNoteByIdUseCase
import com.module.notelycompose.notes.domain.interfaces.InsertNoteUseCase
import com.module.notelycompose.notes.domain.interfaces.UpdateNoteUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


internal expect val platformModule: Module

val appModule = module {

    single<NoteDataSource> {
        NoteSqlDelightDataSource(
            database = NoteDatabase(get())
        )
    }
    
    single<SearchHistoryDataSource> {
        SearchHistoryDataSourceImpl(get())
    }

}

val mapperModule = module {
    single { EditorPresentationToUiStateMapper() }
    single { AudioPlayerPresentationToUiMapper() }
    single { AudioRecorderPresentationToUiMapper() }
    single { NoteDomainMapper(get()) }
    single { TextFormatMapper() }
    single { NotesFilterMapper() }
    single { NotePresentationMapper(get()) }
    single { TextFormatPresentationMapper() }
    single { TextAlignPresentationMapper() }
    single { TextEditorHelper(get()) }
    single { RichTextEditorHelper() }
    single { AmplitudeCollector() }
    single { AudioWaveformExtractor() }
}
val repositoryModule = module {
    singleOf(::PreferencesRepository)
    single { WhisperModelManager(get()) }
    single<TranscriptionRepository> { TranscriptionRepositoryImpl(get(), get()) }
    single { SearchHistoryManager(get()) }
    single { SearchSuggestionProvider(get(), get()) }
    single { TextContentPredictor(get()) }
    single { LanguageAwareAutoComplete(get()) }
    single { AiSettingsRepository(get(), get()) }
    
    // OpenAI Integration
    single { com.module.notelycompose.openai.data.cache.OpenAIResponseCache() }
    single { com.module.notelycompose.openai.domain.analytics.OpenAIAnalytics() }
    single<OpenAIRepository> { 
        OpenAIRepositoryImpl(
            networkConnectivityManager = get(),
            securityHelper = get(),
            responseCache = get(),
            analytics = get()
        )
    }
    single { TFIDFSummarizer() }
}

val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::NoteListViewModel)
    viewModelOf(::PlatformViewModel)
    viewModelOf(::TranscriptionViewModel)
    viewModelOf(::TextEditorViewModel)
    viewModelOf(::NoteDetailScreenViewModel)
    viewModelOf(::ModelDownloaderViewModel)
    viewModelOf(::AudioRecorderViewModel)
    viewModelOf(::AudioPlayerViewModel)
    viewModelOf(::AudioImportViewModel)
    viewModelOf(::LanguageSelectionViewModel)
    viewModelOf(::AISettingsViewModel)
}

val useCaseModule = module {
    // Use case interfaces for testability
    factory<DeleteNoteByIdUseCase> { DeleteNoteById(get()) }
    factory<GetAllNotesUseCase> { com.module.notelycompose.notes.domain.GetAllNotesUseCase(get(), get()) }
    factory<GetLastNoteUseCase> { GetLastNote(get(), get()) }
    factory<GetNoteByIdUseCase> { GetNoteById(get(), get()) }
    factory<InsertNoteUseCase> { com.module.notelycompose.notes.domain.InsertNoteUseCase(get(), get(), get()) }
    factory<UpdateNoteUseCase> { com.module.notelycompose.notes.domain.UpdateNoteUseCase(get(), get(), get()) }
    
    // Other use cases that don't need interface changes yet
    factory { SearchNotesUseCase(get(), get()) }
    factory { ModelAvailabilityService(get(), get()) }
    factory { BackgroundTranscriptionService(get(), get()) }
    
    // OpenAI Use Cases
    factory { TranscribeAudioUseCase(get(), get(), get()) }
    factory { SummarizeTextUseCase(get(), get(), get()) }
}

val securityModule = module {
    // SecurityMonitoringService will be provided by platform-specific modules
    // as it requires platform-specific implementations
    single { SecurityHelper(get<SecurityMonitoringService>()) }
}