package com.mgsanlet.cheftube.data.source.remote

import android.util.Log
import com.mgsanlet.cheftube.data.model.RecipeResponse
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRecipeApi @Inject constructor(private val mainApi: FirebaseApi) {

    suspend fun getAll(): DomainResult<List<RecipeResponse>, RecipeError> {
        return try {
            val result = mainApi.db.collection("recipes").get().await()
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

    suspend fun getById(recipeId: String): DomainResult<RecipeResponse, RecipeError> {
        return try {
            val document = mainApi.db.collection("recipes").document(recipeId).get().await()
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

    suspend fun getByIds(recipeIds: ArrayList<String>): DomainResult<List<RecipeResponse>, RecipeError> {
        return try {
            val result =
                mainApi.db.collection("recipes").whereIn("id", recipeIds).get()
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


    suspend fun getStorageUrlFromPath(path: String): String {
        return try {
            val url = mainApi.storage.reference.child(path).downloadUrl.await()
            Log.i("Firebase", "Download URL: $url")
            url.toString()
        } catch (exception: Exception) {
            Log.e("Firebase", "Error getting download URL: ", exception)
            ""
        }
    }
}