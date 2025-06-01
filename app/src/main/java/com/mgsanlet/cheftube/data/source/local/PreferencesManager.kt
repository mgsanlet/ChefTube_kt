package com.mgsanlet.cheftube.data.source.local

import android.content.Context
import androidx.core.content.edit
import com.mgsanlet.cheftube.data.util.Constants.LANGUAGE_KEY
import com.mgsanlet.cheftube.data.util.Constants.PREFS_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Clase que gestiona el almacenamiento local de preferencias de la aplicación.
 * Utiliza SharedPreferences para persistir configuraciones del usuario.
 *
 * @property context Contexto de la aplicación
 */
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Guarda el código de idioma seleccionado por el usuario.
     *
     * @param languageCode Código del idioma a guardar (ej: "es", "en")
     */
    fun saveLanguageCode(languageCode: String) {
        preferences.edit(commit = true) {
            putString(LANGUAGE_KEY, languageCode)
        }
    }

    /**
     * Obtiene el código de idioma guardado previamente.
     *
     * @return Código del idioma guardado, o null si no hay ninguno guardado
     */
    fun getSavedLanguageCode(): String? {
        return preferences.getString(LANGUAGE_KEY, null)
    }
}
