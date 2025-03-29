package com.mgsanlet.cheftube

import android.app.Application
import android.content.Context
import com.mgsanlet.cheftube.utils.LocaleUtils
import com.yariksoffice.lingver.Lingver
import java.util.Locale

class ChefTubeApplication: Application() {


    override fun onCreate() {
        super.onCreate()
        initializeLocale(this)
    }

    fun initializeLocale(context: Context) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var languageCode = preferences.getString(LANGUAGE_KEY, null)
        // Inicializa con el lenguaje de shared preferences o con el predeterminado
        if (languageCode == null) languageCode = Locale.getDefault().language
        val language = Locale(languageCode)
        Lingver.init(this, language)
    }

    companion object {
        private const val LANGUAGE_KEY = "language"
        private const val PREFS_NAME = "AppPrefs"
    }
}