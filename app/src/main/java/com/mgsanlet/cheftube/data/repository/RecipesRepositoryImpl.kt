package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.source.local.RecipesLocalDataSource
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import javax.inject.Inject
import javax.inject.Singleton
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError

@Singleton
class RecipesRepositoryImpl @Inject constructor(
    private val recipeDataSource: RecipesLocalDataSource
) : RecipesRepository {
    override suspend fun filterRecipesByIngredient(ingredientQuery: String): DomainResult<List<Recipe>, RecipeError> {
        val recipes = recipeDataSource.filterRecipesByIngredient(ingredientQuery)
        return if (recipes.isEmpty()) {
            DomainResult.Error(RecipeError.NoResults)
        } else {
            DomainResult.Success(recipes)
        }
    }

    override suspend fun getById(recipeId: String): DomainResult<Recipe, RecipeError> {
        val recipe: Recipe? = recipeDataSource.getById(recipeId)
        recipe?.let {
            return DomainResult.Success(it)
        } ?: return DomainResult.Error(RecipeError.RecipeNotFound)
    }

    override suspend fun getAll(): DomainResult<List<Recipe>, RecipeError> {
        val recipes = recipeDataSource.getAll()
        return if (recipes.isEmpty()) {
            DomainResult.Error(RecipeError.NoResults)
        } else {
            DomainResult.Success(recipes)
        }
    }
}
