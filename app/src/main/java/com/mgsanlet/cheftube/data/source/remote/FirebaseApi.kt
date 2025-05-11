package com.mgsanlet.cheftube.data.source.remote

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mgsanlet.cheftube.data.model.RecipeResponse
import com.mgsanlet.cheftube.data.model.UserResponse
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import com.mgsanlet.cheftube.domain.util.error.UserError
import kotlinx.coroutines.tasks.await

class FirebaseApi {
    private val db by lazy { Firebase.firestore }
    private val storage by lazy { Firebase.storage }
    val auth by lazy { Firebase.auth }

    // GENERAL

    suspend fun getStorageUrlFromPath(path: String): String {
        return try {
            val url = storage.reference.child(path).downloadUrl.await()
            Log.i("Firebase", "Download URL: $url")
            url.toString()
        } catch (exception: Exception) {
            Log.e("Firebase", "Error getting download URL: ", exception)
            ""
        }
    }

    // USER

    suspend fun getUserDataById(id: String): DomainResult<UserResponse, UserError> {
        return try {
            val document = db.collection("users").document(id).get().await()
            if (document.exists()) {
                document.toObject(UserResponse::class.java)?.let {
                    DomainResult.Success(it)
                } ?: DomainResult.Error(UserError.UserNotFound)
            } else {
                DomainResult.Error(UserError.UserNotFound)
            }
        } catch (exception: Exception) {
            Log.e("FireStore", "get failed with ", exception)
            DomainResult.Error(UserError.Unknown(exception.message))
        }
    }

    suspend fun isAvailableUsername(username: String): DomainResult<Unit, UserError> {
        return try {
            val document =
                db.collection("users").whereEqualTo("username", username).get().await()
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
            val batch = db.batch()
            val userRef = db.collection("users").document(id)
            val user = hashMapOf("id" to id, "username" to username, "email" to email)
            batch.set(userRef, user)
            batch.commit().await()
        } catch (exception: Exception) {
            Log.e("Firestore", "get failed with ", exception)
            return DomainResult.Error(UserError.Unknown(exception.message))
        }
        return DomainResult.Success(Unit)
    }

    suspend fun updateUserData(id: String, userData: DomainUser): DomainResult<Unit, UserError> {
        try {
            val user = hashMapOf(
                "id" to id,
                "username" to userData.username,
                "email" to userData.email,
                "bio" to userData.bio,
                "hasProfilePicture" to userData.profilePictureUrl.isNotBlank(),
                "createdRecipes" to userData.createdRecipes,
                "favouriteRecipes" to userData.favouriteRecipes,
                "followersIds" to userData.followersIds,
                "followingIds" to userData.followingIds
            )
            db.collection("users").document(id).set(user).await()

            // Actualizar los datos del autor en todas las recetas creadas por este usuario
            if (userData.createdRecipes.isNotEmpty()) {
                val batch = db.batch()
                userData.createdRecipes.forEach { recipeId ->
                    val recipeRef = db.collection("recipes").document(recipeId)
                    batch.update(recipeRef, "authorName", userData.username)
                    batch.update(recipeRef, "authorHasProfilePicture",userData.profilePictureUrl.isNotBlank())
                }
                batch.commit().await()
            }

        } catch (exception: Exception) {
            Log.e("Firestore", "get failed with ", exception)
            return DomainResult.Error(UserError.Unknown(exception.message))
        }
        return DomainResult.Success(Unit)
    }

    suspend fun updateUserFavouriteRecipes(
        currentUserId: String,
        recipeId: String,
        isNewFavourite: Boolean
    ): DomainResult<Unit, UserError> {
        try {
            val batch = db.batch()
            val userRef = db.collection("users").document(currentUserId)
            batch.update(
                userRef,
                "favouriteRecipes",
                if (isNewFavourite) FieldValue.arrayUnion(recipeId)
                else FieldValue.arrayRemove(recipeId)
            )
            batch.commit().await()

            return DomainResult.Success(Unit)
        } catch (exception: Exception) {
            return DomainResult.Error(UserError.Unknown(exception.message))
        }
    }

