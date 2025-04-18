package com.mgsanlet.cheftube.utils

object Constants {
    // Shared Preferences
    const val PREFS_NAME = "ChefTubePrefs"
    const val LANGUAGE_KEY = "language_code"
    const val SAVED_USER_ID = "saved_user_id"
    const val PASSWORD_MIN_LENGTH = 5

    object Api {
        const val OFF_API_BASE_URL = "https://world.openfoodfacts.org/api/v3/"
    }

    object Tag {
        const val SIGN_UP = "SignUp"
        const val LOGIN = "Login"
        const val RECIPE_FEED = "RecipeFeed"
        const val RECIPE_DETAIL = "RecipeDetail"
        const val PROFILE = "Profile"
        const val SCANNER = "Scanner"
    }
}