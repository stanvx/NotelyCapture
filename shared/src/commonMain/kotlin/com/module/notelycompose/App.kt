package com.module.notelycompose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.module.notelycompose.Arguments.DEFAULT_NOTE_ID
import com.module.notelycompose.audio.ui.recorder.RecordingScreen
import com.module.notelycompose.core.Routes
import com.module.notelycompose.core.composableWithHorizontalSlide
import com.module.notelycompose.core.composableWithSharedAxis
import com.module.notelycompose.core.composableWithVerticalSlide
import com.module.notelycompose.core.navigateSingleTop
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel
import com.module.notelycompose.notes.ui.calendar.CalendarScreen
import com.module.notelycompose.notes.ui.capture.CaptureHubScreen
import com.module.notelycompose.notes.ui.components.MainScreenScaffold
import com.module.notelycompose.notes.ui.detail.NoteDetailScreen
import com.module.notelycompose.notes.ui.list.InfoScreen
import com.module.notelycompose.notes.ui.list.NoteListScreen
import com.module.notelycompose.notes.ui.settings.LanguageSelectionScreen
import com.module.notelycompose.notes.ui.settings.SettingsScreen
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.ui.theme.MyApplicationTheme
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.onboarding.presentation.OnboardingViewModel
import com.module.notelycompose.onboarding.presentation.model.OnboardingState
import com.module.notelycompose.onboarding.ui.ModelSetupPage
import com.module.notelycompose.onboarding.ui.OnboardingWalkthrough
import com.module.notelycompose.platform.Theme
import com.module.notelycompose.platform.presentation.PlatformUiState
import com.module.notelycompose.platform.presentation.PlatformViewModel
import com.module.notelycompose.transcription.TranscriptionScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object Arguments {
    const val NOTE_ID_PARAM = "noteId"
    const val DEFAULT_NOTE_ID = "0"
    const val ROUTE_SEPARATOR = "/"
}

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App(
    preferencesRepository: PreferencesRepository = koinInject()
) {
    val uiMode by preferencesRepository.getTheme().collectAsState(Theme.SYSTEM.name)
    MyApplicationTheme(
        darkTheme = when (uiMode) {
            Theme.DARK.name -> true
            Theme.LIGHT.name -> false
            else -> isSystemInDarkTheme()
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = LocalCustomColors.current.bodyBackgroundColor
        ) {
            val viewmodel = koinViewModel<OnboardingViewModel>()
            val platformViewModel = koinViewModel<PlatformViewModel>()
            val onboardingState by viewmodel.onboardingState.collectAsState()
            val platformUiState by platformViewModel.state.collectAsState()

            when (onboardingState) {
                is OnboardingState.Initial -> Unit
                is OnboardingState.NotCompleted -> {
                    OnboardingWalkthrough(
                        onFinish = {
                            viewmodel.onCompleteOnboarding()
                        },
                        platformState = platformUiState
                    )
                }

                is OnboardingState.SettingUpModel -> {
                    val downloaderViewModel = koinViewModel<ModelDownloaderViewModel>()
                    ModelSetupPage(
                        onComplete = {
                            viewmodel.onModelSetupCompleted()
                        },
                        onError = { errorMessage ->
                            viewmodel.onModelSetupError(errorMessage)
                        },
                        downloaderViewModel = downloaderViewModel,
                        platformState = platformUiState
                    )
                }

                is OnboardingState.Completed -> NoteAppRoot(platformUiState)
            }
        }
    }
}


