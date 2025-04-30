package com.mgsanlet.cheftube.data.source.remote

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class FirebaseApi {
    val db by lazy { Firebase.firestore }
    val storage by lazy { Firebase.storage }
    val auth by lazy { Firebase.auth }
}