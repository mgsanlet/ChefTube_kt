package com.mgsanlet.cheftube.ui.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.usecase.user.AutomaticLoginUseCase
import com.mgsanlet.cheftube.domain.usecase.user.LoginUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class LoginState {
    data object Initial : LoginState()
    data object Loading : LoginState()
    data object AlreadyLogged : LoginState()
    data object Success : LoginState()
    data class  Error(val error: ChefTubeError) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUser: LoginUserUseCase,
    private val automaticLogin: AutomaticLoginUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<LoginState>(LoginState.Initial)
    val uiState: LiveData<LoginState> = _uiState

    init {
        tryAutoLogin()
    }

    private fun tryAutoLogin() {
        viewModelScope.launch {
            if (automaticLogin()) {
                _uiState.value = LoginState.AlreadyLogged
            }
        }
    }

    fun tryLogin(identity: String, password: String) {

        viewModelScope.launch {
            _uiState.value = LoginState.Loading
            delay(3000L)

            val result = withContext(Dispatchers.IO) {
                loginUser(identity, password)
            }

            result.fold(
                onSuccess = { user ->
                    _uiState.value = LoginState.Success
                }, onFailure = { exception ->
                    _uiState.value = LoginState.Error(exception as ChefTubeError)
                })

        }
    }

    fun resetState() {
        _uiState.value = LoginState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        resetState()
    }
}