@Composable
fun NoteAppRoot(platformUiState: PlatformUiState) {
    val navController = rememberNavController()
    
    // State for calendar go-to-today functionality
    var calendarGoToToday by remember { mutableStateOf<(() -> Unit)?>(null) }

    MainScreenScaffold(
        navController = navController,
        onQuickRecordClick = {
            navController.navigateSingleTop(Routes.QuickRecord)
        },
        onGoToToday = { calendarGoToToday?.invoke() },
        onNavigateToSettings = {
            navController.navigateSingleTop(Routes.Settings)
        },
        onNavigateToMenu = {
            navController.navigateSingleTop(Routes.Menu)
        }
    ) { onScrollStateChanged ->
        NavHost(
            navController,
            startDestination = Routes.Home::class,
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            navigation<Routes.Home>(startDestination = Routes.List::class) {
                composableWithSharedAxis<Routes.List> {
                    NoteListScreen(
                        navigateToSettings = {
                            navController.navigateSingleTop(Routes.Settings)
                        },
                        navigateToMenu = {
                            navController.navigateSingleTop(Routes.Menu)
                        },
                        navigateToNoteDetails = { noteId ->
                            navController.navigateSingleTop(Routes.Details(noteId))
                        },
                        navigateToQuickRecord = {
                            navController.navigateSingleTop(Routes.QuickRecord)
                        },
                        platformUiState = platformUiState,
                        onScrollStateChanged = onScrollStateChanged
                    )
                }
                composableWithVerticalSlide<Routes.Menu> {
                    InfoScreen(
                        navigateBack = { navController.popBackStack() },
                        onNavigateToWebPage = { title, url ->
                            // navController.navigateSingleTopWithPopUp("${Routes.WEB_VIEW}/$title/$url")
                        }
                    )
                }
                composableWithVerticalSlide<Routes.Settings> {
                    SettingsScreen(
                        navigateBack = { navController.popBackStack() },
                        navigateToLanguages = { navController.navigateSingleTop(Routes.Language) }
                    )
                }
                composableWithVerticalSlide<Routes.Language> {
                    LanguageSelectionScreen(
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composableWithSharedAxis<Routes.Calendar> {
                    CalendarScreen(
                        navigateBack = { navController.popBackStack() },
                        navigateToQuickRecord = {
                            navController.navigateSingleTop(Routes.QuickRecord)
                        }
                    )
                }
                composableWithSharedAxis<Routes.Capture> {
                CaptureHubScreen(
                    onVoiceCapture = { navController.navigateSingleTop(Routes.QuickRecord) },
                    onCameraCapture = { navController.navigateSingleTop(Routes.QuickRecord) },
                    onVideoCapture = { navController.navigateSingleTop(Routes.QuickRecord) },
                    onTextCapture = { 
                        navController.navigateSingleTop(Routes.Details(noteId = null))
                    },
                    onWhiteboardCapture = { navController.navigateSingleTop(Routes.Details(noteId = null)) },
                    onFileCapture = { navController.navigateSingleTop(Routes.Details(noteId = null)) },
                    navigateToQuickRecord = {
                        navController.navigateSingleTop(Routes.QuickRecord)
                    },
                    onNavigateToSettings = {
                        navController.navigateSingleTop(Routes.Settings)
                    },
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
        composableWithHorizontalSlide<Routes.QuickRecord> {
            RecordingScreen(
                noteId = null, // No existing note for quick record
                navigateBack = { navController.popBackStack() },
                editorViewModel = koinViewModel(), // Create new instance for quick record
                isQuickRecordMode = true
            )
        }
        navigation<Routes.DetailsGraph>(startDestination = Routes.Details::class) {
            composableWithHorizontalSlide<Routes.Details> { backStackEntry ->
                val route: Routes.Details = backStackEntry.toRoute()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.DetailsGraph)
                }
                NoteDetailScreen(
                    noteId = route.noteId ?: DEFAULT_NOTE_ID,
                    navigateBack = { navController.popBackStack() },
                    navigateToRecorder = { noteId ->
                        navController.navigateSingleTop(Routes.Recorder(noteId))
                    },
                    navigateToTranscription = {
                        navController.navigateSingleTop(Routes.Transcription)
                    },
                    editorViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                )
            }
            composableWithHorizontalSlide<Routes.Transcription> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.DetailsGraph)
                }
                TranscriptionScreen(
                    navigateBack = { navController.popBackStack() },
                    editorViewModel = koinViewModel(viewModelStoreOwner = parentEntry),
                )
            }
            composableWithHorizontalSlide<Routes.Recorder> { backStackEntry ->
                val route: Routes.Recorder = backStackEntry.toRoute()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.DetailsGraph)
                }
                RecordingScreen(
                    noteId = route.noteId?.toLong()?.takeIf { it != 0L },
                    navigateBack = { navController.popBackStack() },
                    editorViewModel = koinViewModel(viewModelStoreOwner = parentEntry),
                    isQuickRecordMode = false // Traditional recording flow
                )
            }
        }
        }
    }
}