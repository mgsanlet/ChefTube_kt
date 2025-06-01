package com.mgsanlet.cheftube.data.source.remote

import com.mgsanlet.cheftube.data.model.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interfaz que define las operaciones de la API de Open Food Facts.
 * Permite obtener información de productos alimenticios escaneando su código de barras.
 */
interface OpenFoodFactsApi {
    /**
     * Obtiene la información de un producto a partir de su código de barras.
     *
     * @param barcode Código de barras del producto a buscar
     * @return [Response] con los datos del producto si se encuentra,
     *         o un error si no se encuentra o hay un problema en la petición
     */
    @GET("product/{barcode}")
    suspend fun fetchProductByBarcode(@Path("barcode") barcode: String): Response<ProductResponse>
}