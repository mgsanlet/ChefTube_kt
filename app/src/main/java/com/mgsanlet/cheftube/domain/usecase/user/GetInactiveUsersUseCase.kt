package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para obtener la lista de usuarios inactivos (más de 30 días sin actividad)
 */
class GetInactiveUsersUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    /**
     * Ejecuta el caso de uso
     * @return Resultado con la lista de usuarios inactivos o un error
     */
    suspend operator fun invoke(): DomainResult<List<DomainUser>, UserError> {
        return usersRepository.getInactiveUsers()
    }
}
