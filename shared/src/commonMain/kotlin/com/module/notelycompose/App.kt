package com.module.notelycompose

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.module.notelycompose.Arguments.DEFAULT_NOTE_ID
import com.module.notelycompose.audio.ui.recorder.RecordingScreen
import com.module.notelycompose.core.Routes
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

                is OnboardingState.Completed -> NoteAppRoot(platformUiState)
            }
        }
    }
}


@Composable
fun NoteAppRoot(platformUiState: PlatformUiState) {
    val navController = rememberNavController()
    NavHost(
        navController,
        startDestination = Routes.Home::class,
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        navigation<Routes.Home>(startDestination = Routes.List::class) {
            composable<Routes.List> {
                NoteListScreen(
                    navigateToSettings = {
                        navController.navigate(Routes.Settings)
                    },
                    navigateToMenu = {
                        navController.navigate(Routes.Menu)
                    },
                    navigateToNoteDetails = { noteId ->
                        navController.navigate(Routes.Details(noteId))
                    },
                    platformUiState = platformUiState
                )
            }
            composable<Routes.Menu>(
                enterTransition = { transitionSlideInVertically() },
                exitTransition = { transitionSlideOutVertically() }
            ) {
                InfoScreen(
                    navigateBack = { navController.popBackStack() },
                    onNavigateToWebPage = { title, url ->
                        // navController.navigate("${Routes.WEB_VIEW}/$title/$url")
                    }
                )
            }
            composable<Routes.Settings>(
                enterTransition = { transitionSlideInVertically() },
                exitTransition = { transitionSlideOutVertically() }
            ) {
                SettingsScreen(
                    navigateBack = { navController.popBackStack() },
                    navigateToLanguages = { navController.navigate(Routes.Language) }
                )
            }
            composable<Routes.Language> {
                LanguageSelectionScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
        navigation<Routes.DetailsGraph>(startDestination = Routes.Details::class) {
            composable<Routes.Details> { backStackEntry ->
                val route: Routes.Details = backStackEntry.toRoute()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.DetailsGraph)
                }
                NoteDetailScreen(
                    noteId = route.noteId ?: DEFAULT_NOTE_ID,
                    navigateBack = { navController.popBackStack() },
                    navigateToRecorder = { noteId ->
                        navController.navigate(Routes.Recorder(noteId))
                    },
                    navigateToTranscription = {
                        navController.navigate(Routes.Transcription)
                    },
                    editorViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                )
            }
            composable<Routes.Transcription> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.DetailsGraph)
                }
                TranscriptionScreen(
                    navigateBack = { navController.popBackStack() },
                    editorViewModel = koinViewModel(viewModelStoreOwner = parentEntry),
                )
            }
            composable<Routes.Recorder> { backStackEntry ->
                val route: Routes.Recorder = backStackEntry.toRoute()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.DetailsGraph)
                }
                RecordingScreen(
                    noteId = route.noteId?.toLong()?.takeIf { it != 0L },
                    navigateBack = { navController.popBackStack() },
                    editorViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                )
            }
        }
    }
}

private fun transitionSlideInVertically() = slideInVertically(
    initialOffsetY = { it },
    animationSpec = tween(durationMillis = 300)
)

private fun transitionSlideOutVertically() = slideOutVertically(
    targetOffsetY = { it },
    animationSpec = tween(durationMillis = 300)
)
