package com.module.notelycompose.di



import com.module.notelycompose.platform.createSettings
import com.module.notelycompose.platform.dataStore
import org.koin.dsl.module


actual val platformModule = module {
    single { dataStore() }
}