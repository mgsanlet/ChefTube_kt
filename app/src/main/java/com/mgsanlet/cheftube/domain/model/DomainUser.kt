package com.mgsanlet.cheftube.domain.model

/**
 * Clase de dominio que representa a un usuario en la aplicación.
 *
 * @property id Identificador único del usuario
 * @property username Nombre de usuario mostrado en la aplicación
 * @property email Correo electrónico del usuario
 * @property bio Biografía o descripción del perfil del usuario
 * @property profilePictureUrl URL de la imagen de perfil del usuario
 * @property createdRecipes Lista de IDs de las recetas creadas por el usuario
 * @property favouriteRecipes Lista de IDs de las recetas marcadas como favoritas
 * @property followersIds Lista de IDs de los seguidores del usuario
 * @property followingIds Lista de IDs de los usuarios que sigue este usuario
 * @property inactiveDays Número de días que el usuario ha estado inactivo
 */
data class DomainUser(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val createdRecipes: List<String> = emptyList(),
    val favouriteRecipes: List<String> = emptyList(),
    val followersIds: List<String> = emptyList(),
    val followingIds: List<String> = emptyList(),
    val inactiveDays: Int = 0
)