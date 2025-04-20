package com.mgsanlet.cheftube.domain.repository

interface LocaleRepository {
    fun getSavedLanguageCode(): String?
    fun saveLanguageCode(languageCode: String)
}