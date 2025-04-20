package com.mgsanlet.cheftube.ui.util

import android.content.Context
import androidx.core.content.edit
import com.mgsanlet.cheftube.domain.Constants
import com.mgsanlet.cheftube.domain.repository.LocaleRepository
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localeRepository: LocaleRepository,
) {
    fun getCurrentLocale(): Locale {
        return Lingver.Companion.getInstance().getLocale()
    }

    fun setLocale(locale: Locale) {
        Lingver.Companion.getInstance().setLocale(context, locale)
        localeRepository.saveLanguageCode(locale.language)
    }

    fun getStoredLanguageCode(): String? {
        return localeRepository.getSavedLanguageCode()
    }
}