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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe
import com.mgsanlet.cheftube.domain.util.error.RecipeError

@HiltViewModel
class RecipeFeedViewModel @Inject constructor(
    private val getAllRecipes: GetAllRecipesUseCase,
    private val getRecipesById: GetRecipesByIdUseCase,
    private val filterRecipes: FilterRecipesUseCase
) : ViewModel() {
    private val _uiState = MutableLiveData<RecipeFeedState>()
    val uiState: LiveData<RecipeFeedState> = _uiState

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

    fun performSearch(params: SearchParams) {
        viewModelScope.launch {
            val result = filterRecipes(params)
            handleFilterResult(result)
        }
    }
    
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

sealed class RecipeFeedState {
    data class InitialLoad(val recipeList: List<Recipe>) : RecipeFeedState()
    data object Loading : RecipeFeedState()
    data class Error(val error: RecipeError) : RecipeFeedState()
    data class SomeResults(val recipeList: List<Recipe>) : RecipeFeedState()
}
