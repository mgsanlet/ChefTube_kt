package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository,
    private val usersRepository: UsersRepository
) {
    suspend operator fun invoke(
        newRecipeData: DomainRecipe,
        newImage: ByteArray?
    ): DomainResult<String?, DomainError> {

        var currentUserData = DomainUser()
        usersRepository.getCurrentUserData().fold(
            onSuccess = {
                currentUserData = it
            },
            onError = {
                return DomainResult.Error(it)
            }
        )
        return recipesRepository.saveRecipe(newRecipeData, newImage, currentUserData)
    }
}