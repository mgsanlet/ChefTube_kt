package com.mgsanlet.cheftube.domain.util.error

sealed class RecipeError: DomainError {
    data object RecipeNotFound: RecipeError()
    data object CommentNotFound: RecipeError()
    data object NoResults: RecipeError()
    data class Unknown(val messageArg: Any? = ""): RecipeError()
}