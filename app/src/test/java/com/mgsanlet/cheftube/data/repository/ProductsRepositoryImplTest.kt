package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.ProductResponse
import com.mgsanlet.cheftube.data.source.remote.OpenFoodFactsApi
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.ProductError
import com.mgsanlet.cheftube.util.TestData.testDomainProduct
import com.mgsanlet.cheftube.util.TestData.testProductResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Clase de prueba para [ProductsRepositoryImpl].
 *
 * Pruebas unitarias para la implementación del repositorio de productos, verificando
 * el manejo correcto de las respuestas de la API y la transformación de datos.
 */
class ProductsRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var mockApi: OpenFoodFactsApi
    
    private lateinit var repository: ProductsRepositoryImpl

    /**
     * Configuración inicial antes de cada prueba.
     * Inicializa los mocks y crea una nueva instancia del repositorio.
     */
    @Before
    fun setUp() {
        repository = ProductsRepositoryImpl(mockApi)
    }

    /**
     * Prueba que la función [ProductsRepositoryImpl.fetchProductByBarcode] retorna un
     * [DomainResult.Success] con el producto
     * correctamente mapeado cuando la API responde con éxito.
     */
    @Test
    fun `fetchProductByBarcode with successful response returns Success with domain product`()
    = runTest {
        // Given
        val barcode = "1234567890123"
        coEvery {
            mockApi.fetchProductByBarcode(barcode) } returns Response.success(testProductResponse)

        // When
        val result = repository.fetchProductByBarcode(barcode)

        // Then
        assertTrue(result is DomainResult.Success)
        val successResult = result as DomainResult.Success
        assertEquals(testDomainProduct.barcode, successResult.data.barcode)
        assertEquals(testDomainProduct.englishName, successResult.data.englishName)
        assertEquals(testDomainProduct.italianName, successResult.data.italianName)
        assertEquals(testDomainProduct.spanishName, successResult.data.spanishName)
        
        coVerify(exactly = 1) { mockApi.fetchProductByBarcode(barcode) }
    }

    /**
     * Prueba que la función [ProductsRepositoryImpl.fetchProductByBarcode]
     * retorna [ProductError.EmptyResponse] cuando
     * la API responde exitosamente pero el cuerpo de la respuesta es nulo.
     */
    @Test
    fun `fetchProductByBarcode with null response body returns EmptyResponse error`() = runTest {
        // Given
        val barcode = "1234567890123"
        val response = Response.success<ProductResponse?>(null)
        coEvery { mockApi.fetchProductByBarcode(barcode) } returns response

        // When
        val result = repository.fetchProductByBarcode(barcode)

        // Then
        assertTrue(result is DomainResult.Error)
        assertEquals(ProductError.EmptyResponse, (result as DomainResult.Error).error)
    }

    /**
     * Prueba que la función [ProductsRepositoryImpl.fetchProductByBarcode]
     * retorna [ProductError.NotFound] cuando
     * la API responde con un código de error 404.
     */
    @Test
    fun `fetchProductByBarcode with 404 response returns NotFound error`() = runTest {
        // Given
        val barcode = "1234567890123"
        val errorResponse = "{\"status\":0,\"status_verbose\":\"product not found\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        val response = Response.error<ProductResponse>(404, errorResponse)
        coEvery { mockApi.fetchProductByBarcode(barcode) } returns response

        // When
        val result = repository.fetchProductByBarcode(barcode)

        // Then
        assertTrue(result is DomainResult.Error)
        assertEquals(ProductError.NotFound, (result as DomainResult.Error).error)
    }

    /**
     * Prueba que la función [ProductsRepositoryImpl.fetchProductByBarcode]
     * retorna [ProductError.ApiError] con el código
     * de estado cuando la API responde con un código de error distinto a 404.
     */
    @Test
    fun `fetchProductByBarcode with non-404 error response returns ApiError with status code`() =
        runTest {
        // Given
        val barcode = "1234567890123"
        val errorCode = 500
        val errorResponse = "{\"status\":0,\"status_verbose\":\"internal server error\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery {
            mockApi.fetchProductByBarcode(barcode)
        } returns Response.error(errorCode, errorResponse)

        // When
        val result = repository.fetchProductByBarcode(barcode)

        // Then
        assertTrue(result is DomainResult.Error)
        val error = (result as DomainResult.Error).error
        assertTrue(error is ProductError.ApiError)
    }

    /**
     * Prueba que la función [ProductsRepositoryImpl.fetchProductByBarcode]
     * retorna [ProductError.NoInternet] cuando
     * se produce una excepción de [UnknownHostException].
     */
    @Test
    fun `fetchProductByBarcode with UnknownHostException returns NoInternet error`() = runTest {
        // Given
        val barcode = "1234567890123"
        coEvery { mockApi.fetchProductByBarcode(barcode) } throws UnknownHostException()

        // When
        val result = repository.fetchProductByBarcode(barcode)

        // Then
        assertTrue(result is DomainResult.Error)
        assertEquals(ProductError.NoInternet, (result as DomainResult.Error).error)
    }

    /**
     * Prueba que la función [ProductsRepositoryImpl.fetchProductByBarcode]
     * retorna [ProductError.Unknown] cuando
     * se produce una excepción inesperada.
     */
    @Test
    fun `fetchProductByBarcode with unexpected exception returns Unknown error`() = runTest {
        // Given
        val barcode = "1234567890123"
        val exception = RuntimeException("Unexpected error")
        coEvery { mockApi.fetchProductByBarcode(barcode) } throws exception

        // When
        val result = repository.fetchProductByBarcode(barcode)

        // Then
        assertTrue(result is DomainResult.Error)
        val error = (result as DomainResult.Error).error
        assertTrue(error is ProductError.Unknown)
    }
}
