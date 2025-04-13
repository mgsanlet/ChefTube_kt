package com.mgsanlet.cheftube.data.repository

import android.content.Context
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.data.source.local.RecipeLocalDataSource
import com.mgsanlet.cheftube.domain.repository.RecipeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDataSource: RecipeLocalDataSource
) : RecipeRepository {
    override suspend fun filterRecipesByIngredient(context: Context, query: String): List<Recipe> {
        return recipeDataSource.filterRecipesByIngredient(context, query)
    }

    override suspend fun getById(recipeId: String): Recipe? = recipeDataSource.getById(recipeId)

    override suspend fun getAll(): List<Recipe> {
        return recipeDataSource.getAll()
    }
}
