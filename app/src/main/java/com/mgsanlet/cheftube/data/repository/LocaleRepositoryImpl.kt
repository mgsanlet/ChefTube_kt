package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.source.local.PreferencesLocalDataSource
import com.mgsanlet.cheftube.domain.repository.LocaleRepository
import javax.inject.Inject

class LocaleRepositoryImpl @Inject constructor(
    private val preferences: PreferencesLocalDataSource
) : LocaleRepository {

    override fun getSavedLanguageCode(): String? {
        return preferences.getSavedLanguageCode()
    }

    override fun saveLanguageCode(languageCode: String) {
        preferences.saveLanguageCode(languageCode)
    }
}