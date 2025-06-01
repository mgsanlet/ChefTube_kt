package com.mgsanlet.cheftube.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.Timestamp.Companion.now
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.data.util.Constants
import com.mgsanlet.cheftube.data.util.Constants.Errors.EMAIL_IN_USE
import com.mgsanlet.cheftube.data.util.Constants.Errors.PASSWORD_PATTERN
import com.mgsanlet.cheftube.data.util.Constants.Errors.USER_NOT_FOUND_CREATE
import com.mgsanlet.cheftube.data.util.Constants.Errors.WEAK_PASSWORD
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import kotlinx.coroutines.tasks.await
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de [UsersRepository] que gestiona las operaciones relacionadas con usuarios.
 * Incluye caché para el usuario actual para mejorar el rendimiento.
 *
 * @property api Cliente de Firebase para operaciones de autenticación y base de datos
 * @property currentUserCache Almacena en caché los datos del usuario actual
 */
@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val api: FirebaseApi
) : UsersRepository {

    /**
     * Almacena en caché los datos del usuario actual para acceso rápido.
     * Se actualiza al realizar operaciones que modifican los datos del usuario.
     */
    var currentUserCache: DomainUser? = null

    /**
     * Obtiene los datos del usuario actual desde la caché o de la base de datos.
     * Si el usuario está en caché, lo devuelve directamente.
     * Si no, lo obtiene de la base de datos y lo guarda en caché.
     *
     * @return [DomainResult] con los datos del usuario o un error
     */
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

    /**
     * Limpia la caché del usuario actual.
     * Útil para forzar una actualización de datos en la próxima solicitud.
     */
    override fun clearCache() {
        currentUserCache = null
    }
    
    /**
     * Verifica si el usuario actual tiene permisos de administrador.
     *
     * @return [DomainResult] con true si es administrador, false si no lo es, o un error
     */
    override suspend fun isCurrentUserAdmin(): DomainResult<Boolean, UserError> {
        val currentUserId = api.auth.currentUser?.uid ?: return DomainResult.Error(
            UserError.UserNotFound)
        return api.isUserAdmin(currentUserId)
    }

    /**
     * Obtiene los datos de un usuario por su ID.
     * Incluye información como seguidores, recetas creadas, etc.
     *
     * @param userId ID del usuario a buscar
     * @return [DomainResult] con los datos del usuario o un error
     */
    override suspend fun getUserDataById(userId: String): DomainResult<DomainUser, UserError> {
        return when (val result = api.getUserDataById(userId)) {
            is DomainResult.Success -> {
                val userResponse = result.data
                val inactiveDays = calculateInactiveDays(userResponse.lastLogin)
                
                DomainResult.Success(
                    DomainUser(
                        id = userId,
                        username = userResponse.username,
                        email = userResponse.email,
                        bio = userResponse.bio,
                        profilePictureUrl = if (userResponse.hasProfilePicture) 
                            api.getStorageUrlFromPath(
                                Constants.Storage.getProfilePicturePath(userId)) else "",
                        createdRecipes = userResponse.createdRecipes,
                        favouriteRecipes = userResponse.favouriteRecipes,
                        followersIds = userResponse.followersIds,
                        followingIds = userResponse.followingIds,
                        inactiveDays = inactiveDays
                    )
                )
            }

            is DomainResult.Error -> DomainResult.Error(result.error)
        }
    }

    /**
     * Crea un nuevo usuario en el sistema con autenticación por correo electrónico.
     * Valida que el nombre de usuario esté disponible antes de crearlo.
     *
     * @param username Nombre de usuario único
     * @param email Correo electrónico del usuario
     * @param password Contraseña (debe cumplir con los requisitos de Firebase)
     * @return [DomainResult] con Unit si fue exitoso, o un error
     * @throws FirebaseAuthUserCollisionException Si el correo ya está en uso
     */
    override suspend fun createUser(
        username: String, email: String, password: String
    ): DomainResult<Unit, UserError> {
        try {
            val isAvailableResult = api.isAvailableUsername(username)
            if (isAvailableResult is DomainResult.Error) {
                return isAvailableResult
            }
            api.auth.createUserWithEmailAndPassword(email, password).await()
            val user = api.auth.currentUser ?: throw Exception(USER_NOT_FOUND_CREATE)

            val insertDataResult = api.insertUserData(user.uid, username, email)
            if (insertDataResult is DomainResult.Error) {
                return insertDataResult
            }

            // Actualizar el lastLogin después de crear el usuario
            api.updateUserLastLogin(user.uid)

            currentUserCache = DomainUser(
                id = user.uid, username = username, email = email, profilePictureUrl = ""
            )
            return DomainResult.Success(Unit)
        } catch (e: Exception) {
            return if (e is FirebaseAuthException) {
                when (e.errorCode) {
                    EMAIL_IN_USE -> DomainResult.Error(UserError.EmailInUse)
                    WEAK_PASSWORD -> DomainResult.Error(UserError.InvalidPasswordPattern)
                    else -> DomainResult.Error(UserError.Unknown(e.message))
                }
            } else {
                if (e.message?.contains(PASSWORD_PATTERN) == true) {
                    DomainResult.Error(UserError.PasswordTooShort)
                } else {
                    DomainResult.Error(UserError.Unknown(e.message))
                }
            }

        }
    }

    /**
     * Inicia sesión con correo electrónico y contraseña.
     * Actualiza la fecha de último inicio de sesión.
     *
     * @param email Correo electrónico del usuario
     * @param password Contraseña del usuario
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
    override suspend fun loginUser(email: String, password: String): DomainResult<Unit, UserError> {
        try {
            api.auth.signInWithEmailAndPassword(email, password).await()
            api.auth.currentUser ?: throw Exception("User not found after login")
            // Actualizar el lastLogin después del login
            api.auth.currentUser?.uid?.let { userId ->
                api.updateUserLastLogin(userId)
            }
            // Se cachean los datos del usuario actual mediante getCurrentUserData()
            return when (val result = getCurrentUserData()) {
                is DomainResult.Success -> {
                    DomainResult.Success(Unit)
                }
                is DomainResult.Error -> DomainResult.Error(result.error)
            }
        } catch (e: Exception) {
            return when (e) {
                is FirebaseAuthInvalidUserException ->
                    DomainResult.Error(UserError.UserNotFound)
                is FirebaseAuthInvalidCredentialsException ->
                    DomainResult.Error(UserError.WrongCredentials)
                else -> DomainResult.Error(UserError.Unknown(e.message))
            }
        }
    }

    /**
     * Actualiza los datos del usuario actual en la base de datos.
     * Actualiza la caché local si la operación es exitosa.
     *
     * @param newUserData Nuevos datos del usuario
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
    override suspend fun updateCurrentUserData(newUserData: DomainUser):
            DomainResult<Unit, UserError> {
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

    /**
     * Actualiza los datos de un usuario en la base de datos.
     * Valida que el nuevo nombre de usuario no esté en uso.
     *
     * @param newUserData Nuevos datos del usuario
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
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
            if (newUserData.username != old.username &&
                api.isAvailableUsername(newUserData.username) is DomainResult.Error) {
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

    /**
     * Intenta iniciar sesión automáticamente si hay un usuario autenticado.
     * Actualiza la fecha de último inicio de sesión.
     *
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
    override suspend fun tryAutoLogin(): DomainResult<Unit, UserError> {
        api.auth.currentUser?.let { user ->
            // Actualizar el lastLogin después del autologin
            api.updateUserLastLogin(user.uid)
            
            // Cacheamos el usuario
            val result = getCurrentUserData()
            result.fold(onSuccess = {
                return DomainResult.Success(Unit)
            }, onError = { error ->
                logout()
                return DomainResult.Error(error)
            })
        } ?: return DomainResult.Error(UserError.UserNotFound)
    }

    /**
     * Actualiza la contraseña del usuario actual.
     * Requiere la contraseña actual para confirmar la identidad.
     *
     * @param currentPassword Contraseña actual del usuario
     * @param newPassword Nueva contraseña
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
    override suspend fun updatePassword(currentPassword: String, newPassword: String):
            DomainResult<Unit, UserError> {
        return try {
            // Reautenticar al usuario
            val user = api.auth.currentUser
            val email = user?.email ?: return DomainResult.Error(UserError.UserNotFound)

            // Reautenticar con el correo y contraseña actuales
            val credential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
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

    /**
     * Elimina la cuenta del usuario actual después de verificar la contraseña.
     * Elimina tanto los datos de autenticación como los datos del usuario en Firestore y Storage.
     *
     * @param password Contraseña actual del usuario para verificación
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
    override suspend fun deleteAccount(password: String): DomainResult<Unit, UserError> {
        return try {
            // Reautenticar al usuario
            val user = api.auth.currentUser ?: throw Exception(Constants.Errors.USER_NOT_FOUND_LOGIN)
            val email = user.email ?: return DomainResult.Error(UserError.UserNotFound)

            val credential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
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

    /**
     * Cierra la sesión del usuario actual y limpia la caché local.
     * No devuelve ningún resultado ya que siempre tiene éxito.
     */
    override fun logout() {
        currentUserCache = null
        api.auth.signOut()
    }

    /**
     * Actualiza la lista de recetas favoritas del usuario actual.
     *
     * @param recipeId ID de la receta a añadir o eliminar de favoritos
     * @param isNewFavourite true para añadir a favoritos, false para quitar
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
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

    /**
     * Guarda una nueva imagen de perfil para el usuario actual.
     * Actualiza tanto el almacenamiento como la referencia en la base de datos.
     *
     * @param profilePicture Imagen de perfil en formato ByteArray
     * @return [DomainResult] con Unit si fue exitoso, o un error
     */
    override suspend fun saveProfilePicture(profilePicture: ByteArray):
            DomainResult<Unit, UserError> {
        try {
            // Obtener datos actuales del usuario
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

            // Actualizar datos del usuario con la nueva URL de la imagen
            val updatedUser = currentUser.copy(profilePictureUrl = downloadUrl)
            val updateResult = updateCurrentUserData(updatedUser)

            // Actualizar caché
            currentUserCache = updatedUser

            return updateResult
        } catch (e: Exception) {
            return DomainResult.Error(UserError.Unknown(e.message))
        }
    }
    
    /**
     * Obtiene una lista de usuarios inactivos (más de 30 días sin iniciar sesión).
     * Principalmente para uso administrativo.
     *
     * @return [DomainResult] con la lista de usuarios inactivos o un error
     */
    override suspend fun getInactiveUsers(): DomainResult<List<DomainUser>, UserError> {
        return when (val result = api.getAllUsers()) {
            is DomainResult.Success -> {
                val inactiveUsers = result.data.mapNotNull { (userId, userResponse) ->
                    val inactiveDays = calculateInactiveDays(userResponse.lastLogin)

                    // Solo incluir usuarios inactivos (más de 30 días sin iniciar sesión)
                    if (inactiveDays > 30) {
                        DomainUser(
                            id = userId,
                            username = userResponse.username,
                            email = userResponse.email,
                            bio = userResponse.bio,
                            profilePictureUrl = if (userResponse.hasProfilePicture)
                                api.getStorageUrlFromPath(
                                    Constants.Storage.getProfilePicturePath(userId)) else "",
                            createdRecipes = userResponse.createdRecipes,
                            favouriteRecipes = userResponse.favouriteRecipes,
                            followersIds = userResponse.followersIds,
                            followingIds = userResponse.followingIds,
                            inactiveDays = inactiveDays
                        )
                    } else {
                        null
                    }
                }
                DomainResult.Success(inactiveUsers)
            }
            is DomainResult.Error -> DomainResult.Error(result.error)
        }
    }

    /**
     * Calcula los días de inactividad de un usuario.
     *
     * @param lastLogin Fecha del último inicio de sesión
     * @return Número de días desde el último inicio de sesión, o 0 si nunca ha iniciado sesión
     */
    private fun calculateInactiveDays(lastLogin: Timestamp?): Int {
            return ChronoUnit.DAYS.between(
                lastLogin?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate(),
                now().toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            ).toInt().coerceAtLeast(0)
    }
}
