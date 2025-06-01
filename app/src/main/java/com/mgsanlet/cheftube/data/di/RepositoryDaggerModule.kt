package com.mgsanlet.cheftube.data.di

import com.mgsanlet.cheftube.data.repository.LanguagesRepositoryImpl
import com.mgsanlet.cheftube.data.repository.ProductsRepositoryImpl
import com.mgsanlet.cheftube.data.repository.RecipesRepositoryImpl
import com.mgsanlet.cheftube.data.repository.StatsRepositoryImpl
import com.mgsanlet.cheftube.data.repository.UsersRepositoryImpl
import com.mgsanlet.cheftube.data.source.local.PreferencesManager
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.data.source.remote.OpenFoodFactsApi
import com.mgsanlet.cheftube.data.util.PatternValidatorImpl
import com.mgsanlet.cheftube.domain.repository.LanguagesRepository
import com.mgsanlet.cheftube.domain.repository.ProductsRepository
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.StatsRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.PatternValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger que proporciona las implementaciones concretas de los repositorios.
 * Todas las dependencias tienen ámbito de Singleton para asegurar una única instancia en toda
 * la aplicación.
 */

@Module
@InstallIn(SingletonComponent::class)
object RepositoryDaggerModule {
    
    /**
     * Proporciona la implementación concreta de [UsersRepository].
     *
     * @param api Cliente de Firebase para operaciones de autenticación y base de datos
     * @return Instancia de [UsersRepositoryImpl] configurada con el cliente de Firebase
     */
    @Provides
    @Singleton
    fun provideUsersRepository(
        api: FirebaseApi
    ): UsersRepository {
        return UsersRepositoryImpl(api)
    }

    /**
     * Proporciona la implementación concreta de [RecipesRepository].
     *
     * @param api Cliente de Firebase para operaciones relacionadas con recetas
     * @return Instancia de [RecipesRepositoryImpl] configurada con el cliente de Firebase
     */
    @Provides
    @Singleton
    fun provideRecipesRepository(
        api: FirebaseApi
    ): RecipesRepository {
        return RecipesRepositoryImpl(api)
    }

    /**
     * Proporciona la implementación concreta de [ProductsRepository].
     *
     * @param openFoodFactsApi Cliente para la API de Open Food Facts
     * @param firebaseApi Cliente de Firebase
     * @return Instancia de [ProductsRepositoryImpl] configurada con el cliente de la API
     */
    @Provides
    @Singleton
    fun provideProductsRepository(
        openFoodFactsApi: OpenFoodFactsApi,
        firebaseApi: FirebaseApi
    ): ProductsRepository {
        return ProductsRepositoryImpl(openFoodFactsApi, firebaseApi)
    }

    /**
     * Proporciona la implementación concreta de [LanguagesRepository].
     *
     * @param preferences Gestor de preferencias para almacenar la configuración de idioma
     * @return Instancia de [LanguagesRepositoryImpl] configurada con el gestor de preferencias
     */
    @Provides
    @Singleton
    fun provideLanguagesRepository(
        preferences: PreferencesManager
    ): LanguagesRepository {
        return LanguagesRepositoryImpl(preferences)
    }

    /**
     * Proporciona la implementación concreta de [StatsRepository].
     *
     * @param api Cliente de Firebase para operaciones relacionadas con estadísticas
     * @return Instancia de [StatsRepositoryImpl] configurada con el cliente de Firebase
     */
    @Provides
    @Singleton
    fun provideStatsRepository(
        api: FirebaseApi
    ): StatsRepository {
        return StatsRepositoryImpl(api)
    }

    /**
     * Proporciona la implementación concreta de [PatternValidator].
     *
     * @return Instancia de [PatternValidatorImpl] para validación de patrones
     */
    @Provides
    @Singleton
    fun providePatternValidator(): PatternValidator {
        return PatternValidatorImpl()
    }
}