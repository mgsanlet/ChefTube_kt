package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para obtener la lista de usuarios inactivos.
 * Un usuario se considera inactivo si no ha tenido actividad en los últimos 30 días.
 * Solo accesible para administradores.
 *
 * @property usersRepository Repositorio de usuarios para obtener los datos
 */
class GetInactiveUsersUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    /**
     * Ejecuta el caso de uso para obtener usuarios inactivos.
     *
     * @return [DomainResult] con la lista de [DomainUser] inactivos o [UserError] si hay un error
     */
    suspend operator fun invoke(): DomainResult<List<DomainUser>, UserError> {
        return usersRepository.getInactiveUsers()
    }
}
