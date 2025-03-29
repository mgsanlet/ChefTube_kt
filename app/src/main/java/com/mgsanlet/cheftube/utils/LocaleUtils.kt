package com.mgsanlet.cheftube.utils

import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleUtils {
    private const val PREFS_NAME = "AppPrefs"
    private const val LANGUAGE_KEY = "language"

    /**
     * Aplica el idioma guardado desde SharedPreferences a la configuración actual.
     */
    fun applyLocale(context: Context) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Si no se encuentra código de lenguaje guardado en SharedPreferences, se obtiene el predeterminado del dispositivo 
        val languageCode = preferences.getString(LANGUAGE_KEY, Locale.getDefault().language)
        val locale = Locale(languageCode)

        val config = context.resources.configuration
        // Para indicar un nuevo lenguaje a la configuración, se hace de forma distinta dependiendo de la versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // Para Android Nougat (API 24) y superior
            config.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        } else { // Para versiones anteriores a Android Nougat (API 24)
            config.setLocale(locale) 
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
}
