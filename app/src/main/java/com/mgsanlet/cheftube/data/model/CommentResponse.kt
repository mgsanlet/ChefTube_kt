package com.mgsanlet.cheftube.data.model

data class CommentResponse (
    val authorId: String = "",
    val authorName: String = "",
    val authorEmail: String = "",
    val authorHasProfilePicture: Boolean = false,
    val content: String = "",
    val timestamp: Long = 0
)