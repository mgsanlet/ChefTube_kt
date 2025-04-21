package com.mgsanlet.cheftube.domain.repository

import android.content.Context
import com.mgsanlet.cheftube.data.model.RecipeDto
import com.mgsanlet.cheftube.domain.util.Result
import com.mgsanlet.cheftube.domain.util.Error


interface RecipesRepository {
    suspend fun filterRecipesByIngredient(context: Context, query: String): Result<List<RecipeDto>, Error>
    suspend fun getById(recipeId: String): Result<RecipeDto, Error>
    suspend fun getAll(): Result<List<RecipeDto>, Error>
}