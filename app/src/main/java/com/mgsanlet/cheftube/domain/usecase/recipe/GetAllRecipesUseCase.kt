package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import javax.inject.Inject

/**
 * Caso de uso para obtener todas las recetas disponibles.
 *
 * @property recipesRepository Repositorio de recetas para obtener los datos
 */
class GetAllRecipesUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository
) {
    /**
     * Ejecuta el caso de uso para obtener todas las recetas.
     *
     * @return [DomainResult] con la lista de recetas o [RecipeError] si hay un error
     */
    suspend operator fun invoke(): DomainResult<List<DomainRecipe>, RecipeError> {
        return recipesRepository.getAll()
    }
}