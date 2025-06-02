package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.usecase.user.DeleteAccountUseCase
import com.mgsanlet.cheftube.domain.usecase.user.GetCurrentUserDataUseCase
import com.mgsanlet.cheftube.domain.usecase.user.GetUserDataByIdUseCase
import com.mgsanlet.cheftube.domain.usecase.user.SaveProfilePictureUseCase
import com.mgsanlet.cheftube.domain.usecase.user.UpdateCurrentUserDataUseCase
import com.mgsanlet.cheftube.domain.usecase.user.UpdatePasswordUseCase
import com.mgsanlet.cheftube.domain.usecase.user.UpdateUserDataUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la gestión de perfiles de usuario.
 *
 * Maneja la lógica relacionada con la visualización y edición de perfiles,
 * incluyendo la carga de datos de usuario, actualización de información,
 * gestión de seguidores y cambio de contraseña.
 *
 * @property getUserDataById Caso de uso para obtener datos de usuario por ID
 * @property getCurrentUserData Caso de uso para obtener datos del usuario actual
 * @property updateCurrentUserData Caso de uso para actualizar datos del usuario actual
 * @property updateUserData Caso de uso para actualizar datos de cualquier usuario
 * @property saveProfilePicture Caso de uso para guardar la imagen de perfil
 * @property updatePassword Caso de uso para actualizar la contraseña
 * @property deleteAccount Caso de uso para eliminar la cuenta
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserDataById: GetUserDataByIdUseCase,
    private val getCurrentUserData: GetCurrentUserDataUseCase,
    private val updateCurrentUserData: UpdateCurrentUserDataUseCase,
    private val updateUserData: UpdateUserDataUseCase,
    private val saveProfilePicture: SaveProfilePictureUseCase,
    private val updatePassword: UpdatePasswordUseCase,
    private val deleteAccount: DeleteAccountUseCase
) : ViewModel() {
    /** Estado interno mutable de la UI */
    private val _uiState = MutableLiveData<ProfileState>()
    
    /** Estado observable de la UI */
    val uiState: LiveData<ProfileState> = _uiState
    
    /** Datos del usuario actual o del perfil visualizado */
    private val _userData = MutableLiveData<DomainUser>()
    
    /** Datos observables del usuario */
    val userData: LiveData<DomainUser> = _userData
    
    /** Indica si el perfil mostrado pertenece al usuario actual */
    private val _isCurrentUserProfile = MutableLiveData<Boolean>()
    
    /** Estado observable que indica si es el perfil del usuario actual */
    val isCurrentUserProfile: LiveData<Boolean> = _isCurrentUserProfile
    
    /** Nueva imagen de perfil a guardar */
    private var newProfilePicture: ByteArray? = null

    /**
     * Carga los datos de un usuario por su ID.
     * Actualiza el estado de la UI según el resultado.
     *
     * @param userId ID del usuario a cargar
     */
    fun loadUserDataById(userId: String) {
        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
            getCurrentUserData().fold(
                onSuccess = { currentUser ->
                    _isCurrentUserProfile.value = currentUser.id == userId
                    if (_isCurrentUserProfile.value == true) {
                        _userData.value = currentUser
                        _uiState.value = ProfileState.LoadSuccess
                    }
                },
                onError = { error -> _uiState.value = ProfileState.Error(error) }
            )
            if (_isCurrentUserProfile.value == true) {
                return@launch
            }
            getUserDataById(userId).fold(
                onSuccess = { user ->
                    _isCurrentUserProfile.value = false
                    _userData.value = user
                    _uiState.value = ProfileState.LoadSuccess
                },
                onError = { error -> _uiState.value = ProfileState.Error(error) }
            )
        }
    }

    /**
     * Carga los datos del usuario actual.
     * Actualiza el estado de la UI según el resultado.
     */
    fun loadCurrentUserData() {
        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
            getCurrentUserData().fold(
                onSuccess = { currentUser ->
                    _isCurrentUserProfile.value = true
                    _userData.value = currentUser
                    _uiState.value = ProfileState.LoadSuccess
                },
                onError = { error -> _uiState.value = ProfileState.Error(error) }
            )
        }
    }

    /**
     * Establece una nueva imagen de perfil para el usuario actual.
     *
     * @param newPicture Nueva imagen de perfil en formato ByteArray
     */
    fun setNewProfilePicture(newPicture: ByteArray) {
        newProfilePicture = newPicture
        //loadCurrentUserData()
    }

    /**
     * Intenta actualizar los datos del usuario actual.
     * Si hay una nueva imagen de perfil, también la guarda.
     *
     * @param newUsername Nuevo nombre de usuario
     * @param newBio Nueva biografía del usuario
     * @throws Exception Si no hay datos de usuario cargados
     */
    fun tryUpdateCurrentUserData(newUsername: String, newBio: String) {
        _uiState.value = ProfileState.Loading

        var newUserData = _userData.value?.copy(
            username = newUsername.ifBlank { _userData.value?.username ?: "" },
            bio = newBio.ifBlank { _userData.value?.bio ?: "" }
        ) ?: throw Exception("Null logged user")

        viewModelScope.launch {
            // Primero actualizamos los datos del usuario
            updateCurrentUserData(newUserData).fold(
                onSuccess = {
                    // Si hay nueva imagen, la guardamos
                    newProfilePicture?.let { picture ->
                        saveProfilePicture(picture).fold(
                            onSuccess = {
                                // Recargar los datos del usuario para obtener la nueva URL de la imagen
                                loadCurrentUserData()
                                _uiState.value = ProfileState.SaveSuccess
                                _uiState.value = ProfileState.LoadSuccess
                            },
                            onError = { error -> _uiState.value = ProfileState.Error(error) }
                        )
                    } ?: run {
                        // Si no hay nueva imagen, ya tenemos los datos actualizados
                        _userData.value = newUserData
                        _uiState.value = ProfileState.SaveSuccess
                        _uiState.value = ProfileState.LoadSuccess
                    }
                },
                onError = { error -> _uiState.value = ProfileState.Error(error) }
            )
        }
    }

    /**
     * Verifica si el usuario actual está siguiendo al usuario del perfil mostrado.
     *
     * @return true si el usuario actual está siguiendo al perfil mostrado, false en caso contrario
     */
    fun isUserBeingFollowed(): Boolean {
        var currentUserId: String? = null
        var isBeingFollowed = false
        viewModelScope.launch {
            getCurrentUserData().fold(
                onSuccess = { currentUserData ->
                    currentUserId = currentUserData.id
                },
                onError = { error ->
                    _uiState.value = ProfileState.Error(error)
                    return@launch
                }
            )
            isBeingFollowed = _userData.value?.followersIds?.contains(currentUserId) == true
        }
        return isBeingFollowed
    }

    /**
     * Maneja la acción de seguir o dejar de seguir a un usuario.
     *
     * @param doFollow true para seguir al usuario, false para dejar de seguirlo
     */
    fun followUser(doFollow: Boolean) {
        var currentUserData: DomainUser? = null
        var newUserData: DomainUser? = null
        var newCurrentUserData: DomainUser? = null
        viewModelScope.launch {
            getCurrentUserData().fold(
                onSuccess = { currentUserData = it },
                onError = { error ->
                    _uiState.value = ProfileState.Error(error)
                    return@launch
                }
            )
            try {// Actualizamos las listas de followers y following
                if (doFollow) {
                    newUserData = _userData.value?.copy(
                        followersIds = _userData.value!!.followersIds.plus(currentUserData!!.id)
                    )
                    newCurrentUserData = currentUserData!!.copy(
                        followingIds = currentUserData.followingIds.plus(_userData.value!!.id)
                    )
                } else {
                    newUserData = _userData.value?.copy(
                        followersIds = _userData.value!!.followersIds.filterNot { it == currentUserData!!.id }
                    )
                    newCurrentUserData = currentUserData!!.copy(
                        followingIds = currentUserData.followingIds.filterNot { it == _userData.value!!.id }
                    )
                }
                // Llamamos a los casos de uso de actualización
                var result = updateUserData(newUserData!!)
                if (result is DomainResult.Error) {
                    _uiState.value = ProfileState.Error(result.error)
                    return@launch
                }
                _userData.value = newUserData!!
                result = updateCurrentUserData(newCurrentUserData)
                if (result is DomainResult.Error) {
                    _uiState.value = ProfileState.Error(result.error)
                    return@launch
                }
            } catch (_: Exception) {
                _uiState.value = ProfileState.Error(UserError.Unknown())
            }
        }

    }

    /**
     * Obtiene la lista de IDs de recetas creadas por el usuario.
     *
     * @return Lista de IDs de recetas creadas
     */
    fun getProfileUserCreatedRecipes(): List<String> {
        return _userData.value?.createdRecipes ?: emptyList()
    }

    /**
     * Obtiene la lista de IDs de recetas favoritas del usuario.
     *
     * @return Lista de IDs de recetas favoritas
     */
    fun getProfileUserFavouriteRecipes(): List<String> {
        return _userData.value?.favouriteRecipes ?: emptyList()
    }

    /**
     * Actualiza la contraseña del usuario actual.
     *
     * @param currentPassword Contraseña actual del usuario
     * @param newPassword Nueva contraseña a establecer
     */
    fun updateUserPassword(
        currentPassword: String,
        newPassword: String
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileState.Loading
            
            updatePassword(currentPassword, newPassword).fold(
                onSuccess = {
                    _uiState.value = ProfileState.PasswordUpdated
                },
                onError = { error ->
                    _uiState.value = ProfileState.Error(error)
                }
            )
        }
    }

    /**
     * Elimina la cuenta del usuario actual.
     *
     * @param password Contraseña actual del usuario para confirmar la eliminación
     */
    fun deleteUserAccount(password: String) {
        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
            deleteAccount(password).fold(
                onSuccess = {
                    _uiState.value = ProfileState.AccountDeleted
                },
                onError = { error ->
                    _uiState.value = ProfileState.Error(error)
                }
            )
        }
    }
}

/**
 * Estados posibles de la UI para la pantalla de perfil.
 */
sealed class ProfileState {
    /** Estado de carga, mostrando un indicador de progreso */
    data object Loading : ProfileState()
    
    /** Datos cargados exitosamente */
    data object LoadSuccess : ProfileState()
    
    /** Cambios guardados exitosamente */
    data object SaveSuccess : ProfileState()
    
    /** Contraseña actualizada exitosamente */
    data object PasswordUpdated : ProfileState()
    
    /** Cuenta eliminada exitosamente */
    data object AccountDeleted : ProfileState()
    
    /** Error durante alguna operación */
    data class Error(val error: UserError) : ProfileState()
}

