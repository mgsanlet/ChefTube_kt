package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para actualizar los datos de un usuario.
 * También se encarga de limpiar la caché de recetas para reflejar los cambios.
 *
 * @property usersRepository Repositorio de usuarios para actualizar los datos
 * @property recipeRepository Repositorio de recetas para limpiar la caché
 */
class UpdateUserDataUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val recipeRepository: RecipesRepository
) {
    /**
     * Ejecuta el caso de uso para actualizar los datos del usuario.
     *
     * @param newUserData Nuevos datos del usuario a guardar
     * @return [DomainResult] con Unit en caso de éxito o [UserError] si hay un error
     */
    suspend operator fun invoke(newUserData: DomainUser): DomainResult<Unit, UserError> {
        val result = usersRepository.updateUserData(newUserData)
        if (result is DomainResult.Success) {
            recipeRepository.clearCache()
        }
        return result
    }
}