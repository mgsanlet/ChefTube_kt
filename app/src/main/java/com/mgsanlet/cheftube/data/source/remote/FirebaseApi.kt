package com.mgsanlet.cheftube.data.source.remote

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mgsanlet.cheftube.data.model.RecipeResponse
import com.mgsanlet.cheftube.data.model.StatsResponse
import com.mgsanlet.cheftube.data.model.UserResponse
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import com.mgsanlet.cheftube.domain.util.error.StatsError
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

    suspend fun isUserAdmin(userId: String): DomainResult<Boolean, UserError> {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            
            if (!userDoc.exists()) {
                return DomainResult.Error(UserError.UserNotFound)
            }
            
            val isAdmin = userDoc.getBoolean("isAdmin") == true
            DomainResult.Success(isAdmin)
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error checking admin status", e)
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    suspend fun getAllUsers(): DomainResult<Map<String, UserResponse>, UserError> {
        return try {
            val snapshot = db.collection("users").get().await()
            val usersMap = snapshot.documents.associate { document ->
                document.id to document.toObject(UserResponse::class.java)!!
            }
            DomainResult.Success(usersMap)
        } catch (exception: Exception) {
            Log.e("FireStore", "Error getting all users: ", exception)
            DomainResult.Error(UserError.Unknown(exception.message))
        }
    }

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
                    batch.update(
                        recipeRef,
                        "authorHasProfilePicture",
                        userData.profilePictureUrl.isNotBlank()
                    )
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

    suspend fun updateUserLastLogin(userId: String): DomainResult<Unit, UserError> {
        return try {
            db.collection("users").document(userId)
                .update("lastLogin", Timestamp.now())
                .await()
            DomainResult.Success(Unit)
        } catch (exception: Exception) {
            Log.e("FireStore", "Error updating last login: ", exception)
            DomainResult.Error(UserError.Unknown(exception.message))
        }
    }

    suspend fun deleteUserData(userId: String): DomainResult<Unit, UserError> {
        return try {
            // Eliminar la imagen de perfil si existe
            val storagePath = "profile_pictures/$userId.jpg"
            val storageRef = storage.reference.child(storagePath)
            try {
                storageRef.delete().await()
            } catch (e: Exception) {
                // Si hay un error al eliminar la imagen, lo registramos pero continuamos
                Log.e("FirebaseStorage", "Error deleting profile picture: ", e)
            }

            // Obtener la lista de recetas favoritas del usuario
            val userDoc = db.collection("users").document(userId).get().await()
            val favoriteRecipes = userDoc.get("favouriteRecipes") as? List<String> ?: emptyList()

            // Crear un solo batch para todas las operaciones de escritura
            val batch = db.batch()

            // Obtener y eliminar las recetas creadas por el usuario
            val userRecipes =
                db.collection("recipes").whereEqualTo("authorId", userId).get().await()
            userRecipes.documents.forEach { document ->
                deleteRecipeAndReferences(
                    document.id,
                    batch
                )
            }

            // Actualizar el contador de favoritos en las recetas que el usuario tenía como favoritas
            favoriteRecipes.forEach { recipeId ->
                val recipeRef = db.collection("recipes").document(recipeId)
                batch.update(recipeRef, "favouriteCount", FieldValue.increment(-1))
            }

            // Obtener recetas con comentarios del usuario
            val recipesWithComments =
                db.collection("recipes").whereArrayContains("comments.authorId", userId).get()
                    .await()
            recipesWithComments.documents.forEach { document ->
                val recipe = document.toObject(RecipeResponse::class.java)
                val updatedComments = recipe?.comments?.filterNot { it.authorId == userId }
                batch.update(document.reference, "comments", updatedComments)
            }

            // Obtener y actualizar seguidores
            val followers =
                db.collection("users").whereArrayContains("followersIds", userId).get().await()
            followers.documents.forEach { document ->
                batch.update(document.reference, "followersIds", FieldValue.arrayRemove(userId))
            }

            // Obtener y actualizar seguidos
            val following =
                db.collection("users").whereArrayContains("followingIds", userId).get().await()
            following.documents.forEach { document ->
                batch.update(document.reference, "followingIds", FieldValue.arrayRemove(userId))
            }

            // Eliminar el documento del usuario
            batch.delete(db.collection("users").document(userId))

            // Ejecutar todas las operaciones en un solo batch
            batch.commit().await()

            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error deleting user data: ", e)
            DomainResult.Error(UserError.Unknown(e.message))
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

            val comments = newRecipeData.comments.map {
                hashMapOf(
                    "authorId" to it.author.id,
                    "authorName" to it.author.username,
                    "authorHasProfilePicture" to it.author.profilePictureUrl.isNotBlank(),
                    "content" to it.content,
                    "timestamp" to it.timestamp
                )
            }
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
                "authorHasProfilePicture" to currentUserData.profilePictureUrl.isNotBlank(),
                "comments" to comments
            )
            batch.set(recipeRef, recipe)
            newImage?.let {
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

    suspend fun postComment(
        recipeId: String,
        commentContent: String,
        user: DomainUser
    ): DomainResult<Unit, RecipeError> {
        val recipeRef = db.collection("recipes").document(recipeId)
        return try {
            val batch = db.batch()
            batch.update(
                recipeRef,
                "comments",
                FieldValue.arrayUnion(
                    hashMapOf(
                        "authorId" to user.id,
                        "authorEmail" to user.email,
                        "authorName" to user.username,
                        "authorHasProfilePicture" to user.profilePictureUrl.isNotBlank(),
                        "content" to commentContent,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            )
            batch.commit().await()
            return DomainResult.Success(Unit)
        } catch (exception: Exception) {
            return DomainResult.Error(RecipeError.Unknown(exception.message))
        }
    }

    /**
     * Elimina una receta y todas sus referencias en la base de datos
     * @param recipeId ID de la receta a eliminar
     * @param batch Lote de operaciones al que se agregarán las operaciones de eliminación (opcional).
     *              Si es nulo, se creará y ejecutará un nuevo batch automáticamente.
     * @return DomainResult.Success(Unit) si la operación fue exitosa, o DomainResult.Error en caso contrario
     */
    suspend fun deleteRecipeAndReferences(
        recipeId: String,
        batch: WriteBatch? = null
    ): DomainResult<Unit, RecipeError> {
        return try {
            var localBatch = batch
            var shouldCommit = false

            // Si no se proporciona un batch, creamos uno local
            if (localBatch == null) {
                localBatch = db.batch()
                shouldCommit = true
            }

            // Obtener la receta para obtener información del autor
            val recipeDoc = db.collection("recipes").document(recipeId).get().await()
            val recipe =
                recipeDoc.toObject(RecipeResponse::class.java) ?: return DomainResult.Error(
                    RecipeError.RecipeNotFound
                )

            // 1. Eliminar la imagen de la receta del almacenamiento
            val storagePath = "recipe_images/$recipeId.jpg"
            val storageRef = storage.reference.child(storagePath)
            try {
                storageRef.delete().await()
            } catch (e: Exception) {
                Log.e("FirebaseStorage", "Error deleting recipe image: ", e)
            }

            // 2. Eliminar la receta
            localBatch.delete(recipeDoc.reference)

            // 3. Eliminar la receta de las listas de favoritos de los usuarios
            val usersWithFavourite = db.collection("users")
                .whereArrayContains("favouriteRecipes", recipeId)
                .get().await()

            usersWithFavourite.documents.forEach { userDoc ->
                localBatch.update(
                    userDoc.reference, "favouriteRecipes",
                    FieldValue.arrayRemove(recipeId)
                )
            }

            // 4. Actualizar la lista de recetas creadas del autor
            if (recipe.authorId.isNotBlank()) {
                val authorRef = db.collection("users").document(recipe.authorId)
                localBatch.update(
                    authorRef, "createdRecipes",
                    FieldValue.arrayRemove(recipeId)
                )
            }

            // Si creamos un batch local, lo ejecutamos
            if (shouldCommit) {
                localBatch.commit().await()
            }

            DomainResult.Success(Unit)

        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error deleting recipe $recipeId: ", e)
            DomainResult.Error(RecipeError.Unknown(e.message))
        }
    }

    // STATS

    suspend fun getStats(): DomainResult<StatsResponse, StatsError> {
        try {
            // Obtener estadísticas de logins
            val statsDoc = db.collection("stats").document("main").get().await()
            statsDoc.toObject(StatsResponse::class.java)?.let{
                    statsResponse -> return DomainResult.Success(statsResponse)
            } ?: return DomainResult.Error(StatsError.StatsNotFound)
        } catch (e: Exception) {
            return DomainResult.Error(StatsError.Unknown(e.message))
        }
    }

    /**
     * Deletes a comment from a recipe
     * @param recipeId The ID of the recipe containing the comment
     * @param commentTimestamp The timestamp of the comment to delete
     * @param userId The ID of the user who made the comment
     * @return DomainResult with Unit on success, or RecipeError on failure
     */
    suspend fun deleteComment(
        recipeId: String,
        commentTimestamp: Long,
        userId: String
    ): DomainResult<Unit, RecipeError> {
        return try {
            // Get the recipe document
            val recipeRef = db.collection("recipes").document(recipeId)
            val recipeDoc = recipeRef.get().await()
            
            if (!recipeDoc.exists()) {
                return DomainResult.Error(RecipeError.RecipeNotFound)
            }
            
            // Get current comments
            val comments = recipeDoc.get("comments") as? List<Map<String, Any>>
                ?: return DomainResult.Error(RecipeError.CommentNotFound)
            
            // Find the comment to delete
            val commentToDelete = comments.find { 
                (it["authorId"] as? String == userId) && 
                (it["timestamp"] as? Long == commentTimestamp)
            } ?: return DomainResult.Error(RecipeError.CommentNotFound)
            
            // Remove the comment
            val batch = db.batch()
            batch.update(recipeRef, "comments", FieldValue.arrayRemove(commentToDelete))
            batch.commit().await()
            
            DomainResult.Success(Unit)
        } catch (exception: Exception) {
            Log.e("FirebaseApi", "Error deleting comment", exception)
            DomainResult.Error(RecipeError.Unknown(exception.message))
        }
    }
}