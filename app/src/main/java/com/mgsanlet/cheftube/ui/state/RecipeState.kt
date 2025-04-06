package com.mgsanlet.cheftube.ui.state

import com.mgsanlet.cheftube.data.model.Recipe

sealed class RecipeState {
    object Loading : RecipeState()
    data class Success(val recipe: Recipe) : RecipeState()
    data class Error(val message: String) : RecipeState()
}