package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.source.local.PreferencesManager
import com.mgsanlet.cheftube.domain.repository.LanguagesRepository
import javax.inject.Inject

/**
 * Implementación de [LanguagesRepository] que gestiona la configuración de idioma de la aplicación.
 * Utiliza [PreferencesManager] para persistir la preferencia de idioma.
 *
 * @property preferences Gestor de preferencias para almacenar la configuración de idioma
 */
class LanguagesRepositoryImpl @Inject constructor(
    private val preferences: PreferencesManager
) : LanguagesRepository {

    /**
     * Obtiene el código de idioma guardado en las preferencias.
     *
     * @return Código de idioma (ej: "es", "en") o null si no hay idioma guardado
     */
    override fun getSavedLanguageCode(): String? {
        return preferences.getSavedLanguageCode()
    }

    /**
     * Guarda el código de idioma en las preferencias.
     *
     * @param languageCode Código de idioma a guardar (ej: "es", "en")
     */
    override fun saveLanguageCode(languageCode: String) {
        preferences.saveLanguageCode(languageCode)
    }
}