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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.module.notelycompose.Arguments.DEFAULT_NOTE_ID
import com.module.notelycompose.Arguments.NOTE_ID_PARAM
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
        startDestination = Routes.HOME,
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        navigation(startDestination = Routes.LIST, route = Routes.HOME) {
            composable(Routes.LIST) {
                NoteListScreen(
                    navigateToSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    navigateToMenu = {
                        navController.navigate(Routes.MENU)
                    },
                    navigateToNoteDetails = { noteId ->
                        navController.navigate("${Routes.DETAILS}/$noteId")
                    },
                    platformUiState = platformUiState
                )
            }
            composable(
                route = Routes.MENU,
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
            composable(
                route = Routes.SETTINGS,
                enterTransition = { transitionSlideInVertically() },
                exitTransition = { transitionSlideOutVertically() }
            ) {
                SettingsScreen(
                    navigateBack = { navController.popBackStack() },
                    navigateToLanguages = { navController.navigate(Routes.LANGUAGE) }
                )
            }
            composable(Routes.LANGUAGE) {
                LanguageSelectionScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
        navigation(startDestination = "${Routes.DETAILS}/{$NOTE_ID_PARAM}", route = Routes.DETAILS_GRAPH) {
            composable(
                route = "${Routes.DETAILS}/{$NOTE_ID_PARAM}",
                arguments = listOf(navArgument(NOTE_ID_PARAM) {
                    type = NavType.StringType
                    defaultValue = DEFAULT_NOTE_ID
                    nullable = false
                })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.DETAILS_GRAPH)
                }
                NoteDetailScreen(
                    noteId = backStackEntry.arguments?.getString(NOTE_ID_PARAM) ?: DEFAULT_NOTE_ID,
                    navigateBack = { navController.popBackStack() },
                    navigateToRecorder = { noteId ->
                        navController.navigate("${Routes.RECORDER}/$noteId")
                    },
                    navigateToTranscription = {
                        navController.navigate(Routes.TRANSCRIPTION)
                    },
                    editorViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                )
            }
            composable(
                route = Routes.TRANSCRIPTION
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.DETAILS_GRAPH)
                }
                TranscriptionScreen(
                    navigateBack = { navController.popBackStack() },
                    editorViewModel = koinViewModel(viewModelStoreOwner = parentEntry),
                )
            }
            composable(
                route = "${Routes.RECORDER}/{$NOTE_ID_PARAM}",
                arguments = listOf(navArgument(NOTE_ID_PARAM) {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = false
                })
            ) { backStackEntry ->
                RecordingScreen(
                    noteId = backStackEntry.arguments?.getString(NOTE_ID_PARAM)?.toLong()?.takeIf { it != 0L },
                    navigateBack = { navController.popBackStack() },
                    editorViewModel = koinViewModel(
                        viewModelStoreOwner = navController.getBackStackEntry(
                            Routes.DETAILS_GRAPH
                        )
                    )
                )
            }
        }
    }
}

fun transitionSlideInVertically() = slideInVertically(
    initialOffsetY = { it },
    animationSpec = tween(durationMillis = 300)
)

fun transitionSlideOutVertically() = slideOutVertically(
    targetOffsetY = { it },
    animationSpec = tween(durationMillis = 300)
)
