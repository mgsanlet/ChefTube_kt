package com.mgsanlet.cheftube.data.repository

import android.content.Context
import com.mgsanlet.cheftube.data.model.RecipeDto
import com.mgsanlet.cheftube.data.source.local.RecipesLocalDataSource
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipesRepositoryImpl @Inject constructor(
    private val recipeDataSource: RecipesLocalDataSource
) : RecipesRepository {
    override suspend fun filterRecipesByIngredient(context: Context, query: String): List<RecipeDto> {
        return recipeDataSource.filterRecipesByIngredient(context, query)
    }

    override suspend fun getById(recipeId: String): RecipeDto? = recipeDataSource.getById(recipeId)

    override suspend fun getAll(): List<RecipeDto> {
        return recipeDataSource.getAll()
    }
}
