package com.mgsanlet.cheftube.data.repository

import com.google.common.truth.Truth.assertThat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.util.TestData
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Clase de prueba para [UsersRepositoryImpl].
 * 
 * Pruebas unitarias para la implementación del repositorio de usuarios, verificando
 * el manejo de autenticación, caché y operaciones de usuario.
 */
class UsersRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var mockApi: FirebaseApi

    @MockK
    private lateinit var mockAuth: FirebaseAuth

    @MockK
    private lateinit var mockFirebaseUser: FirebaseUser

    private lateinit var repository: UsersRepositoryImpl

    // Test data
    private val testUser = TestData.testUser
    private val testUserResponse = TestData.testUserResponse
    private val testRecipeId = "test_recipe_123"

    /**
     * Crea un mock de una [Task] de Firebase para pruebas unitarias.
     * 
     * Helper que simula el comportamiento de las tareas asíncronas de Firebase,
     * permitiendo probar código que depende de estas tareas sin necesidad de una conexión real.
     *
     * @param T Tipo genérico del resultado de la tarea.
     * @param result Resultado exitoso que devolverá la tarea. Si es nulo, se considera fallida.
     * @param exception Excepción que lanzará la tarea en caso de error.
     * @return Un mock de [Task] configurado con el comportamiento especificado.
     */
    private inline fun <reified T> mockTask(
        result: T? = null,
        exception: Exception? = null
    ): Task<T> = mockk(relaxed = true) {
        // Configura el estado básico de la tarea
        every { isComplete } returns true  // Siempre está completa
        every { isCanceled } returns false  // Nunca está cancelada
        
        // Determina si la tarea fue exitosa basado en si hay excepción
        every { isSuccessful } returns (exception == null)
        
        // Configura el resultado o la excepción según corresponda
        every { this@mockk.result } returns result
        every { this@mockk.exception } returns exception
        
        // Maneja el listener de completado, que es llamado inmediatamente
        // ya que la tarea está configurada como completada
        every { addOnCompleteListener(any()) } answers {
            val listener = firstArg<(Task<T>) -> Unit>()
            listener.invoke(this@mockk)  // Ejecuta el listener con esta tarea
            this@mockk  // Permite el encadenamiento de métodos
        }
        
        this  // Retorna el mock configurado
    }

    /**
     * Configuración inicial antes de cada prueba.
     * Inicializa los mocks y configura comportamientos por defecto.
     */
    @Before
    fun setUp() {
        // Inicializa las anotaciones @MockK
        MockKAnnotations.init(this, relaxUnitFun = true)

        // Configuración de mocks para autenticación
        every { mockApi.auth } returns mockAuth
        every { mockAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns testUser.id
        every { mockFirebaseUser.email } returns "test@example.com"
        every { mockAuth.signOut() } just Runs
        
        // Configuración de mocks para tareas comunes
        val mockTask: Task<AuthResult> = mockTask()
        every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns mockTask
        
        // Configuración de mocks para URLs de almacenamiento
        coEvery { mockApi.getStorageUrlFromPath(any()) } returns "test_image_url"

        // Crea una nueva instancia del repositorio para cada prueba
        repository = UsersRepositoryImpl(mockApi)
    }

    /**
     * Limpieza después de cada prueba.
     * Libera los recursos de los mocks.
     */
    @After
    fun tearDown() {
        unmockkAll()
    }

    /**
     * Prueba que [UsersRepositoryImpl.getCurrentUserData] retorne el usuario en caché cuando
     * está disponible.
     */
    @Test
    fun `getCurrentUserData returns cached user when available`() = runTest {
        // Given
        repository.currentUserCache = testUser

        // When
        val result = repository.getCurrentUserData()

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).data).isEqualTo(testUser)
    }

    @Test
    fun `getCurrentUserData fetches from API when cache is empty`() = runTest {
        // Given
        coEvery { mockApi.auth.currentUser?.uid } returns testUser.id
        coEvery {
            mockApi.getUserDataById(testUser.id) } returns DomainResult.Success(testUserResponse)

        // When
        val result = repository.getCurrentUserData()

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).data.id).isEqualTo(testUser.id)
        coVerify { mockApi.getUserDataById(testUser.id) }
    }

    /**
     * Prueba que [UsersRepositoryImpl.getUserDataById] retorne un usuario cuando la
     * llamada a la API es exitosa.
     */
    @Test
    fun `getUserDataById returns user when API call is successful`() = runTest {
        // Given
        coEvery {
            mockApi.getUserDataById(testUser.id) 
        } returns DomainResult.Success(testUserResponse)
        coEvery { 
            mockApi.getStorageUrlFromPath(any()) 
        } returns "https://example.com/profile.jpg"

        // When
        val result = repository.getUserDataById(testUser.id)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).data.id).isEqualTo(testUser.id)
    }

    /**
     * Prueba que [UsersRepositoryImpl.loginUser] retorne éxito cuando las credenciales son válidas.
     */
    @Test
    fun `loginUser returns success with valid credentials`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"

        // Configuración de mocks para AuthResult
        val mockAuthResult: AuthResult = mockk()
        every { mockAuthResult.user } returns mockFirebaseUser

        // Configuración de mocks para la tarea de inicio de sesión
        val signInTask = mockTask<AuthResult>(mockAuthResult)
        every { mockAuth.signInWithEmailAndPassword(email, password) } returns signInTask

        // Configuración de mocks para las llamadas al repositorio
        coEvery { mockApi.updateUserLastLogin(testUser.id) } returns DomainResult.Success(Unit)
        coEvery {
            mockApi.getUserDataById(testUser.id) } returns DomainResult.Success(testUserResponse)

        // When
        val result = repository.loginUser(email, password)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        verify { mockAuth.signInWithEmailAndPassword(email, password) }
        coVerify { mockApi.updateUserLastLogin(testUser.id) }
    }
    
    /**
     * Prueba que [UsersRepositoryImpl.createUser] retorne éxito cuando todas las
     * operaciones son exitosas.
     */
    @Test
    fun `createUser returns success when all operations succeed`() = runTest {
        // Given
        val username = "testuser"
        val email = "test@example.com"
        val password = "password123"

        // Configuración de mocks para la creación de usuario
        val mockAuthResult: AuthResult = mockk()
        every { mockAuthResult.user } returns mockFirebaseUser
        
        val createUserTask = mockTask<AuthResult>(mockAuthResult)
        every { mockAuth.createUserWithEmailAndPassword(email, password) } returns createUserTask
        
        // Configuración de mocks para las llamadas al repositorio
        coEvery { mockApi.isAvailableUsername(username) } returns DomainResult.Success(Unit)
        coEvery {
            mockApi.insertUserData(testUser.id, username, email)
        } returns DomainResult.Success(Unit)
        coEvery { mockApi.updateUserLastLogin(testUser.id) } returns DomainResult.Success(Unit)

        // When
        val result = repository.createUser(username, email, password)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        verify { mockAuth.createUserWithEmailAndPassword(email, password) }
        coVerify {
            mockApi.isAvailableUsername(username)
            mockApi.insertUserData(testUser.id, username, email)
            mockApi.updateUserLastLogin(testUser.id)
        }
    }

    /**
     * Prueba que [UsersRepositoryImpl.updateFavouriteRecipes] actualice la caché
     * después de un éxito.
     */
    @Test
    fun `updateFavouriteRecipes updates cache after success`() = runTest {
        // Given
        val userId = testUser.id
        val isNewFavourite = true

        // Configuración de mocks para las llamadas al repositorio
        coEvery { mockApi.auth.currentUser?.uid } returns userId
        coEvery { mockApi.updateUserFavouriteRecipes(userId, testRecipeId, isNewFavourite) } returns
            DomainResult.Success(Unit)
        coEvery { mockApi.getUserDataById(userId) } returns DomainResult.Success(testUserResponse)

        // When
        val result = repository.updateFavouriteRecipes(testRecipeId, isNewFavourite)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        coVerify {
            mockApi.updateUserFavouriteRecipes(userId, testRecipeId, isNewFavourite)
            mockApi.getUserDataById(userId)
        }
    }

    /**
     * Prueba que [UsersRepositoryImpl.clearCache] elimine el usuario en caché.
     */
    @Test
    fun `clearCache removes cached user`() {
        // Given
        repository.currentUserCache = testUser

        // When
        repository.clearCache()

        // Then
        assertThat(repository.currentUserCache).isNull()
    }

    /**
     * Prueba que [UsersRepositoryImpl.logout] limpie la caché y cierre la sesión.
     */
    @Test
    fun `logout clears cache and signs out`() {
        // Given
        repository.currentUserCache = testUser

        // When
        repository.logout()
        
        // Then
        assertThat(repository.currentUserCache).isNull()
        verify { mockAuth.signOut() }
    }
}
