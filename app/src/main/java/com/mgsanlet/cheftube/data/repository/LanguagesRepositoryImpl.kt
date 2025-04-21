package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.source.local.PreferencesManager
import com.mgsanlet.cheftube.domain.repository.LanguagesRepository
import javax.inject.Inject

class LanguagesRepositoryImpl @Inject constructor(
    private val preferences: PreferencesManager
) : LanguagesRepository {

    override fun getSavedLanguageCode(): String? {
        return preferences.getSavedLanguageCode()
    }

    override fun saveLanguageCode(languageCode: String) {
        preferences.saveLanguageCode(languageCode)
    }
}