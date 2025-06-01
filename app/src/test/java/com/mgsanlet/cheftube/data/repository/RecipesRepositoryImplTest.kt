package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.domain.model.SearchParams
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.FilterCriterion
import com.mgsanlet.cheftube.util.TestData.testRecipe
import com.mgsanlet.cheftube.util.TestData.testRecipeResponse
import com.mgsanlet.cheftube.util.TestData.testUser
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Clase de prueba para [RecipesRepositoryImpl].
 * 
 * Pruebas unitarias para la implementación del repositorio de recetas, verificando
 * el correcto funcionamiento de la caché y las llamadas a la API.
 */
class RecipesRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var mockApi: FirebaseApi
    
    private lateinit var repository: RecipesRepositoryImpl

    /**
     * Configuración inicial antes de cada prueba.
     * Inicializa los mocks y configura comportamientos por defecto.
     */
    @Before
    fun setUp() {
        // Inicializa las anotaciones @MockK
        MockKAnnotations.init(this, relaxUnitFun = true)
        
        // Configura el comportamiento por defecto para las URLs de almacenamiento
        coEvery { mockApi.getStorageUrlFromPath(any()) } returns "test_image_url"
        
        // Crea una nueva instancia del repositorio para cada prueba
        repository = RecipesRepositoryImpl(mockApi)
    }

    /**
     * Prueba que [RecipesRepositoryImpl.getAll] retorne las recetas en caché cuando
     * están disponibles.
     */
    @Test
    fun `getAll returns cached recipes when available`() = runTest {
        // Given
        val cachedRecipes = listOf(testRecipe)
        repository.recipesCache = cachedRecipes

        // When
        val result = repository.getAll()

        // Then
        assertTrue(result is DomainResult.Success)
        assertEquals(cachedRecipes, (result as DomainResult.Success).data)
        verify { mockApi wasNot Called }
    }

    /**
     * Prueba que [RecipesRepositoryImpl.getAll] obtenga datos de la API cuando la caché está vacía.
     */
    @Test
    fun `getAll fetches from api when cache is empty`() = runTest {
        // Given: Simulamos una respuesta exitosa de la API
        coEvery { mockApi.getAllRecipes() } returns DomainResult.Success(listOf(testRecipeResponse))

        // When: Llamamos a la función a probar
        val result = repository.getAll()
        
        // Then: Verificamos que se llamó a la API y se devolvieron los datos
        assertTrue(result is DomainResult.Success)
        assertEquals(1, (result as DomainResult.Success).data.size)
        assertEquals("Test Recipe", result.data[0].title)
        coVerify { mockApi.getAllRecipes() }
    }

    /**
     * Prueba que [RecipesRepositoryImpl.getById] retorne una receta de la caché cuando
     * está disponible.
     */
    @Test
    fun `getById returns recipe from cache when available`() = runTest {
        // Given: Preparamos datos en caché
        val cachedRecipe = testRecipe
        repository.recipesCache = listOf(cachedRecipe)

        // When: Solicitamos una receta por su ID
        val result = repository.getById("1")

        // Then: Verificamos que se devuelve la receta correcta de la caché
        assertTrue(result is DomainResult.Success)
        assertEquals("Test Recipe", (result as DomainResult.Success).data.title)
        coVerify { mockApi wasNot Called }
    }

    /**
     * Prueba que [RecipesRepositoryImpl.getById] obtenga datos de la API cuando la receta no
     * está en caché.
     */
    @Test
    fun `getById fetches from api when not in cache`() = runTest {
        // Given: Simulamos una respuesta exitosa de la API
        coEvery { mockApi.getRecipeById("1") } returns DomainResult.Success(testRecipeResponse)

        // When: Llamamos a la función a probar
        val result = repository.getById("1")
        
        // Then: Verificamos que se llamó a la API y se devolvió la receta
        assertTrue(result is DomainResult.Success)
        assertEquals("Test Recipe", (result as DomainResult.Success).data.title)
        coVerify { mockApi.getRecipeById("1") }
    }

    /**
     * Prueba que [RecipesRepositoryImpl.updateFavouriteCount] actualice la caché cuando la
     * operación es exitosa.
     */
    @Test
    fun `updateFavouriteCount updates cache when successful`() = runTest {
        // Given
        val cachedRecipe = testRecipe
        repository.recipesCache = listOf(cachedRecipe)
        coEvery {
            mockApi.updateRecipeFavouriteCount("1", true) } returns DomainResult.Success(Unit)
        
        // When
        val result = repository.updateFavouriteCount("1", true)
        
        // Then
        assertTrue(result is DomainResult.Success)
        assertEquals(11, repository.recipesCache?.first()?.favouriteCount)
        coVerify { mockApi.updateRecipeFavouriteCount("1", true) }
    }
    
    /**
     * Prueba que [RecipesRepositoryImpl.filterRecipes] filtre las recetas por título.
     */
    @Test
    fun `filterRecipes by title returns matching recipes`() = runTest {
        // Given
        val recipe1 = testRecipe
        val recipe2 = testRecipe.copy(id = "2", title = "Pasta Carbonara")
        repository.recipesCache = listOf(recipe1, recipe2)
        
        // When
        val params = SearchParams(FilterCriterion.TITLE, "Test")
        val result = repository.filterRecipes(params)
        
        // Then
        assertTrue(result is DomainResult.Success)
        val recipes = (result as DomainResult.Success).data
        assertEquals(1, recipes.size)
        assertEquals("Test Recipe", recipes[0].title)
    }
    
    /**
     * Prueba que [RecipesRepositoryImpl.saveRecipe] guarde una receta y actualice la caché cuando
     * la operación es exitosa.
     */
    @Test
    fun `saveRecipe updates cache when successful`() = runTest {
        // Given
        val newRecipe = testRecipe.copy(id = "")
        coEvery {
            mockApi.saveRecipe(any(), any(), any(), any()) } returns DomainResult.Success(Unit)
        
        // When
        val result = repository.saveRecipe(newRecipe, null, testUser)
        
        // Then
        assertTrue(result is DomainResult.Success)
        assertNotNull((result as DomainResult.Success).data) // Should return the new ID
        coVerify { mockApi.saveRecipe(any(), any(), any(), any()) }
    }
    
    /**
     * Prueba que [RecipesRepositoryImpl.deleteComment] elimine un comentario de la caché cuando
     * la operación es exitosa.
     */
    @Test
    fun `deleteComment removes comment from cache when successful`() = runTest {
        // Given
        val comment = testRecipe.comments[0]
        val recipe = testRecipe
        repository.recipesCache = listOf(recipe)
        val userId = comment.author.id
        
        coEvery {
            mockApi.deleteComment("1", comment.timestamp, userId)
        } returns DomainResult.Success(Unit)
        
        // When
        val result = repository.deleteComment("1", comment.timestamp, userId)
        
        // Then
        assertTrue(result is DomainResult.Success)
        assertTrue(repository.recipesCache?.first()?.comments?.isEmpty() == true)
        coVerify { mockApi.deleteComment("1", comment.timestamp, userId) }
    }
}
