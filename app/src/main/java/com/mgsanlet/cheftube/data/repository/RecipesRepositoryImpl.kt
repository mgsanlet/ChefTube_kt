package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.RecipeResponse
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.DomainResult.Error
import com.mgsanlet.cheftube.domain.util.DomainResult.Success
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipesRepositoryImpl @Inject constructor(
    private val api: FirebaseApi
) : RecipesRepository {
    var recipesCache: List<DomainRecipe>? = null

    override suspend fun clearCache() {
        recipesCache = null
    }

    override suspend fun getAll(): DomainResult<List<DomainRecipe>, RecipeError> {
        recipesCache?.let {
            if (it.isNotEmpty()) return Success(it)
        }
        // Si el caché es nulo
        return when (val result = api.getAllRecipes()) {
            is Success -> {
                val domainRecipes = result.data.map { it.toDomainRecipe() }

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

    override suspend fun filterRecipesByIngredient(ingredientQuery: String): DomainResult<List<DomainRecipe>, RecipeError> {
        return when (val result = api.getAllRecipes()) {
            is Success -> {
                val lowercaseQuery = ingredientQuery.lowercase()
                val filteredRecipes = result.data.filter { recipeResponse ->
                    recipeResponse.ingredients.any { ingredient ->
                        ingredient.lowercase().contains(lowercaseQuery)
                    }
                }.map { it.toDomainRecipe() }

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
        recipesCache?.let {
            val recipe = it.find { recipe -> recipe.id == recipeId }
            if (recipe != null) return Success(recipe)
        }
        // Si el caché es nulo
        return when (val result = api.getRecipeById(recipeId)) {
            is Success -> {
                try {
                    Success(result.data.toDomainRecipe())
                } catch (e: Exception) {
                    Error(RecipeError.Unknown(e.message))
                }
            }

            is Error -> {
                Error(result.error)
            }
        }
    }

    override suspend fun getByIds(recipeIds: ArrayList<String>): DomainResult<List<DomainRecipe>, RecipeError> {
        recipesCache?.let {
            return Success(it.filter { recipe -> recipeIds.contains(recipe.id) })
        }
        // Si el caché es nulo
        return when (val result = api.getRecipesByIds(recipeIds)) {
            is Success -> {
                val filteredRecipes = result.data.map { it.toDomainRecipe() }

                if (filteredRecipes.isEmpty()) {
                    Error(RecipeError.NoResults)
                } else {
                    Success(filteredRecipes)
                }
            }

            is Error -> Error(result.error)
        }
    }

    override suspend fun updateFavouriteCount(
        recipeId: String,
        isNewFavourite: Boolean
    ): DomainResult<Unit, RecipeError> {
        val result = api.updateRecipeFavouriteCount(recipeId, isNewFavourite)
        
        // Actualizar el cache si existe
        recipesCache?.let { cache ->
            val index = cache.indexOfFirst { it.id == recipeId }
            if (index != -1) {
                val currentRecipe = cache[index]
                val updatedRecipe = currentRecipe.copy(
                    favouriteCount = if (isNewFavourite) currentRecipe.favouriteCount + 1
                    else currentRecipe.favouriteCount - 1
                )
                recipesCache = cache.toMutableList().apply {
                    set(index, updatedRecipe)
                }
            }
        }

        return result
    }

    private suspend fun RecipeResponse.toDomainRecipe(): DomainRecipe {
        return DomainRecipe(
            id = this.id,
            title = this.title,
            imageUrl = api.getStorageUrlFromPath(this.imagePath),
            videoUrl = this.videoUrl,
            ingredients = this.ingredients,
            steps = this.steps,
            categories = this.categories,
            favouriteCount = this.favouriteCount,
            durationMinutes = this.durationMinutes,
            difficulty = this.difficulty,
            author = DomainUser(
                id = this.authorId,
                username = this.authorName,
                profilePictureUrl = api.getStorageUrlFromPath(
                    if (this.authorHasProfilePicture) "profile_pictures/${this.authorId}.jpg"
                    else "")
            )
        )
    }

}
