package com.mgsanlet.cheftube.data.source.remote

import com.mgsanlet.cheftube.data.model.ProductResponse
import retrofit2.Response
import javax.inject.Inject

class ProductRemoteDataSource @Inject constructor(
    private val productApi: ProductApi
) {
    suspend fun getProduct(barcode: String): Response<ProductResponse> {
        return productApi.getProduct(barcode)
    }
}