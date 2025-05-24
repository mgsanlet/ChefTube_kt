package com.mgsanlet.cheftube.data.model

import com.google.firebase.Timestamp

data class UserResponse(
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val hasProfilePicture: Boolean = false,
    val createdRecipes: List<String> = emptyList(),
    val favouriteRecipes: List<String> = emptyList(),
    val followersIds: List<String> = emptyList(),
    val followingIds: List<String> = emptyList(),
    val lastLogin: Timestamp = Timestamp.now()
)