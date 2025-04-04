package com.mgsanlet.cheftube.data.repository

import android.content.Context
import android.util.Log
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.data.provider.UserProvider

/**
 * Repositorio que gestiona las operaciones de usuarios
 * @param context Contexto de la aplicación para acceder a los recursos
 * @param userProvider Proveedor de datos de usuarios
 */
class UserRepository(
    private val context: Context,
    private val userProvider: UserProvider
) {
    companion object {
        private const val TAG = "UserRepository"
    }

    fun createUser(username: String, email: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Intentando crear usuario con email: $email")
            val existingUser = userProvider.getUserByEmail(email)
            if (existingUser != null) {
                Log.d(TAG, "Email ya existe: $email")
                Result.failure(Exception(context.getString(R.string.email_already)))
            } else {
                val newUser = User.create(username, email, password)
                if (userProvider.insertUser(newUser)) {
                    Log.d(TAG, "Usuario creado exitosamente: ${newUser.username}")
                    Result.success(newUser)
                } else {
                    Log.e(TAG, "Error al insertar usuario en la base de datos")
                    Result.failure(Exception(context.getString(R.string.network_error)))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear usuario", e)
            Result.failure(e)
        }
    }

    fun loginUser(identity: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Intentando login para identidad: $identity")
            val user = userProvider.getUserByEmailOrUsername(identity)
            when {
                user == null -> {
                    Log.d(TAG, "Usuario no encontrado para identidad: $identity")
                    Result.failure(Exception(context.getString(R.string.invalid_login)))
                }
                !user.verifyPassword(password) -> {
                    Log.d(TAG, "Contraseña incorrecta para usuario: ${user.username}")
                    Result.failure(Exception(context.getString(R.string.wrong_pwd)))
                }
                else -> {
                    Log.d(TAG, "Login exitoso para usuario: ${user.username}")
                    Result.success(user)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en proceso de login", e)
            Result.failure(e)
        }
    }

    fun updateUser(user: User, oldPassword: String): Result<User> {
        return try {
            Log.d(TAG, "Intentando actualizar usuario: ${user.username}")
            
            // Obtener usuario actual
            val currentUser = userProvider.getUserById(user.id)
            if (currentUser == null) {
                Log.d(TAG, "Usuario no encontrado: ${user.id}")
                return Result.failure(Exception(context.getString(R.string.user_not_found)))
            }

            // Verificar la contraseña antigua
            if (!currentUser.verifyPassword(oldPassword)) {
                Log.d(TAG, "Contraseña antigua incorrecta para usuario: ${currentUser.username}")
                return Result.failure(Exception(context.getString(R.string.wrong_pwd)))
            }

            if (userProvider.updateUser(user)) {
                Log.d(TAG, "Usuario actualizado exitosamente: ${user.username}")
                Result.success(user)
            } else {
                Log.e(TAG, "Error al actualizar usuario en la base de datos")
                Result.failure(Exception(context.getString(R.string.network_error)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar usuario", e)
            Result.failure(e)
        }
    }

    fun getUserById(id: String): Result<User> {
        return try {
            Log.d(TAG, "Intentando obtener usuario por ID: $id")
            val user = userProvider.getUserById(id)
            if (user != null) {
                Log.d(TAG, "Usuario encontrado: ${user.username}")
                Result.success(user)
            } else {
                Log.d(TAG, "Usuario no encontrado para ID: $id")
                Result.failure(Exception(context.getString(R.string.user_not_found)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuario por ID", e)
            Result.failure(e)
        }
    }
}
