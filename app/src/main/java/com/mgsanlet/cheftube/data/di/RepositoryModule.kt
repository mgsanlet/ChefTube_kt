package com.mgsanlet.cheftube.data.di

import com.mgsanlet.cheftube.data.repository.LanguagesRepositoryImpl
import com.mgsanlet.cheftube.data.repository.ProductsRepositoryImpl
import com.mgsanlet.cheftube.data.repository.RecipesRepositoryImpl
import com.mgsanlet.cheftube.data.repository.UsersRepositoryImpl
import com.mgsanlet.cheftube.data.source.local.PreferencesManager
import com.mgsanlet.cheftube.data.source.local.RecipesLocalDataSource
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import com.mgsanlet.cheftube.data.source.remote.ProductApi
import com.mgsanlet.cheftube.data.util.PatternValidatorImpl
import com.mgsanlet.cheftube.domain.repository.LanguagesRepository
import com.mgsanlet.cheftube.domain.repository.ProductsRepository
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.PatternValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideUsersRepository(
        userLocalDataSource: UserLocalDataSource,
        preferences: PreferencesManager
    ): UsersRepository {
        return UsersRepositoryImpl(userLocalDataSource, preferences)
    }

    @Provides
    @Singleton
    fun provideRecipesRepository(
        recipesLocalDataSource: RecipesLocalDataSource
    ): RecipesRepository {
        return RecipesRepositoryImpl(recipesLocalDataSource)
    }

    @Provides
    @Singleton
    fun provideProductsRepository(
        productApi: ProductApi
    ): ProductsRepository {
        return ProductsRepositoryImpl(productApi)
    }

    @Provides
    @Singleton
    fun provideLanguagesRepository(
        preferences: PreferencesManager
    ): LanguagesRepository {
        return LanguagesRepositoryImpl(preferences)
    }

    @Provides
    @Singleton
    fun providePatternValidator(): PatternValidator {
        return PatternValidatorImpl()
    }
}