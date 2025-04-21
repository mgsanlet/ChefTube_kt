package com.mgsanlet.cheftube.data.di

import android.content.Context
import com.mgsanlet.cheftube.data.repository.ProductsRepositoryImpl
import com.mgsanlet.cheftube.data.repository.RecipesRepositoryImpl
import com.mgsanlet.cheftube.data.repository.UsersRepositoryImpl
import com.mgsanlet.cheftube.data.source.local.RecipesLocalDataSource
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import com.mgsanlet.cheftube.data.source.remote.ProductApi
import com.mgsanlet.cheftube.domain.repository.ProductsRepository
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context,
        userLocalDataSource: UserLocalDataSource
    ): UsersRepository {
        return UsersRepositoryImpl(context, userLocalDataSource)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        recipesLocalDataSource: RecipesLocalDataSource
    ): RecipesRepository {
        return RecipesRepositoryImpl(recipesLocalDataSource)
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        productApi: ProductApi
    ): ProductsRepository {
        return ProductsRepositoryImpl(productApi)
    }
}