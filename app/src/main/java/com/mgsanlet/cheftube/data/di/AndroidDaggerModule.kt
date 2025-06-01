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

/**
 * Módulo de Dagger que proporciona dependencias específicas de Android.
 * Se instala en el componente Singleton para asegurar una única instancia durante el ciclo de vida
 * de la aplicación.
 */

@Module
@InstallIn(SingletonComponent::class)
object AndroidDaggerModule {
    
    /**
     * Proporciona una instancia de [LocaleManager] configurada con el contexto de la aplicación
     * y el repositorio de idiomas.
     *
     * @param context Contexto de la aplicación proporcionado por Hilt
     * @param languagesRepository Repositorio para gestionar los idiomas de la aplicación
     * @return Instancia configurada de [LocaleManager]
     */
    @Provides
    @Singleton
    fun provideLocaleManager(
        @ApplicationContext context: Context,
        languagesRepository: LanguagesRepository
    ): LocaleManager {
        // Crea una instancia de LocaleManager con las dependencias inyectadas
        return LocaleManager(context, languagesRepository)
    }
}