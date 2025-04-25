package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.model.DomainUser as User
import com.mgsanlet.cheftube.domain.usecase.user.AlternateKeepSessionUseCase
import com.mgsanlet.cheftube.domain.usecase.user.GetCurrentUserCopyUseCase
import com.mgsanlet.cheftube.domain.usecase.user.IsSessionKeptUseCase
import com.mgsanlet.cheftube.domain.usecase.user.UpdateUserUseCase
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserCopy: GetCurrentUserCopyUseCase,
    private val updateUser: UpdateUserUseCase,
    private val alternateKeepSession: AlternateKeepSessionUseCase,
    private val isSessionKept: IsSessionKeptUseCase
) : ViewModel() {
    private val _uiState = MutableLiveData<ProfileState>()
    val uiState: LiveData<ProfileState> = _uiState

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _uiState.value = ProfileState.Loading
        getCurrentUserCopy().fold(
            onSuccess = { user -> _uiState.value = ProfileState.LoadSuccess(user) },
            onError = { error -> _uiState.value = ProfileState.Error(error) }
        )
    }

    fun updateUser(
        finalUsername: String, finalEmail: String, newPassword: String?, oldPassword: String
    ) {
        var finalPassword: String = oldPassword
        var updatedUser: User? = null

        newPassword?.let {
            finalPassword = newPassword
        }

        getCurrentUserCopy().fold(
            onSuccess = { currentUser ->
                updatedUser = User(
                    currentUser.id, finalUsername, finalEmail, finalPassword
                )
            },
            onError = { error ->
                _uiState.value = ProfileState.Error(error)
                return //updatedUser nunca será null a partir de aquí
            }
        )

        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
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

    fun handleKeepSessionToggle(keepLoggedIn: Boolean) {
        val result = alternateKeepSession(keepLoggedIn)
        if (result is DomainResult.Error) _uiState.value = ProfileState.Error(result.error)
    }

    fun isSessionBeingKept(): Boolean {
        var isKept = false
        isSessionKept().fold(
            onSuccess = { isKept = it },
            onError = { error -> _uiState.value = ProfileState.Error(error) }
        )
        return isKept
    }
}

sealed class ProfileState {
    data object Loading : ProfileState()
    data class LoadSuccess(val user: User) : ProfileState()
    data object SaveSuccess : ProfileState()
    data class Error(val error: UserError) : ProfileState()
}

