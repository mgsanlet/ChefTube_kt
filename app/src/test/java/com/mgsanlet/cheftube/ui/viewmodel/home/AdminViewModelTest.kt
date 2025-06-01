package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.mgsanlet.cheftube.domain.usecase.user.GetInactiveUsersUseCase
import com.mgsanlet.cheftube.domain.usecase.stats.GetStatsUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.util.TestData
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Clase de prueba para [AdminViewModel].
 *
 * Pruebas unitarias para el ViewModel del panel de administración, verificando
 * la carga de estadísticas y usuarios inactivos.
 */
class AdminViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var getStats: GetStatsUseCase

    @MockK
    private lateinit var getInactiveUsers: GetInactiveUsersUseCase

    private lateinit var viewModel: AdminViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Datos de prueba
    private val testStats = TestData.testStats
    
    private val testInactiveUsers = listOf(TestData.testUser)

    /**
     * Configuración inicial antes de cada prueba.
     * Inicializa los mocks y el ViewModel.
     */
    @Before
    fun setUp() {
        // Configurar el dispatcher de corrutinas para pruebas
        Dispatchers.setMain(testDispatcher)

        // Configurar el comportamiento por defecto de los mocks
        coEvery { getStats() } returns DomainResult.Success(testStats)
        coEvery { getInactiveUsers() } returns DomainResult.Success(testInactiveUsers)
    }

    /**
     * Limpieza después de cada prueba.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Prueba que [AdminViewModel] cargue correctamente las estadísticas
     * y usuarios inactivos al inicializarse.
     */
    @Test
    fun `loadData on init updates state with stats and inactive users`() = runTest {
        // Given: Los mocks ya están configurados en setUp()
        
        // Creamos un observer para capturar los cambios de estado
        val states = mutableListOf<AdminState>()
        val observer = androidx.lifecycle.Observer<AdminState> { state ->
            state.let { states.add(it) }
        }

        try {
            // Crear una nueva instancia del ViewModel para cada prueba
            viewModel = AdminViewModel(getStats, getInactiveUsers)
            // Observamos los cambios de estado
            viewModel.uiState.observeForever(observer)
            
            // When: Avanzamos el tiempo para permitir que se completen las llamadas asíncronas
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: Verificamos que los estados cambien correctamente
            assertThat(states).hasSize(2)
            assertThat(states[0]).isInstanceOf(AdminState.Loading::class.java)
            
            val contentState = states[1] as? AdminState.Content
            assertThat(contentState).isNotNull()
            
            // Verificamos que el estado Content tenga los datos correctos
            assertThat(contentState?.stats).isNotNull()
            assertThat(contentState?.inactiveUsers).containsExactlyElementsIn(testInactiveUsers)
            
            // Verificamos que se llamaron los casos de uso correctos
            coVerify(exactly = 1) { getStats() }
            coVerify(exactly = 1) { getInactiveUsers() }
        } finally {
            // Limpieza: Dejamos de observar los cambios de estado
            viewModel.uiState.removeObserver(observer)
        }
    }
}
