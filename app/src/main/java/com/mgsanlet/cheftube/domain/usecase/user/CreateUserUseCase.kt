package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import java.util.UUID
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val validateNewEmail: ValidateNewEmailUseCase,
    private val validateNewPassword: ValidateNewPasswordUseCase
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String
    ): DomainResult<Unit, UserError> {
        var result = validateNewEmail(email)
        if (result is DomainResult.Error ) { return DomainResult.Error(result.error) }
        result = validateNewPassword(password)
        if (result is DomainResult.Error ) { return DomainResult.Error(result.error) }
        return usersRepository.createUser(UUID.randomUUID().toString(), username, email, password)
    }
}