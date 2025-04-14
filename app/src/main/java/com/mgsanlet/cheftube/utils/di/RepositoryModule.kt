package com.mgsanlet.cheftube.utils.di

import android.content.Context
import com.mgsanlet.cheftube.data.repository.ProductRepositoryImpl
import com.mgsanlet.cheftube.data.repository.RecipeRepositoryImpl
import com.mgsanlet.cheftube.data.repository.UserRepositoryImpl
import com.mgsanlet.cheftube.data.source.local.RecipeLocalDataSource
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import com.mgsanlet.cheftube.data.source.remote.ProductApi
import com.mgsanlet.cheftube.domain.repository.ProductRepository
import com.mgsanlet.cheftube.domain.repository.RecipeRepository
import com.mgsanlet.cheftube.domain.repository.UserRepository
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
    ): UserRepository {
        return UserRepositoryImpl(context, userLocalDataSource)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        recipeLocalDataSource: RecipeLocalDataSource
    ): RecipeRepository {
        return RecipeRepositoryImpl(recipeLocalDataSource)
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        productApi: ProductApi
    ): ProductRepository {
        return ProductRepositoryImpl(productApi)
    }
}