package com.module.notelycompose.di

import android.content.Context
import com.module.notelycompose.data.audio.AndroidAudioPlayer
import com.module.notelycompose.domain.audio.PlatformAudioPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific platform module providing Android implementations
 * of platform-dependent interfaces.
 */
actual val platformModule = module {
    
    // Audio Player - Android implementation
    single<PlatformAudioPlayer> { 
        AndroidAudioPlayer(context = androidContext())
    }
    
    // Android Context (provided by Koin Android)
    // androidContext() is automatically available when using Koin Android
}

/**
 * Extension function to get Android context in a type-safe way.
 * This can be used in other parts of the Android-specific code.
 */
fun org.koin.core.scope.Scope.androidContext(): Context {
    return get<Context>()
}