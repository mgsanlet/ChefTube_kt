package com.mgsanlet.cheftube.domain.usecase.user

import com.mgsanlet.cheftube.domain.repository.UsersRepository
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
import javax.inject.Inject

class AlternateKeepSessionUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    operator fun invoke(keepSession: Boolean): DomainResult<Unit, UserError> {
        return usersRepository.alternateKeepSession(keepSession)
    }
}