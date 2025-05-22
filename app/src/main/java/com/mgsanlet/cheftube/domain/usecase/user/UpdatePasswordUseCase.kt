package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.usecase.user.ValidateNewPasswordUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

class UpdatePasswordUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val validatePassword: ValidateNewPasswordUseCase
) {
    suspend operator fun invoke(
        currentPassword: String, 
        newPassword: String,
        confirmPassword: String
    ): DomainResult<Unit, UserError> {
        // Validar que la nueva contraseña cumpla con los requisitos
        val passwordValidation = validatePassword(newPassword)
        if (passwordValidation is DomainResult.Error) {
            return passwordValidation
        }
        
        return usersRepository.updatePassword(currentPassword, newPassword)
    }
}
