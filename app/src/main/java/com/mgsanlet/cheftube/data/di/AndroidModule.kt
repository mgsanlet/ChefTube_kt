package com.mgsanlet.cheftube.data.di

import android.content.Context
import com.mgsanlet.cheftube.domain.repository.LanguagesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.mgsanlet.cheftube.ui.util.LocaleManager

@Module
@InstallIn(SingletonComponent::class)
object LocaleManager {
    @Provides
    @Singleton
    fun provideLocaleManager(
        @ApplicationContext context: Context,
        languagesRepository: LanguagesRepository
    ): LocaleManager {
        return LocaleManager(context, languagesRepository)
    }
}