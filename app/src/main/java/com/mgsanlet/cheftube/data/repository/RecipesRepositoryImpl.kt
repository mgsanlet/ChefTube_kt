package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.source.remote.FirebaseRecipeApi
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import javax.inject.Inject
import javax.inject.Singleton
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.DomainResult.*
import com.mgsanlet.cheftube.domain.util.error.RecipeError

@Singleton
class RecipesRepositoryImpl @Inject constructor(
    private val api: FirebaseRecipeApi
) : RecipesRepository {
    var recipesCache: List<DomainRecipe>? = null

    override suspend fun filterRecipesByIngredient(ingredientQuery: String): DomainResult<List<DomainRecipe>, RecipeError> {
        return when (val result = api.getAll()) {
            is Success -> {
                val lowercaseQuery = ingredientQuery.lowercase()
                val filteredRecipes = result.data.filter { recipeResponse ->
                    recipeResponse.ingredients.any { ingredient ->
                        ingredient.lowercase().contains(lowercaseQuery)
                    }
                }.map { recipeResponse ->
                    DomainRecipe(
                        id = recipeResponse.id,
                        title = recipeResponse.title,
                        imageUrl = api.getStorageUrlFromPath(recipeResponse.imagePath),
                        videoUrl = recipeResponse.videoUrl,
                        ingredients = recipeResponse.ingredients,
                        steps = recipeResponse.steps
                    )
                }

                if (filteredRecipes.isEmpty()) {
                    Error(RecipeError.NoResults)
                } else {
                    Success(filteredRecipes)
                }
            }
            is Error -> Error(result.error)
        }
    }

    override suspend fun getById(recipeId: String): DomainResult<DomainRecipe, RecipeError> {
        return when (val result = api.getById(recipeId)) {
            is Success -> {
                try {
                    val recipe = result.data
                    val domainRecipe = DomainRecipe(
                        id = recipe.id,
                        title = recipe.title,
                        imageUrl = api.getStorageUrlFromPath(recipe.imagePath),
                        videoUrl = recipe.videoUrl,
                        ingredients = recipe.ingredients,
                        steps = recipe.steps
                    )
                    Success(domainRecipe)
                } catch (e: Exception){
                    Error(RecipeError.Unknown(e.message))
                }
            }
            is Error -> {
                Error(result.error)
            }
        }
    }

    override suspend fun getAll(): DomainResult<List<DomainRecipe>, RecipeError> {
        recipesCache?.let {
            if (!it.isEmpty()) return Success(it)
        }

        return when (val result = api.getAll()) {
            is Success -> {
                val domainRecipes = result.data.map { recipe ->
                    DomainRecipe(
                        id = recipe.id,
                        title = recipe.title,
                        imageUrl = api.getStorageUrlFromPath(recipe.imagePath),
                        videoUrl = recipe.videoUrl,
                        ingredients = recipe.ingredients,
                        steps = recipe.steps
                    )
                }
                if (domainRecipes.isEmpty()) {
                    Error(RecipeError.NoResults)
                } else {
                    recipesCache = domainRecipes
                    Success(domainRecipes)
                }
            }

            is Error -> Error(result.error)
        }
    }
}
