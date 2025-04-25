package com.mgsanlet.cheftube.domain.util.error

sealed class ProductError: DomainError {
    data object NoInternet: ProductError()
    data object EmptyResponse: ProductError()
    data object NotFound: ProductError()
    data class ApiError(val messageArg:Any? = ""): ProductError()
    data class Unknown(val messageArg:Any? = ""): ProductError()
}