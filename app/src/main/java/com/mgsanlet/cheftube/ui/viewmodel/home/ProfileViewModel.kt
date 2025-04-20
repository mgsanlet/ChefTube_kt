package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.data.model.UserDto
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
    private val _currentUser = MutableLiveData<UserDto?>()
    val currentUser: LiveData<UserDto?> = _currentUser

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                userManager.currentUser
            }
            _currentUser.value = user
        }
    }

    fun verifyPassword(password: String): Boolean {
        return currentUser.value?.verifyPassword(password) == true
    }

    fun updateUser(
        finalUsername: String, finalEmail: String, finalPassword: String, oldPassword: String
    ): Result<UserDto> {

        val updatedUser = UserDto.create(
            username = finalUsername, email = finalEmail, password = finalPassword
        ).copy(id = currentUser.value!!.id)

        val result = userRepository.updateUser(updatedUser, oldPassword)
        if (result.isSuccess) {
            userManager.currentUser = updatedUser
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
            userManager.persistCurrentUser()
        } else {
            userManager.deletePersistentUser()
        }
    }

    fun isUserPersistent(): Boolean {
        return userManager.isUserPersistent()
    }
}
