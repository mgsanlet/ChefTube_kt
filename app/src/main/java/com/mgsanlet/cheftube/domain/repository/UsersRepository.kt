package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainUser as User
import com.mgsanlet.cheftube.domain.util.Result
import com.mgsanlet.cheftube.domain.util.Error


interface UsersRepository {
    suspend fun createUser(id: String, username: String, email: String, password: String): Result<User, UserError>
    suspend fun loginUser(emailOrUsername: String, password: String): Result<User, UserError>
    suspend fun getUserById(userId: String): Result<User, UserError>
    suspend fun getUserByName(username: String): Result<User, UserError>
    suspend fun getUserByEmail(userEmail: String): Result<User, UserError>
    suspend fun updateUser(user: User, oldPassword: String): Result<User, UserError>
    suspend fun tryAutoLogin(): Boolean
    fun getCurrentUserCopy(): Result<User, UserError>

    enum class UserError: Error {
        USERNAME_IN_USE,
        EMAIL_IN_USE,
        USER_NOT_FOUND,
        WRONG_PASSWORD,
        UNKNOWN
    }
}