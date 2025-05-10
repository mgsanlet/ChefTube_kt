package com.mgsanlet.cheftube.ui.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.usecase.recipe.GetRecipeByIdUseCase
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeFormViewModel @Inject constructor(
    private val getRecipeById: GetRecipeByIdUseCase
) : ViewModel() {
    private val _uiState = MutableLiveData<RecipeFormState>()
    val uiState: LiveData<RecipeFormState> = _uiState

    private val _recipe = MutableLiveData<DomainRecipe>()
    val recipe: LiveData<DomainRecipe> = _recipe

    var isValidVideoUrl = false

    fun loadRecipe(recipeId: String){
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
        toString: String,
        toString1: String,
        toString2: String,
        toString3: String,
        selectedItemPosition: Int,
        asStringList: List<String>,
        asStringList1: List<String>,
        asStringList2: List<String>
    ) {

    }


}

sealed class RecipeFormState {
    data object Loading : RecipeFormState()
    data class Error(val error: RecipeError) : RecipeFormState()
    data object SaveSuccess : RecipeFormState()
    data object LoadSuccess : RecipeFormState()
}