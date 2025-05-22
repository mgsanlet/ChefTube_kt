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
import com.mgsanlet.cheftube.domain.usecase.user.UpdateEmailUseCase
import com.mgsanlet.cheftube.domain.usecase.user.UpdatePasswordUseCase
import com.mgsanlet.cheftube.domain.usecase.user.UpdateUserDataUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserDataById: GetUserDataByIdUseCase,
    private val getCurrentUserData: GetCurrentUserDataUseCase,
    private val updateCurrentUserData: UpdateCurrentUserDataUseCase,
    private val updateUserData: UpdateUserDataUseCase,
    private val saveProfilePicture: SaveProfilePictureUseCase,
    private val updateEmail: UpdateEmailUseCase,
    private val updatePassword: UpdatePasswordUseCase,
    private val deleteAccount: DeleteAccountUseCase
) : ViewModel() {
    private val _uiState = MutableLiveData<ProfileState>()
    val uiState: LiveData<ProfileState> = _uiState
    private val _userData = MutableLiveData<DomainUser>()
    val userData: LiveData<DomainUser> = _userData
    private val _isCurrentUserProfile = MutableLiveData<Boolean>()
    val isCurrentUserProfile: LiveData<Boolean> = _isCurrentUserProfile
    private var newProfilePicture: ByteArray? = null

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

    fun setNewProfilePicture(newPicture: ByteArray) {
        newProfilePicture = newPicture
        loadCurrentUserData()
    }

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

    fun getProfileUserCreatedRecipes(): List<String> {
        return _userData.value?.createdRecipes ?: emptyList()
    }

    fun getProfileUserFavouriteRecipes(): List<String> {
        return _userData.value?.favouriteRecipes ?: emptyList()
    }

    fun updateUserPassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileState.Loading
            
            updatePassword(currentPassword, newPassword, confirmPassword).fold(
                onSuccess = {
                    _uiState.value = ProfileState.PasswordUpdated
                },
                onError = { error ->
                    _uiState.value = ProfileState.Error(error)
                }
            )
        }
    }

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

sealed class ProfileState {
    data object Loading : ProfileState()
    data object LoadSuccess : ProfileState()
    data object SaveSuccess : ProfileState()
    data object EmailUpdated : ProfileState()
    data object PasswordUpdated : ProfileState()
    data object AccountDeleted : ProfileState()
    data class Error(val error: UserError) : ProfileState()
}

