package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

/**
 * Caso de uso para actualizar la contraseña del usuario actual.
 *
 * @property usersRepository Repositorio de usuarios para actualizar la contraseña
 * @property validatePassword Caso de uso para validar la nueva contraseña
 */
class UpdatePasswordUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val validatePassword: ValidateNewPasswordUseCase
) {
    /**
     * Ejecuta el caso de uso para actualizar la contraseña.
     * Valida la nueva contraseña antes de intentar actualizarla.
     *
     * @param currentPassword Contraseña actual del usuario
     * @param newPassword Nueva contraseña
     * @return [DomainResult] con Unit en caso de éxito o [UserError] si hay un error
     */
    suspend operator fun invoke(
        currentPassword: String, 
        newPassword: String
    ): DomainResult<Unit, UserError> {
        // Validar que la nueva contraseña cumpla con los requisitos
        val passwordValidation = validatePassword(newPassword)
        if (passwordValidation is DomainResult.Error) {
            return passwordValidation
        }
        
        return usersRepository.updatePassword(currentPassword, newPassword)
    }
}
