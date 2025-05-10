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

@Module
@InstallIn(SingletonComponent::class)
object SourceDaggerModule {

    // Local

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context,
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    // Retrofit

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ProductRetrofit


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

    @Provides
    @Singleton
    fun provideProductApi(@ProductRetrofit retrofit: Retrofit): OpenFoodFactsApi {
        return retrofit.create(OpenFoodFactsApi::class.java)
    }

    // Firebase

    @Provides
    @Singleton
    fun provideFirebaseApi(): FirebaseApi {
        return FirebaseApi()
    }
}