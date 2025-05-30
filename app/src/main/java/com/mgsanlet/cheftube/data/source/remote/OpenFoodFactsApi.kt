package com.mgsanlet.cheftube.data.source.remote

import com.mgsanlet.cheftube.data.model.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("product/{barcode}")
    suspend fun fetchProductByBarcode(@Path("barcode") barcode: String): Response<ProductResponse>
}