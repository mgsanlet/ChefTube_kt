package com.mgsanlet.cheftube.data.source.remote
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mgsanlet.cheftube.data.model.RecipeResponse
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError

class FirebaseRecipeProvider {
    private val db by lazy { Firebase.firestore }
    private val storage by lazy { Firebase.storage }

    fun getAll() {
        db.collection("recipes")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Convierte el documento a tu clase de datos
                    val recipe = document.toObject(RecipeResponse::class.java)
                    Log.d("Firestore", "${document.id} => $recipe")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Error getting documents: ", exception)
            }

    }

    fun getById(recipeId: String) {

        val docRef = db.collection("recipes").document(recipeId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Convierte el documento a tu clase de datos
                    val recipeResponse = document.toObject(RecipeResponse::class.java)
                    // Usa los datos
                    Log.d("Firestore", "Datos: $recipeResponse")
                    recipeResponse?.let {
                        getStorageUrlFromPath(it.imagePath)
                    }

//                    return@addOnSuccessListener DomainResult.Success(DomainRecipe(
//                        id = recipeId,
//                        title = recipeResponse.title,
//                        ingredients = recipeResponse.ingredients,
//                        steps = recipeResponse.steps
//                    ))
                } else {
                    Log.d("Firestore", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
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