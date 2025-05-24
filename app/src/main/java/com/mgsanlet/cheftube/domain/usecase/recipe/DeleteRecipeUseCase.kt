package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import javax.inject.Inject

class DeleteRecipeUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository,
    private val usersRepository: UsersRepository
) {
    suspend operator fun invoke(recipeId: String): DomainResult<Unit, DomainError> {
        recipesRepository.deleteRecipe(recipeId).fold(
            onSuccess = {
                usersRepository.clearCache()
                recipesRepository.clearCache()
                return DomainResult.Success(it)
            },
            onError = {
                return DomainResult.Error(it)
            }
        )
    }
}