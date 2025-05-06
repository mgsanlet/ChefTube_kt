package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import javax.inject.Inject

class AlternateFavouriteRecipeUseCase@Inject constructor(
    private val recipesRepository: RecipesRepository,
    private val userRepository: UsersRepository
) {
    suspend operator fun invoke(recipeId: String, isNewFavourite: Boolean): DomainResult<Unit, DomainError>{
        var userResult = userRepository.updateFavouriteRecipes(recipeId, isNewFavourite)
        if (userResult is DomainResult.Error) {
            return userResult
        }
        return recipesRepository.updateFavouriteCount(recipeId, isNewFavourite)
    }
}