package com.module.notelycompose

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class NoteApp : Application(){
    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
    }
}