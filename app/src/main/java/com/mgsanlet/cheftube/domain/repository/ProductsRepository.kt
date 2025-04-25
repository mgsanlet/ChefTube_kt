package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainProduct as Product
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.ProductError

interface ProductsRepository {
    suspend fun fetchProductByBarcode(barcode: String): DomainResult<Product, ProductError>
}