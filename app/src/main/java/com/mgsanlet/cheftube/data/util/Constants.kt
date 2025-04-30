package com.mgsanlet.cheftube.data.util

object Constants {
    // Shared Preferences
    const val PREFS_NAME = "ChefTubePrefs"
    const val LANGUAGE_KEY = "language_code"

    object Api {
        const val OFF_API_BASE_URL = "https://world.openfoodfacts.org/api/v3/"
    }

    object Database {
        const val NAME = "cheftube.db"
        const val VERSION = 1
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
    }
}