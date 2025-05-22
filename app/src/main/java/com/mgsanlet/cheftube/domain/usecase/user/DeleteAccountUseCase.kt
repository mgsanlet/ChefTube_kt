package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    suspend operator fun invoke(password: String): DomainResult<Unit, UserError> {
        return usersRepository.deleteAccount(password)
    }
}
