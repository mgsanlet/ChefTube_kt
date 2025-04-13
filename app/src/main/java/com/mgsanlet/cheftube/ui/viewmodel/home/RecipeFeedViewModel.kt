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
    var recipeList = MutableLiveData<List<Recipe>>()

    init{
        CoroutineScope(Dispatchers.Main).launch {
            recipeList.value = recipeRepository.getAll()
        }
    }

    fun filterRecipesByIngredient(context: Context, query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            recipeList.value = recipeRepository.filterRecipesByIngredient(context, query)
        }
    }
}
