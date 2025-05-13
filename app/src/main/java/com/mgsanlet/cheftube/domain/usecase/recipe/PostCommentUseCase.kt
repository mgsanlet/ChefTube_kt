package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import javax.inject.Inject

class PostCommentUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository
) {
    suspend operator fun invoke( recipeId: String, commentContent: String, currentUserData: DomainUser ): DomainResult<Unit, DomainError> {
        return recipesRepository.postComment(recipeId, commentContent, currentUserData)
    }
}