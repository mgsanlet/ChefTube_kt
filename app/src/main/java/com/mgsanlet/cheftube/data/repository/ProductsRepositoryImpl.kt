package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.toDomainProduct
import com.mgsanlet.cheftube.data.source.remote.ProductApi
import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.domain.repository.ProductsRepository
import com.mgsanlet.cheftube.domain.repository.ProductsRepository.ProductError
import java.net.UnknownHostException
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.util.Result


class ProductsRepositoryImpl @Inject constructor(
    private val api: ProductApi
) : ProductsRepository {
    override suspend fun getProductByBarcode(barcode: String): Result<DomainProduct, ProductError> {
        return try {
            val response = api.getProductByBarcode(barcode)

            if (response.isSuccessful) {
                response.body()?.let { productResponse ->

                    Result.Success(productResponse.toDomainProduct())

                } ?: Result.Error(ProductError.EMPTY_RESPONSE)

            } else {
                if (response.code() == 404) {
                    Result.Error(ProductError.NOT_FOUND)
                }else{
                    Result.Error(ProductError.API_ERROR)
                }
            }

        } catch (offline: UnknownHostException) {
            Result.Error(ProductError.NO_INTERNET)
        } catch (e: Exception) {
            Result.Error(ProductError.UNKNOWN)
        }
    }


}