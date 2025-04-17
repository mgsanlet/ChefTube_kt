package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mgsanlet.cheftube.domain.repository.ProductRepository
import com.mgsanlet.cheftube.utils.LocaleManager
import com.mgsanlet.cheftube.utils.exception.ChefTubeException
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
            onFailure = { exception ->
                _scannerState.value = ScannerState.Error(exception as ChefTubeException)
            }
        )
    }

    fun getProductUrl(): String {
        val locale = localeManager.getCurrentLocale()
        return "https://${locale.language}.openfoodfacts.org/product/${_currentBarcode.value}"
    }

    fun getLocalizedProductName(): String {
        val state = _scannerState.value
        check(state is ScannerState.ProductFound) {
            "Invalid use of getLocalizedProductName()"
        }
        return state.getLocalizedName(localeManager.getCurrentLocale().language)
    }

}

sealed class ScannerState {
    data object Loading : ScannerState()
    data class ProductFound(val product: Product) : ScannerState() {
        fun getLocalizedName(languageCode: String): String {
            return when (languageCode) {
                "en" -> product.englishName
                "it" -> product.italianName
                "es" -> product.spanishName
                else -> product.englishName
            }
        }
    }

    data class Error(val error: ChefTubeException) : ScannerState()
}
