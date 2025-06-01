package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import javax.inject.Inject

/**
 * Caso de uso para obtener múltiples recetas por sus IDs.
 *
 * @property recipesRepository Repositorio de recetas para obtener los datos
 */
class GetRecipesByIdUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository
) {
    /**
     * Ejecuta el caso de uso para obtener múltiples recetas por sus IDs.
     *
     * @param recipeIds Lista de IDs de las recetas a buscar
     * @return [DomainResult] con la lista de recetas encontradas o [RecipeError] si hay un error
     */
    suspend operator fun invoke(recipeIds: ArrayList<String>):
            DomainResult<List<DomainRecipe>, RecipeError> {
        return recipesRepository.getByIds(recipeIds)
    }

}