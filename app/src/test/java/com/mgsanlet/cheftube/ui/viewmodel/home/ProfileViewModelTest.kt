package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.mgsanlet.cheftube.domain.usecase.user.*
import com.mgsanlet.cheftube.domain.util.DomainResult
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

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Clase de prueba para [ProfileViewModel].
 *
 * Pruebas unitarias para el ViewModel del perfil de usuario, verificando
 * la lógica de negocio y las interacciones con los casos de uso.
 */
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var getUserDataById: GetUserDataByIdUseCase

    @MockK
    private lateinit var getCurrentUserData: GetCurrentUserDataUseCase

    @MockK
    private lateinit var updateCurrentUserData: UpdateCurrentUserDataUseCase

    @MockK
    private lateinit var updateUserData: UpdateUserDataUseCase

    @MockK
    private lateinit var saveProfilePicture: SaveProfilePictureUseCase

    @MockK
    private lateinit var updatePassword: UpdatePasswordUseCase

    @MockK
    private lateinit var deleteAccount: DeleteAccountUseCase

    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Datos de prueba
    private val testUser = TestData.testUser

    /**
     * Configuración inicial antes de cada prueba.
     * Inicializa los mocks y el ViewModel.
     */
    @Before
    fun setUp() {
        // Configurar el dispatcher de corrutinas para pruebas
        Dispatchers.setMain(testDispatcher)

        // Configurar el comportamiento por defecto de los mocks
        coEvery { getCurrentUserData() } returns DomainResult.Success(testUser)

        // Crear una nueva instancia del ViewModel para cada prueba
        viewModel = ProfileViewModel(
            getUserDataById = getUserDataById,
            getCurrentUserData = getCurrentUserData,
            updateCurrentUserData = updateCurrentUserData,
            updateUserData = updateUserData,
            saveProfilePicture = saveProfilePicture,
            updatePassword = updatePassword,
            deleteAccount = deleteAccount
        )
    }

    /**
     * Limpieza después de cada prueba.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Prueba que [ProfileViewModel.loadCurrentUserData] cargue correctamente
     * los datos del usuario actual y actualice el estado.
     */
    @Test
    fun `loadCurrentUserData with valid user updates state correctly`() = runTest {
        // Given: Configuramos los mocks necesarios
        coEvery { getCurrentUserData() } returns DomainResult.Success(testUser)

        // Creamos un observer para capturar los cambios de estado
        val states = mutableListOf<ProfileState>()
        val observer = androidx.lifecycle.Observer<ProfileState> { state ->
            state.let { states.add(it) }
        }

        try {
            // Observamos los cambios de estado
            viewModel.uiState.observeForever(observer)

            // When: Solicitamos cargar los datos del usuario actual
            viewModel.loadCurrentUserData()

            // Avanzamos el tiempo para permitir que se emita el estado Loading
            testDispatcher.scheduler.advanceTimeBy(100)

            // Then: Verificamos que los estados cambien correctamente
            assertThat(states).hasSize(2)
            assertThat(states[0]).isInstanceOf(ProfileState.Loading::class.java)
            assertThat(states[1]).isInstanceOf(ProfileState.LoadSuccess::class.java)

            // Verificamos que se llamó al caso de uso correcto
            coVerify { getCurrentUserData() }
        } finally {
            // Limpieza: Dejamos de observar los cambios de estado
            viewModel.uiState.removeObserver(observer)
        }
    }
}
