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

    private var currentUser: User? = null

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val SAVED_USER_ID = "savedUserId"
    }

    fun setCurrentUserAsSaved() {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentUser?.let {
            preferences.edit(commit = true) { putString(SAVED_USER_ID, it.id) }
        }
    }

    fun deleteSavedUser() {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit(commit = true) { remove(SAVED_USER_ID) }
    }

    fun isUserSaved(): Boolean {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getString(SAVED_USER_ID, null) != null && preferences.getString(
            SAVED_USER_ID, null
        ).equals(currentUser?.id)
    }

    private fun trySetSavedUserAsCurrent() {
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

    fun setCurrentUser(user: User?) {
        currentUser = user
    }

    fun getCurrentUser(): User? = currentUser
}