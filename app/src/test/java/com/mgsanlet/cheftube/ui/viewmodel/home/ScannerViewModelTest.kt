package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.mgsanlet.cheftube.domain.usecase.product.FetchProductByBarcodeUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.ui.util.LocaleManager
import com.mgsanlet.cheftube.util.TestData
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Clase de prueba para [ScannerViewModel].
 *
 * Pruebas unitarias para el ViewModel del escáner de códigos de barras, verificando
 * la lógica de negocio y las interacciones con los casos de uso.
 */
class ScannerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var fetchProductByBarcode: FetchProductByBarcodeUseCase

    @MockK
    private lateinit var localeManager: LocaleManager

    private lateinit var viewModel: ScannerViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Datos de prueba
    private val testProduct = TestData.testDomainProduct
    private val testBarcode = testProduct.barcode

    /**
     * Configuración inicial antes de cada prueba.
     * Inicializa los mocks y el ViewModel.
     */
    @Before
    fun setUp() {
        // Configurar el dispatcher de corrutinas para pruebas
        Dispatchers.setMain(testDispatcher)

        // Configurar el comportamiento por defecto de los mocks
        coEvery { fetchProductByBarcode(any()) } returns DomainResult.Success(testProduct)
        every { localeManager.getCurrentLocale() } returns Locale.ENGLISH

        // Crear una nueva instancia del ViewModel para cada prueba
        viewModel = ScannerViewModel(fetchProductByBarcode, localeManager)
    }

    /**
     * Limpieza después de cada prueba.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Prueba que [ScannerViewModel.searchBarcode] actualice correctamente el estado
     * a [ScannerState.ProductFound] cuando la búsqueda es exitosa.
     */
    @Test
    fun `searchBarcode with valid barcode updates state to ProductFound`() = runTest {
        // Given: Configuramos el mock para devolver un producto de prueba
        coEvery { fetchProductByBarcode(testBarcode) } returns DomainResult.Success(testProduct)

        // Creamos un observer para capturar los cambios de estado
        val states = mutableListOf<ScannerState>()
        val observer = androidx.lifecycle.Observer<ScannerState> { state ->
            states.add(state)
        }

        try {
            // Observamos los cambios de estado
            viewModel.scannerState.observeForever(observer)

            // When: Solicitamos buscar un código de barras
            viewModel.searchBarcode(testBarcode)

            // Avanzamos el tiempo para permitir que se emita el estado Loading
            testDispatcher.scheduler.advanceTimeBy(100)

            // Then: Verificamos que los estados cambien correctamente
            assertThat(states).hasSize(2)
            assertThat(states[0]).isInstanceOf(ScannerState.Loading::class.java)
            
            val successState = states[1] as ScannerState.ProductFound
            assertThat(successState.product.barcode).isEqualTo(testBarcode)
            assertThat(successState.product.englishName).isEqualTo("Test Product EN")
            assertThat(successState.product.spanishName).isEqualTo("Producto de Prueba ES")
            assertThat(successState.product.italianName).isEqualTo("Prodotto di Test IT")
            
            // Verificamos que se llamó al caso de uso con el código de barras correcto
            coVerify { fetchProductByBarcode(testBarcode) }
        } finally {
            // Limpieza: Dejamos de observar los cambios de estado
            viewModel.scannerState.removeObserver(observer)
        }
    }

    /**
     * Prueba que [ScannerViewModel.getProductUrl] devuelva la URL correcta
     * para un código de barras dado.
     */
    @Test
    fun `getProductUrl returns correct URL for barcode`() {
        // Given: Configuramos el código de barras actual
        viewModel.searchBarcode(testBarcode)
        
        // When: Obtenemos la URL del producto
        val productUrl = viewModel.getProductUrl()
        
        // Then: Verificamos que la URL sea la esperada
        assertThat(productUrl).isEqualTo("https://en.openfoodfacts.org/product/$testBarcode")
    }

    /**
     * Prueba que [ScannerViewModel.getLocalizedProductName] devuelva el nombre
     * del producto en el idioma actual.
     */
    @Test
    fun `getLocalizedProductName returns name in current locale`() = runTest {
        // Given: Configuramos un producto encontrado
        coEvery { fetchProductByBarcode(testBarcode) } returns DomainResult.Success(testProduct)
        viewModel.searchBarcode(testBarcode)
        
        // Avanzamos el tiempo para permitir que se complete la búsqueda
        testDispatcher.scheduler.advanceTimeBy(100)
        
        // Configuramos el idioma a español
        every { localeManager.getCurrentLocale() } returns Locale("es", "ES")
        
        // When: Obtenemos el nombre localizado
        val localizedName = viewModel.getLocalizedProductName()
        
        // Then: Verificamos que el nombre esté en español
        assertThat(localizedName).isEqualTo("Producto de Prueba ES")
    }
}
