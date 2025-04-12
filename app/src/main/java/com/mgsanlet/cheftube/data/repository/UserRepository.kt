package com.mgsanlet.cheftube.data.repository

import android.content.Context
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource

/**
 * Repositorio que gestiona las operaciones de usuarios
 * @param context Contexto de la aplicación para acceder a los recursos
 * @param userProvider Proveedor de datos de usuarios
 */
class UserRepository(
    private val context: Context, private val userProvider: UserLocalDataSource
) {

    fun createUser(username: String, email: String, password: String): Result<User> {
        return try {
            if (userProvider.getUserByEmailOrUsername(username) != null) {
                Result.failure(Exception(context.getString(R.string.username_already)))
            } else if (userProvider.getUserByEmailOrUsername(email) != null) {
                Result.failure(Exception(context.getString(R.string.email_already)))
            } else {
                val newUser = User.create(username, email, password)
                if (userProvider.insertUser(newUser)) {
                    Result.success(newUser)
                } else {
                    Result.failure(Exception(context.getString(R.string.network_error)))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loginUser(identity: String, password: String): Result<User> {
        return try {
            val user = userProvider.getUserByEmailOrUsername(identity)
            when {
                user == null -> {
                    Result.failure(Exception(context.getString(R.string.invalid_login)))
                }

                !user.verifyPassword(password) -> {
                    Result.failure(Exception(context.getString(R.string.wrong_pwd)))
                }

                else -> {
                    Result.success(user)
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.unknown_error)))
        }
    }

    fun updateUser(user: User, oldPassword: String): Result<User> {
        return try {

            // Obtener usuario actual
            val currentUser = userProvider.getUserById(user.id) ?: return Result.failure(
                Exception(
                    context.getString(R.string.user_not_found)
                )
            )

            // Verificar la contraseña antigua
            if (!currentUser.verifyPassword(oldPassword)) {
                return Result.failure(Exception(context.getString(R.string.wrong_pwd)))
            }

            if (userProvider.updateUser(user)) {
                Result.success(user)
            } else {
                Result.failure(Exception(context.getString(R.string.network_error)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserById(id: String): Result<User> {
        return try {
            val user = userProvider.getUserById(id)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception(context.getString(R.string.user_not_found)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserByName(username: String): Result<User> {
        return try {
            val user = userProvider.getUserByName(username)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception(context.getString(R.string.user_not_found)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserByEmail(email: String): Result<User> {
        return try {
            val user = userProvider.getUserByEmail(email)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception(context.getString(R.string.user_not_found)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
