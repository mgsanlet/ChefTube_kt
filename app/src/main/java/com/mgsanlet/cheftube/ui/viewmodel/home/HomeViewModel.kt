package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.usecase.user.IsCurrentUserAdminUseCase
import com.mgsanlet.cheftube.domain.usecase.user.LogoutUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.ui.util.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel para la pantalla principal de la aplicación.
 *
 * Maneja la lógica relacionada con la interfaz de usuario principal,
 * incluyendo la verificación de roles de administrador y la gestión de preferencias de idioma.
 *
 * @property logout Caso de uso para cerrar la sesión del usuario
 * @property localeManager Gestor de configuración regional
 * @property isCurrentUserAdmin Caso de uso para verificar si el usuario actual es administrador
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logout: LogoutUseCase,
    private val localeManager: LocaleManager,
    private val isCurrentUserAdmin: IsCurrentUserAdminUseCase
) : ViewModel() {

    /** Estado interno mutable del rol de administrador */
    private val _isAdmin = MutableLiveData<Boolean>(false)
    
    /** Estado observable que indica si el usuario actual es administrador */
    val isAdmin: LiveData<Boolean> = _isAdmin

    init {
        checkAdminStatus()
    }

    /**
     * Verifica si el usuario actual tiene rol de administrador.
     * Actualiza el estado [isAdmin] con el resultado de la verificación.
     */
    private fun checkAdminStatus() {
        viewModelScope.launch {
            val result = isCurrentUserAdmin()
            _isAdmin.value = result is DomainResult.Success && result.data
        }
    }

    /**
     * Establece la configuración regional de la aplicación.
     *
     * @param locale Configuración regional a establecer
     */
    fun setLocale(locale: Locale) {
        localeManager.setLocale(locale)
    }

    /**
     * Maneja el proceso de cierre de sesión del usuario.
     * Ejecuta el caso de uso de cierre de sesión.
     */
    fun handleLogout() {
        logout()
    }
}