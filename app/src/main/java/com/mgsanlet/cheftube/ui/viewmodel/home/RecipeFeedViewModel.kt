package com.mgsanlet.cheftube.ui.viewmodel.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeFeedViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    var recipeFeedState = MutableLiveData<RecipeFeedState>()

    init{
        recipeFeedState.value = RecipeFeedState.Loading

        CoroutineScope(Dispatchers.Main).launch {
            val recipeList = recipeRepository.getAll()
            if (recipeList.isEmpty()) {
                recipeFeedState.value = RecipeFeedState.NoResults
            } else {
                recipeFeedState.value = RecipeFeedState.InitialLoad(recipeList)
            }
        }
    }

    fun filterRecipesByIngredient(context: Context, query: String) {
        recipeFeedState.value = RecipeFeedState.Loading
        CoroutineScope(Dispatchers.Main).launch {
            val recipeList = recipeRepository.filterRecipesByIngredient(context, query)
            if (recipeList.isEmpty()) {
                recipeFeedState.value = RecipeFeedState.NoResults
            } else {
                recipeFeedState.value = RecipeFeedState.SomeResults(recipeList)
            }
        }
    }
}

sealed class RecipeFeedState {
    data class InitialLoad(val recipeList: List<Recipe>) : RecipeFeedState()
    data object Loading : RecipeFeedState()
    data object NoResults : RecipeFeedState()
    data class SomeResults(val recipeList: List<Recipe>) : RecipeFeedState()
}
