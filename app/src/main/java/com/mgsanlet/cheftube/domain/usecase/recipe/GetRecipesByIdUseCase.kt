package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import javax.inject.Inject

class GetRecipesByIdUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository
) {
    suspend operator fun invoke(recipeIds: ArrayList<String>): DomainResult<List<DomainRecipe>, RecipeError> {
        return recipesRepository.getByIds(recipeIds)
    }

}