package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import javax.inject.Inject

/**
 * Use case for deleting a comment from a recipe
 * @property recipesRepository The repository that handles recipe-related operations
 */
class DeleteCommentUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository
) {
    /**
     * Deletes a comment from a recipe
     * @param recipeId The ID of the recipe containing the comment
     * @param commentTimestamp The timestamp of the comment to delete
     * @param userId The ID of the user who made the comment
     * @return DomainResult with Unit on success, or DomainError on failure
     */
    suspend operator fun invoke(
        recipeId: String,
        commentTimestamp: Long,
        userId: String
    ): DomainResult<Unit, DomainError> {
        return recipesRepository.deleteComment(recipeId, commentTimestamp, userId)
    }
}
