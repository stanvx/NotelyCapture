package com.module.notelycompose.platform

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler

@Composable
actual fun HandlePlatformBackNavigation(
    enabled: Boolean,
    onBack: () -> Unit
) {
    BackHandler(enabled = enabled, onBack = onBack)
}
