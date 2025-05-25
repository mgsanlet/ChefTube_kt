package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para verificar si el usuario actual tiene permisos de administrador
 */
class IsCurrentUserAdminUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    suspend operator fun invoke(): DomainResult<Boolean, UserError> {
        return usersRepository.isCurrentUserAdmin()
    }
}
