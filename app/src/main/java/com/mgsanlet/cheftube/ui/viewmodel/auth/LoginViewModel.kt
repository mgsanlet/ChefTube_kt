package com.mgsanlet.cheftube.ui.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.usecase.user.AutomaticLoginUseCase
import com.mgsanlet.cheftube.domain.usecase.user.LoginUserUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUser: LoginUserUseCase,
    private val automaticLogin: AutomaticLoginUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<LoginState>(LoginState.Loading)
    val uiState: LiveData<LoginState> = _uiState

    fun tryAutoLogin() {
        viewModelScope.launch {
            if (automaticLogin() is DomainResult.Success) { _uiState.value = LoginState.AlreadyLogged }
            else { _uiState.value = LoginState.Initial }
        }
    }

    fun tryLogin(identity: String, password: String) {

        viewModelScope.launch {
            _uiState.value = LoginState.Loading

            val result = withContext(Dispatchers.IO) {
                loginUser(identity, password)
            }

            result.fold(
                onSuccess = {
                    _uiState.value = LoginState.Success
                }, onError = { error ->
                    _uiState.value = LoginState.Error(error)
                })
        }
    }
}

sealed class LoginState {
    data object Initial : LoginState()
    data object Loading : LoginState()
    data object AlreadyLogged : LoginState()
    data object Success : LoginState()
    data class  Error(val error: UserError) : LoginState()
}

