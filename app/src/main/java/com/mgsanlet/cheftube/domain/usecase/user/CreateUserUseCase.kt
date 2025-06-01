package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para la creación de un nuevo usuario.
 *
 * @property usersRepository Repositorio de usuarios para crear el usuario
 * @property validateNewEmail Caso de uso para validar el correo electrónico
 * @property validateNewPassword Caso de uso para validar la contraseña
 * @property validateNewUsername Caso de uso para validar el nombre de usuario
 */
class CreateUserUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val validateNewEmail: ValidateNewEmailUseCase,
    private val validateNewPassword: ValidateNewPasswordUseCase,
    private val validateNewUsername: ValidateNewUsernameUseCase
) {
    /**
     * Ejecuta el caso de uso para crear un nuevo usuario.
     * Realiza validaciones de nombre de usuario, correo y contraseña antes de crear el usuario.
     *
     * @param username Nombre de usuario
     * @param email Correo electrónico
     * @param password Contraseña
     * @return [DomainResult] con Unit en caso de éxito o [UserError] en caso de error de
     * validación o creación
     */
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String
    ): DomainResult<Unit, UserError> {
        var result = validateNewUsername(username)
        if (result is DomainResult.Error ) { return DomainResult.Error(result.error) }
        result = validateNewEmail(email)
        if (result is DomainResult.Error ) { return DomainResult.Error(result.error) }
        result = validateNewPassword(password)
        if (result is DomainResult.Error ) { return DomainResult.Error(result.error) }
        return usersRepository.createUser(username, email, password)
    }
}