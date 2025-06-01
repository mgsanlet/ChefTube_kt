package com.mgsanlet.cheftube.data.model

import com.google.firebase.Timestamp

/**
 * Representa la información de un usuario en la base de datos.
 *
 * @property username Nombre de usuario único
 * @property email Dirección de correo electrónico del usuario
 * @property bio Breve biografía o descripción del usuario
 * @property hasProfilePicture Indica si el usuario tiene una imagen de perfil
 * @property createdRecipes Lista de IDs de recetas creadas por el usuario
 * @property favouriteRecipes Lista de IDs de recetas marcadas como favoritas
 * @property followersIds Lista de IDs de usuarios que siguen a este usuario
 * @property followingIds Lista de IDs de usuarios que este usuario sigue
 * @property lastLogin Fecha y hora del último inicio de sesión del usuario
 */
data class UserResponse(
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val hasProfilePicture: Boolean = false,
    val createdRecipes: List<String> = emptyList(),
    val favouriteRecipes: List<String> = emptyList(),
    val followersIds: List<String> = emptyList(),
    val followingIds: List<String> = emptyList(),
    val lastLogin: Timestamp = Timestamp.now()
)