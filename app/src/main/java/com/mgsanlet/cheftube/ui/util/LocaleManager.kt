package com.mgsanlet.cheftube.ui.util

import android.content.Context
import com.mgsanlet.cheftube.domain.repository.LanguagesRepository
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de configuración regional (locale) de la aplicación.
 * Se encarga de manejar los cambios de idioma y mantener la configuración persistente.
 *
 * @property context Contexto de la aplicación
 * @property languagesRepository Repositorio para acceder y guardar la configuración de idioma
 */
@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val languagesRepository: LanguagesRepository,
) {
    /**
     * Obtiene la configuración regional actual de la aplicación.
     *
     * @return Objeto Locale que representa la configuración regional actual
     */
    fun getCurrentLocale(): Locale {
        return Lingver.Companion.getInstance().getLocale()
    }

    /**
     * Establece la configuración regional de la aplicación y la guarda para futuros inicios.
     *
     * @param locale Configuración regional a establecer
     */
    fun setLocale(locale: Locale) {
        Lingver.Companion.getInstance().setLocale(context, locale)
        languagesRepository.saveLanguageCode(locale.language)
    }

    /**
     * Obtiene el código de idioma guardado en las preferencias.
     *
     * @return Código de idioma guardado (ej: "es", "en") o null si no hay idioma guardado
     */
    fun getStoredLanguageCode(): String? {
        return languagesRepository.getSavedLanguageCode()
    }
}