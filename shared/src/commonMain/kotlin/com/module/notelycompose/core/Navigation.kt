package com.module.notelycompose.core

import androidx.annotation.MainThread
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@PublishedApi
internal val screenOrder = listOf(
    Routes.List::class.qualifiedName,
    Routes.Calendar::class.qualifiedName,
    Routes.Capture::class.qualifiedName
)

@PublishedApi
internal fun isForwardTransition(initialRoute: String?, targetRoute: String?): Boolean {
    if (initialRoute == null || targetRoute == null) return true
    val initialIndex = screenOrder.indexOf(initialRoute)
    val targetIndex = screenOrder.indexOf(targetRoute)
    return if (initialIndex != -1 && targetIndex != -1) {
        targetIndex > initialIndex
    } else {
        true // Default for transitions not between main screens
    }
}

inline fun <reified T : @Serializable Any> NavGraphBuilder.composableWithSharedAxis(
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) = composable<T>(
    deepLinks = deepLinks,
    enterTransition = {
        val isForward = isForwardTransition(initialState.destination.route, targetState.destination.route)
        slideIntoContainer(
            towards = if (isForward) AnimatedContentTransitionScope.SlideDirection.Start else AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(300)
        )
    },
    exitTransition = {
        val isForward = isForwardTransition(initialState.destination.route, targetState.destination.route)
        slideOutOfContainer(
            towards = if (isForward) AnimatedContentTransitionScope.SlideDirection.Start else AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(300)
        )
    },
    popEnterTransition = {
        val isForward = isForwardTransition(initialState.destination.route, targetState.destination.route)
        slideIntoContainer(
            towards = if (isForward) AnimatedContentTransitionScope.SlideDirection.Start else AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(300)
        )
    },
    popExitTransition = {
        val isForward = isForwardTransition(initialState.destination.route, targetState.destination.route)
        slideOutOfContainer(
            towards = if (isForward) AnimatedContentTransitionScope.SlideDirection.Start else AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(300)
        )
    },
    content = content
)

inline fun <reified T : @Serializable Any> NavGraphBuilder.composableWithHorizontalSlide(
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) = composable<T>(
    deepLinks = deepLinks,
    enterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(300)
        )
    },
    exitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(300)
        )
    },
    popEnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(300)
        )
    },
    popExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(300)
        )
    },
    content = content
)

inline fun <reified T : @Serializable Any> NavGraphBuilder.composableWithVerticalSlide(
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) = composable<T>(
    deepLinks = deepLinks,
    enterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = tween(300)
        )
    },
    exitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = tween(300)
        )
    },
    popEnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = tween(300)
        )
    },
    popExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = tween(300)
        )
    },
    content = content
)

inline fun <reified T : @Serializable Any> NavGraphBuilder.composableNoAnimation(
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) = composable<T>(
    deepLinks = deepLinks,
    enterTransition = { null },
    exitTransition = { null },
    popEnterTransition = { null },
    popExitTransition = { null },
    content = content
)

@MainThread
internal fun <T : Any> NavHostController.navigateSingleTop(
    route: T,
) = navigate(route = route) {
    launchSingleTop = true
}
