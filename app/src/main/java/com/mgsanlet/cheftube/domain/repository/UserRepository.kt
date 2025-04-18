package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.data.model.User
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.utils.Resource

interface UserRepository {
    suspend fun createUser(username: String, email: String, password: String): Resource<DomainUser>
    suspend fun loginUser(emailOrUsername: String, password: String): Resource<DomainUser>
    suspend fun getUserById(userId: String): Resource<DomainUser>
    suspend fun getUserByName(username: String): Resource<DomainUser>
    suspend fun getUserByEmail(userEmail: String): Resource<DomainUser>
    suspend fun updateUser(user: User, oldPassword: String): Resource<DomainUser>
}