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

/**
 * ViewModel para la pantalla de inicio de sesión.
 * Este ViewModel maneja la lógica de autenticación, incluyendo el inicio de sesión
 * manual y el inicio de sesión automático. También gestiona los diferentes estados
 * de la interfaz de usuario durante el proceso de autenticación.
 *
 * @property loginUser Caso de uso para el inicio de sesión de usuario
 * @property automaticLogin Caso de uso para el inicio de sesión automático
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUser: LoginUserUseCase,
    private val automaticLogin: AutomaticLoginUseCase
) : ViewModel() {

    /** Estado interno mutable de la UI */
    private val _uiState = MutableLiveData<LoginState>(LoginState.Loading)
    
    /** Estado observable de la UI */
    val uiState: LiveData<LoginState> = _uiState

    /**
     * Intenta realizar un inicio de sesión automático si hay credenciales guardadas.
     * Actualiza el estado de la UI según el resultado.
     */
    fun tryAutoLogin() {
        viewModelScope.launch {
            if (automaticLogin() is DomainResult.Success) { _uiState.value = LoginState.AlreadyLogged }
            else { _uiState.value = LoginState.Initial }
        }
    }

    /**
     * Intenta autenticar al usuario con las credenciales proporcionadas.
     *
     * @param identity Nombre de usuario o email del usuario
     * @param password Contraseña del usuario
     */
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

/**
 * Estados posibles de la UI para la pantalla de inicio de sesión.
 */
sealed class LoginState {
    /** Estado inicial, mostrando el formulario de inicio de sesión */
    data object Initial : LoginState()
    
    /** Cargando, mostrando un indicador de progreso */
    data object Loading : LoginState()
    
    /** El usuario ya tiene una sesión activa */
    data object AlreadyLogged : LoginState()
    
    /** Inicio de sesión exitoso */
    data object Success : LoginState()
    
    /** Error durante el inicio de sesión, contiene detalles del error */
    data class Error(val error: UserError) : LoginState()
}

