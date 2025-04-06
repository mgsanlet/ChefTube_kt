package com.mgsanlet.cheftube.data.repository

import android.content.Context
import com.mgsanlet.cheftube.data.local.RecipeDataStorage
import com.mgsanlet.cheftube.data.model.Recipe

class RecipeRepository {
    private var recipeDataStorage = RecipeDataStorage

    suspend fun addRecipesToList() : List<Recipe> = recipeDataStorage.addRecipesToList()

    suspend fun filterRecipesByIngredient(context: Context, query: String): List<Recipe> {
        return recipeDataStorage.filterRecipesByIngredient(context, query)
    }

    suspend fun getById(recipeId: String): Recipe? = recipeDataStorage.getById(recipeId)
}
