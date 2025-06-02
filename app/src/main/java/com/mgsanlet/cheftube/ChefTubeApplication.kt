package com.mgsanlet.cheftube

import android.app.Application
import com.google.firebase.FirebaseApp
import com.mgsanlet.cheftube.ui.util.LocaleManager
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

/**
 * Clase Application personalizada para la aplicación ChefTube.
 *
 * Se encarga de la inicialización de componentes globales como:
 * - Firebase
 * - Inyección de dependencias con Hilt
 * - Configuración de internacionalización
 */
@HiltAndroidApp
class ChefTubeApplication : Application() {

    /** Gestor de configuración regional inyectado por Hilt */
    @Inject
    lateinit var localeManager: LocaleManager

    /**
     * Llamado cuando se crea la aplicación.
     * Inicializa componentes globales.
     */
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initializeLocale()
    }

    /**
     * Inicializa la configuración regional de la aplicación.
     * Usa el idioma guardado o el predeterminado del sistema.
     */
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
