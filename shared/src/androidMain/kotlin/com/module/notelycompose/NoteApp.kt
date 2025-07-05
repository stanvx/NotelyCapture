package com.module.notelycompose

import android.app.Application
import com.module.notelycompose.di.initKoinApplication
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class NoteApp : Application(){
    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
        initKoinApplication {
            androidContext(this@NoteApp)
            androidLogger()
        }
    }
}