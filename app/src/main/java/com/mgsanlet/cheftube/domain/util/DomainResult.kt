package com.mgsanlet.cheftube.domain.util

import com.mgsanlet.cheftube.domain.util.error.DomainError

typealias RootError = DomainError

sealed class DomainResult<out D, out E : RootError> {
    data class Success<out D, out E : RootError>(val data: D) : DomainResult<D, E>()
    data class Error<out D, out E : RootError>(val error: E) : DomainResult<D, E>()

    inline fun <R : Any> fold(
        onSuccess: (D) -> R ,
        onError: (E) -> R
    ): R {
        return when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(error)
        }
    }
}