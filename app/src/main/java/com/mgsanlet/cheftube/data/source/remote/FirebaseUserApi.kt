package com.mgsanlet.cheftube.data.source.remote

import android.util.Log
import com.mgsanlet.cheftube.data.model.UserResponse
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserApi @Inject constructor(private val mainApi: FirebaseApi) {

    suspend fun getUserDataById(id: String): DomainResult<UserResponse, UserError> {
        return try {
            val document = mainApi.db.collection("users").document(id).get().await()
            if (document.exists()) {
                document.toObject(UserResponse::class.java)?.let {
                    DomainResult.Success(it)
                } ?: DomainResult.Error(UserError.UserNotFound)
            } else {
                DomainResult.Error(UserError.UserNotFound)
            }
        } catch (exception: Exception) {
            Log.e("Firestore", "get failed with ", exception)
            DomainResult.Error(UserError.Unknown(exception.message))
        }
    }

    suspend fun isAvailableUsername(username: String): DomainResult<Unit, UserError> {
        return try {
            val document =
                mainApi.db.collection("users").whereEqualTo("username", username).get().await()
            if (document.isEmpty) {
                DomainResult.Success(Unit)
            } else {
                DomainResult.Error(UserError.UsernameInUse)
            }
        } catch (exception: Exception) {
            Log.e("Firestore", "get failed with ", exception)
            DomainResult.Error(UserError.Unknown(exception.message))
        }
    }

    suspend fun insertUserData(
        id: String,
        username: String,
        email: String
    ): DomainResult<Unit, UserError> {
        try {
            val user = hashMapOf("id" to id, "username" to username, "email" to email)
            mainApi.db.collection("users").document(id).set(user).await()
        } catch (exception: Exception) {
            Log.e("Firestore", "get failed with ", exception)
            return DomainResult.Error(UserError.Unknown(exception.message))
        }
        return DomainResult.Success(Unit)
    }

    suspend fun updateUserData(id: String, user: DomainUser): DomainResult<Unit, UserError> {
        try {
            val user = hashMapOf(
                "id" to id,
                "username" to user.username,
                "email" to user.email,
                "bio" to user.bio,
                "createdRecipes" to user.createdRecipes,
                "favouriteRecipes" to user.favouriteRecipes,
                "followersIds" to user.followersIds,
                "followingIds" to user.followingIds
            )
            mainApi.db.collection("users").document(id).set(user).await()
        } catch (exception: Exception) {
            Log.e("Firestore", "get failed with ", exception)
            return DomainResult.Error(UserError.Unknown(exception.message))
        }
        return DomainResult.Success(Unit)
    }
}