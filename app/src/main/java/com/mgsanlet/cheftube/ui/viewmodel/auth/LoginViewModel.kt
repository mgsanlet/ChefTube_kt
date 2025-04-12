package com.mgsanlet.cheftube.ui.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val app: ChefTubeApplication) : ViewModel() {
    private val userRepository: UserRepository = app.userRepository

    private val _loginState = MutableLiveData<LoginState>().apply {
        value = LoginState.Initial
    }
    val loginState: LiveData<LoginState> = _loginState

    init {
        checkCurrentUser()
    }

    private fun LoginViewModel.checkCurrentUser() {
        app.getCurrentUser()?.let { user ->
            _loginState.value = LoginState.AlreadyLogged(user)
        }
    }

    fun tryLogin(identity: String, password: String) {

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            delay(3000L)
            try {
                val result = withContext(Dispatchers.IO) {
                    userRepository.loginUser(identity, password)
                }

                result.fold(
                    onSuccess = { user ->
                        _loginState.value = LoginState.Success(user)
                        app.setCurrentUser(user)
                    },
                    onFailure = { exception ->
                        _loginState.value = LoginState.Error(exception.message!!)
                    }
                )
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message!!)
            }
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

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(
    private val app: ChefTubeApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(app) as T
    }
}

sealed class LoginState {
    data object Initial : LoginState()
    data object Loading : LoginState()
    data class AlreadyLogged(val user: User) : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}
