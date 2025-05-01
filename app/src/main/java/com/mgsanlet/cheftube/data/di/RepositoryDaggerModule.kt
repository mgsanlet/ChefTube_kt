package com.mgsanlet.cheftube.data.di

import com.mgsanlet.cheftube.data.repository.LanguagesRepositoryImpl
import com.mgsanlet.cheftube.data.repository.ProductsRepositoryImpl
import com.mgsanlet.cheftube.data.repository.RecipesRepositoryImpl
import com.mgsanlet.cheftube.data.repository.UsersRepositoryImpl
import com.mgsanlet.cheftube.data.source.local.PreferencesManager
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.data.source.remote.FirebaseRecipeApi
import com.mgsanlet.cheftube.data.source.remote.FirebaseUserApi
import com.mgsanlet.cheftube.data.source.remote.OpenFoodFactsApi
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
object RepositoryDaggerModule {
    @Provides
    @Singleton
    fun provideUsersRepository(
        api: FirebaseApi,
        userApi: FirebaseUserApi
    ): UsersRepository {
        return UsersRepositoryImpl(api, userApi)
    }

    @Provides
    @Singleton
    fun provideRecipesRepository(
        api: FirebaseRecipeApi
    ): RecipesRepository {
        return RecipesRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideProductsRepository(
        openFoodFactsApi: OpenFoodFactsApi
    ): ProductsRepository {
        return ProductsRepositoryImpl(openFoodFactsApi)
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