package com.module.notelycompose.di

import org.koin.core.KoinApplication

fun KoinApplication.init() {
        modules(appModule,viewModelModule, repositoryModule, useCaseModule,mapperModule, platformModule)
}