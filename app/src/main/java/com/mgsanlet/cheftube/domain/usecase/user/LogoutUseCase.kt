package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import javax.inject.Inject

/**
 * Caso de uso para cerrar la sesión del usuario actual.
 *
 * @property usersRepository Repositorio de usuarios para realizar el cierre de sesión
 */
class LogoutUseCase @Inject constructor(private val usersRepository: UsersRepository) {
    /**
     * Ejecuta el caso de uso para cerrar la sesión.
     * Limpia las credenciales de autenticación del usuario actual.
     */
    operator fun invoke() {
        usersRepository.logout()
    }
}
