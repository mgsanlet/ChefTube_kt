package com.mgsanlet.cheftube.utils

import com.mgsanlet.cheftube.utils.error.ChefTubeError

sealed class Resource<out T : Any> {
    data class Success<out T : Any>(val data: T) : Resource<T>()
    data class Error(val error: ChefTubeError) : Resource<Nothing>()

    inline fun <R : Any> fold(onSuccess: (T) -> R, onError: (ChefTubeError) -> R): R {
        return when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(error)
        }
    }
}