package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.utils.Resource

interface ProductRepository {
    suspend fun getProductByBarcode(barcode: String): Resource<DomainProduct>
}