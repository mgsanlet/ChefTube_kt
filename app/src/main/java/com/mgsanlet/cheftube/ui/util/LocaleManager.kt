package com.mgsanlet.cheftube.ui.util

import android.content.Context
import com.mgsanlet.cheftube.domain.repository.LanguagesRepository
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val languagesRepository: LanguagesRepository,
) {
    fun getCurrentLocale(): Locale {
        return Lingver.Companion.getInstance().getLocale()
    }

    fun setLocale(locale: Locale) {
        Lingver.Companion.getInstance().setLocale(context, locale)
        languagesRepository.saveLanguageCode(locale.language)
    }

    fun getStoredLanguageCode(): String? {
        return languagesRepository.getSavedLanguageCode()
    }
}