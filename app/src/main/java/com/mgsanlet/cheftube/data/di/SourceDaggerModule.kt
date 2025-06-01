package com.mgsanlet.cheftube.data.di

import android.content.Context
import com.mgsanlet.cheftube.data.source.local.PreferencesManager
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.data.source.remote.OpenFoodFactsApi
import com.mgsanlet.cheftube.data.util.Constants.Api.OFF_API_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Módulo de Dagger que proporciona las dependencias relacionadas con las fuentes de datos.
 * Incluye configuraciones para:
 * - Fuentes locales (SharedPreferences)
 * - Cliente HTTP (OkHttp)
 * - Cliente de API (Retrofit)
 * - Cliente de Firebase
 */

@Module
@InstallIn(SingletonComponent::class)
object SourceDaggerModule {

    // ===== Fuentes Locales =====

    /**
     * Proporciona una instancia de [PreferencesManager] para el manejo de preferencias locales.
     *
     * @param context Contexto de la aplicación para acceder a SharedPreferences
     * @return Instancia configurada de [PreferencesManager]
     */
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context,
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    // ===== Cliente HTTP =====

    /**
     * Proporciona una instancia de [OkHttpClient] configurada con logging.
     * El nivel de logging está establecido a BODY para registrar tanto cabeceras como cuerpo de las
     * peticiones.
     *
     * @return Instancia configurada de [OkHttpClient]
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    /**
     * Calificador para identificar la instancia de Retrofit específica para la API de productos.
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ProductRetrofit

    // ===== Configuración de APIs =====

    /**
     * Proporciona una instancia de [Retrofit] configurada para la API de productos.
     *
     * @param okHttpClient Cliente HTTP configurado
     * @return Instancia de [Retrofit] lista para realizar peticiones a la API de productos
     */
    @Provides
    @Singleton
    @ProductRetrofit
    fun provideProductRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OFF_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Crea la implementación de la API de Open Food Facts usando Retrofit.
     *
     * @param retrofit Instancia de Retrofit configurada
     * @return Implementación de [OpenFoodFactsApi] para realizar peticiones
     */
    @Provides
    @Singleton
    fun provideProductApi(@ProductRetrofit retrofit: Retrofit): OpenFoodFactsApi {
        return retrofit.create(OpenFoodFactsApi::class.java)
    }

    // ===== Firebase =====

    /**
     * Proporciona una instancia de [FirebaseApi] para interactuar con los servicios de Firebase.
     *
     * @return Instancia de [FirebaseApi] configurada
     */
    @Provides
    @Singleton
    fun provideFirebaseApi(): FirebaseApi {
        return FirebaseApi()
    }
}