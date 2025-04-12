package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.data.repository.UserRepository
class ProfileViewModel(private val app: ChefTubeApplication): ViewModel() {
    private val userRepository: UserRepository = app.userRepository
    var currentUser: MutableLiveData<User> = MutableLiveData()

    init {
        app.getCurrentUser()?.let { currentUser.value = it }
    }

    fun verifyPassword(password: String): Boolean {
        return currentUser.value?.verifyPassword(password) ?: false
    }

    fun updateUser(
        finalUsername: String,
        finalEmail: String,
        finalPassword: String,
        oldPassword: String
    ): Result<User> {

        val updatedUser = User.create(
            username = finalUsername,
            email = finalEmail,
            password = finalPassword
        ).copy(id = currentUser.value!!.id)

        val result = userRepository.updateUser(updatedUser, oldPassword)
        if (result.isSuccess){
            app.setCurrentUser(updatedUser)
            currentUser.value = updatedUser
        }
        return result
    }

    fun newEmailAlreadyExists(newEmail: String) : Boolean {
        if (newEmail == currentUser.value?.email){
            return false
        }
        return !userRepository.getUserByEmail(newEmail).isFailure
    }

    fun alternateKeepLoggedIn(keepLoggedIn: Boolean){
        if (keepLoggedIn){
            app.setCurrentUserAsSaved()
        }else{
            app.deleteSavedUser()
        }
    }

    fun isUserBeingKept(): Boolean{
        return app.isUserSaved()
    }
}

@Suppress("UNCHECKED_CAST")
class ProfileViewModelFactory(
    private val app: ChefTubeApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(app) as T
    }
}