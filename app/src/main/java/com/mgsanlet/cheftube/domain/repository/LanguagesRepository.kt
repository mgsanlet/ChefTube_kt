package com.mgsanlet.cheftube.domain.repository

interface LanguagesRepository {
    fun getSavedLanguageCode(): String?
    fun saveLanguageCode(languageCode: String)
}