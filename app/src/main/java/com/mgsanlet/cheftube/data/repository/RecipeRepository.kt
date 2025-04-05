package com.mgsanlet.cheftube.data.repository

import android.content.Context
import com.mgsanlet.cheftube.data.datasource.RecipeDataStorage
import com.mgsanlet.cheftube.data.model.Recipe

class RecipeRepository {
    private var recipeDataStorage = RecipeDataStorage()

    suspend fun addRecipesToList() : List<Recipe> = recipeDataStorage.addRecipesToList()

    suspend fun filterRecipesByIngredient(context: Context, query: String): List<Recipe> {
        return recipeDataStorage.filterRecipesByIngredient(context, query)
    }
}
