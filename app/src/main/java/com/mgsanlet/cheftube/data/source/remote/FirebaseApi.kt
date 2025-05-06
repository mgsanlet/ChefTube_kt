package com.mgsanlet.cheftube.data.source.remote

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class FirebaseApi {
    val db by lazy { Firebase.firestore }
    val storage by lazy { Firebase.storage }
    val auth by lazy { Firebase.auth }

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
}