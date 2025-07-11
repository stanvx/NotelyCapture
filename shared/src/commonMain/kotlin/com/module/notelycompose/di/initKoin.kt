package com.module.notelycompose.di

import audio.di.audioModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

fun KoinApplication.init() {
    modules(
        appModule,
        viewModelModule,
        repositoryModule,
        useCaseModule,
        mapperModule,
        platformModule,
        audioModule
    )
}

fun initKoinApplication(config: KoinAppDeclaration? = null) {
    startKoin {
        includes(config)
        modules(
            appModule,
            viewModelModule,
            repositoryModule,
            useCaseModule,
            mapperModule,
            platformModule,
            audioModule
        )
    }
}