package com.mgsanlet.cheftube.data.model

import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

/**
 * Representa un usuario con ID universal, nombre de usuario, email y hash de contraseña.
 */
data class UserDto(
    val id: String = UUID.randomUUID().toString(), // ID único generado automáticamente
    val username: String, val email: String, val passwordHash: String
) {

    /**
     * Crea un nuevo usuario con un hash de contraseña.
     */
    companion object {

        fun create(username: String, email: String, password: String): UserDto {
            val passwordHash = hashPassword(password)
            return UserDto(username = username, email = email, passwordHash = passwordHash)
        }

        private fun hashPassword(rawPassword: String): String {
            return BCrypt.hashpw(rawPassword, BCrypt.gensalt())
        }
    }

    // Método para verificar la contraseña ingresada
    fun verifyPassword(rawPassword: String): Boolean {
        return BCrypt.checkpw(rawPassword, passwordHash)

    }
}