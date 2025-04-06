package com.mgsanlet.cheftube.ui.viewmodel.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.data.model.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeFeedViewModel(app: ChefTubeApplication) : ViewModel() {
    private var recipeRepository = app.recipeRepository
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

@Suppress("UNCHECKED_CAST")
class RecipeFeedViewModelFactory(
    private val app: ChefTubeApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecipeFeedViewModel(app) as T
    }
}