package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError

/**
 * Interfaz que define las operaciones disponibles para la gestión de usuarios.
 * Todas las operaciones son suspendidas para permitir operaciones asíncronas.
 */
interface UsersRepository {

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param username Nombre de usuario
     * @param email Correo electrónico
     * @param password Contraseña
     * @return [DomainResult] de la operación con Unit en caso de éxito o UserError en caso de error
     */
    suspend fun createUser(username: String, email: String, password: String):
            DomainResult<Unit, UserError>

    /**
     * Inicia sesión con las credenciales proporcionadas.
     *
     * @param email Correo electrónico del usuario
     * @param password Contraseña del usuario
     * @return [DomainResult] de la operación con Unit en caso de éxito o UserError en caso de error
     */
    suspend fun loginUser(email: String, password: String): DomainResult<Unit, UserError>

    /**
     * Actualiza los datos del usuario actual.
     *
     * @param newUserData Nuevos datos del usuario
     * @return [DomainResult] de la operación con Unit en caso de éxito o UserError en caso de error
     */
    suspend fun updateCurrentUserData(newUserData: DomainUser): DomainResult<Unit, UserError>

    /**
     * Actualiza los datos de cualquier usuario (solo para administradores).
     *
     * @param newUserData Nuevos datos del usuario
     * @return [DomainResult] de la operación con Unit en caso de éxito o UserError en caso de error
     */
    suspend fun updateUserData(newUserData: DomainUser): DomainResult<Unit, UserError>

    /**
     * Intenta iniciar sesión automáticamente si hay credenciales guardadas.
     *
     * @return [DomainResult] de la operación con Unit en caso de éxito o UserError en caso de error
     */
    suspend fun tryAutoLogin(): DomainResult<Unit, UserError>

    /**
     * Obtiene los datos del usuario actual.
     *
     * @return [DomainResult] con los datos del usuario o error
     */
    suspend fun getCurrentUserData(): DomainResult<DomainUser, UserError>

    /**
     * Obtiene los datos de un usuario por su ID.
     *
     * @param userId ID del usuario a buscar
     * @return [DomainResult] con los datos del usuario o error
     */
    suspend fun getUserDataById(userId: String): DomainResult<DomainUser, UserError>

    /**
     * Actualiza la lista de recetas favoritas del usuario actual.
     *
     * @param recipeId ID de la receta a actualizar
     * @param isNewFavourite true para marcar como favorita, false para quitar de favoritos
     * @return [DomainResult] de la operación con Unit en caso de éxito o error
     */
    suspend fun updateFavouriteRecipes(recipeId: String, isNewFavourite: Boolean): DomainResult<Unit, UserError>

    /**
     * Guarda la imagen de perfil del usuario actual.
     *
     * @param profilePicture Imagen en formato ByteArray
     * @return [DomainResult] de la operación con Unit en caso de éxito o error
     */
    suspend fun saveProfilePicture(profilePicture: ByteArray): DomainResult<Unit, UserError>

    /**
     * Actualiza la contraseña del usuario actual.
     *
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @return [DomainResult] de la operación con Unit en caso de éxito o error
     */
    suspend fun updatePassword(currentPassword: String, newPassword: String): DomainResult<Unit, UserError>

    /**
     * Elimina la cuenta del usuario actual.
     *
     * @param password Contraseña actual para confirmar la eliminación
     * @return [DomainResult] de la operación con Unit en caso de éxito o error
     */
    suspend fun deleteAccount(password: String): DomainResult<Unit, UserError>

    /**
     * Obtiene la lista de usuarios inactivos (solo para administradores).
     *
     * @return Lista de usuarios inactivos o error
     */
    suspend fun getInactiveUsers(): DomainResult<List<DomainUser>, UserError>

    /**
     * Verifica si el usuario actual es administrador.
     *
     * @return true si es administrador, false en caso contrario
     */
    suspend fun isCurrentUserAdmin(): DomainResult<Boolean, UserError>
    
    /**
     * Limpia la caché de usuarios.
     */
    fun clearCache()
    
    /**
     * Cierra la sesión del usuario actual.
     */
    fun logout()
}