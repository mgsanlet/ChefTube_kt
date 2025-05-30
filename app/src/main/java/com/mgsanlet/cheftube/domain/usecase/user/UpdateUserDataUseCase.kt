package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

class UpdateUserDataUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val recipeRepository: RecipesRepository
) {
    suspend operator fun invoke(newUserData: DomainUser): DomainResult<Unit, UserError> {
        val result = usersRepository.updateUserData(newUserData)
        if (result is DomainResult.Success) {
            recipeRepository.clearCache()
        }
        return result
    }
}