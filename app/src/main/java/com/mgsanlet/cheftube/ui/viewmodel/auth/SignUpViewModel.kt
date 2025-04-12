package com.mgsanlet.cheftube.ui.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpViewModel(private val app: ChefTubeApplication) : ViewModel() {
    private val userRepository: UserRepository = app.userRepository

    private val _signUpState = MutableLiveData<SignUpState>().apply {
        value = SignUpState.Initial
    }
    val signUpState: LiveData<SignUpState> = _signUpState

    fun trySignUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            delay(3000L)

            val result = withContext(Dispatchers.IO) {
                userRepository.createUser(
                    username, email, password
                )
            }

            result.fold(
                onSuccess = { user ->
                    _signUpState.value = SignUpState.Success
                    app.setCurrentUser(user)
                }, onFailure = { error ->
                    _signUpState.value = SignUpState.Error(error.message)
                })
        }
    }

    fun newUsernameAlreadyExists(username: String): Boolean {
        return !userRepository.getUserByName(username).isFailure
    }

    fun newEmailAlreadyExists(newEmail: String): Boolean {
        return !userRepository.getUserByEmail(newEmail).isFailure
    }

    private fun resetState() {
        _signUpState.value = SignUpState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        resetState()
    }
}

@Suppress("UNCHECKED_CAST")
class SignUpViewModelFactory(
    private val app: ChefTubeApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignUpViewModel(app) as T
    }
}

sealed class SignUpState {
    data object Initial : SignUpState()
    data object Loading : SignUpState()
    data object Success : SignUpState()
    data class Error(val message: String?) : SignUpState()
}