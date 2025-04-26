package com.mgsanlet.cheftube.data.source.remote
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mgsanlet.cheftube.data.model.RecipeResponse
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError

class FirebaseRecipeApi {
    private val db by lazy { Firebase.firestore }
    private val storage by lazy { Firebase.storage }

    fun getAll(callback: (DomainResult<List<RecipeResponse>, RecipeError>) -> Unit) {
        val recipeList: MutableList<RecipeResponse> = mutableListOf()

        db.collection("recipes")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Convierte el documento a tu clase de datos
                    val recipeResponse = document.toObject(RecipeResponse::class.java)
                    Log.d("Firestore", "${document.id} => $recipeResponse")
                    recipeList.add(recipeResponse)
                }
                callback(DomainResult.Success(recipeList))
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Error getting documents: ", exception)
                callback(DomainResult.Error(RecipeError.Unknown(exception.message)))
            }
    }

    fun getById(recipeId: String, callback: (DomainResult<RecipeResponse, RecipeError>) -> Unit) {
        val docRef = db.collection("recipes").document(recipeId)
        docRef.get()
            .addOnSuccessListener { document ->
                document?.let {
                    val recipeResponse = document.toObject(RecipeResponse::class.java)

                    recipeResponse?.let {
                        callback(DomainResult.Success(recipeResponse))
                    } ?: callback(DomainResult.Error(RecipeError.RecipeNotFound))

                } ?: callback(DomainResult.Error(RecipeError.RecipeNotFound))

            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
                // Llama al callback con un error
                callback(DomainResult.Error(RecipeError.Unknown(exception.message)))
            }
    }

    fun getStorageUrlFromPath(path: String): String {
        val storageRef = storage.reference
        val imageRef = storageRef.child(path)
        var imageUrl = ""
        imageRef.downloadUrl
            .addOnSuccessListener { url ->
                imageUrl = url.toString()
                Log.i("Firebase", "Download URL: $imageUrl")
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting download URL: ", exception)
            }
        return imageUrl
    }
}