package com.mgsanlet.cheftube.data.source.local

import android.content.Context
import androidx.core.content.edit
import com.mgsanlet.cheftube.data.util.Constants.LANGUAGE_KEY
import com.mgsanlet.cheftube.data.util.Constants.PREFS_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLanguageCode(languageCode: String) {
        preferences.edit(commit = true) {
            putString(LANGUAGE_KEY, languageCode)
        }
    }

    fun getSavedLanguageCode(): String? {
        return preferences.getString(LANGUAGE_KEY, null)
    }
}
