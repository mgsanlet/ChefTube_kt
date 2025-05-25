package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.model.CommentResponse
import com.mgsanlet.cheftube.data.model.RecipeResponse
import com.mgsanlet.cheftube.data.source.remote.FirebaseApi
import com.mgsanlet.cheftube.domain.model.DomainComment
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.model.SearchParams
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.DomainResult.Error
import com.mgsanlet.cheftube.domain.util.DomainResult.Success
import com.mgsanlet.cheftube.domain.util.FilterCriterion
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import java.util.UUID
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

    override suspend fun filterRecipes(params: SearchParams): DomainResult<List<DomainRecipe>, RecipeError> {
        return when (val result = getAll()) {
            is Success -> {
                val filteredRecipes = when (params.criterion) {
                    FilterCriterion.TITLE -> {
                        val lowercaseQuery = params.query.lowercase()
                        result.data.filter { recipe ->
                            recipe.title.lowercase().contains(lowercaseQuery)
                        }
                    }
                    FilterCriterion.INGREDIENT -> {
                        val lowercaseQuery = params.query.lowercase()
                        result.data.filter { recipe ->
                            recipe.ingredients.any { ingredient ->
                                ingredient.lowercase().contains(lowercaseQuery)
                            }
                        }
                    }
                    FilterCriterion.DURATION -> {
                        val min = params.minDuration.toIntOrNull() ?: 0
                        val max = params.maxDuration.toIntOrNull() ?: Int.MAX_VALUE
                        result.data.filter { recipe ->
                            val duration = recipe.durationMinutes
                            duration in min..max
                        }
                    }
                    FilterCriterion.CATEGORY -> {
                        val lowercaseQuery = params.query.lowercase()
                        result.data.filter { recipe ->
                            recipe.categories.any { category ->
                                category.lowercase().contains(lowercaseQuery)
                            }
                        }
                    }
                    FilterCriterion.DIFFICULTY -> {
                        result.data.filter { recipe ->
                            recipe.difficulty == params.difficulty
                        }
                    }
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

    override suspend fun saveRecipe(
        newRecipeData: DomainRecipe,
        newImage: ByteArray?,
        currentUserData: DomainUser
    ): DomainResult<String?, RecipeError> {

        var newId: String? = null
        var finalId = newRecipeData.id
        if (finalId.isBlank()) {
            finalId = UUID.randomUUID().toString()
            newId = finalId
        }
        val result = api.saveRecipe(finalId, newRecipeData, newImage, currentUserData)
        if (result is Success) {
            recipesCache?.let { cache ->

                val finalRecipeData = newRecipeData.copy(
                    id = finalId,
                    imageUrl = api.getStorageUrlFromPath("recipe_images/$finalId.jpg"),
                    author = currentUserData
                )

                val index = cache.indexOfFirst { it.id == finalId }
                recipesCache = if (index != -1) {
                    cache.toMutableList().apply {
                        set(index, finalRecipeData)
                    }
                } else {
                    cache.toMutableList().apply {
                        add(finalRecipeData)
                    }
                }
            }
            return Success(newId)
        } else {
            return Error((result as Error).error)
        }
    }

    override suspend fun postComment(
        recipeId: String,
        commentContent: String,
        currentUserData: DomainUser
    ): DomainResult<Unit, RecipeError> {
        return api.postComment(recipeId, commentContent, currentUserData)
    }
    
    override suspend fun deleteRecipe(recipeId: String): DomainResult<Unit, RecipeError> {
        return api.deleteRecipeAndReferences(recipeId)
    }
    
    override suspend fun deleteComment(
        recipeId: String,
        commentTimestamp: Long,
        userId: String
    ): DomainResult<Unit, RecipeError> {
        // Delete the comment from Firebase
        val result = api.deleteComment(recipeId, commentTimestamp, userId)
        
        // Update cache if deletion was successful
        if (result is DomainResult.Success) {
            recipesCache?.let { cache ->
                val recipeIndex = cache.indexOfFirst { it.id == recipeId }
                if (recipeIndex != -1) {
                    val updatedRecipe = cache[recipeIndex].copy(
                        comments = cache[recipeIndex].comments.filterNot {
                            it.author.id == userId && it.timestamp == commentTimestamp
                        }
                    )
                    recipesCache = cache.toMutableList().apply {
                        set(recipeIndex, updatedRecipe)
                    }
                }
            }
        }
        
        return result
    }

    private suspend fun RecipeResponse.toDomainRecipe(): DomainRecipe {
        return DomainRecipe(
            id = this.id,
            title = this.title,
            imageUrl = api.getStorageUrlFromPath("recipe_images/${this.id}.jpg"),
            videoUrl = this.videoUrl,
            ingredients = this.ingredients,
            steps = this.steps,
            categories = this.categories,
            comments = this.comments.map { it.toDomainComment() },
            favouriteCount = this.favouriteCount,
            durationMinutes = this.durationMinutes,
            difficulty = this.difficulty,
            author = DomainUser(
                id = this.authorId,
                email = this.authorEmail,
                username = this.authorName,
                profilePictureUrl = if (this.authorHasProfilePicture) api.getStorageUrlFromPath(
                    "profile_pictures/${this.authorId}.jpg"
                )
                else ""

            )
        )
    }

    private suspend fun CommentResponse.toDomainComment(): DomainComment {
        return DomainComment(
            author = DomainUser(
                id = this.authorId,
                email = this.authorEmail,
                username = this.authorName,
                profilePictureUrl = if (this.authorHasProfilePicture) api.getStorageUrlFromPath(
                    "profile_pictures/${this.authorId}.jpg"
                )
                else ""
            ),
            content = this.content,
            timestamp = this.timestamp
        )
    }

}
