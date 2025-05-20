package com.mgsanlet.cheftube.data.repository

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val api: FirebaseApi
) : UsersRepository {

    private var currentUserCache: DomainUser? = null

    override suspend fun getCurrentUserData(): DomainResult<DomainUser, UserError> {
        currentUserCache?.let {
            return DomainResult.Success(currentUserCache!!)
        } ?: when (val result = getUserDataById(api.auth.currentUser!!.uid)) {
            is DomainResult.Success -> {
                currentUserCache = result.data
                return DomainResult.Success(result.data)
            }

            is DomainResult.Error -> return DomainResult.Error(result.error)
        }
    }

    override suspend fun getUserDataById(userId: String): DomainResult<DomainUser, UserError> {
        return when (val result = api.getUserDataById(userId)) {
            is DomainResult.Success -> {
                DomainResult.Success(
                    DomainUser(
                        id = userId,
                        username = result.data.username,
                        email = result.data.email,
                        bio = result.data.bio,
                        profilePictureUrl = api.getStorageUrlFromPath(
                            if (result.data.hasProfilePicture) "profile_pictures/${userId}.jpg"
                            else ""),
                        createdRecipes = result.data.createdRecipes,
                        favouriteRecipes = result.data.favouriteRecipes,
                        followersIds = result.data.followersIds,
                        followingIds = result.data.followingIds
                    )
                )
            }

            is DomainResult.Error -> DomainResult.Error(result.error)
        }
    }

    override suspend fun createUser(
        username: String, email: String, password: String
    ): DomainResult<Unit, UserError> {
        try {
            val isAvailableResult = api.isAvailableUsername(username)
            if (isAvailableResult is DomainResult.Error) {
                return isAvailableResult
            }
            api.auth.createUserWithEmailAndPassword(email, password).await()
            val user = api.auth.currentUser ?: throw Exception("User not found after creation")

            val insertDataResult = api.insertUserData(user.uid, username, email)
            if (insertDataResult is DomainResult.Error) {
                return insertDataResult
            }

            currentUserCache = DomainUser(
                id = user.uid, username = username, email = email, profilePictureUrl = ""
            )
            return DomainResult.Success(Unit)
        } catch (e: Exception) {
            return if (e is FirebaseAuthException) {
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> DomainResult.Error(UserError.EmailInUse)
                    "WEAK_PASSWORD" -> DomainResult.Error(UserError.InvalidPasswordPattern)
                    else -> DomainResult.Error(UserError.Unknown(e.message))
                }
            } else {
                DomainResult.Error(UserError.Unknown(e.message))
            }
        }
    }

    override suspend fun loginUser(
        email: String, password: String
    ): DomainResult<Unit, UserError> {
        try {
            api.auth.signInWithEmailAndPassword(email, password).await()
            // Se capturan excepciones de validación en el catch
            api.auth.currentUser ?: throw Exception("User not found after login")
            // Se cachean los datos del usuario actual mediante getCurrentUserData()
            return when (val result = getCurrentUserData()) {
                is DomainResult.Success -> {
                    DomainResult.Success(Unit)
                }

                is DomainResult.Error -> DomainResult.Error(result.error)
            }
        } catch (e: Exception) {
            return when (e) {
                is FirebaseAuthInvalidUserException -> DomainResult.Error(UserError.UserNotFound)
                is FirebaseAuthInvalidCredentialsException -> DomainResult.Error(UserError.WrongCredentials)
                else -> DomainResult.Error(UserError.Unknown(e.message))
            }
        }
    }

    override suspend fun updateCurrentUserData(newUserData: DomainUser): DomainResult<Unit, UserError> {
        currentUserCache?.let {
            var result = updateUserData(newUserData)
            if (result is DomainResult.Success) {
                currentUserCache = newUserData
                return DomainResult.Success(Unit)
            } else {
                return result
            }
        } ?: throw Exception("User not found after login")
    }

    override suspend fun updateUserData(newUserData: DomainUser): DomainResult<Unit, UserError> {
        //Obtenemos los datos antiguos según el Id
        var oldUserData: DomainUser? = null
        getUserDataById(newUserData.id).fold(onSuccess = { userData ->
            oldUserData = userData
        }, onError = { error ->
            return DomainResult.Error(error)
        })

        oldUserData?.let { old ->
            // Comprobamos que no se esté intentando cambiar el username a uno ya existente
            if (newUserData.username != old.username && api.isAvailableUsername(newUserData.username) is DomainResult.Error) {
                return DomainResult.Error(UserError.UsernameInUse)
            }

            // Actualizamos los datos que pueden cambiar
            val updatedUserData = old.copy(
                username = newUserData.username.ifBlank { old.username },
                bio = newUserData.bio.ifBlank { old.bio },
                profilePictureUrl = newUserData.profilePictureUrl.ifBlank { old.profilePictureUrl },
                followersIds = newUserData.followersIds,
                followingIds = newUserData.followingIds
            )
            return api.updateUserData(old.id, updatedUserData)

        } ?: return DomainResult.Error(UserError.UserNotFound)
    }

    override suspend fun tryAutoLogin(): DomainResult<Unit, UserError> {

        api.auth.currentUser?.let {
            var result = getCurrentUserData() //Cacheamos el usuario
            result.fold(onSuccess = {
                return DomainResult.Success(Unit)
            }, onError = { error ->
                logout()
                return DomainResult.Error(error)
            })
        } ?: return DomainResult.Error(UserError.UserNotFound)
    }

    override suspend fun updateEmail(newEmail: String, password: String): DomainResult<Unit, UserError> {
        return try {
            val user = api.auth.currentUser ?: throw Exception("User not found after login")

            // Reautenticar al usuario
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(user.email ?: "", password)
            user.reauthenticate(credential).await()

            // Actualizar el correo electrónico
            user.verifyBeforeUpdateEmail(newEmail).await()


            // Actualizar el correo en la base de datos
            val currentUser = getCurrentUserData().fold(
                onSuccess = { it },
                onError = { return DomainResult.Error(it) }
            )
            val updatedUser = currentUser.copy(email = newEmail)
            api.updateUserData(currentUser.id, updatedUser)

            // Actualizar caché
            currentUserCache = updatedUser

            DomainResult.Success(Unit)
        } catch (_: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            DomainResult.Error(UserError.EmailInUse)
        } catch (_: FirebaseAuthInvalidCredentialsException) {
            DomainResult.Error(UserError.WrongCredentials)
        } catch (_: FirebaseAuthInvalidUserException) {
            DomainResult.Error(UserError.UserNotFound)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String): DomainResult<Unit, UserError> {
        return try {
            // Reautenticar al usuario
            val user = api.auth.currentUser
            val email = user?.email ?: return DomainResult.Error(UserError.UserNotFound)

            // Reautenticar con el correo y contraseña actuales
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            // Actualizar la contraseña
            user.updatePassword(newPassword).await()

            DomainResult.Success(Unit)
        } catch (_: FirebaseAuthInvalidCredentialsException) {
            DomainResult.Error(UserError.WrongCredentials)
        } catch (_: FirebaseAuthInvalidUserException) {
            DomainResult.Error(UserError.UserNotFound)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun deleteAccount(password: String): DomainResult<Unit, UserError> {
        return try {
            // Reautenticar al usuario
            val user = api.auth.currentUser ?: throw Exception("User not found after login")
            val email = user.email ?: return DomainResult.Error(UserError.UserNotFound)
            
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            
            // Eliminar los datos del usuario de Firestore y Storage
            val deleteDataResult = api.deleteUserData(user.uid)
            if (deleteDataResult is DomainResult.Error) {
                return deleteDataResult
            }
            
            // Eliminar la cuenta de autenticación
            user.delete().await()
            
            // Limpiar caché
            currentUserCache = null

            DomainResult.Success(Unit)
        } catch (_: FirebaseAuthInvalidCredentialsException) {
            DomainResult.Error(UserError.WrongCredentials)
        } catch (_: FirebaseAuthInvalidUserException) {
            DomainResult.Error(UserError.UserNotFound)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override fun logout() {
        currentUserCache = null
        api.auth.signOut()
    }

    override suspend fun updateFavouriteRecipes(
        recipeId: String, isNewFavourite: Boolean
    ): DomainResult<Unit, UserError> {
        var userResult = getCurrentUserData()
        if (userResult is DomainResult.Error) {
            return DomainResult.Error(userResult.error)
        }
        var updateResult = api.updateUserFavouriteRecipes(
            (userResult as DomainResult.Success).data.id, recipeId, isNewFavourite
        )
        updateResult.fold(onSuccess = {
            // Actualizar el caché
            currentUserCache = null
            userResult = getCurrentUserData()
            if (userResult is DomainResult.Error) {
                return DomainResult.Error(userResult.error)
            }
            return DomainResult.Success(Unit)
        }, onError = { error ->
            return DomainResult.Error(error)
        })
    }

    override suspend fun saveProfilePicture(profilePicture: ByteArray): DomainResult<Unit, UserError> {
        try {
            // Get current user data
            val currentUserResult = getCurrentUserData()
            if (currentUserResult is DomainResult.Error) {
                return DomainResult.Error(currentUserResult.error)
            }

            val currentUser = (currentUserResult as DomainResult.Success).data
            val saveResult = api.saveUserProfilePicture(currentUser.id, profilePicture)
            if (saveResult is DomainResult.Error) {
                return DomainResult.Error(saveResult.error)
            }
            val downloadUrl = (saveResult as DomainResult.Success).data
            // Update user data with new profile picture URL

            val updatedUser = currentUser.copy(profilePictureUrl = downloadUrl)
            val updateResult = updateCurrentUserData(updatedUser)

            currentUserCache = updatedUser

            return updateResult
        } catch (e: Exception) {
            return DomainResult.Error(UserError.Unknown(e.message))
        }
    }
}
