package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainProduct

interface ProductRepository {
    suspend fun getProductByBarcode(barcode: String): Result<DomainProduct>
}