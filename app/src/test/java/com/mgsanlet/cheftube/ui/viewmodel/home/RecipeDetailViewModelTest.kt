package com.mgsanlet.cheftube.ui.viewmodel.home

import com.google.common.truth.Truth.assertThat
import com.mgsanlet.cheftube.domain.usecase.recipe.*
import com.mgsanlet.cheftube.domain.usecase.user.GetCurrentUserDataUseCase
import com.mgsanlet.cheftube.domain.usecase.user.IsCurrentUserAdminUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
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
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mgsanlet.cheftube.util.TestData
import org.junit.ClassRule

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Clase de prueba para [RecipeDetailViewModel].
 *
 * Pruebas unitarias para el ViewModel de detalle de receta, verificando
 * la lógica de negocio y las interacciones con los casos de uso.
 */
class RecipeDetailViewModelTest {

    companion object {
        @ClassRule
        @JvmField
        val instantTaskExecutorRule = InstantTaskExecutorRule()
    }

    @get:Rule
    val mockkRule = MockKRule(this)

    // Mocks de los casos de uso
    @MockK
    private lateinit var getRecipeById: GetRecipeByIdUseCase

    @MockK
    private lateinit var getCurrentUserData: GetCurrentUserDataUseCase

    @MockK
    private lateinit var alternateFavouriteRecipe: AlternateFavouriteRecipeUseCase

    @MockK
    private lateinit var postComment: PostCommentUseCase

    @MockK
    private lateinit var isCurrentUserAdminUseCase: IsCurrentUserAdminUseCase

    @MockK
    private lateinit var deleteRecipeUseCase: DeleteRecipeUseCase

    @MockK
    private lateinit var deleteCommentUseCase: DeleteCommentUseCase

    // Instancia del ViewModel bajo prueba
    private lateinit var viewModel: RecipeDetailViewModel

    // Datos de prueba
    private val testUser = TestData.testUser
    private val testRecipe = TestData.testRecipe

    private val testDispatcher = StandardTestDispatcher()

    /**
     * Configuración inicial antes de cada prueba.
     * Inicializa los mocks y el ViewModel.
     */
    @Before
    fun setUp() {
        // Configurar el dispatcher de corrutinas para pruebas
        Dispatchers.setMain(testDispatcher)

        // Inicializar los mocks
        MockKAnnotations.init(this, relaxUnitFun = true)

        // Configurar el dispatcher principal para pruebas
        Dispatchers.setMain(testDispatcher)

        // Configurar el comportamiento por defecto de los mocks
        coEvery { getRecipeById(any()) } returns DomainResult.Success(testRecipe)
        coEvery { getCurrentUserData() } returns DomainResult.Success(testUser)
        coEvery { isCurrentUserAdminUseCase() } returns DomainResult.Success(false)
        coEvery { alternateFavouriteRecipe(any(), any()) } returns DomainResult.Success(Unit)
        coEvery { postComment(any(), any(), any()) } returns DomainResult.Success(Unit)
        coEvery { deleteRecipeUseCase(any()) } returns DomainResult.Success(Unit)
        coEvery { deleteCommentUseCase(any(), any(), any()) } returns DomainResult.Success(Unit)

        // Crear una nueva instancia del ViewModel para cada prueba
        viewModel = RecipeDetailViewModel(
            getRecipeById = getRecipeById,
            getCurrentUserData = getCurrentUserData,
            alternateFavouriteRecipe = alternateFavouriteRecipe,
            postComment = postComment,
            isCurrentUserAdminUseCase = isCurrentUserAdminUseCase,
            deleteRecipeUseCase = deleteRecipeUseCase,
            deleteCommentUseCase = deleteCommentUseCase
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
     * Prueba que [RecipeDetailViewModel.loadRecipe] cargue correctamente una receta
     * cuando la operación es exitosa.
     */
    @Test
    fun `loadRecipe with valid id updates state with recipe`() = runTest {
        // Given: Configuramos los mocks necesarios
        coEvery { getRecipeById(testRecipe.id) } returns DomainResult.Success(testRecipe)

        coEvery { getCurrentUserData() } returns DomainResult.Success(
            testUser.copy(
                favouriteRecipes = emptyList(),
                createdRecipes = emptyList()
            )
        )

        // Creamos un observer para capturar los cambios de estado
        val states = mutableListOf<RecipeState>()
        val observer = object : androidx.lifecycle.Observer<RecipeState> {
            override fun onChanged(state: RecipeState) {
                states.add(state)
            }
        }

        // Observamos los cambios de estado
        viewModel.recipeState.observeForever(observer)

        try {
            // When: Solicitamos cargar la receta
            viewModel.loadRecipe(testRecipe.id)

            // Avanzamos el tiempo para permitir que se emita el estado Loading
            testDispatcher.scheduler.advanceTimeBy(100)
            
            // Verificamos que el primer estado sea Loading
            assertThat(states).hasSize(1)
            assertThat(states[0]).isInstanceOf(RecipeState.Loading::class.java)

            // Avanzamos el tiempo para que se complete la operación
            testDispatcher.scheduler.advanceUntilIdle()

            // Verificamos que se hayan emitido exactamente 2 estados (Loading y Success)
            assertThat(states).hasSize(2)


            // Verificamos que el último estado sea Success
            val successState = states[1] as? RecipeState.Success
            assertThat(successState).isNotNull()

            // Verificamos que la receta en el estado Success sea la esperada
            assertThat(successState?.recipe).isEqualTo(testRecipe)

            // Verificamos las interacciones con los mocks
            coVerify(exactly = 1) { getRecipeById(testRecipe.id) }
            coVerify(exactly = 2) { getCurrentUserData() }

        } catch (e: Exception) {
            println("Error durante la prueba: ${e.message}")
            e.printStackTrace()
            throw e
        } finally {
            viewModel.recipeState.removeObserver(observer)
        }
    }
}
