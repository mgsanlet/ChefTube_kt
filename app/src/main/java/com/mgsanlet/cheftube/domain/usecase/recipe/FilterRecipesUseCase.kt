package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.model.SearchParams
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe

/**
 * Caso de uso para filtrar recetas según diferentes criterios de búsqueda.
 *
 * @property repository Repositorio de recetas para realizar la búsqueda filtrada
 */
class FilterRecipesUseCase @Inject constructor(
    private val repository: RecipesRepository
) {
    /**
     * Ejecuta el caso de uso para filtrar recetas.
     *
     * @param params Parámetros de búsqueda que incluyen filtros como ingredientes,
     * dificultad, tiempo de preparación, etc.
     * @return [DomainResult] con la lista de recetas que coinciden con los filtros o
     * [RecipeError] si hay un error
     */
    suspend operator fun invoke(params: SearchParams): DomainResult<List<Recipe>, RecipeError> {
        return repository.filterRecipes(params)
    }
}
