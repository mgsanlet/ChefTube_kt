package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.model.SearchParams
import com.mgsanlet.cheftube.domain.usecase.recipe.FilterRecipesUseCase
import com.mgsanlet.cheftube.domain.usecase.recipe.GetAllRecipesUseCase
import com.mgsanlet.cheftube.domain.usecase.recipe.GetRecipesByIdUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe

/**
 * ViewModel para el feed de recetas.
 *
 * Gestiona la carga y filtrado de recetas para mostrarlas en el feed principal.
 * Permite cargar todas las recetas, recetas específicas por ID y realizar búsquedas.
 *
 * @property getAllRecipes Caso de uso para obtener todas las recetas
 * @property getRecipesById Caso de uso para obtener recetas por sus IDs
 * @property filterRecipes Caso de uso para filtrar recetas según parámetros de búsqueda
 */
@HiltViewModel
class RecipeFeedViewModel @Inject constructor(
    private val getAllRecipes: GetAllRecipesUseCase,
    private val getRecipesById: GetRecipesByIdUseCase,
    private val filterRecipes: FilterRecipesUseCase
) : ViewModel() {
    /** Estado interno mutable de la UI */
    private val _uiState = MutableLiveData<RecipeFeedState>()
    
    /** Estado observable de la UI */
    val uiState: LiveData<RecipeFeedState> = _uiState

    /**
     * Carga la lista inicial de recetas.
     * Actualiza el estado a [RecipeFeedState.Loading] durante la carga y
     * a [RecipeFeedState.InitialLoad] cuando finaliza con éxito.
     */
    fun loadInitialRecipes() {
        _uiState.value = RecipeFeedState.Loading

        viewModelScope.launch {
            val result = getAllRecipes()
            result.fold(
                onSuccess = { recipeList ->
                    _uiState.value = RecipeFeedState.InitialLoad(recipeList)
                },
                onError = { error ->
                    _uiState.value = RecipeFeedState.Error(error)
                }
            )
        }
    }

    /**
     * Carga recetas específicas por sus IDs.
     *
     * @param recipeIds Lista de IDs de las recetas a cargar
     */
    fun loadSentRecipes(recipeIds: ArrayList<String>) {
        _uiState.value = RecipeFeedState.Loading
        viewModelScope.launch {
            val result = getRecipesById(recipeIds)
            result.fold(
                onSuccess = { recipeList ->
                    _uiState.value = RecipeFeedState.InitialLoad(recipeList)
                },
                onError = { error ->
                    _uiState.value = RecipeFeedState.Error(error)
                }
            )
        }
    }

    /**
     * Realiza una búsqueda de recetas según los parámetros proporcionados.
     *
     * @param params Parámetros de búsqueda (término de búsqueda, filtros, etc.)
     */
    fun performSearch(params: SearchParams) {
        viewModelScope.launch {
            val result = filterRecipes(params)
            handleFilterResult(result)
        }
    }
    
    /**
     * Maneja el resultado de una operación de filtrado.
     * Actualiza el estado según el resultado de la operación.
     *
     * @param result Resultado de la operación de filtrado
     */
    private fun handleFilterResult(result: DomainResult<List<Recipe>, RecipeError>) {
        result.fold(
            onSuccess = { recipeList ->
                _uiState.value = RecipeFeedState.SomeResults(recipeList)
            },
            onError = { error ->
                _uiState.value = RecipeFeedState.Error(error)
            }
        )
    }
}

/**
 * Estados posibles de la UI para el feed de recetas.
 */
sealed class RecipeFeedState {
    /** Estado inicial con la lista de recetas cargada */
    data class InitialLoad(val recipeList: List<Recipe>) : RecipeFeedState()
    
    /** Estado de carga, mostrando un indicador de progreso */
    data object Loading : RecipeFeedState()
    
    /** Error al cargar o filtrar recetas */
    data class Error(val error: RecipeError) : RecipeFeedState()
    
    /** Resultados de búsqueda o filtrado */
    data class SomeResults(val recipeList: List<Recipe>) : RecipeFeedState()
}
