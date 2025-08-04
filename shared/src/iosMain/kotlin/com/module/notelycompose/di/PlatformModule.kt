package com.module.notelycompose.di

import com.module.notelycompose.data.audio.IOSAudioPlayer
import com.module.notelycompose.domain.audio.PlatformAudioPlayer
import org.koin.dsl.module

/**
 * iOS-specific platform module providing iOS implementations
 * of platform-dependent interfaces.
 */
actual val platformModule = module {
    
    // Audio Player - iOS implementation
    single<PlatformAudioPlayer> { 
        IOSAudioPlayer()
    }
}