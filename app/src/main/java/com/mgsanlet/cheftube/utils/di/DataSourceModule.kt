package com.mgsanlet.cheftube.utils.di

import com.mgsanlet.cheftube.data.source.local.DatabaseHelper
import com.mgsanlet.cheftube.data.source.local.RecipeLocalDataSource
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import com.mgsanlet.cheftube.data.source.remote.ProductApi
import com.mgsanlet.cheftube.data.source.remote.ProductRemoteDataSource
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
    fun provideRecipeLocalDataSource(): RecipeLocalDataSource {
        return RecipeLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideProductRemoteDataSource(productApi: ProductApi): ProductRemoteDataSource {
        return ProductRemoteDataSource(productApi)
    }
}