package com.mgsanlet.cheftube.data.util

/**
 * Objeto que contiene constantes utilizadas en toda la aplicación.
 * Agrupa las constantes en objetos anidados según su propósito.
 */
object Constants {
    // Shared Preferences
    const val PREFS_NAME = "ChefTubePrefs"
    const val LANGUAGE_KEY = "language_code"

    // Storage paths
    object Storage {
        const val PROFILE_PICTURES = "profile_pictures"
        const val RECIPE_IMAGES = "recipe_images"
        const val FILE_EXTENSION = ".jpg"
        
        fun getProfilePicturePath(userId: String): String {
            return "$PROFILE_PICTURES/$userId$FILE_EXTENSION"
        }
        
        fun getRecipeImagePath(recipeId: String): String {
            return "$RECIPE_IMAGES/$recipeId$FILE_EXTENSION"
        }
    }

    // Error messages
    object Errors {
        const val USER_NOT_FOUND_LOGIN = "Usuario no encontrado después del login"
        const val USER_NOT_FOUND_CREATE = "Usuario no encontrado después del login"
        const val EMAIL_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"
        const val WEAK_PASSWORD = "WEAK_PASSWORD"
        const val PASSWORD_PATTERN = "PASSWORD_DOES_NOT_MEET_REQUIREMENTS"
    }

    object Api {
        const val OFF_API_BASE_URL = "https://world.openfoodfacts.org/api/v3/"
    }
}