package com.mgsanlet.cheftube.utils

import android.content.Context
import androidx.core.content.edit
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.util.Constants.PREFS_NAME
import com.mgsanlet.cheftube.domain.util.Constants.SAVED_USER_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    var currentUser: DomainUser? = null

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
}