package com.mgsanlet.cheftube.data.di

import com.mgsanlet.cheftube.data.source.local.DatabaseHelper
import com.mgsanlet.cheftube.data.source.local.RecipesLocalDataSource
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideRecipeLocalDataSource(): RecipesLocalDataSource {
        return RecipesLocalDataSource()
    }
}