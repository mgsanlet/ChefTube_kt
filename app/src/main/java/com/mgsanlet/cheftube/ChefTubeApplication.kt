package com.mgsanlet.cheftube

import android.app.Application
import android.content.Context
import com.mgsanlet.cheftube.chore.DatabaseHelper
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.data.provider.UserProvider
import com.mgsanlet.cheftube.data.repository.RecipeRepository
import com.mgsanlet.cheftube.data.repository.UserRepository
import com.yariksoffice.lingver.Lingver
import java.util.Locale
import androidx.core.content.edit

class ChefTubeApplication : Application() {
    lateinit var userRepository: UserRepository
        private set
    lateinit var recipeRepository: RecipeRepository
        private set

    // Usuario actual de la aplicación
    private var currentUser: User? = null

    override fun onCreate() {
        super.onCreate()
        initializeLocale()
        initializeRepositories()
        trySetSavedUserAsCurrent()
    }

    private fun initializeLocale() {
        val preferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
        recipeRepository = RecipeRepository()
    }

    fun setCurrentUserAsSaved(){
        val preferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentUser?.let{
            preferences.edit(commit = true) { putString(SAVED_USER_ID, it.id) }
        }
    }

    fun deleteSavedUser(){
        val preferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit(commit = true) { remove(SAVED_USER_ID) }
    }

    fun isUserSaved(): Boolean{
        val preferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getString(SAVED_USER_ID, null) != null &&
               preferences.getString(SAVED_USER_ID, null).equals(currentUser?.id)
    }

    private fun trySetSavedUserAsCurrent(){
        val preferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = preferences.getString(SAVED_USER_ID, null)
        savedUserId?.let{
            userRepository.getUserById(it).fold(
                onSuccess = { user ->
                    currentUser = user
                },
                onFailure = {
                    currentUser = null
                }
            )
        }
    }

    fun setCurrentUser(user: User?) {
        currentUser = user
    }

    fun getCurrentUser(): User? = currentUser

    companion object {
        private const val LANGUAGE_KEY = "language"
        private const val SAVED_USER_ID = "savedUserId"
        private const val PREFS_NAME = "AppPrefs"

        fun getInstance(context: Context): ChefTubeApplication {
            return context.applicationContext as ChefTubeApplication
        }
    }
}