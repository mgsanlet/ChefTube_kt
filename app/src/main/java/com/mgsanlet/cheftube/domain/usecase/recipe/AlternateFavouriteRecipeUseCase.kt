package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import javax.inject.Inject

/**
 * Caso de uso para alternar una receta como favorita.
 *
 * @property recipesRepository Repositorio de recetas para actualizar el contador de favoritos
 * @property userRepository Repositorio de usuarios para actualizar la lista de favoritos del usuario
 */
class AlternateFavouriteRecipeUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository,
    private val userRepository: UsersRepository
) {
    /**
     * Ejecuta el caso de uso para marcar o desmarcar una receta como favorita.
     * Actualiza tanto la lista de favoritos del usuario como el contador de la receta.
     *
     * @param recipeId ID de la receta a marcar/desmarcar como favorita
     * @param isNewFavourite true para marcar como favorita, false para quitar de favoritos
     * @return [DomainResult] con Unit en caso de éxito o [DomainError] si hay un error
     */
    suspend operator fun invoke(recipeId: String, isNewFavourite: Boolean):
            DomainResult<Unit, DomainError> {
        var userResult = userRepository.updateFavouriteRecipes(recipeId, isNewFavourite)
        if (userResult is DomainResult.Error) {
            return userResult
        }
        return recipesRepository.updateFavouriteCount(recipeId, isNewFavourite)
    }
}