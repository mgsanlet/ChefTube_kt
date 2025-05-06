package com.mgsanlet.cheftube.data.source.remote

import android.util.Log
import com.google.firebase.firestore.FieldValue
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

    suspend fun updateFavouriteCount(recipeId: String, isNewFavourite: Boolean): DomainResult<Unit, RecipeError> {
        try {
            mainApi.db.collection("recipes").document(recipeId).update(
                "favouriteCount",
                if (isNewFavourite) FieldValue.increment(1) else FieldValue.increment(-1)
            ).await()
            return DomainResult.Success(Unit)
        }catch (exception: Exception) {
            return DomainResult.Error(RecipeError.Unknown(exception.message))
        }
    }
}