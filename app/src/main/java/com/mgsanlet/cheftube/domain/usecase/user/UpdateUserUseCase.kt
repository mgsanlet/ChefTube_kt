package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser as User
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.domain.util.DomainResult
import javax.inject.Inject

class UpdateUserUseCase@Inject constructor(
    private val usersRepository: UsersRepository,
    private val validateNewEmail: ValidateNewEmailUseCase,
    private val validateNewPassword: ValidateNewPasswordUseCase
) {
    suspend operator fun invoke(updatedUser: User, oldPassword: String): DomainResult<User, UserError> {
        if (updatedUser.password != oldPassword) {
            var result = validateNewEmail(updatedUser.email)
            if (result is DomainResult.Error ) { return DomainResult.Error(result.error) }
            result = validateNewPassword(updatedUser.password)
            if (result is DomainResult.Error ) { return DomainResult.Error(result.error) }
        }
        return usersRepository.updateUser(updatedUser, oldPassword)
    }
}