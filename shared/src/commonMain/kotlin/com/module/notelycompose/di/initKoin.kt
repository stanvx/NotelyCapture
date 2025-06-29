package com.module.notelycompose.di

import com.module.notelycompose.di.platformModule
import com.module.notelycompose.di.repositoryModule
import com.module.notelycompose.di.useCaseModule
import com.module.notelycompose.di.viewModelModule
import org.koin.core.KoinApplication

fun KoinApplication.init() {
        modules(appModule,viewModelModule, repositoryModule, useCaseModule, platformModule, mapperModule)
}