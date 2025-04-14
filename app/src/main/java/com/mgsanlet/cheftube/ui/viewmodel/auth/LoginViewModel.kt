package com.mgsanlet.cheftube.ui.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.repository.UserRepository
import com.mgsanlet.cheftube.utils.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
@HiltViewModel
class LoginViewModel@Inject constructor(
    private val userRepository: UserRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>().apply {
        value = LoginState.Initial
    }
    val loginState: LiveData<LoginState> = _loginState

    init {
        checkCurrentUser()
    }

    private fun LoginViewModel.checkCurrentUser() {
        userManager.trySetPersistentUserAsCurrent()
        userManager.currentUser?.let { _loginState.value = LoginState.AlreadyLogged }
    }

    fun tryLogin(identity: String, password: String) {

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            delay(3000L)

            val result = withContext(Dispatchers.IO) {
                userRepository.loginUser(identity, password)
            }

            result.fold(
                onSuccess = { user ->
                    _loginState.value = LoginState.Success
                    userManager.currentUser = user
                }, onFailure = { exception ->
                    _loginState.value = LoginState.Error(exception.message!!)
                })

        }
    }

    fun resetState() {
        _loginState.value = LoginState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        resetState()
    }

}

sealed class LoginState {
    data object Initial : LoginState()
    data object Loading : LoginState()
    data object AlreadyLogged : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
