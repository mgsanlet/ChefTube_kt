package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.UserDto
import com.mgsanlet.cheftube.data.model.toDomainUser
import com.mgsanlet.cheftube.data.model.toUserDto
import com.mgsanlet.cheftube.data.source.local.PreferencesManager
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository.UserError
import com.mgsanlet.cheftube.domain.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val preferences: PreferencesManager
) : UsersRepository {

    private var currentUser: DomainUser? = null

    override fun getCurrentUserCopy(): Result<DomainUser, UserError> {
        return currentUser?.let { user ->
            Result.Success(
                DomainUser(
                    currentUser!!.id,
                    currentUser!!.username,
                    currentUser!!.email,
                    currentUser!!.password
                )
            )
        } ?: Result.Error(UserError.USER_NOT_FOUND)
    }

    override suspend fun createUser(
        id: String,
        username: String,
        email: String,
        password: String
    ): Result<DomainUser, UserError> {
        return try {
            if (userLocalDataSource.getUserByName(username) != null) {
                Result.Error(UserError.USERNAME_IN_USE)

            } else if (userLocalDataSource.getUserByEmail(email) != null) {
                Result.Error(UserError.EMAIL_IN_USE)

            } else {
                val newUser = UserDto(id, username, email, password)

                if (userLocalDataSource.insertUser(newUser)) {
                    currentUser = newUser.toDomainUser()
                    Result.Success(newUser.toDomainUser())
                } else {
                    Result.Error(UserError.UNKNOWN)
                }
            }
        } catch (e: Exception) {
            Result.Error(UserError.UNKNOWN)
        }
    }

    override suspend fun loginUser(
        emailOrUsername: String,
        password: String
    ): Result<DomainUser, UserError> {
        return try {
            val user = userLocalDataSource.getUserByEmailOrUsername(emailOrUsername)
            when {
                user == null -> {
                    Result.Error(UserError.USER_NOT_FOUND)
                }

                user.password != password -> {
                    Result.Error(UserError.WRONG_PASSWORD)
                }

                else -> {
                    currentUser = user.toDomainUser()
                    Result.Success(user.toDomainUser())
                }
            }
        } catch (e: Exception) {
            Result.Error(UserError.UNKNOWN)
        }
    }

    override suspend fun updateUser(
        user: DomainUser,
        oldPassword: String
    ): Result<DomainUser, UserError> {
        return try {

            // Verificar la contraseña antigua
            if (currentUser?.password != oldPassword) {
                return Result.Error(UserError.WRONG_PASSWORD)
            }

            if (userLocalDataSource.updateUser(user.toUserDto())) {
                currentUser = user
                Result.Success(user)
            } else {
                Result.Error(UserError.UNKNOWN)
            }
        } catch (e: Exception) {
            Result.Error(UserError.UNKNOWN)
        }
    }

    override suspend fun getUserById(userId: String): Result<DomainUser, UserError> {
        return try {
            val user = userLocalDataSource.getUserById(userId)
            if (user != null) {
                Result.Success(user.toDomainUser())
            } else {
                Result.Error(UserError.USER_NOT_FOUND)
            }
        } catch (e: Exception) {
            Result.Error(UserError.UNKNOWN)
        }
    }

    override suspend fun getUserByName(username: String): Result<DomainUser, UserError> {
        return try {
            val user = userLocalDataSource.getUserByName(username)
            if (user != null) {
                Result.Success(user.toDomainUser())
            } else {
                Result.Error(UserError.USER_NOT_FOUND)
            }
        } catch (e: Exception) {
            Result.Error(UserError.UNKNOWN)
        }
    }

    override suspend fun getUserByEmail(userEmail: String): Result<DomainUser, UserError> {
        return try {
            val user = userLocalDataSource.getUserByEmail(userEmail)
            if (user != null) {
                Result.Success(user.toDomainUser())
            } else {
                Result.Error(UserError.USER_NOT_FOUND)
            }
        } catch (e: Exception) {
            Result.Error(UserError.UNKNOWN)
        }
    }

    override suspend fun tryAutoLogin(): Boolean {
        var result = false
        val persistentUserId = preferences.getSavedUserId()
        persistentUserId?.let {
            getUserById(it).fold(
                onSuccess = { user ->
                    currentUser = user
                    result = true
                }, onError = {
                    currentUser = null
                })
        }
        return result
    }
}
