package com.mgsanlet.cheftube.ui.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.repository.UsersRepository.UserError
import com.mgsanlet.cheftube.domain.usecase.user.CreateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class SignUpState {
    data object Initial : SignUpState()
    data object Loading : SignUpState()
    data object Success : SignUpState()
    data class  Error(val error: UserError) : SignUpState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val createUser: CreateUserUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<SignUpState>(SignUpState.Initial)
    val uiState: LiveData<SignUpState> = _uiState

    fun trySignUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = SignUpState.Loading
            delay(3000L)

            val result = withContext(Dispatchers.IO) {
                createUser(username, email, password)
            }

            result.fold(
                onSuccess = { user ->
                    _uiState.value = SignUpState.Success
                }, onError = { error ->
                    _uiState.value = SignUpState.Error(error)
                })
        }
    }

    private fun resetState() {
        _uiState.value = SignUpState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        resetState()
    }
}