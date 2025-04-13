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
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userManager: UserManager
) : ViewModel() {

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
                    userManager.setCurrentUser(user)
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

sealed class SignUpState {
    data object Initial : SignUpState()
    data object Loading : SignUpState()
    data object Success : SignUpState()
    data class Error(val message: String?) : SignUpState()
}