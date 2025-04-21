package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainProduct as Product
import com.mgsanlet.cheftube.domain.util.Result
import com.mgsanlet.cheftube.domain.util.Error

interface ProductsRepository {
    suspend fun getProductByBarcode(barcode: String): Result<Product, Error>

    enum class ProductError: Error {
        NO_INTERNET,
        EMPTY_RESPONSE,
        NOT_FOUND,
        API_ERROR,
        UNKNOWN
    }
}