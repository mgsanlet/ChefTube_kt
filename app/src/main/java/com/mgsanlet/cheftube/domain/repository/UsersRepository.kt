package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainUser as User
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError


interface UsersRepository {
    suspend fun createUser(username: String, email: String, password: String): DomainResult<Unit, UserError>
    suspend fun loginUser(email: String, password: String): DomainResult<Unit, UserError>
    suspend fun updateUser(user: User, oldPassword: String): DomainResult<User, UserError>
    suspend fun tryAutoLogin(): DomainResult<Unit, UserError>
    suspend fun getCurrentUserData(): DomainResult<User, UserError>
    fun logout()
}