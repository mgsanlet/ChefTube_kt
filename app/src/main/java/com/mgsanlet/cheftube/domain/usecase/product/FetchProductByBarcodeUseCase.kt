package com.mgsanlet.cheftube.domain.usecase.product

import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.domain.repository.ProductsRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.ProductError
import javax.inject.Inject

class FetchProductByBarcodeUseCase @Inject constructor(
    private val productsRepository: ProductsRepository
) {
    suspend operator fun invoke(barcode: String): DomainResult<DomainProduct, ProductError> {
        return productsRepository.fetchProductByBarcode(barcode)
    }
}