package com.mgsanlet.cheftube.domain.util

import com.mgsanlet.cheftube.domain.util.Error

typealias RootError = Error

sealed class Result<out D, out E:RootError> {
    data class Success<out D, out E:RootError> (val data: D) : Result<D,E>()
    data class Error<out D, out E:RootError> (val error: E) : Result<D,E>()

    inline fun <R : Any> fold(onSuccess: (D) -> R, onError: (E) -> R): R {
        return when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(error)
        }
    }
}