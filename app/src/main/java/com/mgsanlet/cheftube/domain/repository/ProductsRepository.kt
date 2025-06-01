package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.ProductError
import com.mgsanlet.cheftube.domain.model.DomainProduct as Product

/**
 * Interfaz que define las operaciones disponibles para la gestión de productos.
 * Todas las operaciones son suspendidas para permitir operaciones asíncronas.
 */
interface ProductsRepository {
    /**
     * Obtiene un producto por su código de barras.
     *
     * @param barcode Código de barras del producto a buscar
     * @return [DomainResult] con el producto encontrado o error
     */
    suspend fun fetchProductByBarcode(barcode: String): DomainResult<Product, ProductError>
}