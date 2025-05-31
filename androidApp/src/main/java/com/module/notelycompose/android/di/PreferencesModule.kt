package com.module.notelycompose.android.di

import android.content.Context
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.onboarding.presentation.OnboardingViewModel
import com.module.notelycompose.preferences.SettingsFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideSettingsFactory(@ApplicationContext context: Context): SettingsFactory {
        return SettingsFactory(context)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(settingsFactory: SettingsFactory): PreferencesRepository {
        return PreferencesRepository(settingsFactory.createSettings())
    }

    @Provides
    @Singleton
    fun provideOnboardingViewModel(preferencesRepository: PreferencesRepository): OnboardingViewModel {
        return OnboardingViewModel(preferencesRepository)
    }
}
