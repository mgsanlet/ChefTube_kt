package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para el inicio de sesión automático del usuario.
 *
 * @property usersRepository Repositorio de usuarios para realizar el inicio de sesión automático
 */
class AutomaticLoginUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    /**
     * Ejecuta el caso de uso para el inicio de sesión automático.
     * Intenta iniciar sesión con las credenciales guardadas si existen.
     *
     * @return [DomainResult] con Unit en caso de éxito o [UserError] en caso de error
     */
    suspend operator fun invoke(): DomainResult<Unit, UserError> {
        return usersRepository.tryAutoLogin()
    }
}