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

    override suspend fun getCurrentUserData(): DomainResult<DomainUser, UserError> {
        currentUser?.let {
            when (val result = userApi.getUserDataById(it.id)) {
                is DomainResult.Success -> {
                    currentUser = DomainUser(it.id, result.data.username, it.email)
                    return DomainResult.Success(currentUser!!)
                }

                is DomainResult.Error -> return DomainResult.Error(result.error)
            }
        } ?: return DomainResult.Error(UserError.UserNotFound)
    }

    override suspend fun createUser(
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

    override suspend fun tryAutoLogin(): DomainResult<Unit, UserError> {

        mainApi.auth.currentUser?.let {
            currentUser = DomainUser(
                it.uid,
                "",
                it.email ?: ""
            )
            return DomainResult.Success(Unit)
        } ?: return DomainResult.Error(UserError.UserNotFound)
    }

    override fun logout() {
        currentUser = null
        mainApi.auth.signOut()
    }
}
