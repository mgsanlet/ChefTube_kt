package com.mgsanlet.cheftube

import android.app.Application
import com.mgsanlet.cheftube.utils.LocaleManager
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
