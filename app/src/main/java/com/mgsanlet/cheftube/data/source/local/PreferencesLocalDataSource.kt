package com.mgsanlet.cheftube.data.source.local

import android.content.Context
import androidx.core.content.edit
import com.mgsanlet.cheftube.domain.Constants.LANGUAGE_KEY
import com.mgsanlet.cheftube.domain.Constants.PREFS_NAME
import com.mgsanlet.cheftube.domain.Constants.SAVED_USER_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferencesLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUserId(id: String) {
        preferences.edit(commit = true) {
            putString(SAVED_USER_ID, id)
        }
    }

    fun deleteUserId() {
        preferences.edit(commit = true) { remove(SAVED_USER_ID) }
    }

    fun isIdSaved(id: String): Boolean {
        return preferences.getString(SAVED_USER_ID, null) != null &&
                preferences.getString(SAVED_USER_ID, null).equals(id)
    }

    fun getSavedUserId(): String? {
        return preferences.getString(SAVED_USER_ID, null)
    }

    fun saveLanguageCode(languageCode: String) {
        preferences.edit(commit = true) {
            putString(LANGUAGE_KEY, languageCode)
        }
    }

    fun getSavedLanguageCode(): String? {
        return preferences.getString(LANGUAGE_KEY, null)
    }
}
