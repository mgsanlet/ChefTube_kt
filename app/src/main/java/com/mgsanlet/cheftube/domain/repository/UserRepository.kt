package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainUser as User
import com.mgsanlet.cheftube.domain.util.Result
import com.mgsanlet.cheftube.domain.util.Error


interface UserRepository {
    suspend fun createUser(username: String, email: String, password: String): Result<User, Error>
    suspend fun loginUser(emailOrUsername: String, password: String): Result<User, Error>
    suspend fun getUserById(userId: String): Result<User, Error>
    suspend fun getUserByName(username: String): Result<User, Error>
    suspend fun getUserByEmail(userEmail: String): Result<User, Error>
    suspend fun updateUser(user: User, oldPassword: String): Result<User, Error>
    suspend fun tryAutoLogin(): Boolean

    enum class UserError: Error {
        USERNAME_IN_USE,
        EMAIL_IN_USE,
        USER_NOT_FOUND,
        WRONG_PASSWORD,
        UNKNOWN
    }
}