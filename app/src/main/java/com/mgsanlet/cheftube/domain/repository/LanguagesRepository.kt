package com.mgsanlet.cheftube.domain.repository

/**
 * Interfaz que define las operaciones para la gestión de idiomas en la aplicación.
 */
interface LanguagesRepository {
    /**
     * Obtiene el código de idioma guardado en las preferencias.
     *
     * @return Código de idioma guardado o null si no hay ninguno
     */
    fun getSavedLanguageCode(): String?
    
    /**
     * Guarda el código de idioma en las preferencias.
     *
     * @param languageCode Código del idioma a guardar (ej: "es", "en", etc.)
     */
    fun saveLanguageCode(languageCode: String)
}