package com.mgsanlet.cheftube.data.model

data class UserResponse(
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val createdRecipes: List<String> = emptyList(),
    val favouriteRecipes: List<String> = emptyList(),
    val followersIds: List<String> = emptyList(),
    val followingIds: List<String> = emptyList()
)