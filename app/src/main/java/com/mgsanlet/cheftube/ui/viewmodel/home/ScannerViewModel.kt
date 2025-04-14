package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mgsanlet.cheftube.domain.repository.ProductRepository
import com.mgsanlet.cheftube.utils.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainProduct as Product

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val localeManager: LocaleManager
) : ViewModel() {

    private val _currentBarcode = MutableLiveData<String>()
    val currentBarcode: LiveData<String> = _currentBarcode

    private val _scannerState = MutableLiveData<ScannerState>()
    val scannerState: LiveData<ScannerState> = _scannerState

    private val _productName = MutableLiveData<String>()
    val productName: LiveData<String> = _productName


    fun setBarcode(barcode: String) {
        _currentBarcode.value = barcode
        CoroutineScope(Dispatchers.Main).launch {
            fetchProductData(barcode)
        }
    }

    private suspend fun fetchProductData(barcode: String) {
        _scannerState.value = ScannerState.Loading


        productRepository.getProductByBarcode(barcode).fold(
            onSuccess = { product ->
                _scannerState.value = ScannerState.ProductFound(product)
            },
            onFailure = { error ->
                _scannerState.value = when {
                    error.message?.contains("404") == true -> ScannerState.ProductNotFound
                    error is java.net.UnknownHostException -> ScannerState.NetworkError
                    else -> ScannerState.Error(0)
                }
            }
        )
    }

    fun getProductUrl(): String {
        val locale = localeManager.getCurrentLocale()
        return "https://${locale.language}.openfoodfacts.org/product/${_currentBarcode.value}"
    }

}

sealed class ScannerState {
    data object Loading : ScannerState()
    data class ProductFound(val product: Product) : ScannerState()
    data object ProductNotFound : ScannerState()
    data object NetworkError : ScannerState()
    data class Error(val code: Int) : ScannerState()
}
