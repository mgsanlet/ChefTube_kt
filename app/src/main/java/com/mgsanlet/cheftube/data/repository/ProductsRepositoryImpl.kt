package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.ProductResponse
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.data.source.remote.OpenFoodFactsApi
import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.domain.repository.ProductsRepository
import java.net.UnknownHostException
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.ProductError

/**
 * Implementación concreta de [ProductsRepository] que obtiene datos de productos
 * desde la API de Open Food Facts.
 *
 * @property api Cliente de la API de Open Food Facts
 */
class ProductsRepositoryImpl @Inject constructor(
    private val api: OpenFoodFactsApi,
    private val firebaseApi: FirebaseApi
) : ProductsRepository {
    
    /**
     * Obtiene un producto por su código de barras.
     *
     * @param barcode Código de barras del producto a buscar
     * @return [DomainResult] que contiene el producto si la operación fue exitosa,
     *         o un error en caso contrario
     */
    override suspend fun fetchProductByBarcode(barcode: String):
            DomainResult<DomainProduct, ProductError> {
        firebaseApi.registerScanTimestamp()

        return try {
            val response = api.fetchProductByBarcode(barcode)

            if (response.isSuccessful) {
                response.body()?.let { productResponse ->
                    DomainResult.Success(productResponse.toDomainProduct())
                } ?: DomainResult.Error(ProductError.EmptyResponse)
            } else {
                if (response.code() == 404) {
                    DomainResult.Error(ProductError.NotFound)
                } else {
                    DomainResult.Error(ProductError.ApiError(response.code()))
                }
            }
        } catch (_: UnknownHostException) {
            DomainResult.Error(ProductError.NoInternet)
        } catch (e: Exception) {
            DomainResult.Error(ProductError.Unknown(e.message))
        }
    }

    /**
     * Extensión para convertir un [ProductResponse] a un [DomainProduct].
     * 
     * @receiver Respuesta de la API a convertir
     * @return [DomainProduct] con los datos del producto
     */
    private fun ProductResponse.toDomainProduct(): DomainProduct {
        val defaultName = product!!.name ?: ""

        return DomainProduct(
            barcode = product.code,
            englishName = product.nameEn ?: defaultName,
            italianName = product.nameIt ?: defaultName,
            spanishName = product.nameEs ?: defaultName
        )
    }
}