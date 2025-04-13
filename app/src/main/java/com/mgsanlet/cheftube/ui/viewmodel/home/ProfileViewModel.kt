package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.domain.repository.UserRepository
import com.mgsanlet.cheftube.utils.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userManager: UserManager
) : ViewModel() {
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                userManager.getCurrentUser()
            }
            _currentUser.value = user
        }
    }

    fun verifyPassword(password: String): Boolean {
        return currentUser.value?.verifyPassword(password) ?: false
    }

    fun updateUser(
        finalUsername: String, finalEmail: String, finalPassword: String, oldPassword: String
    ): Result<User> {

        val updatedUser = User.create(
            username = finalUsername, email = finalEmail, password = finalPassword
        ).copy(id = currentUser.value!!.id)

        val result = userRepository.updateUser(updatedUser, oldPassword)
        if (result.isSuccess) {
            userManager.setCurrentUser(updatedUser)
            _currentUser.value = updatedUser
        }
        return result
    }

    fun newUsernameAlreadyExists(newUsername: String): Boolean {
        if (newUsername == currentUser.value?.username) {
            return false
        }
        return !userRepository.getUserByName(newUsername).isFailure
    }

    fun newEmailAlreadyExists(newEmail: String): Boolean {
        if (newEmail == currentUser.value?.email) {
            return false
        }
        return !userRepository.getUserByEmail(newEmail).isFailure
    }

    fun alternateKeepLoggedIn(keepLoggedIn: Boolean) {
        if (keepLoggedIn) {
            userManager.setCurrentUserAsSaved()
        } else {
            userManager.deleteSavedUser()
        }
    }

    fun isUserBeingKept(): Boolean {
        return userManager.isUserSaved()
    }
}
