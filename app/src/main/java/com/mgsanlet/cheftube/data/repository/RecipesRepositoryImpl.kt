package com.mgsanlet.cheftube.data.repository

import com.mgsanlet.cheftube.data.source.local.RecipesLocalDataSource
import com.mgsanlet.cheftube.data.source.remote.FirebaseRecipeApi
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import javax.inject.Inject
import javax.inject.Singleton
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError

@Singleton
class RecipesRepositoryImpl @Inject constructor(
    //private val recipeDataSource: RecipesLocalDataSource,
    private val api: FirebaseRecipeApi
) : RecipesRepository {
    override suspend fun filterRecipesByIngredient(ingredientQuery: String): DomainResult<List<DomainRecipe>, RecipeError> {
//        val recipes = recipeDataSource.filterRecipesByIngredient(ingredientQuery)
//        return if (recipes.isEmpty()) {
        return DomainResult.Error(RecipeError.NoResults)
//        } else {
//            DomainResult.Success(recipes)
//        }
    }

    //    override suspend fun getById(recipeId: String): DomainResult<DomainRecipe, RecipeError> {
//        val recipe: DomainRecipe? = recipeDataSource.getById(recipeId)
//        recipe?.let {
//            return DomainResult.Success(it)
//        } ?: return DomainResult.Error(RecipeError.RecipeNotFound)
//    }
//
//    override suspend fun getAll(): DomainResult<List<DomainRecipe>, RecipeError> {
//        val recipes = recipeDataSource.getAll()
//        return if (recipes.isEmpty()) {
//            DomainResult.Error(RecipeError.NoResults)
//        } else {
//            DomainResult.Success(recipes)
//        }
//    }
    override suspend fun getById(recipeId: String): DomainResult<DomainRecipe, RecipeError> {
        var recipe: DomainRecipe? = null
        var error: RecipeError? = null
        api.getById(recipeId) { result ->
            when (result) {
                is DomainResult.Success -> {
                    recipe = DomainRecipe(
                        id = result.data.id,
                        title = result.data.title,
                        imageUrl = api.getStorageUrlFromPath(result.data.imagePath),
                        videoUrl = result.data.videoUrl,
                        ingredients = result.data.ingredients,
                        steps = result.data.steps
                    )
                }
                is DomainResult.Error -> { error = result.error }
            }
        }
        recipe?.let {
            return DomainResult.Success(it)
        } ?: return DomainResult.Error(error!!)
    }

    override suspend fun getAll(): DomainResult<List<DomainRecipe>, RecipeError> {
        var recipeList: List<DomainRecipe>? = null
        var error: RecipeError? = null

        api.getAll() { result ->
            when (result) {
                is DomainResult.Success -> {
                    recipeList = result.data.map { recipe ->
                        DomainRecipe(
                            id = recipe.id,
                            title = recipe.title,
                            imageUrl = api.getStorageUrlFromPath(recipe.imagePath),
                            videoUrl = recipe.videoUrl,
                            ingredients = recipe.ingredients,
                            steps = recipe.steps
                        )
                    }
                }
                is DomainResult.Error -> {
                    error = result.error
                }
            }

        }
        recipeList?.let {
            return if (it.isEmpty()) {
                DomainResult.Error(RecipeError.NoResults)
            } else {
                DomainResult.Success(it)
            }
        } ?: return DomainResult.Error(error!!)
    }
}
