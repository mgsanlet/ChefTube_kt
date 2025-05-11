package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.usecase.recipe.GetRecipeByIdUseCase
import com.mgsanlet.cheftube.domain.usecase.recipe.SaveRecipeUseCase
import com.mgsanlet.cheftube.domain.util.error.DomainError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeFormViewModel @Inject constructor(
    private val getRecipeById: GetRecipeByIdUseCase,
    private val saveRecipe: SaveRecipeUseCase
) : ViewModel() {
    private val _uiState = MutableLiveData<RecipeFormState>()
    val uiState: LiveData<RecipeFormState> = _uiState

    private val _recipe = MutableLiveData<DomainRecipe>()
    val recipe: LiveData<DomainRecipe> = _recipe

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
}

sealed class RecipeFormState {
    data object Loading : RecipeFormState()
    data class Error(val error: DomainError) : RecipeFormState()
    data class SaveSuccess(val newRecipeId: String?) : RecipeFormState()
    data object LoadSuccess : RecipeFormState()
}