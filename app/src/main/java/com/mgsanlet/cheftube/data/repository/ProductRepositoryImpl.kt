package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.source.remote.ProductRemoteDataSource
import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.domain.repository.ProductRepository
import java.util.Locale
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource
) : ProductRepository {
    override suspend fun getProductByBarcode(barcode: String): Result<DomainProduct> {
        return try {
            val response = remoteDataSource.getProduct(barcode)
            if (response.isSuccessful) {
                response.body()?.let { productResponse ->
                    val currentLocale = Locale.getDefault().language
                    val name = when (currentLocale) {
                        "en" -> productResponse.product.product_name_en
                        "it" -> productResponse.product.product_name_it
                        "es" -> productResponse.product.product_name_es
                        else -> productResponse.product.product_name
                    } ?: productResponse.product.product_name ?: "Unknown Product"

                    Result.success(DomainProduct(barcode, name))
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}