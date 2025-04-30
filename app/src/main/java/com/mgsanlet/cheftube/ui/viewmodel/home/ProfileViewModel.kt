package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.usecase.user.GetCurrentUserDataUseCase
import com.mgsanlet.cheftube.domain.usecase.user.UpdateUserUseCase
import com.mgsanlet.cheftube.domain.util.error.UserError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainUser as User

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserData: GetCurrentUserDataUseCase,
    private val updateUser: UpdateUserUseCase
) : ViewModel() {
    private val _uiState = MutableLiveData<ProfileState>()
    val uiState: LiveData<ProfileState> = _uiState

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
            getCurrentUserData().fold(
                onSuccess = { user -> _uiState.value = ProfileState.LoadSuccess(user) },
                onError = { error -> _uiState.value = ProfileState.Error(error) }
            )
        }
    }

    fun updateUser(
        finalUsername: String, finalEmail: String, newPassword: String?, oldPassword: String
    ) {
        var finalPassword: String = oldPassword
        var updatedUser: User? = null

        newPassword?.let {
            finalPassword = newPassword
        }
        viewModelScope.launch {
            getCurrentUserData().fold(
                onSuccess = { currentUser ->
                    updatedUser = User(
                        currentUser.id, finalUsername, finalEmail
                    )
                },
                onError = { error ->
                    _uiState.value = ProfileState.Error(error)
                    return@fold //updatedUser nunca será null a partir de aquí
                }
            )

            _uiState.value = ProfileState.Loading

            //updatedUser!! ya que por aquí nunca será null
            updateUser(updatedUser!!, oldPassword).fold(
                onSuccess = { user ->
                    _uiState.value = ProfileState.SaveSuccess
                    _uiState.value = ProfileState.LoadSuccess(user)
                },
                onError = { error -> _uiState.value = ProfileState.Error(error) }
            )
        }
    }
}

sealed class ProfileState {
    data object Loading : ProfileState()
    data class LoadSuccess(val user: User) : ProfileState()
    data object SaveSuccess : ProfileState()
    data class Error(val error: UserError) : ProfileState()
}

