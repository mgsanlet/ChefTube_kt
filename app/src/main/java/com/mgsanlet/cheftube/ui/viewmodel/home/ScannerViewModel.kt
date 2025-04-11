package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mgsanlet.cheftube.ChefTubeApplication
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScannerViewModel(private val app: ChefTubeApplication) : ViewModel() {

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
        delay(2000L)
        val url = BASE_URL + barcode
        val requestQueue = Volley.newRequestQueue(app.applicationContext)

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            {
                _scannerState.value = ScannerState.ProductFound
            },
            { error ->
                _scannerState.value = when (error.networkResponse?.statusCode) {
                    404 -> ScannerState.ProductNotFound
                    null -> ScannerState.NetworkError
                    else -> ScannerState.Error(error.networkResponse.statusCode)
                }
            }
        )
        requestQueue.add(stringRequest)
    }

    fun getProductUrl(): String {
        val locale = app.resources.configuration.locales[0]
        return "https://${locale.language}.openfoodfacts.org/product/${_currentBarcode.value}"
    }

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/api/v0/product/"
    }
}

@Suppress("UNCHECKED_CAST")
class ScannerViewModelFactory(
    private val app: ChefTubeApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScannerViewModel(app) as T
    }
}

sealed class ScannerState {
    data object Loading : ScannerState()
    data object ProductFound : ScannerState()
    data object ProductNotFound : ScannerState()
    data object NetworkError : ScannerState()
    data class Error(val code: Int) : ScannerState()
}
