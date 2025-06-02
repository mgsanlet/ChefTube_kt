package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.usecase.recipe.DeleteRecipeUseCase
import com.mgsanlet.cheftube.domain.usecase.recipe.GetRecipeByIdUseCase
import com.mgsanlet.cheftube.domain.usecase.recipe.SaveRecipeUseCase
import com.mgsanlet.cheftube.domain.util.error.DomainError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el formulario de recetas.
 *
 * Gestiona las operaciones CRUD de recetas, incluyendo la carga de una receta existente,
 * el guardado de una nueva receta o actualización de una existente, y la eliminación de recetas.
 *
 * @property getRecipeById Caso de uso para obtener una receta por su ID
 * @property saveRecipe Caso de uso para guardar o actualizar una receta
 * @property deleteRecipe Caso de uso para eliminar una receta
 */
@HiltViewModel
class RecipeFormViewModel @Inject constructor(
    private val getRecipeById: GetRecipeByIdUseCase,
    private val saveRecipe: SaveRecipeUseCase,
    private val deleteRecipe: DeleteRecipeUseCase
) : ViewModel() {
    /** Estado interno mutable de la UI */
    private val _uiState = MutableLiveData<RecipeFormState>()
    
    /** Estado observable de la UI */
    val uiState: LiveData<RecipeFormState> = _uiState

    /** Receta actual siendo editada */
    private val _recipe = MutableLiveData<DomainRecipe>()
    
    /** Receta observable */
    val recipe: LiveData<DomainRecipe> = _recipe

    /**
     * Carga una receta existente para su edición.
     * Actualiza el estado a [RecipeFormState.Loading] durante la carga.
     *
     * @param recipeId ID de la receta a cargar
     */
    fun loadRecipe(recipeId: String) {
        _uiState.value = RecipeFormState.Loading
        viewModelScope.launch {
            val result = getRecipeById(recipeId)
            result.fold(
                onSuccess = { recipe ->
                    _recipe.value = recipe
                    _uiState.value = RecipeFormState.LoadSuccess
                },
                onError = { error ->
                    _uiState.value = RecipeFormState.Error(error)
                }
            )
        }
    }

    /**
     * Intenta guardar o actualizar una receta con los datos proporcionados.
     * Actualiza el estado a [RecipeFormState.Loading] durante la operación.
     *
     * @param title Título de la receta
     * @param videoUrl URL del video de la receta
     * @param imageBytes Bytes de la imagen de la receta (opcional)
     * @param durationMinutes Duración en minutos
     * @param difficulty Nivel de dificultad (1-5)
     * @param categories Lista de categorías
     * @param ingredients Lista de ingredientes
     * @param steps Lista de pasos de preparación
     */
    fun trySaveRecipe(
        title: String,
        videoUrl: String,
        imageBytes: ByteArray?,
        durationMinutes: Int,
        difficulty: Int,
        categories: List<String>,
        ingredients: List<String>,
        steps: List<String>,
    ) {
        viewModelScope.launch {
            _uiState.value = RecipeFormState.Loading
            val result = saveRecipe(
                DomainRecipe(
                    id = recipe.value?.id ?: "",
                    title = title,
                    videoUrl = videoUrl,
                    imageUrl = imageBytes?.let { "" } ?: run {
                        recipe.value?.imageUrl ?: ""
                    },
                    categories = categories,
                    ingredients = ingredients,
                    steps = steps,
                    favouriteCount = recipe.value?.favouriteCount ?: 0,
                    author = recipe.value?.author,
                    durationMinutes = durationMinutes,
                    difficulty = difficulty,
                ),
                imageBytes
            )

            result.fold(
                onSuccess = { newRecipeId ->
                    _uiState.value = RecipeFormState.SaveSuccess(newRecipeId)
                },
                onError = { error ->
                    _uiState.value = RecipeFormState.Error(error)
                }
            )
        }
    }

    /**
     * Elimina la receta actual.
     * Actualiza el estado a [RecipeFormState.Loading] durante la operación
     * y a [RecipeFormState.DeleteSuccess] si tiene éxito.
     */
    fun deleteRecipe() {
        val recipeId = _recipe.value?.id ?: return
        viewModelScope.launch {
            _uiState.value = RecipeFormState.Loading
            val result = deleteRecipe(recipeId)
            result.fold(
                onSuccess = {
                    _uiState.value = RecipeFormState.DeleteSuccess
                },
                onError = { error ->
                    _uiState.value = RecipeFormState.Error(error)
                }
            )
        }
    }
}

/**
 * Estados posibles de la UI para el formulario de recetas.
 */
sealed class RecipeFormState {
    /** Estado de carga, mostrando un indicador de progreso */
    data object Loading : RecipeFormState()
    
    /** Error durante alguna operación */
    data class Error(val error: DomainError) : RecipeFormState()
    
    /** Receta guardada exitosamente */
    data class SaveSuccess(val newRecipeId: String?) : RecipeFormState()
    
    /** Receta cargada exitosamente */
    data object LoadSuccess : RecipeFormState()
    
    /** Receta eliminada exitosamente */
    data object DeleteSuccess : RecipeFormState()
}