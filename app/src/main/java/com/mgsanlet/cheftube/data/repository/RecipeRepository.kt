package com.mgsanlet.cheftube.data.repository

import android.content.Context
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.data.source.local.RecipeLocalDataSource

class RecipeRepository {
    private var recipeDataStorage = RecipeLocalDataSource

    suspend fun filterRecipesByIngredient(context: Context, query: String): List<Recipe> {
        return recipeDataStorage.filterRecipesByIngredient(context, query)
    }

    suspend fun getById(recipeId: String): Recipe? = recipeDataStorage.getById(recipeId)

    suspend fun getAll(): List<Recipe> {
        return recipeDataStorage.getAll()
    }
}
