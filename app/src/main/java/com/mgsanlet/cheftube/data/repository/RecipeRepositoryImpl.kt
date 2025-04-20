package com.mgsanlet.cheftube.data.repository

import android.content.Context
import com.mgsanlet.cheftube.data.model.RecipeDto
import com.mgsanlet.cheftube.data.source.local.RecipeLocalDataSource
import com.mgsanlet.cheftube.domain.repository.RecipeRepository
import javax.inject.Inject
import javax.inject.Singleton
import com.mgsanlet.cheftube.domain.util.Result
import com.mgsanlet.cheftube.domain.util.Error

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDataSource: RecipeLocalDataSource
) : RecipeRepository {
    override suspend fun filterRecipesByIngredient(context: Context, query: String): List<RecipeDto> {
        return recipeDataSource.filterRecipesByIngredient(context, query)
    }

    override suspend fun getById(recipeId: String): RecipeDto? = recipeDataSource.getById(recipeId)

    override suspend fun getAll(): List<RecipeDto> {
        return recipeDataSource.getAll()
    }
}
