package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para obtener los datos de un usuario específico por su ID.
 * Útil para visualizar perfiles de otros usuarios.
 *
 * @property userRepository Repositorio de usuarios para obtener los datos
 */
class GetUserDataByIdUseCase @Inject constructor(
    private val userRepository: UsersRepository
) {
    /**
     * Ejecuta el caso de uso para obtener los datos de un usuario por su ID.
     *
     * @param userId ID del usuario cuyos datos se desean obtener
     * @return [DomainResult] con los datos del usuario o [UserError] si hay un error
     */
    suspend operator fun invoke(userId: String): DomainResult<DomainUser, UserError> {
        return userRepository.getUserDataById(userId)
    }

}