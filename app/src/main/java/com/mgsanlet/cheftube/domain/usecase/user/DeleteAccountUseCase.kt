package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.RecipesRepository
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para eliminar la cuenta del usuario actual.
 *
 * @property usersRepository Repositorio de usuarios para eliminar la cuenta
 * @property recipesRepository Repositorio de recetas para limpiar la caché
 */
class DeleteAccountUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val recipesRepository: RecipesRepository
) {
    /**
     * Ejecuta el caso de uso para eliminar la cuenta del usuario actual.
     * Limpia la caché de recetas después de eliminar la cuenta.
     *
     * @param password Contraseña actual del usuario para confirmar la eliminación
     * @return [DomainResult] con Unit en caso de éxito o [UserError] si hay un error
     */
    suspend operator fun invoke(password: String): DomainResult<Unit, UserError> {
        return usersRepository.deleteAccount(password).fold(
            onSuccess = {
                recipesRepository.clearCache()
                DomainResult.Success(Unit)
            },
            onError = {
                DomainResult.Error(it)
            }
        )
    }
}
