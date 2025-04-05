package com.mgsanlet.cheftube

import android.app.Application
import android.content.Context
import com.mgsanlet.cheftube.chore.DatabaseHelper
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.data.provider.UserProvider
import com.mgsanlet.cheftube.data.repository.UserRepository
import com.yariksoffice.lingver.Lingver
import java.util.Locale

class ChefTubeApplication : Application() {
    lateinit var userRepository: UserRepository
        private set

    // Usuario actual de la aplicación
    private var currentUser: User? = null

    override fun onCreate() {
        super.onCreate()
        initializeLocale(this)
        initializeRepositories()
    }

    private fun initializeLocale(context: Context) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = preferences.getString(LANGUAGE_KEY, null)
        val locale = if (languageCode != null) {
            Locale(languageCode)
        } else {
            Locale.getDefault()
        }
        Lingver.init(this, locale)
    }

    private fun initializeRepositories() {
        val dbHelper = DatabaseHelper(this)
        val userProvider = UserProvider(dbHelper)
        userRepository = UserRepository(this, userProvider)
    }

    fun setCurrentUser(user: User?) {
        currentUser = user
    }

    fun getCurrentUser(): User? = currentUser

    companion object {
        private const val LANGUAGE_KEY = "language"
        private const val PREFS_NAME = "AppPrefs"

        fun getInstance(context: Context): ChefTubeApplication {
            return context.applicationContext as ChefTubeApplication
        }
    }
}