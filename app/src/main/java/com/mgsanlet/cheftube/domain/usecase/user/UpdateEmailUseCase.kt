package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.usecase.user.ValidateNewEmailUseCase
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

class UpdateEmailUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val validateEmail: ValidateNewEmailUseCase
) {
    suspend operator fun invoke(newEmail: String, password: String): DomainResult<Unit, UserError> {
        // Validar el formato del email usando el mismo validador que en el registro
        return when (val validation = validateEmail(newEmail)) {
            is DomainResult.Success -> usersRepository.updateEmail(newEmail, password)
            is DomainResult.Error -> validation
        }
    }
}
