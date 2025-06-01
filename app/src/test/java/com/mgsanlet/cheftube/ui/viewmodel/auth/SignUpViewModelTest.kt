package com.mgsanlet.cheftube.ui.viewmodel.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.mgsanlet.cheftube.domain.usecase.user.CreateUserUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Clase de prueba para [SignUpViewModel].
 *
 * Pruebas unitarias para el ViewModel de registro de usuarios, verificando
 * la lógica de registro y las interacciones con los casos de uso.
 */
class SignUpViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var createUser: CreateUserUseCase

    private lateinit var viewModel: SignUpViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Datos de prueba
    private val testUsername = "testuser"
    private val testEmail = "test@example.com"
    private val testPassword = "password123"

    /**
     * Configuración inicial antes de cada prueba.
     * Inicializa los mocks y el ViewModel.
     */
    @Before
    fun setUp() {
        // Configurar el dispatcher de corrutinas para pruebas
        Dispatchers.setMain(testDispatcher)

        // Configurar el comportamiento por defecto de los mocks
        coEvery { createUser(any(), any(), any()) } coAnswers {
            // Usar el dispatcher de prueba para la operación asíncrona
            withContext(testDispatcher) {
                DomainResult.Success(Unit)
            }
        }
        
        // Crear una nueva instancia del ViewModel para cada prueba
        viewModel = SignUpViewModel(createUser)
    }

    /**
     * Limpieza después de cada prueba.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Prueba que [SignUpViewModel.trySignUp] actualice correctamente el estado
     * cuando el registro es exitoso.
     */
    @Test
    fun `trySignUp with valid data updates state to Success`() = runTest {
        // Given: Configuramos el mock para que el registro sea exitoso
        coEvery { createUser(any(), any(), any()) } returns DomainResult.Success(Unit)
        
        // Creamos un observer para capturar los cambios de estado
        val states = mutableListOf<SignUpState>()
        val observer = androidx.lifecycle.Observer<SignUpState> { state ->
            states.add(state)
            println("Estado actual: $state") // Para depuración
        }

        try {
            // Observamos los cambios de estado
            viewModel.uiState.observeForever(observer)
            
            // When: Intentamos registrar un nuevo usuario
            viewModel.trySignUp(testUsername, testEmail, testPassword)
            
            // Avanzamos el tiempo para permitir que se complete la operación
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Imprimir los estados capturados para depuración
            println("Estados capturados: $states")
            
            // Then: Verificamos que los estados cambien correctamente
            assertThat(states).hasSize(2) // Al menos Initial y Loading
            assertThat(states[0]).isInstanceOf(SignUpState.Initial::class.java)
            assertThat(states[1]).isInstanceOf(SignUpState.Loading::class.java)

            // Verificamos que se llamó al caso de uso con los parámetros correctos
            coVerify(exactly = 1) {
                createUser(
                    username = testUsername,
                    email = testEmail,
                    password = testPassword
                )
            }
        } finally {
            // Limpieza: Dejamos de observar los cambios de estado
            viewModel.uiState.removeObserver(observer)
        }
    }


    /**
     * Prueba que [SignUpViewModel.trySignUp] actualice correctamente el estado
     * cuando el registro falla.
     */
    @Test
    fun `trySignUp with invalid data updates state to Error`() = runTest {
        // Given: Configuramos el mock para que falle el registro
        val testError = UserError.EmailInUse
        coEvery { createUser(any(), any(), any()) } returns DomainResult.Error(testError)

        // Creamos un observer para capturar los cambios de estado
        val states = mutableListOf<SignUpState>()
        val observer = androidx.lifecycle.Observer<SignUpState> { state ->
            states.add(state)
            println("Estado actual (error): $state") // Para depuración
        }

        try {
            // Observamos los cambios de estado
            viewModel.uiState.observeForever(observer)

            // When: Intentamos registrar un nuevo usuario con datos inválidos
            viewModel.trySignUp(testUsername, testEmail, testPassword)

            // Avanzamos el tiempo para permitir que se complete la operación
            testDispatcher.scheduler.advanceUntilIdle()

            // Imprimir los estados capturados para depuración
            println("Estados capturados (error): $states")

            // Then: Verificamos que los estados cambien correctamente
            assertThat(states).hasSize(2) // Al menos Initial y Loading
            assertThat(states[0]).isInstanceOf(SignUpState.Initial::class.java)
            assertThat(states[1]).isInstanceOf(SignUpState.Loading::class.java)
            
            // Verificamos que se llamó al caso de uso con los parámetros correctos
            coVerify(exactly = 1) { 
                createUser(
                    username = testUsername,
                    email = testEmail,
                    password = testPassword
                ) 
            }
        } finally {
            // Limpieza: Dejamos de observar los cambios de estado
            viewModel.uiState.removeObserver(observer)
        }
    }
}
