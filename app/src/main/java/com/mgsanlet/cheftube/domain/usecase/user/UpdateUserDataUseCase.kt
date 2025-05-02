package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.model.DomainUser
import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

class UpdateUserDataUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val validateNewUsername: ValidateNewUsernameUseCase
) {
    suspend operator fun invoke(newUserData: DomainUser): DomainResult<Unit, UserError> {
        // Nombre de usuario vacío se utiliza para campo no actualizado
        return if (validateNewUsername(newUserData.username) is DomainResult.Error) {
            DomainResult.Error(UserError.InvalidUsernamePattern)
        } else {
            usersRepository.updateCurrentUserData(newUserData)
        }
    }
}