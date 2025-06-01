package com.mgsanlet.cheftube.domain.usecase.product

import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.domain.repository.ProductsRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.ProductError
import javax.inject.Inject

/**
 * Caso de uso para buscar un producto por su código de barras.
 * Se utiliza para obtener información detallada de un producto escaneado.
 *
 * @property productsRepository Repositorio de productos para realizar la búsqueda
 */
class FetchProductByBarcodeUseCase @Inject constructor(
    private val productsRepository: ProductsRepository
) {
    /**
     * Ejecuta el caso de uso para buscar un producto por su código de barras.
     *
     * @param barcode Código de barras del producto a buscar
     * @return [DomainResult] con los datos del producto encontrado o [ProductError] si no se encuentra o hay un error
     */
    suspend operator fun invoke(barcode: String): DomainResult<DomainProduct, ProductError> {
        return productsRepository.fetchProductByBarcode(barcode)
    }
}