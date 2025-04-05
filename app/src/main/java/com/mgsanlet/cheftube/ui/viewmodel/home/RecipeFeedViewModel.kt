package com.mgsanlet.cheftube.ui.viewmodel.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.data.repository.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeFeedViewModel : ViewModel() {
    private var recipeRepository = RecipeRepository()
    var recipeList = MutableLiveData<List<Recipe>>()

    init {
        addRecipesToList()
    }
    private fun addRecipesToList() {
        CoroutineScope(Dispatchers.Main).launch {
            recipeList.value = recipeRepository.addRecipesToList()
        }
    }
    fun filterRecipesByIngredient(context: Context, query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            recipeList.value = recipeRepository.filterRecipesByIngredient(context, query)
        }
    }



}