package com.mgsanlet.cheftube.utils

import android.content.Context
import com.mgsanlet.cheftube.utils.Constants.LANGUAGE_KEY
import com.mgsanlet.cheftube.utils.Constants.PREFS_NAME
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getCurrentLocale(): Locale {
        return Lingver.getInstance().getLocale()
    }

    fun setLocale(locale: Locale) {
        Lingver.getInstance().setLocale(context, locale)
        // Guardar el código de idioma en SharedPreferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit() {
                putString(LANGUAGE_KEY, locale.language)
            }
    }

    fun getStoredLanguageCode(): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, null)
    }
}