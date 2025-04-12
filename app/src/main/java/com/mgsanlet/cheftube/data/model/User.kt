package com.mgsanlet.cheftube.data.model

import org.mindrot.jbcrypt.BCrypt
import java.io.Serializable
import java.util.UUID

/**
 * Representa un usuario con ID universal, nombre de usuario, email y hash de contraseña.
 */
data class User(
    val id: String = UUID.randomUUID().toString(), // ID único generado automáticamente
    val username: String, val email: String, private val passwordHash: String
) : Serializable {

    /**
     * Crea un nuevo usuario con un hash de contraseña.
     */
    companion object {
        const val PASSWORD_MIN_LENGTH = 5

        fun create(username: String, email: String, password: String): User {
            val passwordHash = hashPassword(password)
            return User(username = username, email = email, passwordHash = passwordHash)
        }

        private fun hashPassword(rawPassword: String): String {
            return BCrypt.hashpw(rawPassword, BCrypt.gensalt())
        }
    }

    // Método para obtener el hash de la contraseña (usado por UserProvider)
    internal fun getPasswordHash(): String = passwordHash

    // Método para verificar la contraseña ingresada
    fun verifyPassword(rawPassword: String): Boolean {
        return BCrypt.checkpw(rawPassword, passwordHash)
    }
}