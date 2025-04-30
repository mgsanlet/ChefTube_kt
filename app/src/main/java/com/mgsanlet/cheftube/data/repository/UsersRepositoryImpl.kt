package com.mgsanlet.cheftube.data.repository

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.mgsanlet.cheftube.data.source.local.UserLocalDataSource
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.data.source.remote.FirebaseUserApi
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val mainApi: FirebaseApi,
    private val userApi: FirebaseUserApi,
    private val userLocalDataSource: UserLocalDataSource
) : UsersRepository {

    private var currentUser: DomainUser? = null

    override fun getCurrentUser(): DomainResult<DomainUser, UserError> {
        currentUser?.let {
            return DomainResult.Success(it)
        } ?: return DomainResult.Error(UserError.UserNotFound)
    }

    suspend fun createUserOld(
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
                val newUser = DomainUser(id, username, email)

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

    override suspend fun createUser(
        id: String,
        username: String,
        email: String,
        password: String
    ): DomainResult<Unit, UserError> {
        try {
            val isAvailableResult = userApi.isAvailableUsername(username)
            if (isAvailableResult is DomainResult.Error) {
                return isAvailableResult
            }
            mainApi.auth.createUserWithEmailAndPassword(email, password).await()
            val user = mainApi.auth.currentUser ?: throw Exception("User not found after creation")

            val insertDataResult = userApi.insertUserData(user.uid, username)
            if (insertDataResult is DomainResult.Error) {
                return insertDataResult
            }

            currentUser = DomainUser(user.uid, username, email)
            return DomainResult.Success(Unit)
        } catch (e: Exception) {
            return if (e is FirebaseAuthException) {
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> DomainResult.Error(UserError.EmailInUse)
                    "WEAK_PASSWORD" -> DomainResult.Error(UserError.InvalidPasswordPattern)
                    else -> DomainResult.Error(UserError.Unknown(e.message))
                }
            } else {
                DomainResult.Error(UserError.Unknown(e.message))
            }
        }
    }

    override suspend fun loginUser(email: String, password: String): DomainResult<Unit, UserError> {
        try {
            mainApi.auth.signInWithEmailAndPassword(email, password).await()
            val user = mainApi.auth.currentUser ?: throw Exception("User not found after login")
            when (val result = userApi.getUserDataById(user.uid)) {
                is DomainResult.Success -> {
                    currentUser = DomainUser(user.uid, result.data.username, email)
                    return DomainResult.Success(Unit)
                }

                is DomainResult.Error -> return DomainResult.Error(result.error)
            }
        } catch (e: Exception) {
            return when (e) {
                is FirebaseAuthInvalidUserException -> DomainResult.Error(UserError.UserNotFound)
                is FirebaseAuthInvalidCredentialsException -> DomainResult.Error(UserError.WrongPassword)
                else -> DomainResult.Error(UserError.Unknown(e.message))
            }
        }
    }

    override suspend fun updateUser(
        user: DomainUser,
        oldPassword: String
    ): DomainResult<DomainUser, UserError> {
        return try {

//            // Verificar la contraseña antigua
//            if (currentUser?.password != oldPassword) {
//                return DomainResult.Error(UserError.WrongPassword)
//            }

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

    override suspend fun getUserById(userId: String): DomainResult<DomainUser, UserError> {
        return try {
            userLocalDataSource.getUserById(userId)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun getUserByName(username: String): DomainResult<DomainUser, UserError> {
        return try {
            userLocalDataSource.getUserByName(username)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun getUserByEmail(userEmail: String): DomainResult<DomainUser, UserError> {
        return try {
            userLocalDataSource.getUserByEmail(userEmail)
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    override suspend fun tryAutoLogin(): DomainResult<Unit, UserError> {

        mainApi.auth.currentUser?.let {
            when (val result = userApi.getUserDataById(it.uid)) {
                is DomainResult.Success -> {
                    currentUser = DomainUser(it.uid, result.data.username, it.email ?: "")
                    return DomainResult.Success(Unit)
                }

                is DomainResult.Error -> return DomainResult.Error(result.error)
            }
        } ?: return DomainResult.Error(UserError.UserNotFound)
    }

    override fun logout() {
        currentUser = null
        mainApi.auth.signOut()
    }
}
