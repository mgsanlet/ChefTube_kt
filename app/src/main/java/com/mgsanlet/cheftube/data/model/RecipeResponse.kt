package com.mgsanlet.cheftube.data.model

data class RecipeResponse(
    val id: String = "",
    val title: String = "",
    val imagePath: String = "",
    val videoUrl: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val favouriteCount: Int = 0,
    val durationMinutes: Int = 0,
    val difficulty: Int = 0,
    // Author
    val authorId: String = "",
    val authorName: String = ""
)
