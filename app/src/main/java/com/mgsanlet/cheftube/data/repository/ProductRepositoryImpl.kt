package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.toDomainProduct
import com.mgsanlet.cheftube.data.source.remote.ProductApi
import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.domain.repository.ProductRepository
import com.mgsanlet.cheftube.utils.exception.ChefTubeException
import com.mgsanlet.cheftube.utils.exception.ProductException
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: ProductApi
) : ProductRepository {
    override suspend fun getProductByBarcode(barcode: String): Result<DomainProduct> {
        return try {
            val response = api.getProductByBarcode(barcode)

            if (response.isSuccessful) {
                response.body()?.let { productResponse ->

                    Result.success(productResponse.toDomainProduct())

                } ?: Result.failure(ProductException.EmptyResponse)

            } else {
                if (response.code() == 404) {
                    Result.failure(ProductException.NotFound)
                }else{
                    Result.failure(ChefTubeException.ApiError(response.code()))
                }
            }

        } catch (e: Exception) {
            Result.failure(ChefTubeException.UnknownError)
        }
    }
}