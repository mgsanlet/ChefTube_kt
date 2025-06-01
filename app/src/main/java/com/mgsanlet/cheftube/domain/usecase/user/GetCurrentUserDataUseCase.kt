package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainUser as User

/**
 * Caso de uso para obtener los datos del usuario actualmente autenticado.
 *
 * @property usersRepository Repositorio de usuarios para obtener los datos
 */
class GetCurrentUserDataUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    /**
     * Ejecuta el caso de uso para obtener los datos del usuario actual.
     *
     * @return [DomainResult] con los datos del usuario o [UserError] si hay un error
     */
    suspend operator fun invoke(): DomainResult<User, UserError> {
        return usersRepository.getCurrentUserData()
    }
}