package com.module.notelycompose.di

import com.module.notelycompose.data.security.SecurityHelperImpl
import com.module.notelycompose.domain.security.SecurityHelper
import com.module.notelycompose.presentation.texteditor.TextEditorViewModel
import org.koin.dsl.module

/**
 * Koin module for domain layer dependencies.
 * This module provides the production implementations that can be easily
 * replaced with test doubles during testing.
 */
val domainModule = module {
    
    // Security
    single<SecurityHelper> { SecurityHelperImpl() }
    
    // ViewModels - Factory pattern for proper lifecycle management
    factory { 
        TextEditorViewModel(
            securityHelper = get(),
            audioPlayer = get(),
            noteRepository = get()
        )
    }
}

