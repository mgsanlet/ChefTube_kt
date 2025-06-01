package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para verificar si el usuario actual tiene permisos de administrador.
 * Se utiliza para controlar el acceso a funcionalidades restringidas.
 *
 * @property usersRepository Repositorio de usuarios para verificar los permisos
 */
class IsCurrentUserAdminUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    /**
     * Ejecuta el caso de uso para verificar si el usuario actual es administrador.
     *
     * @return [DomainResult] con `true` si el usuario es administrador, `false` en caso contrario,
     * o [UserError] si hay un error al verificar los permisos
     */
    suspend operator fun invoke(): DomainResult<Boolean, UserError> {
        return usersRepository.isCurrentUserAdmin()
    }
}
