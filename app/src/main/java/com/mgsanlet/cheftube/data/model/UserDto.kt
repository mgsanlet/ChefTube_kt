package com.mgsanlet.cheftube.data.model

/**
 * Representa un usuario con ID universal, nombre de usuario, email y hash de contraseña.
 */
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val password: String
)