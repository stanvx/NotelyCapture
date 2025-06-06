package com.module.notelycompose.platform

import androidx.compose.runtime.Composable

@Composable
actual fun HandlePlatformBackNavigation(enabled: Boolean, onBack: () -> Unit) {
    // No implementation required for iOS
}
