package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError


interface UsersRepository {
    suspend fun createUser(username: String, email: String, password: String): DomainResult<Unit, UserError>
    suspend fun loginUser(email: String, password: String): DomainResult<Unit, UserError>
    suspend fun updateCurrentUserData(newUserData: DomainUser): DomainResult<Unit, UserError>
    suspend fun updateUserData(newUserData: DomainUser): DomainResult<Unit, UserError>
    suspend fun tryAutoLogin(): DomainResult<Unit, UserError>
    suspend fun getCurrentUserData(): DomainResult<DomainUser, UserError>
    suspend fun getUserDataById(userId: String): DomainResult<DomainUser, UserError>
    suspend fun updateFavouriteRecipes(recipeId: String, isNewFavourite: Boolean): DomainResult<Unit, UserError>
    suspend fun saveProfilePicture(profilePicture: ByteArray): DomainResult<Unit, UserError>
    suspend fun updateEmail(newEmail: String, password: String): DomainResult<Unit, UserError>
    suspend fun updatePassword(currentPassword: String, newPassword: String): DomainResult<Unit, UserError>
    suspend fun deleteAccount(password: String): DomainResult<Unit, UserError>

    fun logout()
}