package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.ProductResponse
import com.mgsanlet.cheftube.data.source.remote.OpenFoodFactsApi
import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.domain.repository.ProductsRepository
import java.net.UnknownHostException
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.ProductError


class ProductsRepositoryImpl @Inject constructor(
    private val api: OpenFoodFactsApi
) : ProductsRepository {
    override suspend fun fetchProductByBarcode(barcode: String): DomainResult<DomainProduct, ProductError> {
        return try {
            val response = api.fetchProductByBarcode(barcode)

            if (response.isSuccessful) {
                response.body()?.let { productResponse ->

                    DomainResult.Success(productResponse.toDomainProduct())

                } ?: DomainResult.Error(ProductError.EmptyResponse)

            } else {
                if (response.code() == 404) {
                    DomainResult.Error(ProductError.NotFound)
                }else{
                    DomainResult.Error(ProductError.ApiError(response.code()))
                }
            }

        } catch (offline: UnknownHostException) {
            DomainResult.Error(ProductError.NoInternet)
        } catch (e: Exception) {
            DomainResult.Error(ProductError.Unknown(e.message))
        }
    }

    fun ProductResponse.toDomainProduct(): DomainProduct {
        val defaultName = product!!.product_name ?: ""

        return DomainProduct(
            barcode = product.code,
            englishName = product.product_name_en ?: defaultName,
            italianName = product.product_name_it ?: defaultName,
            spanishName = product.product_name_es ?: defaultName
        )
    }
}