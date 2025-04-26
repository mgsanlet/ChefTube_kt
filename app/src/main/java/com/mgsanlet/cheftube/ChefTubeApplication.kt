package com.mgsanlet.cheftube

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.mgsanlet.cheftube.ui.util.LocaleManager
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class ChefTubeApplication : Application() {

    @Inject
    lateinit var localeManager: LocaleManager

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initializeLocale()
    }

    private fun initializeLocale() {
        val languageCode = localeManager.getStoredLanguageCode()
        val locale = if (languageCode != null) {
            Locale(languageCode)
        } else {
            Locale.getDefault()
        }
        Lingver.init(this, locale)
    }
}
