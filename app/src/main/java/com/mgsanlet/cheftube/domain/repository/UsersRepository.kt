package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError


interface UsersRepository {
    suspend fun createUser(username: String, email: String, password: String): DomainResult<Unit, UserError>
    suspend fun loginUser(email: String, password: String): DomainResult<Unit, UserError>
    suspend fun updateCurrentUserData(newUserData: DomainUser): DomainResult<Unit, UserError>
    suspend fun tryAutoLogin(): DomainResult<Unit, UserError>
    suspend fun getCurrentUserData(): DomainResult<DomainUser, UserError>
    suspend fun getUserDataById(userId: String): DomainResult<DomainUser, UserError>
    fun logout()
}