package com.mgsanlet.cheftube.data.di

import android.content.Context
import com.mgsanlet.cheftube.data.source.local.DatabaseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabaseHelper(
        @ApplicationContext context: Context
    ): DatabaseHelper {
        return DatabaseHelper(context)
    }
}