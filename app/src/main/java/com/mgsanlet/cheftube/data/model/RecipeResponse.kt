package com.mgsanlet.cheftube.data.model

import com.google.firebase.firestore.PropertyName

data class RecipeResponse(
    val id: String = "",
    val title: String = "",
    val imagePath: String = "",
    val videoUrl: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList()
)
