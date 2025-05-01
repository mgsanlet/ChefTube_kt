package com.mgsanlet.cheftube.domain.model

data class DomainUser(
    val id: String,
    val username: String,
    val email: String,
    val bio: String,
    val profilePictureUrl: String
)