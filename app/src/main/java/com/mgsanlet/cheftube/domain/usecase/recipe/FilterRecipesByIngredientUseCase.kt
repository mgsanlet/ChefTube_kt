package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError

class FilterRecipesByIngredientUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository
) {
    suspend operator fun invoke(ingredientQuery: String): DomainResult<List<Recipe>, RecipeError> {
        return recipesRepository.filterRecipesByIngredient(ingredientQuery)
    }
}