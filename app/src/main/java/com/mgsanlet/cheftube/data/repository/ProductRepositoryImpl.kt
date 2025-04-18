package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.toDomainProduct
import com.mgsanlet.cheftube.data.source.remote.ProductApi
import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.domain.repository.ProductRepository
import com.mgsanlet.cheftube.utils.error.ChefTubeError
import com.mgsanlet.cheftube.utils.error.ProductError
import java.net.UnknownHostException
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

                } ?: Result.failure(ProductError.EmptyResponse)

            } else {
                if (response.code() == 404) {
                    Result.failure(ProductError.NotFound)
                }else{
                    Result.failure(ChefTubeError.ApiError(response.code()))
                }
            }

        } catch (offline: UnknownHostException) {
            Result.failure(ChefTubeError.NoInternet)
        } catch (e: Exception) {
            Result.failure(ChefTubeError.UnknownError(e.message))
        }
    }
}