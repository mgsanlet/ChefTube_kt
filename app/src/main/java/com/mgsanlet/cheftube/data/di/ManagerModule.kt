package com.mgsanlet.cheftube.data.di

import android.content.Context
import com.mgsanlet.cheftube.domain.repository.UserRepository
import com.mgsanlet.cheftube.utils.UserManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {
    @Provides
    @Singleton
    fun provideUserManager(
        @ApplicationContext context: Context,
        userRepository: UserRepository
    ): UserManager {
        return UserManager(context, userRepository)
    }
}