    suspend fun saveUserProfilePicture(
        userId: String,
        profilePicture: ByteArray
    ): DomainResult<String, UserError> {
        try {
            // Determinar el path de storage
            val storagePath = "profile_pictures/$userId.jpg"
            val storageRef = storage.reference.child(storagePath)

            // Subir la imagen
            storageRef.putBytes(profilePicture).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // Actualizar el campo hasProfilePicture en el documento del usuario
            val batch = db.batch()
            val userRef = db.collection("users").document(userId)
            batch.update(userRef, "hasProfilePicture", true)

            // Actualizar el atributo authorHasProfilePicture en todas las recetas del usuario
            val userResult = getUserDataById(userId)
            if (userResult is DomainResult.Success && userResult.data.createdRecipes.isNotEmpty()) {
                val recipesBatch = db.batch()
                userResult.data.createdRecipes.forEach { recipeId ->
                    val recipeRef = db.collection("recipes").document(recipeId)
                    recipesBatch.update(recipeRef, "authorHasProfilePicture", true)
                }
                recipesBatch.commit().await()
            }

            batch.commit().await()

            return DomainResult.Success(downloadUrl)
        } catch (e: Exception) {
            return DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    // RECIPE

    suspend fun getAllRecipes(): DomainResult<List<RecipeResponse>, RecipeError> {
        return try {
            val result = db.collection("recipes").get().await()
            val recipeList = result.documents.mapNotNull { document ->
                try {
                    document.toObject(RecipeResponse::class.java)
                } catch (e: Exception) {
                    Log.e("Firestore", "Error converting document: ${document.id} to object", e)
                    null
                }
            }
            DomainResult.Success(recipeList)
        } catch (exception: Exception) {
            Log.e("Firestore", "Error getting documents: ", exception)
            DomainResult.Error(RecipeError.Unknown(exception.message))
        }
    }

    suspend fun getRecipeById(recipeId: String): DomainResult<RecipeResponse, RecipeError> {
        return try {
            val document = db.collection("recipes").document(recipeId).get().await()
            if (document.exists()) {
                document.toObject(RecipeResponse::class.java)?.let {
                    DomainResult.Success(it)
                } ?: DomainResult.Error(RecipeError.RecipeNotFound)
            } else {
                DomainResult.Error(RecipeError.RecipeNotFound)
            }
        } catch (exception: Exception) {
            Log.e("Firestore", "get failed with ", exception)
            DomainResult.Error(RecipeError.Unknown(exception.message))
        }
    }

    suspend fun getRecipesByIds(recipeIds: ArrayList<String>): DomainResult<List<RecipeResponse>, RecipeError> {
        return try {
            val result =
                db.collection("recipes").whereIn("id", recipeIds).get()
                    .await()
            val recipeList = result.documents.mapNotNull { document ->
                try {
                    document.toObject(RecipeResponse::class.java)
                } catch (e: Exception) {
                    Log.e("Firestore", "Error converting document: ${document.id} to object", e)
                    null
                }
            }
            DomainResult.Success(recipeList)
        } catch (exception: Exception) {
            Log.e("Firestore", "Error getting documents: ", exception)
            DomainResult.Error(RecipeError.Unknown(exception.message))
        }
    }

    suspend fun updateRecipeFavouriteCount(
        recipeId: String,
        isNewFavourite: Boolean
    ): DomainResult<Unit, RecipeError> {
        try {
            val batch = db.batch()
            val recipeRef = db.collection("recipes").document(recipeId)
            batch.update(
                recipeRef,
                "favouriteCount",
                if (isNewFavourite) FieldValue.increment(1) else FieldValue.increment(-1)
            )
            batch.commit().await()
            return DomainResult.Success(Unit)
        } catch (exception: Exception) {
            return DomainResult.Error(RecipeError.Unknown(exception.message))
        }
    }

    suspend fun saveRecipe(
        finalId: String,
        newRecipeData: DomainRecipe,
        newImage: ByteArray?,
        currentUserData: DomainUser
    ): DomainResult<Unit, RecipeError> {
        try {
            val batch = db.batch()
            val recipeRef = db.collection("recipes").document(finalId)
            val recipe = hashMapOf(
                "id" to finalId,
                "title" to newRecipeData.title,
                "videoUrl" to newRecipeData.videoUrl,
                "ingredients" to newRecipeData.ingredients,
                "steps" to newRecipeData.steps,
                "categories" to newRecipeData.categories,
                "favouriteCount" to newRecipeData.favouriteCount,
                "durationMinutes" to newRecipeData.durationMinutes,
                "difficulty" to newRecipeData.difficulty,

                "authorId" to currentUserData.id,
                "authorName" to currentUserData.username,
                "authorHasProfilePicture" to currentUserData.profilePictureUrl.isNotBlank()
            )
            batch.set(recipeRef, recipe)
            newImage?.let{
                val storageRef = storage.reference.child("recipe_images/$finalId.jpg")
                storageRef.putBytes(it).await()
            }

            val userRef = db.collection("users").document(currentUserData.id)
            batch.update(userRef, "createdRecipes", FieldValue.arrayUnion(finalId))

            batch.commit().await()
            return DomainResult.Success(Unit)
        } catch (exception: Exception) {
            return DomainResult.Error(RecipeError.Unknown(exception.message))
        }
    }
}