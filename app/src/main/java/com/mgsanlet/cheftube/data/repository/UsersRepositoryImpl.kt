package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.source.local.PreferencesManager
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import com.mgsanlet.cheftube.domain.model.DomainUser as User
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val preferences: PreferencesManager
) : UsersRepository {

    private var currentUser: User? = null

    override fun getCurrentUserCopy(): DomainResult<User, UserError> {
        return currentUser?.let {
            DomainResult.Success(
                User(
                    currentUser!!.id,
                    currentUser!!.username,
                    currentUser!!.email,
                    currentUser!!.password
                )
            )
        } ?: DomainResult.Error(UserError.UserNotFound)
    }

    override suspend fun createUser(
        id: String,
        username: String,
        email: String,
        password: String
    ): DomainResult<Unit, UserError> {
        return try {
            if (userLocalDataSource.getUserByName(username) is DomainResult.Success) {
                DomainResult.Error(UserError.UsernameInUse)

            } else if (userLocalDataSource.getUserByEmail(email) is DomainResult.Success) {
                DomainResult.Error(UserError.EmailInUse)

            } else {
                val newUser = User(id, username, email, password)

                userLocalDataSource.insertUser(newUser).fold(
                    onSuccess = {
                        currentUser = newUser
                        DomainResult.Success(Unit)
                    },
                    onError = { error ->
                        DomainResult.Error(error)
                    }
                )
            }
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun loginUser(
        emailOrUsername: String,
        password: String
    ): DomainResult<Unit, UserError> {
        return try {
            val result = userLocalDataSource.getUserByEmailOrUsername(emailOrUsername)
            result.fold(
                onSuccess = { user ->
                    if (user.password != password) {
                        DomainResult.Error(UserError.WrongPassword)
                    } else {
                        currentUser = user
                        DomainResult.Success(Unit)
                    }
                },
                onError = { error ->
                    DomainResult.Error(error)
                }
            )
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun updateUser(
        user: User,
        oldPassword: String
    ): DomainResult<User, UserError> {
        return try {

            // Verificar la contraseña antigua
            if (currentUser?.password != oldPassword) {
                return DomainResult.Error(UserError.WrongPassword)
            }

            userLocalDataSource.updateUser(user).fold(
                onSuccess = {
                    currentUser = user
                    DomainResult.Success(user)
                }, onError = { error ->
                    DomainResult.Error(error)
                })
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun getUserById(userId: String): DomainResult<User, UserError> {
        return try {
            userLocalDataSource.getUserById(userId)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun getUserByName(username: String): DomainResult<User, UserError> {
        return try {
            userLocalDataSource.getUserByName(username)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun getUserByEmail(userEmail: String): DomainResult<User, UserError> {
        return try {
            userLocalDataSource.getUserByEmail(userEmail)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun tryAutoLogin(): DomainResult<Unit, UserError> {

        val persistentUserId = preferences.getSavedUserId()
        persistentUserId?.let {
            getUserById(it).fold(
                onSuccess = { user ->
                    currentUser = user
                    return DomainResult.Success(Unit)
                }, onError = {
                    currentUser = null
                })
        }
        return DomainResult.Error(UserError.UserNotFound)
    }

    override fun alternateKeepSession(keep: Boolean): DomainResult<Unit, UserError> {
        currentUser?.let {
            if (keep) {
                preferences.saveUserId(it.id)
            } else {
                preferences.deleteUserId()
            }
        } ?: return DomainResult.Error(UserError.UserNotFound)

        return DomainResult.Success(Unit)
    }

    override fun isSessionKept(): DomainResult<Boolean, UserError> {
        var isKept = false
        currentUser?.let {
            isKept = preferences.isIdSaved(it.id)
        } ?: return DomainResult.Error(UserError.UserNotFound)
        return DomainResult.Success(isKept)
    }

    override fun logout() {
        currentUser = null
        preferences.deleteUserId()
    }
}
