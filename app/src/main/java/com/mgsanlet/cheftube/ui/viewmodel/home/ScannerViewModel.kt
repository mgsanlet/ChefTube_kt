package com.mgsanlet.cheftube.ui.viewmodel.home

/**
 * ViewModel para la funcionalidad de escaneo de códigos de barras.
 *
 * Maneja la lógica relacionada con el escaneo de códigos de barras,
 * búsqueda de productos y gestión del estado de la UI del escáner.
 */

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mgsanlet.cheftube.domain.usecase.product.FetchProductByBarcodeUseCase
import com.mgsanlet.cheftube.domain.util.error.ProductError
import com.mgsanlet.cheftube.ui.util.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainProduct as Product

/**
 * ViewModel para la pantalla de escaneo de productos.
 *
 * Gestiona el proceso de escaneo de códigos de barras, búsqueda de productos
 * y proporciona funcionalidades para obtener información localizada de los productos.
 *
 * @property fetchProductByBarcode Caso de uso para buscar productos por código de barras
 * @property localeManager Gestor de configuración regional para localización
 */
@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val fetchProductByBarcode: FetchProductByBarcodeUseCase,
    private val localeManager: LocaleManager
) : ViewModel() {

    /** Código de barras actualmente escaneado */
    private val _currentBarcode = MutableLiveData<String>()

    /** Estado interno del escáner */
    private val _scannerState = MutableLiveData<ScannerState>()
    
    /** Estado observable del escáner */
    val scannerState: LiveData<ScannerState> = _scannerState

    /**
     * Inicia la búsqueda de un producto por su código de barras.
     * Actualiza el estado del escáner durante la búsqueda.
     *
     * @param barcode Código de barras a buscar
     */
    fun searchBarcode(barcode: String) {
        _currentBarcode.value = barcode
        CoroutineScope(Dispatchers.Main).launch {
            fetchProductData(barcode)
        }
    }

    /**
     * Obtiene los datos del producto a partir de un código de barras.
     * Actualiza el estado del escáner con el resultado.
     *
     * @param barcode Código de barras del producto
     */
    private suspend fun fetchProductData(barcode: String) {
        _scannerState.value = ScannerState.Loading

        fetchProductByBarcode(barcode).fold(
            onSuccess = { product ->
                _scannerState.value = ScannerState.ProductFound(product)
            },
            onError = { error ->
                _scannerState.value = ScannerState.Error(error)
            }
        )
    }

    /**
     * Genera la URL del producto en OpenFoodFacts.
     *
     * @return URL completa del producto en el idioma actual
     */
    fun getProductUrl(): String {
        val locale = localeManager.getCurrentLocale()
        return "https://${locale.language}.openfoodfacts.org/product/${_currentBarcode.value}"
    }

    /**
     * Obtiene el nombre del producto en el idioma actual.
     *
     * @return Nombre localizado del producto
     * @throws IllegalStateException si se llama cuando no hay un producto cargado
     */
    fun getLocalizedProductName(): String {
        val state = _scannerState.value
        check(state is ScannerState.ProductFound) {
            "Invalid use of getLocalizedProductName()"
        }
        return state.getLocalizedName(localeManager.getCurrentLocale().language)
    }

}

/**
 * Estados posibles de la UI para el escáner de códigos de barras.
 */
sealed class ScannerState {
    /** Estado de carga, mostrando un indicador de progreso */
    data object Loading : ScannerState()
    
    /** Producto encontrado exitosamente */
    data class ProductFound(val product: Product) : ScannerState() {
        /**
         * Obtiene el nombre del producto en el idioma especificado.
         *
         * @param languageCode Código de idioma (ej: "es", "en", "it")
         * @return Nombre del producto en el idioma solicitado, o en inglés si no está disponible
         */
        fun getLocalizedName(languageCode: String): String {
            return when (languageCode) {
                "en" -> product.englishName
                "it" -> product.italianName
                "es" -> product.spanishName
                else -> product.englishName
            }
        }
    }
    
    /** Error durante el escaneo o búsqueda */
    data class Error(val error: ProductError) : ScannerState()
}
