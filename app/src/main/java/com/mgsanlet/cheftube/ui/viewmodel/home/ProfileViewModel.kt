package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository.UserError
import com.mgsanlet.cheftube.utils.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileState {
    data object Initial : ProfileState()
    data object Loading : ProfileState()
    data class Success(val user: DomainUser) : ProfileState()
    data class Error(val error: UserError) : ProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val userManager: UserManager
) : ViewModel() {
    private val _uiState = MutableLiveData<ProfileState>(ProfileState.Initial)
    val uiState: LiveData<ProfileState> = _uiState

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        usersRepository.getCurrentUserCopy().fold(
            onSuccess = { user -> _uiState.value = ProfileState.Success(user) },
            onError = { error -> _uiState.value = ProfileState.Error(error) }
        )
    }

    fun updateUser(
        finalUsername: String, finalEmail: String, finalPassword: String, oldPassword: String
    ) {
        if (uiState.value !is ProfileState.Success) {
            _uiState.value = ProfileState.Error(UserError.UNKNOWN)
            return
        }
        val currentUser = (uiState.value as ProfileState.Success).user
        val updatedUser = DomainUser(
            currentUser.id, finalUsername, finalEmail, finalPassword
        )
        viewModelScope.launch{
            usersRepository.updateUser(updatedUser, oldPassword).fold(
                onSuccess = { user -> _uiState.value = ProfileState.Success(user) },
                onError = { error -> _uiState.value = ProfileState.Error(error) }
            )
        }
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
