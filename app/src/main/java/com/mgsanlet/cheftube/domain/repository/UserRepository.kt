package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.data.model.User

interface UserRepository {
    fun createUser(username: String, email: String, password: String): Result<User>
    fun loginUser(emailOrUsername: String, password: String): Result<User>
    fun getUserById(userId: String): Result<User>
    fun getUserByName(username: String): Result<User>
    fun getUserByEmail(userEmail: String): Result<User>
    fun updateUser(user: User, oldPassword: String): Result<User>
}