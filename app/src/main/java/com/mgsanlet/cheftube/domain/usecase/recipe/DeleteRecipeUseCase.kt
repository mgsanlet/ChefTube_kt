package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import javax.inject.Inject

/**
 * Caso de uso para eliminar una receta.
 *
 * @property recipesRepository Repositorio de recetas para eliminar la receta
 * @property usersRepository Repositorio de usuarios para limpiar la caché
 */
class DeleteRecipeUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository,
    private val usersRepository: UsersRepository
) {
    /**
     * Ejecuta el caso de uso para eliminar una receta.
     * Limpia las cachés de usuarios y recetas después de la eliminación.
     *
     * @param recipeId ID de la receta a eliminar
     * @return [DomainResult] con Unit en caso de éxito o [DomainError] si hay un error
     */
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