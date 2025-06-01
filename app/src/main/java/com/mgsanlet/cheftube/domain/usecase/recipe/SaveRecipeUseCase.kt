package com.mgsanlet.cheftube.domain.usecase.recipe

import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.DomainError
import javax.inject.Inject

/**
 * Caso de uso para guardar o actualizar una receta.
 *
 * @property recipesRepository Repositorio de recetas para guardar los datos
 * @property usersRepository Repositorio de usuarios para obtener los datos del usuario actual
 */
class SaveRecipeUseCase @Inject constructor(
    private val recipesRepository: RecipesRepository,
    private val usersRepository: UsersRepository
) {
    /**
     * Ejecuta el caso de uso para guardar o actualizar una receta.
     * Obtiene los datos del usuario actual y guarda la receta con su imagen opcional.
     *
     * @param newRecipeData Datos de la receta a guardar
     * @param newImage Imagen de la receta en formato ByteArray (opcional)
     * @return [DomainResult] con el ID de la receta guardada o [DomainError] si hay un error
     */
    suspend operator fun invoke(
        newRecipeData: DomainRecipe,
        newImage: ByteArray? = null
    ): DomainResult<String?, DomainError> {

        var currentUserData = DomainUser()
        usersRepository.getCurrentUserData().fold(
            onSuccess = {
                currentUserData = it
            },
            onError = {
                return DomainResult.Error(it)
            }
        )
        recipesRepository.saveRecipe(newRecipeData, newImage, currentUserData).fold(
            onSuccess = {
                usersRepository.clearCache()
                return DomainResult.Success(it)
            },
            onError = {
                return DomainResult.Error(it)
            }
        )
    }
}