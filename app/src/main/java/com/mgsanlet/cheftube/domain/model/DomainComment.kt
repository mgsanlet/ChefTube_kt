package com.mgsanlet.cheftube.domain.model

data class DomainComment(
    val author: DomainUser = DomainUser(),
    val content: String = "",
    val timestamp: Long = 0
)
