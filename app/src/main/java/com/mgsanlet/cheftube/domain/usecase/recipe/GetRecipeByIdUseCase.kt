package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.RecipeError
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe

/**
 * Caso de uso para obtener una receta específica por su ID.
 *
 * @property recipesRepository Repositorio de recetas para obtener los datos
 */
class GetRecipeByIdUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository
) {
    /**
     * Ejecuta el caso de uso para obtener una receta por su ID.
     *
     * @param recipeId ID de la receta a buscar
     * @return [DomainResult] con la receta encontrada o [RecipeError] si no se encuentra o hay un error
     */
    suspend operator fun invoke(recipeId: String): DomainResult<Recipe, RecipeError> {
        return recipesRepository.getById(recipeId)
    }

}