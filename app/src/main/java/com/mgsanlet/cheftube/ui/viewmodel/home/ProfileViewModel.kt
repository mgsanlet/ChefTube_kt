package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.usecase.user.GetCurrentUserDataUseCase
import com.mgsanlet.cheftube.domain.usecase.user.GetUserDataByIdUseCase
import com.mgsanlet.cheftube.domain.usecase.user.UpdateUserDataUseCase
import com.mgsanlet.cheftube.domain.util.error.UserError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserDataById: GetUserDataByIdUseCase,
    private val getCurrentUserData: GetCurrentUserDataUseCase,
    private val updateUserData: UpdateUserDataUseCase
) : ViewModel() {
    private val _uiState = MutableLiveData<ProfileState>()
    val uiState: LiveData<ProfileState> = _uiState
    private val _userData = MutableLiveData<DomainUser>()
    val userData: LiveData<DomainUser> = _userData
    private val _isCurrentUserProfile = MutableLiveData<Boolean>()
    val isCurrentUserProfile: LiveData<Boolean> = _isCurrentUserProfile

    fun loadUserDataById(userId: String) {
        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
            getUserDataById(userId).fold(
                onSuccess = { user ->
                    _isCurrentUserProfile.value = false
                    _userData.value = user
                    _uiState.value = ProfileState.LoadSuccess
                },
                onError = { error -> _uiState.value = ProfileState.Error(error) }
            )
        }
    }

    fun loadCurrentUserData() {
        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
            getCurrentUserData().fold(
                onSuccess = { user ->
                    _isCurrentUserProfile.value = true
                    _userData.value = user
                    _uiState.value = ProfileState.LoadSuccess
                },
                onError = { error -> _uiState.value = ProfileState.Error(error) }
            )
        }
    }

    fun tryUpdateUserData(newUsername: String, newBio: String) {
        _uiState.value = ProfileState.Loading

        var newUserData = _userData.value?.copy(
            username = newUsername.ifBlank { _userData.value?.username ?: "" },
            bio = newBio.ifBlank { _userData.value?.bio ?: "" }
        ) ?: throw Exception("Null logged user")

        viewModelScope.launch {
            updateUserData(newUserData).fold(
                onSuccess = {
                    _userData.value = newUserData
                    _uiState.value = ProfileState.SaveSuccess
                    _uiState.value = ProfileState.LoadSuccess
                },
                onError = { error -> _uiState.value = ProfileState.Error(error) }
            )
        }
    }

    fun followUser() {
        TODO("Not yet implemented")
    }

    fun unfollowUser() {
        TODO("Not yet implemented")
    }

}

sealed class ProfileState {
    data object Loading : ProfileState()
    data object LoadSuccess : ProfileState()
    data object SaveSuccess : ProfileState()
    data class Error(val error: UserError) : ProfileState()
}

