package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para actualizar los datos del usuario actual.
 *
 * @property usersRepository Repositorio de usuarios para actualizar los datos
 * @property validateNewUsername Caso de uso para validar el nuevo nombre de usuario
 * @property recipeRepository Repositorio de recetas para limpiar la caché si es necesario
 */
class UpdateCurrentUserDataUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val validateNewUsername: ValidateNewUsernameUseCase,
    private val recipeRepository: RecipesRepository
) {
    /**
     * Ejecuta el caso de uso para actualizar los datos del usuario actual.
     * Valida el nombre de usuario y actualiza los datos si es válido.
     * Limpia la caché de recetas si la actualización es exitosa.
     *
     * @param newUserData Nuevos datos del usuario
     * @return [DomainResult] con Unit en caso de éxito o [UserError] si hay un error
     */
    suspend operator fun invoke(newUserData: DomainUser): DomainResult<Unit, UserError> {
        // Nombre de usuario vacío se utiliza para campo no actualizado
        return if (validateNewUsername(newUserData.username) is DomainResult.Error) {
            DomainResult.Error(UserError.InvalidUsernamePattern)
        } else {
            val result = usersRepository.updateCurrentUserData(newUserData)
            if (result is DomainResult.Success) {
                recipeRepository.clearCache()
            }
            return result
        }
    }
}