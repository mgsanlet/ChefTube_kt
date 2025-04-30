package com.mgsanlet.cheftube.data.di

import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.data.source.remote.FirebaseRecipeApi
import com.mgsanlet.cheftube.data.source.remote.FirebaseUserApi
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

    @Provides
    @Singleton
    fun provideFirebaseRecipeApi(mainApi: FirebaseApi): FirebaseRecipeApi {
        return FirebaseRecipeApi(mainApi)
    }

    @Provides
    @Singleton
    fun provideFirebaseUserApi(mainApi: FirebaseApi): FirebaseUserApi {
        return FirebaseUserApi(mainApi)
    }


}