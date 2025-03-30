package com.mgsanlet.cheftube

import android.app.Application
import android.content.Context
import com.yariksoffice.lingver.Lingver
import java.util.Locale

class ChefTubeApplication: Application() {


    override fun onCreate() {
        super.onCreate()
        initializeLocale(this)
    }

    private fun initializeLocale(context: Context) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = preferences.getString(LANGUAGE_KEY, null)
        // Inicializa con el lenguaje de shared preferences o con el predeterminado
        val locale = if (languageCode != null) {
            Locale(languageCode)
        } else {
            Locale.getDefault()
        }

        Lingver.init(this, locale)
    }

    companion object {
        private const val LANGUAGE_KEY = "language"
        private const val PREFS_NAME = "AppPrefs"
    }
}