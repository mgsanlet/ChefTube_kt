package com.mgsanlet.cheftube.data.repository

import android.content.Context
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import com.mgsanlet.cheftube.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userLocalDataSource: UserLocalDataSource
) : UserRepository {

    override fun createUser(username: String, email: String, password: String): Result<User> {
        return try {
            if (userLocalDataSource.getUserByEmailOrUsername(username) != null) {
                Result.failure(Exception(context.getString(R.string.username_already)))
            } else if (userLocalDataSource.getUserByEmailOrUsername(email) != null) {
                Result.failure(Exception(context.getString(R.string.email_already)))
            } else {
                val newUser = User.create(username, email, password)
                if (userLocalDataSource.insertUser(newUser)) {
                    Result.success(newUser)
                } else {
                    Result.failure(Exception(context.getString(R.string.network_error)))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun loginUser(emailOrUsername: String, password: String): Result<User> {
        return try {
            val user = userLocalDataSource.getUserByEmailOrUsername(emailOrUsername)
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

    override fun updateUser(user: User, oldPassword: String): Result<User> {
        return try {

            // Obtener usuario actual
            val currentUser = userLocalDataSource.getUserById(user.id) ?: return Result.failure(
                Exception(
                    context.getString(R.string.user_not_found)
                )
            )

            // Verificar la contraseña antigua
            if (!currentUser.verifyPassword(oldPassword)) {
                return Result.failure(Exception(context.getString(R.string.wrong_pwd)))
            }

            if (userLocalDataSource.updateUser(user)) {
                Result.success(user)
            } else {
                Result.failure(Exception(context.getString(R.string.network_error)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserById(userId: String): Result<User> {
        return try {
            val user = userLocalDataSource.getUserById(userId)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception(context.getString(R.string.user_not_found)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserByName(username: String): Result<User> {
        return try {
            val user = userLocalDataSource.getUserByName(username)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception(context.getString(R.string.user_not_found)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserByEmail(userEmail: String): Result<User> {
        return try {
            val user = userLocalDataSource.getUserByEmail(userEmail)
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
