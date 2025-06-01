package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import javax.inject.Inject

/**
 * Caso de uso para publicar un comentario en una receta.
 *
 * @property recipesRepository Repositorio de recetas para guardar el comentario
 */
class PostCommentUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository
) {
    /**
     * Ejecuta el caso de uso para publicar un comentario.
     *
     * @param recipeId ID de la receta donde se publicará el comentario
     * @param commentContent Contenido del comentario
     * @param currentUserData Datos del usuario que publica el comentario
     * @return [DomainResult] con Unit en caso de éxito o [DomainError] si hay un error
     */
    suspend operator fun invoke(
        recipeId: String, commentContent: String, currentUserData: DomainUser):
            DomainResult<Unit, DomainError> {
        return recipesRepository.postComment(recipeId, commentContent, currentUserData)
    }
}