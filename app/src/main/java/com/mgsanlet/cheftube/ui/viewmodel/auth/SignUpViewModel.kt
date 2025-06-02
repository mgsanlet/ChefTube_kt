package com.mgsanlet.cheftube.ui.viewmodel.auth

/**
 * Módulo de ViewModel para la pantalla de registro de usuario.
 *

 */

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.usecase.user.CreateUserUseCase
import com.mgsanlet.cheftube.domain.util.error.UserError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel para la pantalla de registro de usuario.
 * Este ViewModel maneja la lógica de creación de nuevas cuentas de usuario,
 * incluyendo la validación de datos y la comunicación con la capa de dominio.
 * Gestiona los diferentes estados de la interfaz de usuario durante el proceso de registro.
 *
 * @property createUser Caso de uso para la creación de nuevos usuarios
 */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val createUser: CreateUserUseCase
) : ViewModel() {

    /** Estado interno mutable de la UI */
    private val _uiState = MutableLiveData<SignUpState>(SignUpState.Initial)
    
    /** Estado observable de la UI */
    val uiState: LiveData<SignUpState> = _uiState

    /**
     * Intenta registrar un nuevo usuario con los datos proporcionados.
     *
     * @param username Nombre de usuario deseado
     * @param email Correo electrónico del usuario
     * @param password Contraseña para la nueva cuenta
     */
    fun trySignUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = SignUpState.Loading

            val result = withContext(Dispatchers.IO) {
                createUser(username, email, password)
            }

            result.fold(
                onSuccess = {
                    _uiState.value = SignUpState.Success
                }, onError = { error ->
                    _uiState.value = SignUpState.Error(error)
                })
        }
    }

    /**
     * Reinicia el estado de la UI al estado inicial.
     * Útil para limpiar el estado después de ciertas acciones.
     */
    private fun resetState() {
        _uiState.value = SignUpState.Initial
    }

    /**
     * Se llama cuando el ViewModel ya no se usará y será destruido.
     * Aquí se limpian los recursos y se resetea el estado.
     */
    override fun onCleared() {
        super.onCleared()
        resetState()
    }
}

/**
 * Estados posibles de la UI para la pantalla de registro.
 */
sealed class SignUpState {
    /** Estado inicial, mostrando el formulario de registro */
    data object Initial : SignUpState()
    
    /** Cargando, mostrando un indicador de progreso */
    data object Loading : SignUpState()
    
    /** Registro exitoso */
    data object Success : SignUpState()
    
    /** Error durante el registro, contiene detalles del error */
    data class Error(val error: UserError) : SignUpState()
}
