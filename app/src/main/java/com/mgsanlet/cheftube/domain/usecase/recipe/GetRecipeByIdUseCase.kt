package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe
import com.mgsanlet.cheftube.domain.util.DomainResult

class GetRecipeByIdUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository
) {
    suspend operator fun invoke(recipeId: String): DomainResult<Recipe, RecipeError> {
        return recipesRepository.getById(recipeId)
    }

}