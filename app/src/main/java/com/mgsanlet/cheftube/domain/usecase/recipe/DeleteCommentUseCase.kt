package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import javax.inject.Inject

/**
 * Caso de uso para eliminar un comentario de una receta.
 *
 * @property recipesRepository Repositorio de recetas para eliminar el comentario
 */
class DeleteCommentUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository
) {
    /**
     * Ejecuta el caso de uso para eliminar un comentario.
     *
     * @param recipeId ID de la receta que contiene el comentario
     * @param commentTimestamp Marca de tiempo del comentario a eliminar
     * @param userId ID del usuario que realizó el comentario
     * @return [DomainResult] con Unit en caso de éxito o [DomainError] si hay un error
     */
    suspend operator fun invoke(
        recipeId: String,
        commentTimestamp: Long,
        userId: String
    ): DomainResult<Unit, DomainError> {
        return recipesRepository.deleteComment(recipeId, commentTimestamp, userId)
    }
}
