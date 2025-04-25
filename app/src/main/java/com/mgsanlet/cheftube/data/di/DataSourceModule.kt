package com.mgsanlet.cheftube.data.di

import android.content.Context
import com.mgsanlet.cheftube.data.source.local.DatabaseHelper
import com.mgsanlet.cheftube.data.source.local.PreferencesManager
import com.mgsanlet.cheftube.data.source.local.RecipesLocalDataSource
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideUserLocalDataSource(
        databaseHelper: DatabaseHelper
    ): UserLocalDataSource {
        return UserLocalDataSource(databaseHelper)
    }

    @Provides
    @Singleton
    fun provideRecipeLocalDataSource(
        @ApplicationContext context: Context
    ): RecipesLocalDataSource {
        return RecipesLocalDataSource(context)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context,
    ): PreferencesManager {
        return PreferencesManager(context)
    }
}