package com.mgsanlet.cheftube.domain.repository

import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe


interface RecipesRepository {
    suspend fun clearCache()
    suspend fun filterRecipesByIngredient(ingredientQuery: String): DomainResult<List<Recipe>, RecipeError>
    suspend fun getById(recipeId: String): DomainResult<Recipe, RecipeError>
    suspend fun getAll(): DomainResult<List<Recipe>, RecipeError>
    suspend fun getByIds(recipeIds: ArrayList<String>): DomainResult<List<Recipe>, RecipeError>
    suspend fun updateFavouriteCount(recipeId: String, isNewFavourite: Boolean): DomainResult<Unit, RecipeError>
}