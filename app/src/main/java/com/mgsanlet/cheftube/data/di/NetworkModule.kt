package com.mgsanlet.cheftube.data.di

import com.mgsanlet.cheftube.data.source.remote.FirebaseRecipeApi
import com.mgsanlet.cheftube.data.source.remote.OpenFoodFactsApi
import com.mgsanlet.cheftube.data.util.Constants.Api.OFF_API_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    // Qualifiers para diferentes APIs
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ProductRetrofit

//    @Qualifier
//    @Retention(AnnotationRetention.BINARY)
//    annotation class RecipeRetrofit  // Para futuras APIs de recetas

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
    fun provideFirebaseRecipeAp(): FirebaseRecipeApi {
        return FirebaseRecipeApi()
    }

    @Provides
    @Singleton
    fun provideProductApi(@ProductRetrofit retrofit: Retrofit): OpenFoodFactsApi {
        return retrofit.create(OpenFoodFactsApi::class.java)
    }
}