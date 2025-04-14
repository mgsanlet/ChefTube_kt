package com.mgsanlet.cheftube.utils

import android.content.Context
import androidx.core.content.edit
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository
) {

    var currentUser: User? = null

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val SAVED_USER_ID = "savedUserId"
    }

    fun persistCurrentUser() {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentUser?.let {
            preferences.edit(commit = true) { putString(SAVED_USER_ID, it.id) }
        }
    }

    fun deletePersistentUser() {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit(commit = true) { remove(SAVED_USER_ID) }
    }

    fun isUserPersistent(): Boolean {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getString(SAVED_USER_ID, null) != null && preferences.getString(
            SAVED_USER_ID, null
        ).equals(currentUser?.id)
    }

    fun trySetPersistentUserAsCurrent() {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = preferences.getString(SAVED_USER_ID, null)
        savedUserId?.let {
            userRepository.getUserById(it).fold(onSuccess = { user ->
                currentUser = user
            }, onFailure = {
                currentUser = null
            })
        }
    }
}