package com.mgsanlet.cheftube.domain.repository

import android.content.Context
import com.mgsanlet.cheftube.data.model.Recipe

interface RecipeRepository {
    suspend fun filterRecipesByIngredient(context: Context, query: String): List<Recipe>
    suspend fun getById(recipeId: String): Recipe?
    suspend fun getAll(): List<Recipe>
}